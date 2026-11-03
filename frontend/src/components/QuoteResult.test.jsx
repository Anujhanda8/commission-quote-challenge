import { afterEach, expect, it } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import QuoteResult from './QuoteResult'

afterEach(cleanup)

it('shows the quote id, rate as a percentage and commission as currency', () => {
  render(<QuoteResult quote={{ quoteId: 'q-123', commissionRate: 1.75, totalCommission: 1750 }} />)

  expect(screen.getByText('q-123')).toBeTruthy()
  expect(screen.getByText('1.75%')).toBeTruthy()
  expect(screen.getByText(/1,750\.00/)).toBeTruthy()
})
