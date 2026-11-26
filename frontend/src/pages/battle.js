import { icon } from '../components/icons.js'
import { getReachablePositions, manhattanDistance } from '../services/battle/grid-rules.js'
import { renderSprite } from '../services/sprite-assets.js'
import { escapeHtml } from '../services/escape-html.js'

function healthBar(label, health, maximum, kind) {
  const percent = Math.max(0, Math.round((health / maximum) * 100))
  return `
    <div class="battle-meter">
      <div class="meter-label"><span>${label}</span><b>${health} <i>/ ${maximum}</i></b></div>
      <div class="meter-track meter-${kind}" role="progressbar" aria-label="${label}" aria-valuemin="0" aria-valuemax="${maximum}" aria-valuenow="${health}">
        <span style="width:${percent}%"></span>
      </div>
    </div>
  `
}

function renderUnitPanel(unit, kind, sprites) {
  const sprite = renderSprite(kind === 'player' ? unit.classId : 'goblin', sprites)
  return `
    <section class="unit-panel unit-panel-${kind}" aria-label="${unit.name}">
      <div class="unit-heading">
        <span class="unit-portrait unit-portrait-${kind} sprite-frame ${sprite ? 'has-sprite' : ''}" aria-hidden="true">
          <span class="unit-placeholder">${unit.symbol}</span>${sprite}
        </span>
        <div><p>${kind === 'player' ? 'AVENTUREIRO' : 'ADVERSÁRIO'}</p><h2>${unit.name}</h2></div>
        ${kind === 'enemy' ? '<span class="enemy-level">NÍVEL 1</span>' : ''}
      </div>
      ${healthBar('VIDA', unit.health, unit.maxHealth, 'health')}
      ${kind === 'player' ? healthBar('MANA', unit.mana, unit.maxMana, 'mana') : `
        <div class="unit-detail-row"><span>ATAQUE</span><b>${unit.attack}</b><span>ALCANCE</span><b>${unit.range} ${unit.range === 1 ? 'célula' : 'células'}</b></div>
      `}
      ${kind === 'player' ? `<div class="unit-detail-row"><span>ATAQUE</span><b>${unit.attack}</b><span>ALCANCE</span><b>${unit.range}</b></div>` : ''}
    </section>
  `
}

function renderBoard(state, sprites) {
  const boardSize = state.boardSize ?? 8
  const reachable = new Set(
    state.moveMode
      ? getReachablePositions(state, 'player', state.player.movementMax).map(({ x, y }) => `${x},${y}`)
      : [],
  )
  const attackRange = state.player.range

  return Array.from({ length: boardSize * boardSize }, (_, index) => {
    const x = index % boardSize
    const y = Math.floor(index / boardSize)
    const position = { x, y }
    const isPlayer = state.player.position.x === x && state.player.position.y === y
    const isEnemy = state.enemy.position.x === x && state.enemy.position.y === y
    const isAvailable = reachable.has(`${x},${y}`)
    const inRange = !isPlayer && manhattanDistance(state.player.position, position) <= attackRange
    const unit = isPlayer ? state.player : isEnemy ? state.enemy : null
    const sprite = unit ? renderSprite(isPlayer ? unit.classId : 'goblin', sprites) : ''
    const cellName = `Linha ${y + 1}, coluna ${x + 1}${unit ? `, ${unit.name}` : ''}${isAvailable ? ', destino de movimento disponível' : ''}${inRange ? ', dentro do alcance de ataque' : ''}`

    return `
      <button class="arena-cell ${(x + y) % 2 ? 'cell-odd' : 'cell-even'} ${isPlayer ? 'cell-player' : ''} ${isEnemy ? 'cell-enemy' : ''} ${isAvailable ? 'cell-available' : ''} ${inRange ? 'cell-in-range' : ''}"
        type="button" role="gridcell" aria-label="${cellName}" aria-disabled="${isAvailable ? 'false' : 'true'}"
        data-action="move-to" data-x="${x}" data-y="${y}" ${isAvailable && !state.pending && !state.syncRequired ? '' : 'disabled tabindex="-1"'}>
        ${unit ? `<span class="arena-unit ${isPlayer ? 'arena-unit-player' : 'arena-unit-enemy'} sprite-frame ${sprite ? 'has-sprite' : ''}" aria-hidden="true"><span class="unit-placeholder">${unit.symbol}</span>${sprite}</span>` : ''}
        ${isAvailable ? '<span class="cell-destination" aria-hidden="true">+</span>' : ''}
      </button>
    `
  }).join('')
}

function actionButton({ action, label, detail, disabled, className = '', iconName }) {
  return `
    <button class="battle-action ${className} ${disabled ? 'action-disabled' : ''}" type="button"
      data-action="${action}" ${disabled ? 'disabled' : ''}>
      <span class="action-icon">${icon(iconName)}</span>
      <span class="action-copy"><b>${label}</b><small>${detail}</small></span>
      <span class="action-key" aria-hidden="true">${disabled ? '—' : '›'}</span>
    </button>
  `
}

