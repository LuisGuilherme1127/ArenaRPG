import test from 'node:test'
import assert from 'node:assert/strict'
import { mapPartidaToBattle } from '../src/services/battle/partida-view.js'

const partita = {
  id: 'match-1',
  estado: 'em_andamento',
  turnoAtual: 'jogador',
  rodada: 2,
  jogador: {
    tipo: 'mago', nome: 'Mago', vidaMaxima: 80, vidaAtual: 68, ataque: 15,
    defesa: 0, manaMaxima: 100, manaAtual: 80, alcance: 3, movimentoMaximo: 3,
    habilidades: [{ nome: 'Bola de Fogo', dano: 30, custoMana: 20, alcance: 3 }],
  },
  inimigo: {
    tipo: 'goblin', nome: 'Goblin', vidaMaxima: 100, vidaAtual: 70, ataque: 12,
    defesa: 0, manaMaxima: 0, manaAtual: 0, alcance: 1, movimentoMaximo: 2, habilidades: [],
  },
  tabuleiro: {
    linhas: 8, colunas: 8,
    posicaoJogador: { x: 4, y: 4 }, posicaoGoblin: { x: 5, y: 4 },
  },
  movimentoRealizado: true,
  acaoOfensivaRealizada: true,
  eventos: [
    { rodada: 1, tipo: 'sistema', mensagem: 'A batalha começou.' },
    { rodada: 2, tipo: 'habilidade', mensagem: 'Mago usou Bola de Fogo.' },
  ],
}

test('mapeia snapshot Java para apresentação sem recalcular recursos ou resultado', () => {
  const view = mapPartidaToBattle(partita)

  assert.equal(view.id, 'match-1')
  assert.equal(view.status, 'active')
  assert.equal(view.turn, 'player')
  assert.equal(view.round, 2)
  assert.deepEqual(view.player.position, { x: 4, y: 4 })
  assert.equal(view.player.health, 68)
  assert.equal(view.player.mana, 80)
  assert.equal(view.player.abilityDamage, 30)
  assert.equal(view.player.abilityCost, 20)
  assert.deepEqual(view.enemy.position, { x: 5, y: 4 })
  assert.equal(view.enemy.health, 70)
  assert.equal(view.movedThisTurn, true)
  assert.equal(view.actionUsed, true)
  assert.equal(view.feedback, 'skill')
  assert.equal(view.log.length, 2)
})

test('converte vitória, derrota e finalização de turno retornados pelo servidor', () => {
  const base = {
    estado: 'em_andamento', turnoAtual: 'jogador', rodada: 1,
    jogador: { tipo: 'guerreiro', nome: 'Guerreiro', vidaMaxima: 120, vidaAtual: 120, ataque: 20,
      manaMaxima: 30, manaAtual: 30, alcance: 1, movimentoMaximo: 3,
      habilidades: [{ nome: 'Golpe Poderoso', dano: 35, custoMana: 10, alcance: 1 }] },
    inimigo: { tipo: 'goblin', nome: 'Goblin', vidaMaxima: 100, vidaAtual: 100, ataque: 12,
      manaMaxima: 0, manaAtual: 0, alcance: 1, movimentoMaximo: 2, habilidades: [] },
    tabuleiro: { linhas: 8, posicaoJogador: { x: 1, y: 6 }, posicaoGoblin: { x: 6, y: 2 } },
    movimentoRealizado: false, acaoOfensivaRealizada: false, eventos: [],
  }
  assert.equal(mapPartidaToBattle({ ...base, estado: 'vitoria', turnoAtual: 'finalizado' }).status, 'victory')
  assert.equal(mapPartidaToBattle({ ...base, estado: 'derrota', turnoAtual: 'finalizado' }).status, 'defeat')
  assert.equal(mapPartidaToBattle({ ...base, turnoAtual: 'goblin' }).turn, 'enemy')
})
