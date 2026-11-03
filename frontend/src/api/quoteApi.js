import { API_PATHS, ERROR_CODES, MESSAGES, MESSAGES_BY_ERROR_CODE } from '../constants'

// Error thrown for any failed quote request. fieldErrors holds per-field validation messages from the server;
// code is the backend's error ID (e.g. "CQ-2001") and requestId identifies the request in the server logs.
export class QuoteApiError extends Error {
  constructor(message, fieldErrors = {}, code = null, requestId = null) {
    super(message)
    this.fieldErrors = fieldErrors
    this.code = code
    this.requestId = requestId
  }
}

// A successful response must still contain a usable quote; {} or missing values count as an empty response.
function isCompleteQuote(body) {
  return Boolean(body?.quoteId) && body.commissionRate != null && body.totalCommission != null
}

// Requests a commission quote from our backend. Resolves with the quote, or rejects with a QuoteApiError
// whose message is safe to show to the user.
export async function requestQuote(loan) {
  let response
  try {
    response = await fetch(API_PATHS.QUOTES, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loan),
    })
  } catch {
    // fetch only rejects when no response arrived at all (offline, server down, DNS failure).
    throw new QuoteApiError(MESSAGES.SERVER_UNREACHABLE)
  }

  // Error responses may not be JSON (e.g. from a proxy), so a parse failure must not hide the real error.
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    const message = MESSAGES_BY_ERROR_CODE[body?.code] ?? body?.message ?? MESSAGES.GENERIC_ERROR
    throw new QuoteApiError(message, body?.fieldErrors ?? {}, body?.code ?? null, body?.requestId ?? null)
  }
  if (!isCompleteQuote(body)) {
    throw new QuoteApiError(MESSAGES.EMPTY_RESPONSE, {}, ERROR_CODES.VENDOR_EMPTY_RESPONSE)
  }
  return body
}
