import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { FiUser, FiLock, FiSmartphone, FiEye, FiCamera } from 'react-icons/fi'
import { useAuth } from '../context/AuthContext'
import { Button, Input, Card, Alert } from '../components/common'

const authMethods = [
  { type: 'PASSWORD', label: 'Mot de passe', icon: FiLock, description: 'Connexion classique' },
  { type: 'OTP', label: 'Code OTP', icon: FiSmartphone, description: 'Code unique par SMS' },
  { type: 'FINGERPRINT', label: 'Empreinte', icon: FiEye, description: 'Biometrie digitale' },
  { type: 'FACIAL', label: 'Visage', icon: FiCamera, description: 'Reconnaissance faciale' },
]

function LoginPage() {
  const navigate = useNavigate()
  const { login, generateOtp, isAuthenticated } = useAuth()

  const [selectedMethod, setSelectedMethod] = useState('PASSWORD')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [otpGenerated, setOtpGenerated] = useState(false)

  const [formData, setFormData] = useState({
    userId: '',
    password: '',
    otpCode: '',
    fingerprintData: '',
    facialData: '',
  })

  // Redirect if already authenticated
  if (isAuthenticated) {
    navigate('/')
    return null
  }

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
    setError('')
  }

  const handleGenerateOtp = async () => {
    if (!formData.userId) {
      setError('Veuillez entrer votre identifiant')
      return
    }

    setLoading(true)
    const result = await generateOtp(formData.userId)
    setLoading(false)

    if (result.success) {
      setOtpGenerated(true)
      // In dev mode, show the OTP
      if (result.data?.otp_dev_only) {
        setFormData({ ...formData, otpCode: result.data.otp_dev_only })
      }
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    const credentials = {
      userId: formData.userId,
      authenticationType: selectedMethod,
    }

    switch (selectedMethod) {
      case 'PASSWORD':
        credentials.password = formData.password
        break
      case 'OTP':
        credentials.otpCode = formData.otpCode
        break
      case 'FINGERPRINT':
        credentials.fingerprintData = formData.fingerprintData || 'SIMULATED_FINGERPRINT'
        break
      case 'FACIAL':
        credentials.facialData = formData.facialData || 'SIMULATED_FACIAL'
        break
    }

    const result = await login(credentials)
    setLoading(false)

    if (result.success) {
      navigate('/')
    } else {
      setError(result.message || 'Echec de connexion')
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-600 to-primary-800 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-white rounded-2xl mx-auto flex items-center justify-center shadow-lg mb-4">
            <span className="text-3xl font-bold text-primary-600">B</span>
          </div>
          <h1 className="text-2xl font-bold text-white">BanqueApp</h1>
          <p className="text-primary-200">Systeme Multi-Operateurs</p>
        </div>

        {/* Login Card */}
        <Card className="shadow-2xl">
          <div className="mb-6">
            <h2 className="text-xl font-semibold text-gray-800">Connexion</h2>
            {/* <p className="text-sm text-gray-500">
              Objectif 1: Pattern Strategy - Authentification Multi-Methodes
            </p> */}
          </div>

          {error && (
            <Alert variant="error" className="mb-4">
              {error}
            </Alert>
          )}

          {/* Auth Method Selection */}
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Methode d'authentification
            </label>
            <div className="grid grid-cols-2 gap-2">
              {authMethods.map((method) => (
                <button
                  key={method.type}
                  type="button"
                  onClick={() => {
                    setSelectedMethod(method.type)
                    setOtpGenerated(false)
                  }}
                  className={`p-3 rounded-lg border-2 transition-all text-left ${
                    selectedMethod === method.type
                      ? 'border-primary-500 bg-primary-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  <method.icon
                    className={selectedMethod === method.type ? 'text-primary-600' : 'text-gray-400'}
                    size={20}
                  />
                  <p className={`text-sm font-medium mt-1 ${
                    selectedMethod === method.type ? 'text-primary-700' : 'text-gray-700'
                  }`}>
                    {method.label}
                  </p>
                  <p className="text-xs text-gray-500">{method.description}</p>
                </button>
              ))}
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* User ID */}
            <Input
              label="Identifiant"
              name="userId"
              value={formData.userId}
              onChange={handleInputChange}
              placeholder="Entrez votre identifiant"
              icon={FiUser}
              required
            />

            {/* Password */}
            {selectedMethod === 'PASSWORD' && (
              <Input
                label="Mot de passe"
                name="password"
                type="password"
                value={formData.password}
                onChange={handleInputChange}
                placeholder="Entrez votre mot de passe"
                icon={FiLock}
                required
              />
            )}

            {/* OTP */}
            {selectedMethod === 'OTP' && (
              <div className="space-y-3">
                {!otpGenerated ? (
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={handleGenerateOtp}
                    loading={loading}
                    className="w-full"
                  >
                    Generer un code OTP
                  </Button>
                ) : (
                  <Input
                    label="Code OTP"
                    name="otpCode"
                    value={formData.otpCode}
                    onChange={handleInputChange}
                    placeholder="Entrez le code recu"
                    icon={FiSmartphone}
                    required
                  />
                )}
              </div>
            )}

            {/* Biometric info */}
            {(selectedMethod === 'FINGERPRINT' || selectedMethod === 'FACIAL') && (
              <Alert variant="info">
                Mode simulation: L'authentification biometrique sera simulee.
              </Alert>
            )}

            <Button
              type="submit"
              loading={loading}
              className="w-full"
              disabled={selectedMethod === 'OTP' && !otpGenerated}
            >
              Se connecter
            </Button>
          </form>

          {/* Demo credentials */}
          <div className="mt-6 pt-4 border-t border-gray-200">
            <p className="text-xs text-gray-500 text-center">
              Demo: Utilisez n'importe quel identifiant avec le mot de passe "password"
            </p>
          </div>
        </Card>

        {/* Footer */}
        <p className="text-center text-primary-200 text-sm mt-6">
          TP INF461 - Universite de Yaounde I
        </p>
      </div>
    </div>
  )
}

export default LoginPage
