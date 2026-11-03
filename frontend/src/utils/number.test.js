import { describe, expect, it } from 'vitest'
import { countDecimals, isNumeric, isWholeNumber } from './number'

describe('isNumeric', () => {
  it.each(['0', '42', '1000.5', '0.01'])('accepts %s', (value) => {
    expect(isNumeric(value)).toBe(true)
  })

  it.each(['', 'abc', '12a', '5,000', '1e5', '10 000', '.5', '5.', '-7', '+7', '<script>alert(1)</script>', '1;'])('rejects "%s"', (value) => {
    expect(isNumeric(value)).toBe(false)
  })
})

describe('isWholeNumber', () => {
  it('accepts integers and rejects decimals', () => {
    expect(isWholeNumber('360')).toBe(true)
    expect(isWholeNumber('-5')).toBe(false)
    expect(isWholeNumber('12.5')).toBe(false)
    expect(isWholeNumber('12.0')).toBe(false)
  })
})

describe('countDecimals', () => {
  it('counts digits after the decimal point', () => {
    expect(countDecimals('12')).toBe(0)
    expect(countDecimals('12.5')).toBe(1)
    expect(countDecimals('12.345')).toBe(3)
  })
})
