// Checks on numbers typed by the user, which arrive as strings.

// Plain decimal notation only: digits and an optional decimal part, matching the backend's allow-list
// (digits and '.'). Rejects signs, letters, commas, spaces, exponents ("1e5"), bare dots (".5", "5.") and anything
// else, e.g. "<script>". Values are trimmed before these checks.
const NUMBER = /^\d+(\.\d+)?$/
const WHOLE_NUMBER = /^\d+$/

export function isNumeric(value) {
  return NUMBER.test(value)
}

export function isWholeNumber(value) {
  return WHOLE_NUMBER.test(value)
}

// Number of digits after the decimal point, e.g. "12.345" -> 3, "12" -> 0.
export function countDecimals(value) {
  return (value.split('.')[1] ?? '').length
}
