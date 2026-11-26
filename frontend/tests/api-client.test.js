import test from 'node:test'
import assert from 'node:assert/strict'
import {
  ApiError,
  attackMatch,
  createMatch,
  finishMatchTurn,
  getCharacter,
  getMatch,
  listCharacters,
  moveMatch,
  useMatchSkill,
} from '../src/services/api-client.js'

function mockFetch(t, implementation) {
  const originalFetch = globalThis.fetch
  globalThis.fetch = implementation
  t.after(() => { globalThis.fetch = originalFetch })
}

test('consulta personagens e codifica o tipo no caminho', async (t) => {
  const calls = []
  mockFetch(t, async (url, options) => {
    calls.push({ url, options })
    return Response.json([{ tipo: 'mago' }])
  })

  assert.deepEqual(await listCharacters(), [{ tipo: 'mago' }])
  assert.deepEqual(await getCharacter('mago arcano'), [{ tipo: 'mago' }])
  assert.equal(calls[0].url, '/api/personagens')
  assert.equal(calls[0].options.method, 'GET')
  assert.equal(calls[1].url, '/api/personagens/mago%20arcano')
})

test('cria partida com o tipo de personagem e lê o estado inicial retornado', async (t) => {
  const calls = []
  mockFetch(t, async (url, options) => {
    calls.push({ url, options })
    return Response.json({ id: 'partida-1', estado: 'em_andamento' }, { status: 201 })
  })

  assert.deepEqual(await createMatch('arqueiro'), { id: 'partida-1', estado: 'em_andamento' })
  assert.equal(calls[0].url, '/api/partidas')
  assert.equal(calls[0].options.method, 'POST')
  assert.deepEqual(JSON.parse(calls[0].options.body), { tipoPersonagem: 'arqueiro' })
})

test('usa os caminhos e corpos dos comandos da partida', async (t) => {
  const calls = []
  mockFetch(t, async (url, options) => {
    calls.push({ url, options })
    return Response.json({ id: 'match-123', estado: 'em_andamento' })
  })

  await getMatch('match-123')
  await moveMatch('match-123', { x: 3, y: 4 })
  await attackMatch('match-123')
  await useMatchSkill('match-123')
  await finishMatchTurn('match-123')

  assert.deepEqual(calls.map(({ url }) => url), [
    '/api/partidas/match-123',
    '/api/partidas/match-123/mover',
    '/api/partidas/match-123/atacar',
    '/api/partidas/match-123/habilidade',
    '/api/partidas/match-123/finalizar-turno',
  ])
  assert.equal(calls[0].options.method, 'GET')
  assert.deepEqual(JSON.parse(calls[1].options.body), { x: 3, y: 4 })
  assert.equal(calls[2].options.body, undefined)
  assert.equal(calls[3].options.body, undefined)
  assert.equal(calls[4].options.body, undefined)
})

test('propaga detalhe HTTP do backend e informa falha de conexão', async (t) => {
  let offline = false
  let status = 409
  mockFetch(t, async () => {
    if (offline) throw new TypeError('offline')
    if (status === 502) return Response.json({ error: 'proxy offline' }, { status })
    return Response.json({ status, detail: 'Fora de alcance.' }, { status })
  })
  await assert.rejects(attackMatch('match-123'), (error) => {
    assert.ok(error instanceof ApiError)
    assert.equal(error.status, 409)
    assert.equal(error.message, 'Fora de alcance.')
    return true
  })

  status = 502
  await assert.rejects(getMatch('match-123'), (error) => {
    assert.ok(error instanceof ApiError)
    assert.equal(error.status, 502)
    assert.equal(error.network, true)
    return true
  })

  offline = true
  await assert.rejects(getMatch('match-123'), (error) => {
    assert.ok(error instanceof ApiError)
    assert.equal(error.network, true)
    return true
  })
})
