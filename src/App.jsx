import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'

// Layout
import Navbar from './components/layout/Navbar'
import Sidebar from './components/layout/Sidebar'

// Pages
import Dashboard from './pages/Dashboard'
import LoginPage from './pages/LoginPage'
import OperatorsPage from './pages/OperatorsPage'
import TransactionsPage from './pages/TransactionsPage'
import SingletonsPage from './pages/SingletonsPage'
import SmsPage from './pages/SmsPage'
import NotificationsPage from './pages/NotificationsPage'
import AccountsPage from './pages/AccountsPage'
import AnalyticsPage from './pages/AnalyticsPage'
import EventsPage from './pages/EventsPage'

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return children
}

function AppLayout({ children }) {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar />
        <main className="flex-1 p-6 ml-64 mt-16">
          {children}
        </main>
      </div>
    </div>
  )
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route path="/" element={
        <ProtectedRoute>
          <AppLayout>
            <Dashboard />
          </AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/operators" element={
        <ProtectedRoute>
          <AppLayout>
            <OperatorsPage />
          </AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/transactions" element={
        <ProtectedRoute>
          <AppLayout>
            <TransactionsPage />
          </AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/singletons" element={
        <ProtectedRoute>
          <AppLayout>
            <SingletonsPage />
          </AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/sms" element={
        <ProtectedRoute>
          <AppLayout>
            <SmsPage />
          </AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/notifications" element={
        <ProtectedRoute>
          <AppLayout>
            <NotificationsPage />
          </AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/accounts" element={
        <ProtectedRoute>
          <AppLayout>
            <AccountsPage />
          </AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/analytics" element={
        <ProtectedRoute>
          <AppLayout>
            <AnalyticsPage />
          </AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/events" element={
        <ProtectedRoute>
          <AppLayout>
            <EventsPage />
          </AppLayout>
        </ProtectedRoute>
      } />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