export function renderBattle(state, sprites = {}) {
  const boardSize = state.boardSize ?? 8
  const isPlayerTurn = state.turn === 'player' && state.status === 'active'
  const distance = manhattanDistance(state.player.position, state.enemy.position)
  const inBasicRange = distance <= state.player.range
  const inSkillRange = distance <= state.player.abilityRange
  const reachable = state.moveMode
    ? getReachablePositions(state, 'player', state.player.movementMax).length
    : 0
  const canMove = isPlayerTurn && !state.movedThisTurn && !state.pending && !state.syncRequired
  const canAct = isPlayerTurn && !state.actionUsed && !state.pending && !state.syncRequired
  const skillNeedsMana = state.player.mana < state.player.abilityCost
  const latestLogs = state.log.slice(-5).reverse()

  return `
    <main class="screen battle-screen">
      <div class="ambient ambient-one" aria-hidden="true"></div>
      <header class="topbar battle-topbar">
        <button class="brand-mark brand-button" type="button" data-action="home" aria-label="Voltar ao menu principal">
          <span class="brand-shield">A</span><span class="brand-name">ARENA <b>RPG</b></span>
        </button>
        <div class="turn-indicator ${isPlayerTurn ? 'turn-player' : 'turn-enemy'}" aria-live="polite" data-focus="turn" tabindex="-1">
          <i></i><span>${state.pending ? 'AGUARDANDO O BACKEND JAVA' : state.syncRequired ? 'SINCRONIZAÇÃO NECESSÁRIA' : isPlayerTurn ? `SUA VEZ · TURNO ${state.round}` : 'TURNO DO GOBLIN'}</span>
        </div>
          ${state.syncRequired ? '<button class="battle-exit" type="button" data-action="sync-battle" aria-label="Tentar sincronizar com o backend">TENTAR CONEXÃO</button>' : ''}
          <button class="battle-exit" type="button" data-action="home">SAIR DA ARENA</button>
      </header>

      <section class="battle-layout turn-${state.turn} feedback-${state.feedback ?? 'system'}" aria-label="Arena de batalha">
        <aside class="battle-sidebar">
          ${renderUnitPanel(state.player, 'player', sprites)}
          ${renderUnitPanel(state.enemy, 'enemy', sprites)}

          <section class="battle-controls" aria-label="Ações do jogador">
            <p class="panel-kicker">SUAS AÇÕES</p>
            ${actionButton({ action: 'move', label: state.moveMode ? 'Cancelar movimento' : 'Mover', detail: state.moveMode ? `${reachable} destinos disponíveis` : `Até ${state.player.movementMax} células · uma vez por turno`, disabled: !canMove, className: state.moveMode ? 'action-active' : '', iconName: 'move' })}
            ${actionButton({ action: 'basic-attack', label: 'Ataque básico', detail: inBasicRange ? `${state.player.attack} de dano · alcance ${state.player.range}` : `Fora de alcance · até ${state.player.range} células`, disabled: !canAct || !inBasicRange, iconName: 'sword' })}
            ${actionButton({ action: 'special-attack', label: state.player.abilityName, detail: skillNeedsMana ? `Mana insuficiente · ${state.player.abilityCost} necessários` : `${state.player.abilityDamage} de dano · ${state.player.abilityCost} mana`, disabled: !canAct || !inSkillRange || skillNeedsMana, className: 'action-special', iconName: 'sparkle' })}
            <button class="end-turn-button" type="button" data-action="end-turn" ${isPlayerTurn && !state.pending && !state.syncRequired ? '' : 'disabled'}>
              <span>Encerrar turno</span>${icon('arrow')}
            </button>
            <p class="action-hint" aria-live="polite">${state.pending ? 'Aguardando resposta do backend...' : state.syncRequired ? 'A conexão foi interrompida. Sincronize a partida antes de continuar.' : !isPlayerTurn ? 'Aguarde o Goblin concluir sua ação.' : state.moveMode ? 'Escolha uma célula dourada para se mover.' : state.movedThisTurn ? 'Movimento realizado neste turno.' : 'Você pode mover e atacar em qualquer ordem.'}</p>
          </section>

          <section class="battle-log" aria-label="Registro da batalha" aria-live="polite">
            <div class="log-heading"><span class="panel-kicker">REGISTRO DA BATALHA</span><i></i></div>
            <ol>${latestLogs.map((entry) => `<li class="log-${entry.type}">${escapeHtml(entry.message)}</li>`).join('')}${state.localNotice ? `<li class="log-warning">${escapeHtml(state.localNotice)}</li>` : ''}</ol>
          </section>
        </aside>

        <div class="arena-main">
          <div class="arena-heading">
            <div><p class="panel-kicker">CAMPO DE COMBATE</p><h1 tabindex="-1">Ruínas da <em>fronteira</em></h1></div>
            <span class="arena-size">${boardSize} × ${boardSize} <i>·</i> TABULEIRO</span>
          </div>
          <div class="arena-board-wrap">
            <div class="arena-board" role="group" aria-label="Tabuleiro de batalha com ${boardSize} colunas por ${boardSize} linhas">
              ${renderBoard(state, sprites)}
            </div>
          </div>
          <div class="board-legend" aria-label="Legenda do tabuleiro">
            <span><i class="legend-player"></i>${state.player.name}</span>
            <span><i class="legend-enemy"></i>Goblin</span>
            <span><i class="legend-range"></i>Alcance de ataque (${state.player.range})</span>
            ${state.moveMode ? '<span><i class="legend-move"></i>Destino possível</span>' : ''}
          </div>
          <p class="arena-tip">${state.moveMode ? 'Os destinos consideram o movimento ortogonal e evitam a posição do inimigo.' : 'Distância calculada em células. Movimentos diagonais não são permitidos.'}</p>
        </div>
      </section>
    </main>
  `
}
