import { describe, expect, it } from 'vitest'
import { formatCurrency, formatNumber, formatPercent } from './format'

describe('format', () => {
  it('formats numbers with thousand separators', () => {
    expect(formatNumber(5000000, 'en-AU')).toBe('5,000,000')
  })

  it('formats currency with 2 decimal places', () => {
    expect(formatCurrency(1750, 'AUD', 'en-AU')).toBe('$1,750.00')
    expect(formatCurrency(0.5, 'USD', 'en-US')).toBe('$0.50')
  })

  it('adds a percent sign to percentage values', () => {
    expect(formatPercent(1.75)).toBe('1.75%')
  })
})
