export function manhattanDistance(first, second) {
  return Math.abs(first.x - second.x) + Math.abs(first.y - second.y)
}

/** Computes the highlighted movement preview only; the Java service validates every move. */
export function getReachablePositions(state, actor = 'player', maxSteps = state[actor].movementMax) {
  const boardSize = state.boardSize ?? 8
  const unit = state[actor]
  const other = actor === 'player' ? state.enemy : state.player
  const key = ({ x, y }) => `${x},${y}`
  const insideBoard = ({ x, y }) => x >= 0 && x < boardSize && y >= 0 && y < boardSize
  const neighbors = ({ x, y }) => [
    { x: x + 1, y },
    { x: x - 1, y },
    { x, y: y + 1 },
    { x, y: y - 1 },
  ]
  const queue = [{ position: unit.position, distance: 0 }]
  const visited = new Set([key(unit.position)])
  const reachable = []

  while (queue.length) {
    const current = queue.shift()
    if (current.distance >= maxSteps) continue

    for (const position of neighbors(current.position)) {
      const positionKey = key(position)
      if (!insideBoard(position) || visited.has(positionKey) || positionKey === key(other.position)) continue

      visited.add(positionKey)
      reachable.push(position)
      queue.push({ position, distance: current.distance + 1 })
    }
  }

  return reachable
}
