import { getClassPresentation } from '../character-definitions.js'

const STATUS_MAP = {
  em_andamento: 'active',
  vitoria: 'victory',
  derrota: 'defeat',
}

const TURN_MAP = {
  jogador: 'player',
  goblin: 'enemy',
  finalizado: 'enemy',
}

const EVENT_TYPE_MAP = {
  ataque: 'attack',
  habilidade: 'skill',
  dano: 'damage',
  movimento: 'move',
  sistema: 'system',
  vitoria: 'victory',
  derrota: 'defeat',
  aviso: 'warning',
}

function mapUnit(personagem, position, isEnemy = false) {
  const ability = personagem.habilidades?.[0]
  const presentation = getClassPresentation(personagem.tipo)
  return {
    classId: personagem.tipo,
    name: personagem.nome,
    symbol: isEnemy ? 'G' : presentation.symbol,
    maxHealth: personagem.vidaMaxima,
    health: personagem.vidaAtual,
    maxMana: personagem.manaMaxima,
    mana: personagem.manaAtual,
    attack: personagem.ataque,
    range: personagem.alcance,
    movementMax: personagem.movimentoMaximo,
    abilityName: ability?.nome ?? '',
    abilityDamage: ability?.dano ?? 0,
    abilityCost: ability?.custoMana ?? 0,
    abilityRange: ability?.alcance ?? 0,
    position: { x: position.x, y: position.y },
  }
}

export function mapPartidaToBattle(partida, previousBattle = null) {
  const status = STATUS_MAP[partida.estado]
  const turn = TURN_MAP[partida.turnoAtual]
  if (!status || !turn) throw new Error('A API retornou um estado de partida desconhecido.')

  const events = partida.eventos.map((event) => ({
    message: event.mensagem,
    type: EVENT_TYPE_MAP[event.tipo] ?? 'system',
  }))
  const previousEventCount = previousBattle?.log.length ?? 0
  const latestActionEvent = partida.eventos
    .slice(previousEventCount)
    .reverse()
    .find((event) => ['ataque', 'habilidade', 'dano', 'movimento'].includes(event.tipo))

  return {
    id: partida.id,
    player: mapUnit(partida.jogador, partida.tabuleiro.posicaoJogador),
    enemy: mapUnit(partida.inimigo, partida.tabuleiro.posicaoGoblin, true),
    boardSize: partida.tabuleiro.linhas,
    turn,
    round: partida.rodada,
    movedThisTurn: partida.movimentoRealizado,
    actionUsed: partida.acaoOfensivaRealizada,
    moveMode: previousBattle?.moveMode ?? false,
    status,
    feedback: latestActionEvent ? EVENT_TYPE_MAP[latestActionEvent.tipo] : null,
    log: events,
    localNotice: '',
    pending: false,
    syncRequired: false,
  }
}
