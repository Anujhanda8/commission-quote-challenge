import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import App from './App'
import { MESSAGES } from './constants'
import { mockFetchNetworkError, mockFetchResponse } from './testUtils/mockFetch'

function fillAndSubmit() {
  fireEvent.change(screen.getByLabelText(/^Loan amount/), { target: { value: '100000' } })
  fireEvent.change(screen.getByLabelText('Loan term (months)'), { target: { value: '72' } })
  fireEvent.change(screen.getByLabelText('Risk band'), { target: { value: 'MEDIUM' } })
  fireEvent.click(screen.getByRole('button', { name: 'Generate Quote' }))
}

describe('App', () => {
  afterEach(() => {
    cleanup()
    vi.unstubAllGlobals()
  })

  it('sends the loan details and shows the quote', async () => {
    mockFetchResponse(200, { quoteId: 'q-123', commissionRate: 1.75, totalCommission: 1750 })
    render(<App />)

    fillAndSubmit()

    expect(screen.getByRole('button', { name: 'Generating…' }).disabled).toBe(true)
    expect(await screen.findByText('q-123')).toBeTruthy()
    expect(screen.getByText('1.75%')).toBeTruthy()
    expect(JSON.parse(fetch.mock.calls[0][1].body)).toEqual({
      loanAmount: 100000,
      loanTermInMonths: 72,
      riskBand: 'MEDIUM',
    })
  })

  it('shows the error message when the vendor fails', async () => {
    mockFetchResponse(504, { code: 'CQ-2001', message: 'The commission vendor timed out. Please try again.', fieldErrors: {}, requestId: 'req-9' })
    render(<App />)

    fillAndSubmit()

    const alert = await screen.findByRole('alert')
    expect(alert.textContent).toContain('The commission vendor timed out. Please try again.')
    expect(alert.textContent).toContain(`${MESSAGES.ERROR_REFERENCE}: CQ-2001 · req-9`)
    expect(screen.getByRole('button', { name: 'Generate Quote' }).disabled).toBe(false)
  })

  it('shows field errors returned by the server', async () => {
    mockFetchResponse(400, { message: 'Invalid loan details', fieldErrors: { loanAmount: 'must be at least 1,000' } })
    render(<App />)

    fillAndSubmit()

    expect(await screen.findByText('must be at least 1,000')).toBeTruthy()
  })

  it('shows the empty-response message when the API returns an empty quote', async () => {
    mockFetchResponse(200, {})
    render(<App />)

    fillAndSubmit()

    expect((await screen.findByRole('alert')).textContent).toContain(MESSAGES.EMPTY_RESPONSE)
  })

  it('shows a friendly message when the server is unreachable', async () => {
    mockFetchNetworkError()
    render(<App />)

    fillAndSubmit()

    expect((await screen.findByRole('alert')).textContent).toBe(MESSAGES.SERVER_UNREACHABLE)
  })
})
