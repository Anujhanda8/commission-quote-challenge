// Test helpers that replace the global fetch. Call vi.unstubAllGlobals() after each test to restore it.
import { vi } from 'vitest'

// fetch resolves with the given HTTP status and JSON body.
export function mockFetchResponse(status, body) {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    json: () => Promise.resolve(body),
  }))
}

// fetch resolves, but the body isn't valid JSON (e.g. an HTML error page from a proxy).
export function mockFetchNonJsonResponse(status) {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
    ok: status >= 200 && status < 300,
    json: () => Promise.reject(new SyntaxError('Unexpected token')),
  }))
}

// fetch rejects, as it does when the server can't be reached at all.
export function mockFetchNetworkError() {
  vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))
}
