import { createContext, useContext, useState, useEffect } from 'react'
import { authApi } from '../api/client'
import toast from 'react-hot-toast'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [loading, setLoading] = useState(true)
  const [authMethods, setAuthMethods] = useState([])

  // Check for existing session on mount
  useEffect(() => {
    const storedUser = localStorage.getItem('user')
    const storedToken = localStorage.getItem('authToken')

    if (storedUser && storedToken) {
      setUser(JSON.parse(storedUser))
      setIsAuthenticated(true)
    }
    setLoading(false)

    // Load auth methods
    loadAuthMethods()
  }, [])

  const loadAuthMethods = async () => {
    try {
      const response = await authApi.getMethods()
      setAuthMethods(response.data)
    } catch (error) {
      console.error('Failed to load auth methods:', error)
    }
  }

  const login = async (credentials) => {
    try {
      const response = await authApi.login(credentials)
      const data = response.data

      if (data.success) {
        const userData = {
          userId: data.userId,
          authenticationType: data.authenticationType,
        }

        setUser(userData)
        setIsAuthenticated(true)
        localStorage.setItem('user', JSON.stringify(userData))
        localStorage.setItem('authToken', data.accessToken || 'demo-token')

        toast.success(`Connexion reussie via ${data.authenticationType}`)
        return { success: true }
      } else {
        toast.error(data.message || 'Echec de connexion')
        return { success: false, message: data.message }
      }
    } catch (error) {
      const message = error.response?.data?.message || 'Erreur de connexion'
      toast.error(message)
      return { success: false, message }
    }
  }

  const logout = () => {
    setUser(null)
    setIsAuthenticated(false)
    localStorage.removeItem('user')
    localStorage.removeItem('authToken')
    toast.success('Deconnexion reussie')
  }

  const generateOtp = async (userId) => {
    try {
      const response = await authApi.generateOtp(userId)
      toast.success('OTP genere avec succes')
      return { success: true, data: response.data }
    } catch (error) {
      const message = error.response?.data?.message || 'Erreur de generation OTP'
      toast.error(message)
      return { success: false, message }
    }
  }

  const value = {
    user,
    isAuthenticated,
    loading,
    authMethods,
    login,
    logout,
    generateOtp,
  }

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export default AuthContext
