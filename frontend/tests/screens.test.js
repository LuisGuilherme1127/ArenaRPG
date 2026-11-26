import test from 'node:test'
import assert from 'node:assert/strict'
import { renderBattle } from '../src/pages/battle.js'
import { renderBattleResult } from '../src/pages/battle-result.js'
import { renderCharacterSelection } from '../src/pages/character-selection.js'
import { renderHome } from '../src/pages/home.js'
import { buildCharacterCards } from '../src/services/character-definitions.js'
import { mapPartidaToBattle } from '../src/services/battle/partida-view.js'

test('renderiza classes, tabuleiro 8x8, bloqueio pendente e resultados do servidor', () => {
  const home = renderHome()
  assert.match(home, /Iniciar aventura/)
  assert.match(home, /Personagens/)
  assert.match(home, /Ranking/)
  assert.match(home, /Configurações/)

  const apiCharacters = [
    { tipo: 'guerreiro', nome: 'Guerreiro', vidaMaxima: 120, manaMaxima: 30, ataque: 20, alcance: 1, movimentoMaximo: 3, habilidades: [{ nome: 'Golpe Poderoso', dano: 35, custoMana: 10, alcance: 1 }] },
    { tipo: 'mago', nome: 'Mago', vidaMaxima: 80, manaMaxima: 100, ataque: 15, alcance: 3, movimentoMaximo: 3, habilidades: [{ nome: 'Bola de Fogo', dano: 30, custoMana: 20, alcance: 3 }] },
    { tipo: 'arqueiro', nome: 'Arqueiro', vidaMaxima: 90, manaMaxima: 50, ataque: 18, alcance: 4, movimentoMaximo: 3, habilidades: [{ nome: 'Flecha Precisa', dano: 28, custoMana: 15, alcance: 4 }] },
    { tipo: 'goblin', nome: 'Goblin', vidaMaxima: 100, manaMaxima: 0, ataque: 12, alcance: 1, movimentoMaximo: 2, habilidades: [] },
  ]
  const cards = buildCharacterCards(apiCharacters)
  const selection = renderCharacterSelection(null, {}, cards)
  assert.match(selection, /Guerreiro/)
  assert.match(selection, /Mago/)
  assert.match(selection, /Arqueiro/)
  assert.doesNotMatch(selection, /data-action="start-battle"/)

  const partida = {
    id: 'ui-match', estado: 'em_andamento', turnoAtual: 'jogador', rodada: 1,
    jogador: { ...apiCharacters[1], vidaAtual: 80, manaAtual: 100, defesa: 0 },
    inimigo: { ...apiCharacters[3], vidaAtual: 100, defesa: 0 },
    tabuleiro: { linhas: 8, colunas: 8, posicaoJogador: { x: 1, y: 6 }, posicaoGoblin: { x: 6, y: 2 } },
    movimentoRealizado: false, acaoOfensivaRealizada: false,
    eventos: [{ rodada: 1, tipo: 'sistema', mensagem: 'A batalha começou.' }],
  }
  const battle = mapPartidaToBattle(partida)
  const html = renderBattle(battle)
  assert.equal((html.match(/class="arena-cell/g) ?? []).length, 64)
  assert.match(html, /Bola de Fogo/)
  assert.match(html, /data-action="end-turn"/)

  const pending = renderBattle({ ...battle, pending: true })
  assert.match(pending, /AGUARDANDO O BACKEND JAVA/)
  assert.match(pending, /data-action="basic-attack"[^>]*disabled/)

  const victory = renderBattleResult(mapPartidaToBattle({ ...partida, estado: 'vitoria', turnoAtual: 'finalizado' }))
  const defeat = renderBattleResult(mapPartidaToBattle({ ...partida, estado: 'derrota', turnoAtual: 'finalizado' }))
  assert.match(victory, /Vitória/)
  assert.match(victory, /data-action="play-again"/)
  assert.match(defeat, /Derrota/)
  assert.match(defeat, /data-action="home"/)
})
