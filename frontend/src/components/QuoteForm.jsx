import { useState } from 'react'
import { Button, Paper, Stack, TextField } from '@mui/material'
import { CURRENCY_CODE, RISK_BANDS } from '../constants'
import { validateLoan } from '../validation/validateLoan'

const EMPTY_FORM = { loanAmount: '', loanTermInMonths: '', riskBand: '' }

// Loan details form. Values are checked by validateLoan on submit, and each problem is shown under its field;
// nothing is sent until the form is valid. Field errors returned by the server are shown the same way.
//
// The number fields are text inputs (with a numeric keyboard on mobile) rather than type="number", because browsers
// silently discard invalid number input, which would make it impossible to tell the user what's wrong.
export default function QuoteForm({ onSubmit, loading, fieldErrors }) {
  // Inputs are kept as strings while typing and only converted to numbers once they're valid.
  const [form, setForm] = useState(EMPTY_FORM)
  const [clientErrors, setClientErrors] = useState({})

  function handleChange(event) {
    const { name, value } = event.target
    setForm({ ...form, [name]: value })
    // Clear a field's error as soon as the user starts correcting it.
    setClientErrors({ ...clientErrors, [name]: undefined })
  }

  function handleSubmit(event) {
    event.preventDefault()
    const errors = validateLoan(form)
    setClientErrors(errors)
    if (Object.keys(errors).length > 0) return

    onSubmit({
      loanAmount: Number(form.loanAmount.trim()),
      loanTermInMonths: Number(form.loanTermInMonths.trim()),
      riskBand: form.riskBand,
    })
  }

  // Our own check wins; otherwise show what the server said about the field.
  const errorFor = (field) => clientErrors[field] ?? fieldErrors[field]

  // Shared props for each field: value, change handler, and the error state + message under it.
  const fieldProps = (name) => ({
    name,
    value: form[name],
    onChange: handleChange,
    error: Boolean(errorFor(name)),
    helperText: errorFor(name),
    fullWidth: true,
  })

  return (
    // noValidate: we show our own messages instead of the browser's validation pop-ups.
    <Paper component="form" onSubmit={handleSubmit} noValidate sx={{ p: 3, mb: 2 }}>
      <Stack spacing={2.5}>
        <TextField
          label={`Loan amount (${CURRENCY_CODE})`}
          autoComplete="off"
          slotProps={{ htmlInput: { inputMode: 'decimal' } }}
          {...fieldProps('loanAmount')}
        />

        <TextField
          label="Loan term (months)"
          autoComplete="off"
          slotProps={{ htmlInput: { inputMode: 'numeric' } }}
          {...fieldProps('loanTermInMonths')}
        />

        {/* A native <select>: simple, accessible, and uses the platform picker on mobile. */}
        <TextField
          select
          label="Risk band"
          slotProps={{ select: { native: true }, inputLabel: { shrink: true } }}
          {...fieldProps('riskBand')}
        >
          <option value="">Select…</option>
          {RISK_BANDS.map((band) => (
            <option key={band.value} value={band.value}>{band.label}</option>
          ))}
        </TextField>

        {/* While loading, MUI shows a spinner and disables the button, preventing duplicate quote requests. */}
        <Button type="submit" variant="contained" size="large" loading={loading} loadingPosition="start">
          {loading ? 'Generating…' : 'Generate Quote'}
        </Button>
      </Stack>
    </Paper>
  )
}
