import { useState, useEffect } from 'react'
import {
  FiActivity,
  FiUsers,
  FiAlertTriangle,
  FiBell,
  FiRefreshCw,
  FiPlay,
} from 'react-icons/fi'
import { Card, Button, Alert, StatCard, Table } from '../components/common'
import { eventsApi } from '../api/client'
import toast from 'react-hot-toast'

function EventsPage() {
  const [observers, setObservers] = useState(null)
  const [history, setHistory] = useState([])
  const [stats, setStats] = useState(null)
  const [alerts, setAlerts] = useState([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)

  useEffect(() => {
    loadData()
    // Auto-refresh every 10 seconds
    const interval = setInterval(loadData, 10000)
    return () => clearInterval(interval)
  }, [])

  const loadData = async () => {
    try {
      const [observersRes, historyRes, statsRes, alertsRes] = await Promise.all([
        eventsApi.getObservers().catch(() => ({ data: null })),
        eventsApi.getHistory(20).catch(() => ({ data: [] })),
        eventsApi.getStatistics().catch(() => ({ data: null })),
        eventsApi.getAlerts().catch(() => ({ data: [] })),
      ])

      setObservers(observersRes.data)
      setHistory(historyRes.data || [])
      setStats(statsRes.data)
      setAlerts(alertsRes.data || [])
    } catch (error) {
      console.error('Error loading events:', error)
    } finally {
      setLoading(false)
    }
  }

  const refreshData = async () => {
    setRefreshing(true)
    await loadData()
    setRefreshing(false)
    toast.success('Donnees actualisees')
  }

  const runDemo = async () => {
    try {
      const response = await eventsApi.demo()
      toast.success(response.data.message || 'Demo executee!')
      loadData()
    } catch (error) {
      toast.error('Erreur de demo')
    }
  }

  const historyColumns = [
    { header: 'ID', accessor: 'id' },
    { header: 'Type', accessor: 'type' },
    { header: 'Source', accessor: 'source' },
    {
      header: 'Severite',
      accessor: 'severity',
      render: (val) => (
        <span className={`px-2 py-1 text-xs rounded-full ${
          val === 'HIGH' || val === 'CRITICAL' ? 'bg-red-100 text-red-700' :
          val === 'MEDIUM' ? 'bg-yellow-100 text-yellow-700' :
          'bg-green-100 text-green-700'
        }`}>
          {val}
        </span>
      ),
    },
    { header: 'Timestamp', accessor: 'timestamp' },
  ]

  const alertColumns = [
    { header: 'ID Alerte', accessor: 'alertId' },
    { header: 'Type', accessor: 'type' },
    { header: 'Message', accessor: 'message' },
    {
      header: 'Severite',
      accessor: 'severity',
      render: (val) => (
        <span className={`px-2 py-1 text-xs rounded-full ${
          val === 'HIGH' || val === 'CRITICAL' ? 'bg-red-100 text-red-700' :
          val === 'MEDIUM' ? 'bg-yellow-100 text-yellow-700' :
          'bg-green-100 text-green-700'
        }`}>
          {val}
        </span>
      ),
    },
    { header: 'Source', accessor: 'source' },
    { header: 'Timestamp', accessor: 'timestamp' },
  ]

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Evenements</h1>
          {/* <p className="text-gray-500">
            Objectif 10: Pattern Observer - Systeme d'evenements temps reel
          </p> */}
        </div>
        <div className="flex gap-3">
          <Button onClick={refreshData} variant="secondary" loading={refreshing} icon={FiRefreshCw}>
            Actualiser
          </Button>
          <Button onClick={runDemo} icon={FiPlay}>
            test Observer
          </Button>
        </div>
      </div>

      {/* Info Alert */}
      <Alert variant="info">
        Le pattern Observer permet a plusieurs observateurs (Logging, Notification, Security, Statistics)
        de reagir automatiquement aux evenements bancaires. Quand un evenement est publie,
        tous les observateurs interesses sont notifies.
      </Alert>

      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <StatCard
            title="Total Evenements"
            value={stats.totalEvents || 0}
            icon={FiActivity}
            color="primary"
          />
          <StatCard
            title="Transactions"
            value={stats.transactionCount || 0}
            icon={FiBell}
            color="success"
          />
          <StatCard
            title="Montant Total"
            value={`${(stats.totalAmount || 0).toLocaleString()} FCFA`}
            icon={FiBell}
            color="warning"
          />
          <StatCard
            title="Alertes Securite"
            value={alerts.length}
            icon={FiAlertTriangle}
            color="danger"
          />
        </div>
      )}

      {/* Observers List */}
      <Card title="Observateurs Actifs" icon={FiUsers}>
        {observers ? (
          <div className="flex flex-wrap gap-3">
            {observers.observers?.map((name, index) => (
              <div
                key={index}
                className="flex items-center gap-2 px-4 py-2 bg-primary-50 text-primary-700 rounded-lg"
              >
                <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
                <span className="font-medium">{name}</span>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-500">Chargement des observateurs...</p>
        )}
        {observers && (
          <p className="text-sm text-gray-500 mt-4">
            {observers.count} observateur(s) enregistre(s)
          </p>
        )}
      </Card>

      {/* Event Statistics by Type */}
      {stats?.byType && (
        <Card title="Evenements par Type" icon={FiActivity}>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {Object.entries(stats.byType).map(([type, count]) => (
              <div key={type} className="p-4 bg-gray-50 rounded-lg text-center">
                <p className="text-2xl font-bold text-gray-800">{count}</p>
                <p className="text-xs text-gray-500 truncate">{type}</p>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Security Alerts */}
      <Card title="Alertes de Securite" icon={FiAlertTriangle} padding={false}>
        <Table
          columns={alertColumns}
          data={alerts}
          loading={loading}
          emptyMessage="Aucune alerte de securite - Tout est normal"
        />
      </Card>

      {/* Event History */}
      <Card title="Historique des Evenements" icon={FiActivity} padding={false}>
        <Table
          columns={historyColumns}
          data={history}
          loading={loading}
          emptyMessage="Aucun evenement enregistre"
        />
      </Card>

      {/* Observer Pattern Explanation */}
      <Card title="Comment ca marche" icon={FiActivity}>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h4 className="font-medium text-gray-800 mb-2">Observateurs</h4>
            <ul className="text-sm text-gray-600 space-y-2">
              <li><strong>LoggingObserver:</strong> Enregistre tous les evenements dans les logs</li>
              <li><strong>NotificationObserver:</strong> Envoie des SMS/emails aux clients</li>
              <li><strong>SecurityObserver:</strong> Detecte les problemes de securite</li>
              <li><strong>StatisticsObserver:</strong> Collecte les statistiques en temps reel</li>
            </ul>
          </div>
          <div>
            <h4 className="font-medium text-gray-800 mb-2">Types d'evenements</h4>
            <ul className="text-sm text-gray-600 space-y-2">
              <li><strong>DEPOSIT_MADE:</strong> Depot effectue</li>
              <li><strong>WITHDRAWAL_MADE:</strong> Retrait effectue</li>
              <li><strong>LOGIN_FAILED:</strong> Echec de connexion</li>
              <li><strong>LOW_BALANCE:</strong> Solde faible</li>
              <li><strong>FRAUD_DETECTED:</strong> Fraude detectee</li>
            </ul>
          </div>
        </div>
      </Card>
    </div>
  )
}

export default EventsPage
