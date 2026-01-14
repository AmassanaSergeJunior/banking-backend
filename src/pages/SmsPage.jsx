import { useState, useEffect } from 'react'
import {
  FiMessageSquare,
  FiSend,
  FiUsers,
  FiCheckCircle,
  FiXCircle,
  FiRefreshCw,
} from 'react-icons/fi'
import { Card, Button, Input, Select, Alert, Table, StatCard } from '../components/common'
import { smsApi } from '../api/client'
import toast from 'react-hot-toast'

function SmsPage() {
  const [providers, setProviders] = useState(null)
  const [balances, setBalances] = useState(null)
  const [availability, setAvailability] = useState(null)
  const [statistics, setStatistics] = useState(null)
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)

  const [smsForm, setSmsForm] = useState({
    phoneNumber: '+237690123456',
    message: 'Test SMS via Adapter Pattern',
    provider: '',
  })

  const [bulkForm, setBulkForm] = useState({
    phoneNumbers: '+237690123456,+237677889900',
    message: 'Bulk SMS test',
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [providersRes, balancesRes, availRes, statsRes, summaryRes] = await Promise.all([
        smsApi.getProviders().catch(() => ({ data: null })),
        smsApi.getBalances().catch(() => ({ data: null })),
        smsApi.getAvailability().catch(() => ({ data: null })),
        smsApi.getStatistics().catch(() => ({ data: null })),
        smsApi.getSummary().catch(() => ({ data: null })),
      ])

      setProviders(providersRes.data)
      setBalances(balancesRes.data)
      setAvailability(availRes.data)
      setStatistics(statsRes.data)
      setSummary(summaryRes.data)
    } catch (error) {
      console.error('Error loading SMS data:', error)
    } finally {
      setLoading(false)
    }
  }

  const sendSms = async () => {
    setSending(true)
    try {
      let response
      if (smsForm.provider) {
        response = await smsApi.sendWithProvider(smsForm.provider, smsForm.phoneNumber, smsForm.message)
      } else {
        response = await smsApi.send(smsForm.phoneNumber, smsForm.message)
      }
      toast.success(`SMS envoye! ID: ${response.data.messageId}`)
      loadData()
    } catch (error) {
      toast.error('Erreur d\'envoi SMS')
    } finally {
      setSending(false)
    }
  }

  const sendWithFallback = async () => {
    setSending(true)
    try {
      const response = await smsApi.sendWithFallback(smsForm.phoneNumber, smsForm.message)
      toast.success(`SMS envoye avec fallback! ID: ${response.data.messageId}`)
      loadData()
    } catch (error) {
      toast.error('Erreur d\'envoi SMS')
    } finally {
      setSending(false)
    }
  }

  const sendBulk = async () => {
    setSending(true)
    try {
      const phoneNumbers = bulkForm.phoneNumbers.split(',').map((p) => p.trim())
      const response = await smsApi.bulk({ phoneNumbers, message: bulkForm.message })
      toast.success(`SMS en masse: ${response.data.sent} envoyes, ${response.data.failed} echoues`)
      loadData()
    } catch (error) {
      toast.error('Erreur d\'envoi en masse')
    } finally {
      setSending(false)
    }
  }

  const detectProvider = async () => {
    try {
      const response = await smsApi.detectProvider(smsForm.phoneNumber)
      toast.success(`Provider recommande: ${response.data.providerName}`)
      setSmsForm({ ...smsForm, provider: response.data.recommendedProvider })
    } catch (error) {
      toast.error('Erreur de detection')
    }
  }

  const providerOptions = providers
    ? Object.entries(providers)
        .filter(([key]) => key !== 'default')
        .map(([key, value]) => ({
          value: key,
          label: value.fullName || key,
        }))
    : []

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-800">Service SMS</h1>
        <p className="text-gray-500">
          Objectif 5: Pattern Adapter - Integration multi-providers SMS
        </p>
      </div>

      {/* Info Alert */}
      <Alert variant="info">
        Le pattern Adapter permet d'utiliser differents providers SMS (Twilio, MTN, Orange)
        avec une interface unifiee. Le systeme peut automatiquement basculer entre providers.
      </Alert>

      {/* Stats */}
      {summary && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <StatCard
            title="Total Envoyes"
            value={summary.totalSent || 0}
            icon={FiSend}
            color="success"
          />
          <StatCard
            title="Echecs"
            value={summary.totalFailed || 0}
            icon={FiXCircle}
            color="danger"
          />
          <StatCard
            title="Credits Utilises"
            value={summary.totalCreditsUsed || 0}
            icon={FiMessageSquare}
            color="warning"
          />
          <StatCard
            title="Taux de Succes"
            value={`${(summary.successRate || 0).toFixed(1)}%`}
            icon={FiCheckCircle}
            color="primary"
          />
        </div>
      )}

      {/* Provider Status */}
      <Card title="Etat des Providers" icon={FiRefreshCw}>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {availability && Object.entries(availability)
            .filter(([key]) => !['availableCount', 'totalProviders'].includes(key))
            .map(([provider, data]) => (
              <div
                key={provider}
                className={`p-4 rounded-lg border-2 ${
                  data.available ? 'border-green-300 bg-green-50' : 'border-red-300 bg-red-50'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-medium">{data.fullName || provider}</h4>
                    <p className="text-sm text-gray-500">
                      Credits: {balances?.[provider]?.credits || 0}
                    </p>
                  </div>
                  {data.available ? (
                    <FiCheckCircle className="text-green-600" size={24} />
                  ) : (
                    <FiXCircle className="text-red-600" size={24} />
                  )}
                </div>
                {statistics?.[provider] && (
                  <div className="mt-2 text-xs text-gray-500">
                    Envoyes: {statistics[provider].sent || 0} |
                    Taux: {(statistics[provider].successRate || 0).toFixed(1)}%
                  </div>
                )}
              </div>
            ))}
        </div>
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Single SMS */}
        <Card title="Envoyer un SMS" icon={FiSend}>
          <div className="space-y-4">
            <Input
              label="Numero de telephone"
              value={smsForm.phoneNumber}
              onChange={(e) => setSmsForm({ ...smsForm, phoneNumber: e.target.value })}
              placeholder="+237690123456"
            />
            <div className="flex gap-2">
              <Select
                label="Provider (optionnel)"
                options={providerOptions}
                value={smsForm.provider}
                onChange={(e) => setSmsForm({ ...smsForm, provider: e.target.value })}
                placeholder="Auto-selection"
                className="flex-1"
              />
              <Button
                onClick={detectProvider}
                variant="outline"
                className="mt-6"
                title="Detecter le meilleur provider"
              >
                <FiRefreshCw />
              </Button>
            </div>
            <Input
              label="Message"
              value={smsForm.message}
              onChange={(e) => setSmsForm({ ...smsForm, message: e.target.value })}
              placeholder="Votre message..."
            />
            <div className="flex gap-3">
              <Button
                onClick={sendSms}
                loading={sending}
                className="flex-1"
                icon={FiSend}
              >
                Envoyer
              </Button>
              <Button
                onClick={sendWithFallback}
                loading={sending}
                variant="secondary"
                className="flex-1"
              >
                Avec Fallback
              </Button>
            </div>
          </div>
        </Card>

        {/* Bulk SMS */}
        <Card title="SMS en Masse" icon={FiUsers}>
          <div className="space-y-4">
            <Input
              label="Numeros (separes par des virgules)"
              value={bulkForm.phoneNumbers}
              onChange={(e) => setBulkForm({ ...bulkForm, phoneNumbers: e.target.value })}
              placeholder="+237690123456,+237677889900"
            />
            <Input
              label="Message"
              value={bulkForm.message}
              onChange={(e) => setBulkForm({ ...bulkForm, message: e.target.value })}
              placeholder="Message pour tous les destinataires..."
            />
            <Button
              onClick={sendBulk}
              loading={sending}
              className="w-full"
              icon={FiUsers}
            >
              Envoyer en Masse
            </Button>
          </div>
        </Card>
      </div>

      {/* Statistics per Provider */}
      {statistics && (
        <Card title="Statistiques par Provider" icon={FiMessageSquare} padding={false}>
          <Table
            columns={[
              { header: 'Provider', accessor: 'provider' },
              { header: 'Envoyes', accessor: 'sent' },
              { header: 'Echecs', accessor: 'failed' },
              {
                header: 'Credits',
                accessor: 'creditsUsed',
                render: (val) => val?.toLocaleString() || 0,
              },
              {
                header: 'Temps Moyen (ms)',
                accessor: 'avgResponseTime',
                render: (val) => val?.toFixed(0) || '-',
              },
              {
                header: 'Taux de Succes',
                accessor: 'successRate',
                render: (val) => `${(val || 0).toFixed(1)}%`,
              },
            ]}
            data={Object.entries(statistics).map(([provider, stats]) => ({
              provider,
              ...stats,
            }))}
          />
        </Card>
      )}
    </div>
  )
}

export default SmsPage
