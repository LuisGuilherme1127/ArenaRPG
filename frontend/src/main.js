import './styles/main.css'
import { renderHome } from './pages/home.js'
import { renderCharacterSelection } from './pages/character-selection.js'
import { renderInfoPanel } from './pages/info-panel.js'
import { renderBattle } from './pages/battle.js'
import { renderBattleResult } from './pages/battle-result.js'
import {
  attackMatch,
  createMatch,
  finishMatchTurn,
  getCharacter,
  getMatch,
  listCharacters,
  moveMatch,
  useMatchSkill,
} from './services/api-client.js'
import { buildCharacterCards, mapApiCharacter } from './services/character-definitions.js'
import { mapPartidaToBattle } from './services/battle/partida-view.js'
import { createRequestLock } from './services/request-lock.js'
import { loadSpriteAvailability } from './services/sprite-assets.js'

const app = document.querySelector('#app')
const requestLock = createRequestLock()
const state = {
  screen: 'home',
  selectedClass: null,
  infoPanel: null,
  battle: null,
  characters: [],
  sprites: {},
  pending: null,
  syncRequired: false,
  notice: '',
}

app.addEventListener('error', (event) => {
  const image = event.target
  if (!image?.classList?.contains('sprite-image')) return

  image.closest('.sprite-frame')?.classList.remove('has-sprite')
  image.remove()
}, true)

function render() {
  if (state.battle?.status === 'victory' || state.battle?.status === 'defeat') {
    state.screen = 'result'
  }

  if (state.screen === 'characters') {
    app.innerHTML = renderCharacterSelection(
      state.selectedClass,
      state.sprites,
      state.characters,
      state.pending === 'characters',
      Boolean(state.pending),
      state.notice,
    )
    return
  }

  if (state.screen === 'info') {
    app.innerHTML = renderInfoPanel(state.infoPanel)
    return
  }

  if (state.screen === 'battle' && state.battle) {
    app.innerHTML = renderBattle({
      ...state.battle,
      pending: Boolean(state.pending),
      syncRequired: state.syncRequired,
    }, state.sprites)
    return
  }

  if (state.screen === 'result' && state.battle) {
    app.innerHTML = renderBattleResult(state.battle)
    return
  }

  app.innerHTML = renderHome()
}

function startRequest(operation) {
  const requestId = requestLock.begin()
  if (requestId === null) return null
  state.pending = operation
  state.notice = ''
  if (state.battle) state.battle.localNotice = ''
  render()
  return requestId
}

function isCurrentRequest(requestId) {
  return requestLock.isCurrent(requestId)
}

function finishRequest(requestId) {
  if (!requestLock.finish(requestId)) return false
  state.pending = null
  render()
  return true
}

function errorMessage(error) {
  return error instanceof Error ? error.message : 'Ocorreu um erro inesperado.'
}

async function loadCharacterList() {
  state.screen = 'characters'
  state.selectedClass = null
  state.characters = []
  const requestId = startRequest('characters')

  try {
    const response = await listCharacters()
    if (!isCurrentRequest(requestId)) return
    state.characters = buildCharacterCards(response)
  } catch (error) {
    if (isCurrentRequest(requestId)) state.notice = errorMessage(error)
  } finally {
    finishRequest(requestId)
  }
}

async function selectCharacter(type) {
  const requestId = startRequest('character')
  try {
    const response = await getCharacter(type)
    if (!isCurrentRequest(requestId)) return
    const refreshed = mapApiCharacter(response)
    state.characters = state.characters.map((character) => character.id === type ? refreshed : character)
    state.selectedClass = refreshed.id
  } catch (error) {
    if (isCurrentRequest(requestId)) state.notice = errorMessage(error)
  } finally {
    finishRequest(requestId)
  }
}

async function startBackendMatch() {
  if (!state.selectedClass) return
  const requestId = startRequest('match')

  try {
    const created = await createMatch(state.selectedClass)
    if (!isCurrentRequest(requestId)) return

    let current = created
    try {
      current = await getMatch(created.id)
    } catch (error) {
      if (!error.network) throw error
      state.notice = 'A partida foi criada, mas a consulta de confirmação falhou. Exibindo o estado recebido na criação.'
    }

    if (!isCurrentRequest(requestId)) return
    state.battle = mapPartidaToBattle(current)
    if (state.notice) {
      state.battle.localNotice = state.notice
      state.notice = ''
    }
    state.screen = 'battle'
    state.syncRequired = false
  } catch (error) {
    if (isCurrentRequest(requestId)) {
      state.notice = errorMessage(error)
      state.screen = 'characters'
    }
  } finally {
    finishRequest(requestId)
  }
}

