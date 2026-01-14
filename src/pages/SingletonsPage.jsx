import { useState, useEffect } from 'react'
import {
  FiDatabase,
  FiActivity,
  FiSettings,
  FiCheck,
  FiAlertCircle,
} from 'react-icons/fi'
import { Card, Button, Input, Alert, StatCard } from '../components/common'
import { singletonsApi } from '../api/client'
import toast from 'react-hot-toast'

function SingletonsPage() {
  const [stats, setStats] = useState(null)
  const [health, setHealth] = useState(null)
  const [authConfig, setAuthConfig] = useState(null)
  const [uniquenessDemo, setUniquenessDemo] = useState(null)
  const [loading, setLoading] = useState(true)

  const [smsForm, setSmsForm] = useState({
    phoneNumber: '+237690123456',
    message: 'Test SMS via Singleton',
  })

  const [eventForm, setEventForm] = useState({
    type: 'SYSTEM_INFO',
    source: 'Frontend',
    message: 'Test event',
  })

  const [configForm, setConfigForm] = useState({
    maxFailedAttempts: 3,
    sessionTimeoutMinutes: 30,
    multiFactorEnabled: true,
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [statsRes, healthRes, configRes] = await Promise.all([
        singletonsApi.getStats().catch(() => ({ data: null })),
        singletonsApi.health().catch(() => ({ data: null })),
        singletonsApi.getAuthConfig().catch(() => ({ data: null })),
      ])

      setStats(statsRes.data)
      setHealth(healthRes.data)
      setAuthConfig(configRes.data)

      if (configRes.data) {
        setConfigForm({
          maxFailedAttempts: configRes.data.maxFailedAttempts || 3,
          sessionTimeoutMinutes: configRes.data.sessionTimeoutMinutes || 30,
          multiFactorEnabled: configRes.data.multiFactorEnabled || false,
        })
      }
    } catch (error) {
      console.error('Error loading singleton data:', error)
    } finally {
      setLoading(false)
    }
  }

  const sendSms = async () => {
    try {
      const response = await singletonsApi.sendSms(smsForm.phoneNumber, smsForm.message)
      toast.success('SMS envoye via Singleton!')
      loadData()
    } catch (error) {
      toast.error('Erreur d\'envoi SMS')
    }
  }

  const publishEvent = async () => {
    try {
      await singletonsApi.publishEvent(eventForm.type, eventForm.source, eventForm.message)
      toast.success('Evenement publie!')
      loadData()
    } catch (error) {
      toast.error('Erreur de publication')
    }
  }

  const updateConfig = async () => {
    try {
      await singletonsApi.updateAuthConfig(
        configForm.maxFailedAttempts,
        configForm.sessionTimeoutMinutes,
        configForm.multiFactorEnabled
      )
      toast.success('Configuration mise a jour!')
      loadData()
    } catch (error) {
      toast.error('Erreur de mise a jour')
    }
  }

  const demoUniqueness = async () => {
    try {
      const response = await singletonsApi.demoUniqueness()
      setUniquenessDemo(response.data)
      toast.success('Demo d\'unicite executee!')
    } catch (error) {
      toast.error('Erreur de demo')
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-800">Singletons</h1>
        <p className="text-gray-500">
          Objectif 4: Pattern Singleton - Services uniques globaux
        </p>
      </div>

      {/* Info Alert */}
      <Alert variant="info">
        Le pattern Singleton garantit qu'une classe n'a qu'une seule instance et fournit
        un point d'acces global a cette instance. Ici: NotificationService, AuthConfig, EventManager.
      </Alert>

      {/* Health Status */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className={`p-4 rounded-lg border-2 ${
          health?.notificationService ? 'border-green-300 bg-green-50' : 'border-red-300 bg-red-50'
        }`}>
          <div className="flex items-center gap-2">
            {health?.notificationService ? (
              <FiCheck className="text-green-600" size={20} />
            ) : (
              <FiAlertCircle className="text-red-600" size={20} />
            )}
            <span className="font-medium">NotificationService</span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            {health?.notificationService ? 'Operationnel' : 'Hors service'}
          </p>
        </div>

        <div className={`p-4 rounded-lg border-2 ${
          health?.authConfig ? 'border-green-300 bg-green-50' : 'border-red-300 bg-red-50'
        }`}>
          <div className="flex items-center gap-2">
            {health?.authConfig ? (
              <FiCheck className="text-green-600" size={20} />
            ) : (
              <FiAlertCircle className="text-red-600" size={20} />
            )}
            <span className="font-medium">AuthConfig</span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            {health?.authConfig ? 'Operationnel' : 'Hors service'}
          </p>
        </div>

        <div className={`p-4 rounded-lg border-2 ${
          health?.eventManager ? 'border-green-300 bg-green-50' : 'border-red-300 bg-red-50'
        }`}>
          <div className="flex items-center gap-2">
            {health?.eventManager ? (
              <FiCheck className="text-green-600" size={20} />
            ) : (
              <FiAlertCircle className="text-red-600" size={20} />
            )}
            <span className="font-medium">EventManager</span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            {health?.eventManager ? 'Operationnel' : 'Hors service'}
          </p>
        </div>
      </div>

      {/* Demo Uniqueness */}
      <Card title="Demo d'Unicite" icon={FiDatabase}>
        <p className="text-gray-600 mb-4">
          Cette demo prouve que chaque Singleton ne possede qu'une seule instance
          partagee dans toute l'application.
        </p>
        <Button onClick={demoUniqueness} icon={FiActivity}>
          Executer le test
        </Button>

        {uniquenessDemo && (
          <div className="mt-4 p-4 bg-gray-50 rounded-lg">
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-gray-500">NotificationService acces:</p>
                <p className="font-mono font-bold">{uniquenessDemo.notificationServiceAccesses}</p>
              </div>
              <div>
                <p className="text-gray-500">Meme instance:</p>
                <p className={`font-bold ${uniquenessDemo.sameNotificationInstance ? 'text-green-600' : 'text-red-600'}`}>
                  {uniquenessDemo.sameNotificationInstance ? 'OUI' : 'NON'}
                </p>
              </div>
              <div>
                <p className="text-gray-500">AuthConfig acces:</p>
                <p className="font-mono font-bold">{uniquenessDemo.authConfigAccesses}</p>
              </div>
              <div>
                <p className="text-gray-500">Meme instance:</p>
                <p className={`font-bold ${uniquenessDemo.sameAuthConfigInstance ? 'text-green-600' : 'text-red-600'}`}>
                  {uniquenessDemo.sameAuthConfigInstance ? 'OUI' : 'NON'}
                </p>
              </div>
            </div>
            <p className="mt-3 text-sm text-gray-600">{uniquenessDemo.message}</p>
          </div>
        )}
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* SMS via Singleton */}
        <Card title="NotificationService (SMS)" icon={FiActivity}>
          <div className="space-y-4">
            <Input
              label="Numero de telephone"
              value={smsForm.phoneNumber}
              onChange={(e) => setSmsForm({ ...smsForm, phoneNumber: e.target.value })}
            />
            <Input
              label="Message"
              value={smsForm.message}
              onChange={(e) => setSmsForm({ ...smsForm, message: e.target.value })}
            />
            <Button onClick={sendSms} className="w-full">
              Envoyer SMS
            </Button>
            {stats?.notificationStats && (
              <div className="text-sm text-gray-500">
                Total envoyes: {stats.notificationStats.totalSent || 0}
              </div>
            )}
          </div>
        </Card>

        {/* Auth Config */}
        <Card title="AuthConfig" icon={FiSettings}>
          <div className="space-y-4">
            <Input
              label="Tentatives max echouees"
              type="number"
              value={configForm.maxFailedAttempts}
              onChange={(e) => setConfigForm({ ...configForm, maxFailedAttempts: parseInt(e.target.value) })}
            />
            <Input
              label="Timeout session (minutes)"
              type="number"
              value={configForm.sessionTimeoutMinutes}
              onChange={(e) => setConfigForm({ ...configForm, sessionTimeoutMinutes: parseInt(e.target.value) })}
            />
            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={configForm.multiFactorEnabled}
                onChange={(e) => setConfigForm({ ...configForm, multiFactorEnabled: e.target.checked })}
                className="w-4 h-4 text-primary-600 rounded"
              />
              <span className="text-sm">Multi-facteur active</span>
            </label>
            <Button onClick={updateConfig} variant="secondary" className="w-full">
              Mettre a jour la config
            </Button>
          </div>
        </Card>

        {/* Event Publisher */}
        <Card title="EventManager" icon={FiActivity}>
          <div className="space-y-4">
            <Input
              label="Type d'evenement"
              value={eventForm.type}
              onChange={(e) => setEventForm({ ...eventForm, type: e.target.value })}
            />
            <Input
              label="Source"
              value={eventForm.source}
              onChange={(e) => setEventForm({ ...eventForm, source: e.target.value })}
            />
            <Input
              label="Message"
              value={eventForm.message}
              onChange={(e) => setEventForm({ ...eventForm, message: e.target.value })}
            />
            <Button onClick={publishEvent} className="w-full">
              Publier l'evenement
            </Button>
            {stats?.eventManagerStats && (
              <div className="text-sm text-gray-500">
                Total publies: {stats.eventManagerStats.totalPublished || 0}
              </div>
            )}
          </div>
        </Card>

        {/* Stats Summary */}
        <Card title="Statistiques Globales" icon={FiDatabase}>
          <div className="space-y-3">
            <div className="flex justify-between p-3 bg-gray-50 rounded-lg">
              <span className="text-gray-600">Notifications envoyees</span>
              <span className="font-bold">{stats?.notificationStats?.totalSent || 0}</span>
            </div>
            <div className="flex justify-between p-3 bg-gray-50 rounded-lg">
              <span className="text-gray-600">Evenements publies</span>
              <span className="font-bold">{stats?.eventManagerStats?.totalPublished || 0}</span>
            </div>
            <div className="flex justify-between p-3 bg-gray-50 rounded-lg">
              <span className="text-gray-600">Modifications config</span>
              <span className="font-bold">{stats?.authConfigStats?.modificationCount || 0}</span>
            </div>
          </div>
        </Card>
      </div>
    </div>
  )
}

export default SingletonsPage
