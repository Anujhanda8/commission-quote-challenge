import { afterEach, describe, expect, it, vi } from 'vitest'
import { QuoteApiError, requestQuote } from './quoteApi'
import { API_PATHS, ERROR_CODES, MESSAGES } from '../constants'
import { mockFetchNetworkError, mockFetchNonJsonResponse, mockFetchResponse } from '../testUtils/mockFetch'

const loan = { loanAmount: 10000, loanTermInMonths: 12, riskBand: 'LOW' }

describe('requestQuote', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('posts the loan as JSON and returns the quote', async () => {
    const quote = { quoteId: 'q-1', commissionRate: 1, totalCommission: 100 }
    mockFetchResponse(200, quote)

    await expect(requestQuote(loan)).resolves.toEqual(quote)
    expect(fetch).toHaveBeenCalledWith(API_PATHS.QUOTES, expect.objectContaining({
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(loan),
    }))
  })

  it('throws the server message and field errors on failure', async () => {
    mockFetchResponse(400, { message: 'Invalid loan details', fieldErrors: { riskBand: 'is required' } })

    const error = await requestQuote(loan).catch((e) => e)

    expect(error).toBeInstanceOf(QuoteApiError)
    expect(error.message).toBe('Invalid loan details')
    expect(error.fieldErrors).toEqual({ riskBand: 'is required' })
  })

  it('carries the error code and request ID from the server', async () => {
    mockFetchResponse(504, { code: 'CQ-2001', message: 'The commission vendor timed out.', fieldErrors: {}, requestId: 'req-1' })

    const error = await requestQuote(loan).catch((e) => e)

    expect(error.code).toBe('CQ-2001')
    expect(error.requestId).toBe('req-1')
    expect(error.message).toBe('The commission vendor timed out.')
  })

  it('shows the empty-response message for the empty-response error code', async () => {
    mockFetchResponse(502, { code: ERROR_CODES.VENDOR_EMPTY_RESPONSE, message: 'server text', fieldErrors: {} })

    const error = await requestQuote(loan).catch((e) => e)

    expect(error.message).toBe(MESSAGES.EMPTY_RESPONSE)
  })

  it.each([{}, null, { quoteId: 'q-1', commissionRate: 1 }])('treats a successful but empty quote %j as an error', async (body) => {
    mockFetchResponse(200, body)

    const error = await requestQuote(loan).catch((e) => e)

    expect(error).toBeInstanceOf(QuoteApiError)
    expect(error.message).toBe(MESSAGES.EMPTY_RESPONSE)
    expect(error.code).toBe(ERROR_CODES.VENDOR_EMPTY_RESPONSE)
  })

  it('uses a default message when the error body is not JSON', async () => {
    mockFetchNonJsonResponse(502)

    const error = await requestQuote(loan).catch((e) => e)

    expect(error.message).toBe(MESSAGES.GENERIC_ERROR)
    expect(error.fieldErrors).toEqual({})
  })

  it('throws a connection message when the request cannot be sent', async () => {
    mockFetchNetworkError()

    await expect(requestQuote(loan)).rejects.toThrow(MESSAGES.SERVER_UNREACHABLE)
  })
})
