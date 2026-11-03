import { afterEach, expect, it, vi } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import ErrorBoundary from './ErrorBoundary'
import { MESSAGES } from '../constants'

function Broken() {
  throw new Error('boom')
}

afterEach(() => {
  cleanup()
  vi.restoreAllMocks()
})

it('shows a fallback message when a child component crashes', () => {
  vi.spyOn(console, 'error').mockImplementation(() => {})

  render(
    <ErrorBoundary>
      <Broken />
    </ErrorBoundary>,
  )

  expect(screen.getByRole('alert').textContent).toBe(MESSAGES.UNEXPECTED_UI_ERROR)
})

it('renders children when nothing goes wrong', () => {
  render(
    <ErrorBoundary>
      <p>All good</p>
    </ErrorBoundary>,
  )

  expect(screen.getByText('All good')).toBeTruthy()
})
