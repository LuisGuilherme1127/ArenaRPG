const API_ROOT = '/api'

export class ApiError extends Error {
  constructor(message, { status = null, network = false } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.network = network
  }
}

async function request(path, { method = 'GET', body } = {}) {
  let response
  try {
    response = await fetch(`${API_ROOT}${path}`, {
      method,
      headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError('Não foi possível conectar ao backend. Confirme se o Spring Boot está ativo.', {
      network: true,
    })
  }

  const responseText = await response.text()
  let data = null
  if (responseText) {
    try {
      data = JSON.parse(responseText)
    } catch {
      data = responseText
    }
  }

  if (!response.ok) {
    const detail = typeof data === 'object' && data !== null
      ? data.detail ?? data.message ?? `O backend respondeu com HTTP ${response.status}.`
      : `O backend respondeu com HTTP ${response.status}.`
    const unavailable = response.status >= 500
    const message = unavailable
      ? `${detail} Verifique se o backend Java está em execução.`
      : detail
    throw new ApiError(message, { status: response.status, network: unavailable })
  }

  return data
}

export function listCharacters() {
  return request('/personagens')
}

export function getCharacter(type) {
  return request(`/personagens/${encodeURIComponent(type)}`)
}

export function createMatch(type) {
  return request('/partidas', { method: 'POST', body: { tipoPersonagem: type } })
}

export function getMatch(id) {
  return request(`/partidas/${encodeURIComponent(id)}`)
}

export function moveMatch(id, position) {
  return request(`/partidas/${encodeURIComponent(id)}/mover`, {
    method: 'POST',
    body: { x: position.x, y: position.y },
  })
}

export function attackMatch(id) {
  return request(`/partidas/${encodeURIComponent(id)}/atacar`, { method: 'POST' })
}

export function useMatchSkill(id) {
  return request(`/partidas/${encodeURIComponent(id)}/habilidade`, { method: 'POST' })
}

export function finishMatchTurn(id) {
  return request(`/partidas/${encodeURIComponent(id)}/finalizar-turno`, { method: 'POST' })
}
