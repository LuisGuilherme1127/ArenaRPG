export const SPRITE_ASSETS = {
  guerreiro: { path: '/assets/characters/guerreiro.png', pixelArt: false },
  mago: { path: '/assets/characters/mago.png', pixelArt: false },
  arqueiro: { path: '/assets/characters/arqueiro.png', pixelArt: false },
  goblin: { path: '/assets/enemies/goblin.png', pixelArt: false },
}

function isImageResponse(response) {
  if (!response.ok) return false
  const contentType = response.headers.get('content-type')?.split(';')[0]
  return !contentType || contentType.startsWith('image/') || contentType === 'application/octet-stream'
}

async function checkSprite(path, fetcher) {
  try {
    const response = await fetcher(path, { method: 'HEAD' })

    if (response.status === 405 || response.status === 501) {
      const fallbackResponse = await fetcher(path)
      return isImageResponse(fallbackResponse) ? path : null
    }

    return isImageResponse(response) ? path : null
  } catch {
    return null
  }
}

export async function loadSpriteAvailability(fetcher = globalThis.fetch) {
  if (typeof fetcher !== 'function') return {}

  const results = await Promise.all(
    Object.entries(SPRITE_ASSETS).map(async ([spriteId, asset]) => [
      spriteId,
      await checkSprite(asset.path, fetcher),
    ]),
  )

  return Object.fromEntries(results.filter(([, path]) => path))
}

export function renderSprite(spriteId, availableSprites = {}) {
  const src = availableSprites[spriteId]
  if (!src) return ''

  const pixelArt = SPRITE_ASSETS[spriteId]?.pixelArt ?? false
  return `<img class="sprite-image" src="${src}" alt="" aria-hidden="true" draggable="false" decoding="async" data-pixel-art="${pixelArt}">`
}
