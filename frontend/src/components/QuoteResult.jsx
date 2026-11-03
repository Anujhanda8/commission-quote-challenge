import { Box, Paper, Typography } from '@mui/material'
import { CURRENCY_CODE } from '../constants'
import { formatCurrency, formatPercent } from '../utils/format'

// Shows a successful quote. commissionRate is a percentage (1.75 = 1.75%).
// Amounts use the user's browser locale; the currency is configured in .env.
export default function QuoteResult({ quote }) {
  const rows = [
    ['Quote ID', quote.quoteId],
    ['Commission rate', formatPercent(quote.commissionRate)],
    ['Total commission', formatCurrency(quote.totalCommission, CURRENCY_CODE)],
  ]

  return (
    <Paper component="section" aria-label="Quote result" sx={{ p: 3 }}>
      <Typography variant="h6" component="h2" gutterBottom>Commission quote</Typography>
      {/* Two-column label/value list; a long value only wraps if it really can't fit (e.g. on a phone). */}
      <Box component="dl" sx={{ display: 'grid', gridTemplateColumns: 'auto 1fr', gap: '8px 16px', m: 0 }}>
        {rows.map(([label, value]) => (
          <Box key={label} sx={{ display: 'contents' }}>
            <Box component="dt" sx={{ fontWeight: 600 }}>{label}</Box>
            <Box component="dd" sx={{ m: 0, overflowWrap: 'anywhere' }}>{value}</Box>
          </Box>
        ))}
      </Box>
    </Paper>
  )
}
