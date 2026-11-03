import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { CssBaseline } from '@mui/material'
import App from './App.jsx'
import ErrorBoundary from './components/ErrorBoundary.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {/* MUI's CSS reset: consistent base styles across browsers. */}
    <CssBaseline />
    <ErrorBoundary>
      <App />
    </ErrorBoundary>
  </StrictMode>,
)