function handleExpiredMatch() {
  state.battle = null
  state.selectedClass = null
  state.syncRequired = false
  state.screen = 'characters'
  state.notice = 'Esta partida não existe mais no backend. Selecione uma classe para iniciar outra.'
}

async function synchronizeMatch(requestId, previousBattle) {
  try {
    const updated = await getMatch(previousBattle.id)
    if (!isCurrentRequest(requestId)) return false
    state.battle = mapPartidaToBattle(updated, previousBattle)
    state.syncRequired = false
    state.battle.localNotice = 'Estado consultado novamente no backend. Confira o registro antes de continuar.'
    return true
  } catch (error) {
    if (!isCurrentRequest(requestId)) return false
    if (error.status === 404) {
      handleExpiredMatch()
    } else {
      state.syncRequired = true
      state.battle.localNotice = 'Não foi possível consultar a partida. As ações seguem bloqueadas até a reconexão.'
    }
    return false
  }
}

async function submitBattleAction(action, operation) {
  if (!state.battle) return
  const previousBattle = state.battle
  const requestId = startRequest(action)

  try {
    const updated = await operation(previousBattle.id)
    if (!isCurrentRequest(requestId)) return
    state.battle = mapPartidaToBattle(updated, previousBattle)
    state.syncRequired = false
    state.screen = state.battle.status === 'active' ? 'battle' : 'result'
  } catch (error) {
    if (!isCurrentRequest(requestId)) return
    if (error.status === 404) {
      handleExpiredMatch()
    } else if (error.network) {
      const synchronized = await synchronizeMatch(requestId, previousBattle)
      if (synchronized) {
        state.battle.localNotice = 'A conexão caiu durante a ação. O estado atual foi recarregado; confira o registro antes de agir novamente.'
      }
    } else {
      state.battle.localNotice = errorMessage(error)
    }
  } finally {
    finishRequest(requestId)
  }
}

async function synchronizeCurrentMatch() {
  if (!state.battle) return
  const previousBattle = state.battle
  const requestId = startRequest('sync')
  await synchronizeMatch(requestId, previousBattle)
  finishRequest(requestId)
}

function returnHome() {
  requestLock.cancel()
  state.pending = null
  state.notice = ''
  state.screen = 'home'
  state.battle = null
  state.selectedClass = null
  state.syncRequired = false
  render()
  app.querySelector('h1')?.focus({ preventScroll: true })
}

app.addEventListener('click', async (event) => {
  const button = event.target.closest('[data-action]')
  if (!button) return

  const { action, panel, character, x, y } = button.dataset
  if (state.pending && action !== 'home') return
  if (state.syncRequired && !['sync-battle', 'home'].includes(action)) return

  if (action === 'home') {
    returnHome()
    return
  }
  if (action === 'start' || action === 'retry-characters') {
    await loadCharacterList()
    return
  }
  if (action === 'panel') {
    state.screen = 'info'
    state.infoPanel = panel
    render()
    app.querySelector('h1')?.focus({ preventScroll: true })
    return
  }
  if (action === 'select' && character) {
    await selectCharacter(character)
    app.querySelector('.selection-feedback')?.focus({ preventScroll: true })
    return
  }
  if (action === 'start-battle' || action === 'play-again') {
    await startBackendMatch()
    app.querySelector('[data-focus="turn"]')?.focus({ preventScroll: true })
    return
  }
  if (action === 'move' && state.battle?.turn === 'player' && !state.battle.movedThisTurn) {
    state.battle.moveMode = !state.battle.moveMode
    render()
    app.querySelector(state.battle.moveMode ? '.cell-available' : '[data-action="move"]')?.focus({ preventScroll: true })
    return
  }
  if (action === 'move-to' && state.battle?.moveMode) {
    await submitBattleAction('move', (id) => moveMatch(id, { x: Number(x), y: Number(y) }))
    app.querySelector('[data-action="end-turn"]')?.focus({ preventScroll: true })
    return
  }
  if (action === 'basic-attack') {
    await submitBattleAction('attack', attackMatch)
    app.querySelector('[data-action="end-turn"]')?.focus({ preventScroll: true })
    return
  }
  if (action === 'special-attack') {
    await submitBattleAction('skill', useMatchSkill)
    app.querySelector('[data-action="end-turn"]')?.focus({ preventScroll: true })
    return
  }
  if (action === 'end-turn') {
    await submitBattleAction('end-turn', finishMatchTurn)
    app.querySelector('[data-focus="turn"]')?.focus({ preventScroll: true })
    return
  }
  if (action === 'sync-battle') {
    await synchronizeCurrentMatch()
    app.querySelector('[data-focus="turn"]')?.focus({ preventScroll: true })
  }
})

render()

loadSpriteAvailability().then((sprites) => {
  state.sprites = sprites
  if (Object.keys(sprites).length) render()
})
