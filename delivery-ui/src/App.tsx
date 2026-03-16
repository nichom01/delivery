import { BrowserRouter, HashRouter } from 'react-router'
import { ThemeProvider } from './contexts/ThemeContext'
import { AppProvider } from './contexts/AppContext'
import { ErrorBoundary } from './components/ErrorBoundary'
import Router from './Router'

const AppRouter = import.meta.env.VITE_USE_HASH_ROUTE === 'true' ? HashRouter : BrowserRouter

export default function App() {
    return (
        <ErrorBoundary>
            <ThemeProvider>
                <AppRouter>
                    <AppProvider>
                        <Router />
                    </AppProvider>
                </AppRouter>
            </ThemeProvider>
        </ErrorBoundary>
    )
}
