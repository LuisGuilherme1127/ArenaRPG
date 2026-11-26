export function createRequestLock() {
  let currentRequest = 0
  let pending = false

  return {
    begin() {
      if (pending) return null
      pending = true
      currentRequest += 1
      return currentRequest
    },
    isCurrent(requestId) {
      return pending && requestId === currentRequest
    },
    finish(requestId) {
      if (!this.isCurrent(requestId)) return false
      pending = false
      return true
    },
    cancel() {
      currentRequest += 1
      pending = false
    },
  }
}
