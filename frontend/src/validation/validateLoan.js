import { LOAN_LIMITS, RISK_BANDS, VALIDATION_MESSAGES as MSG } from '../constants'
import { countDecimals, isNumeric, isWholeNumber } from '../utils/number'

// Checks the raw form values (strings, as typed) and returns { fieldName: message } for every invalid field.
// An empty object means the form is valid. The server re-validates everything; this gives instant feedback.
export function validateLoan({ loanAmount, loanTermInMonths, riskBand }) {
  const errors = {}

  const amountError = validateAmount(loanAmount.trim())
  if (amountError) errors.loanAmount = amountError

  const termError = validateTerm(loanTermInMonths.trim())
  if (termError) errors.loanTermInMonths = termError

  if (!RISK_BANDS.some((band) => band.value === riskBand)) {
    errors.riskBand = MSG.RISK_BAND_REQUIRED
  }

  return errors
}

function validateAmount(value) {
  if (value === '') return MSG.AMOUNT_REQUIRED
  if (!isNumeric(value)) return MSG.AMOUNT_NOT_A_NUMBER
  if (countDecimals(value) > LOAN_LIMITS.MAX_AMOUNT_DECIMALS) return MSG.AMOUNT_TOO_MANY_DECIMALS
  const amount = Number(value)
  if (amount < LOAN_LIMITS.MIN_AMOUNT || amount > LOAN_LIMITS.MAX_AMOUNT) return MSG.AMOUNT_OUT_OF_RANGE
  return null
}

function validateTerm(value) {
  if (value === '') return MSG.TERM_REQUIRED
  if (!isNumeric(value)) return MSG.TERM_NOT_A_NUMBER
  if (!isWholeNumber(value)) return MSG.TERM_NOT_WHOLE
  const term = Number(value)
  if (term < LOAN_LIMITS.MIN_TERM_MONTHS || term > LOAN_LIMITS.MAX_TERM_MONTHS) return MSG.TERM_OUT_OF_RANGE
  return null
}
