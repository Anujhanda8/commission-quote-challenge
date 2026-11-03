import { Component } from 'react'
import { Alert, Container } from '@mui/material'
import { MESSAGES } from '../constants'

// Catches unexpected rendering errors anywhere below it, so the user sees a message instead of a blank page.
// Must be a class component: React has no hook equivalent for error boundaries.
export default class ErrorBoundary extends Component {
  state = { hasError: false }

  static getDerivedStateFromError() {
    return { hasError: true }
  }

  componentDidCatch(error, info) {
    console.error('Unexpected UI error', error, info.componentStack)
  }

  render() {
    if (this.state.hasError) {
      return (
        <Container component="main" maxWidth="sm" sx={{ py: 3 }}>
          <Alert severity="error">{MESSAGES.UNEXPECTED_UI_ERROR}</Alert>
        </Container>
      )
    }
    return this.props.children
  }
}
