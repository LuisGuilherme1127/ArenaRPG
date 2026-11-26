import test from 'node:test'
import assert from 'node:assert/strict'
import { createRequestLock } from '../src/services/request-lock.js'

test('permite uma requisição por vez e invalida respostas após cancelamento', () => {
  const lock = createRequestLock()
  const first = lock.begin()
  assert.equal(typeof first, 'number')
  assert.equal(lock.begin(), null)
  assert.equal(lock.isCurrent(first), true)
  assert.equal(lock.finish(first), true)

  const second = lock.begin()
  assert.notEqual(second, first)
  lock.cancel()
  assert.equal(lock.isCurrent(second), false)
  assert.equal(lock.finish(second), false)
})
