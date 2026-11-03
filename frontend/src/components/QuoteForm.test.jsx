import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import QuoteForm from './QuoteForm'
import { CURRENCY_CODE, VALIDATION_MESSAGES as MSG } from '../constants'

function renderForm(props = {}) {
  const onSubmit = vi.fn()
  render(<QuoteForm onSubmit={onSubmit} loading={false} fieldErrors={{}} {...props} />)
  return onSubmit
}

function fill({ amount, term, riskBand }) {
  if (amount !== undefined) fireEvent.change(screen.getByLabelText(/^Loan amount/), { target: { value: amount } })
  if (term !== undefined) fireEvent.change(screen.getByLabelText(/^Loan term/), { target: { value: term } })
  if (riskBand !== undefined) fireEvent.change(screen.getByLabelText('Risk band'), { target: { value: riskBand } })
}

function submit() {
  fireEvent.click(screen.getByRole('button', { name: 'Generate Quote' }))
}

describe('QuoteForm', () => {
  afterEach(cleanup)

  it('submits the values converted to numbers', () => {
    const onSubmit = renderForm()

    fill({ amount: '2500.50', term: '24', riskBand: 'HIGH' })
    submit()

    expect(onSubmit).toHaveBeenCalledWith({ loanAmount: 2500.5, loanTermInMonths: 24, riskBand: 'HIGH' })
  })

  it('trims leading and trailing whitespace before sending', () => {
    const onSubmit = renderForm()

    fill({ amount: '  2500.50 ', term: ' 24\t', riskBand: 'HIGH' })
    submit()

    expect(onSubmit).toHaveBeenCalledWith({ loanAmount: 2500.5, loanTermInMonths: 24, riskBand: 'HIGH' })
  })

  it('shows the currency in the loan amount label', () => {
    renderForm()

    expect(screen.getByLabelText(`Loan amount (${CURRENCY_CODE})`)).toBeTruthy()
  })

  it('shows type errors under the fields and does not submit', () => {
    const onSubmit = renderForm()

    fill({ amount: 'abc', term: '12.5', riskBand: 'LOW' })
    submit()

    expect(screen.getByText(MSG.AMOUNT_NOT_A_NUMBER)).toBeTruthy()
    expect(screen.getByText(MSG.TERM_NOT_WHOLE)).toBeTruthy()
    expect(screen.getByLabelText(/^Loan amount/).getAttribute('aria-invalid')).toBe('true')
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('shows required-field errors when submitted empty', () => {
    const onSubmit = renderForm()

    submit()

    expect(screen.getByText(MSG.AMOUNT_REQUIRED)).toBeTruthy()
    expect(screen.getByText(MSG.TERM_REQUIRED)).toBeTruthy()
    expect(screen.getByText(MSG.RISK_BAND_REQUIRED)).toBeTruthy()
    expect(onSubmit).not.toHaveBeenCalled()
  })

  it('clears a field error once the user edits that field', () => {
    renderForm()
    fill({ amount: 'abc' })
    submit()

    fill({ amount: '5000' })

    expect(screen.queryByText(MSG.AMOUNT_NOT_A_NUMBER)).toBeNull()
  })

  it('disables the button and shows progress while loading', () => {
    renderForm({ loading: true })

    expect(screen.getByRole('button', { name: 'Generating…' }).disabled).toBe(true)
    expect(screen.getByRole('progressbar')).toBeTruthy()
  })

  it('shows no spinner when not loading', () => {
    renderForm()

    expect(screen.queryByRole('progressbar')).toBeNull()
  })

  it('shows field errors returned by the server', () => {
    renderForm({ fieldErrors: { loanTermInMonths: 'must be at most 360' } })

    expect(screen.getByText('must be at most 360')).toBeTruthy()
    expect(screen.getByLabelText(/^Loan term/).getAttribute('aria-invalid')).toBe('true')
    expect(screen.getByLabelText(/^Loan amount/).getAttribute('aria-invalid')).toBe('false')
  })
})
