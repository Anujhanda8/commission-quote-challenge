// Fixed values used across the UI. Environment-specific settings come from .env (VITE_* variables).
import { formatNumber } from './utils/format'

// Locale used for numbers inside the validation messages below.
const MESSAGE_LOCALE = 'en-AU'

export const API_PATHS = {
  QUOTES: '/api/quotes',
}

// Must match the backend limits in AppConstants.LoanLimits. The server re-validates everything;
// these only give the user quick feedback in the form.
export const LOAN_LIMITS = {
  MIN_AMOUNT: 1000,
  MAX_AMOUNT: 5000000,
  MAX_AMOUNT_DECIMALS: 2,
  MIN_TERM_MONTHS: 1,
  MAX_TERM_MONTHS: 360,
}

export const RISK_BANDS = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
]

export const CURRENCY_CODE = import.meta.env.VITE_CURRENCY_CODE ?? 'AUD'


// Shown under each form field when the input is invalid.
export const VALIDATION_MESSAGES = {
  AMOUNT_REQUIRED: 'Loan amount is required',
  AMOUNT_NOT_A_NUMBER: 'Loan amount must be a number (digits only, e.g. 250000.50)',
  AMOUNT_TOO_MANY_DECIMALS: `Loan amount can have at most ${LOAN_LIMITS.MAX_AMOUNT_DECIMALS} decimal places`,
  AMOUNT_OUT_OF_RANGE: `Loan amount must be between ${formatNumber(LOAN_LIMITS.MIN_AMOUNT, MESSAGE_LOCALE)} and ${formatNumber(LOAN_LIMITS.MAX_AMOUNT, MESSAGE_LOCALE)}`,
  TERM_REQUIRED: 'Loan term is required',
  TERM_NOT_A_NUMBER: 'Loan term must be a number',
  TERM_NOT_WHOLE: 'Loan term must be a whole number of months',
  TERM_OUT_OF_RANGE: `Loan term must be between ${LOAN_LIMITS.MIN_TERM_MONTHS} and ${LOAN_LIMITS.MAX_TERM_MONTHS} months`,
  RISK_BAND_REQUIRED: 'Please select a risk band',
}

export const MESSAGES = {
  SERVER_UNREACHABLE: 'Could not reach the server. Check your connection and try again.',
  GENERIC_ERROR: 'Something went wrong while generating the quote.',
  UNEXPECTED_UI_ERROR: 'Something went wrong. Please refresh the page.',
  EMPTY_RESPONSE:
    'The commission API returned an empty response, so a quote could not be generated for the given input. Please try again.',
  ERROR_REFERENCE: 'Reference',
}

// Error IDs returned by the backend in every error response ("code"). The full list is in the backend's
// ErrorCode.java; only the ones the UI treats specially are listed here.
export const ERROR_CODES = {
  VENDOR_EMPTY_RESPONSE: 'CQ-2003',
}

// For these codes the UI shows its own message instead of the server's.
export const MESSAGES_BY_ERROR_CODE = {
  [ERROR_CODES.VENDOR_EMPTY_RESPONSE]: MESSAGES.EMPTY_RESPONSE,
}
