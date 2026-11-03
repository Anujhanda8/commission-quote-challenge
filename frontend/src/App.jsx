import { useState } from 'react'
import { Alert, Container, Typography } from '@mui/material'
import { MESSAGES } from './constants'
import { requestQuote } from './api/quoteApi'
import QuoteForm from './components/QuoteForm'
import QuoteResult from './components/QuoteResult'

// The quote page. Owns the request state: loading, the last error, and the last successful quote.
export default function App() {
  const [quote, setQuote] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(loan) {
    setLoading(true)
    // Clear the previous outcome, so an old quote or error is never shown next to a new request.
    setError(null)
    setQuote(null)
    try {
      setQuote(await requestQuote(loan))
    } catch (err) {
      setError(err)
    } finally {
      setLoading(false)
    }
  }

  return (
    // maxWidth="sm" (600px) fits the quote ID on one line next to its label.
    <Container component="main" maxWidth="sm" sx={{ py: 3 }}>
      <Typography variant="h4" component="h1" gutterBottom>Commission Quote</Typography>
      <QuoteForm onSubmit={handleSubmit} loading={loading} fieldErrors={error?.fieldErrors ?? {}} />
      {/* aria-live makes screen readers announce the result or error when it appears. */}
      <div aria-live="polite">
        {error && (
          <Alert severity="error">
            {error.message}
            {/* The error ID and request ID let support find this exact failure in the server logs. */}
            {error.code && (
              <Typography variant="caption" component="div" sx={{ mt: 0.5 }}>
                {MESSAGES.ERROR_REFERENCE}: {error.code}
                {error.requestId && ` · ${error.requestId}`}
              </Typography>
            )}
          </Alert>
        )}
        {quote && <QuoteResult quote={quote} />}
      </div>
    </Container>
  )
}
