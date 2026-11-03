// Display formatting. Passing no locale uses the user's browser settings.

// e.g. 5000000 -> "5,000,000"
export function formatNumber(value, locale) {
  return new Intl.NumberFormat(locale).format(value)
}

// e.g. (1750, 'AUD') -> "$1,750.00" (exact symbol depends on the locale)
export function formatCurrency(value, currencyCode, locale) {
  return new Intl.NumberFormat(locale, { style: 'currency', currency: currencyCode }).format(value)
}

// For values that are already percentages, e.g. 1.75 -> "1.75%"
export function formatPercent(value) {
  return `${value}%`
}
