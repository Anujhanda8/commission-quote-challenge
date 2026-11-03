import { describe, expect, it } from 'vitest'
import { validateLoan } from './validateLoan'
import { VALIDATION_MESSAGES as MSG } from '../constants'

const valid = { loanAmount: '250000.50', loanTermInMonths: '72', riskBand: 'MEDIUM' }

function errorsFor(changes) {
  return validateLoan({ ...valid, ...changes })
}

describe('validateLoan', () => {
  it('accepts valid values, including the limits themselves', () => {
    expect(validateLoan(valid)).toEqual({})
    expect(errorsFor({ loanAmount: '1000', loanTermInMonths: '1' })).toEqual({})
    expect(errorsFor({ loanAmount: '5000000', loanTermInMonths: '360' })).toEqual({})
    expect(errorsFor({ loanAmount: ' 2000 ', loanTermInMonths: '\t12  ' })).toEqual({}) // surrounding spaces are trimmed
  })

  it.each(['abc', '12a', '5,000', '1e5', '10 000', '.5', '5.', '-1000', '<script>alert(1)</script>'])('rejects non-numeric amount "%s"', (amount) => {
    expect(errorsFor({ loanAmount: amount })).toEqual({ loanAmount: MSG.AMOUNT_NOT_A_NUMBER })
  })

  it('rejects amounts with more than 2 decimal places', () => {
    expect(errorsFor({ loanAmount: '1000.123' })).toEqual({ loanAmount: MSG.AMOUNT_TOO_MANY_DECIMALS })
  })

  it.each(['999.99', '5000000.01', '0'])('rejects out-of-range amount %s', (amount) => {
    expect(errorsFor({ loanAmount: amount })).toEqual({ loanAmount: MSG.AMOUNT_OUT_OF_RANGE })
  })

  it.each(['abc', '12 months', '1e2', '-12', '<b>12</b>'])('rejects non-numeric term "%s"', (term) => {
    expect(errorsFor({ loanTermInMonths: term })).toEqual({ loanTermInMonths: MSG.TERM_NOT_A_NUMBER })
  })

  it('rejects terms that are not whole months', () => {
    expect(errorsFor({ loanTermInMonths: '12.5' })).toEqual({ loanTermInMonths: MSG.TERM_NOT_WHOLE })
  })

  it.each(['0', '361'])('rejects out-of-range term %s', (term) => {
    expect(errorsFor({ loanTermInMonths: term })).toEqual({ loanTermInMonths: MSG.TERM_OUT_OF_RANGE })
  })

  it('reports every missing field at once', () => {
    expect(validateLoan({ loanAmount: '', loanTermInMonths: '  ', riskBand: '' })).toEqual({
      loanAmount: MSG.AMOUNT_REQUIRED,
      loanTermInMonths: MSG.TERM_REQUIRED,
      riskBand: MSG.RISK_BAND_REQUIRED,
    })
  })

  it('rejects an unknown risk band', () => {
    expect(errorsFor({ riskBand: 'EXTREME' })).toEqual({ riskBand: MSG.RISK_BAND_REQUIRED })
  })
})
