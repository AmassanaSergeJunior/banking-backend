import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import {
  FiActivity,
  FiCreditCard,
  FiDollarSign,
  FiUsers,
  FiAlertTriangle,
  FiTrendingUp,
  FiArrowRight,
} from 'react-icons/fi'
import { Card, StatCard } from '../components/common'
import { eventsApi, analyticsApi, accountsApi } from '../api/client'

function Dashboard() {
  const [stats, setStats] = useState(null)
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    try {
      const [statsRes, eventsRes] = await Promise.all([
        analyticsApi.getStatistics().catch(() => ({ data: null })),
        eventsApi.getHistory(5).catch(() => ({ data: [] })),
      ])
      setStats(statsRes.data)
      setEvents(eventsRes.data || [])
    } catch (error) {
      console.error('Dashboard load error:', error)
    } finally {
      setLoading(false)
    }
  }

  const patterns = [
    { name: 'Strategy', obj: 1, path: '/login', color: 'bg-blue-500' },
    { name: 'Factory', obj: 2, path: '/operators', color: 'bg-green-500' },
    { name: 'Builder', obj: 3, path: '/transactions', color: 'bg-purple-500' },
    { name: 'Singleton', obj: 4, path: '/singletons', color: 'bg-orange-500' },
    { name: 'Adapter', obj: 5, path: '/sms', color: 'bg-pink-500' },
    { name: 'Template', obj: 6, path: '/transactions', color: 'bg-teal-500' },
    { name: 'Composite', obj: 7, path: '/notifications', color: 'bg-indigo-500' },
    { name: 'Decorator', obj: 8, path: '/accounts', color: 'bg-red-500' },
    { name: 'Visitor', obj: 9, path: '/analytics', color: 'bg-yellow-500' },
    { name: 'Observer', obj: 10, path: '/events', color: 'bg-cyan-500' },
  ]

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-800">Dashboard</h1>
        <p className="text-gray-500">
          Bienvenue sur le systeme bancaire multi-operateurs
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Total Transactions"
          value={stats?.totalTransactions || 0}
          icon={FiActivity}
          color="primary"
        />
        <StatCard
          title="Total Credits"
          value={`${(stats?.totalCredits || 0).toLocaleString()} FCFA`}
          icon={FiTrendingUp}
          color="success"
        />
        <StatCard
          title="Total Debits"
          value={`${(stats?.totalDebits || 0).toLocaleString()} FCFA`}
         // icon={FiDollarSign}
          color="warning"
        />
        <StatCard
          title="Flux Net"
          value={`${(stats?.netFlow || 0).toLocaleString()} FCFA`}
          icon={FiCreditCard}
          color="info"
        />
      </div>

      {/* Design Patterns Grid */}
      {/* <Card title="Design Patterns Implementes" icon={FiUsers}>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          {patterns.map((pattern) => (
            <Link
              key={pattern.obj}
              to={pattern.path}
              className="group p-4 rounded-lg border border-gray-200 hover:border-primary-300 hover:shadow-md transition-all"
            >
              <div className="flex items-center gap-3">
                <div className={`w-8 h-8 ${pattern.color} rounded-lg flex items-center justify-center text-white text-sm font-bold`}>
                  {pattern.obj}
                </div>
                <div>
                  <p className="font-medium text-gray-800 group-hover:text-primary-600">
                    {pattern.name}
                  </p>
                  <p className="text-xs text-gray-500">Objectif {pattern.obj}</p>
                </div>
              </div>
            </Link>
          ))}
        </div>
      </Card> */}

      {/* Recent Events & Quick Actions */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Events */}
        <Card title="Evenements Recents" icon={FiActivity}>
          {events.length === 0 ? (
            <p className="text-gray-500 text-center py-4">
              Aucun evenement recent
            </p>
          ) : (
            <div className="space-y-3">
              {events.map((event, index) => (
                <div
                  key={index}
                  className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg"
                >
                  <div className={`w-2 h-2 rounded-full ${
                    event.severity === 'HIGH' ? 'bg-red-500' :
                    event.severity === 'MEDIUM' ? 'bg-yellow-500' : 'bg-green-500'
                  }`} />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-800 truncate">
                      {event.type}
                    </p>
                    <p className="text-xs text-gray-500">
                      {event.source} - {event.timestamp}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          )}
          <Link
            to="/events"
            className="mt-4 flex items-center justify-center gap-2 text-primary-600 hover:text-primary-700 text-sm font-medium"
          >
            Voir tous les evenements
            <FiArrowRight size={16} />
          </Link>
        </Card>

        {/* Quick Actions */}
        <Card title="Actions Rapides" icon={FiAlertTriangle}>
          <div className="grid grid-cols-2 gap-3">
            <Link
              to="/transactions"
              className="p-4 bg-primary-50 hover:bg-primary-100 rounded-lg text-center transition-colors"
            >
              <FiDollarSign className="mx-auto text-primary-600 mb-2" size={24} />
              <p className="text-sm font-medium text-primary-700">
                Nouvelle Transaction
              </p>
            </Link>
            <Link
              to="/accounts"
              className="p-4 bg-green-50 hover:bg-green-100 rounded-lg text-center transition-colors"
            >
              <FiCreditCard className="mx-auto text-green-600 mb-2" size={24} />
              <p className="text-sm font-medium text-green-700">
                Creer un Compte
              </p>
            </Link>
            <Link
              to="/sms"
              className="p-4 bg-purple-50 hover:bg-purple-100 rounded-lg text-center transition-colors"
            >
              <FiActivity className="mx-auto text-purple-600 mb-2" size={24} />
              <p className="text-sm font-medium text-purple-700">
                Envoyer SMS
              </p>
            </Link>
            <Link
              to="/analytics"
              className="p-4 bg-orange-50 hover:bg-orange-100 rounded-lg text-center transition-colors"
            >
              <FiTrendingUp className="mx-auto text-orange-600 mb-2" size={24} />
              <p className="text-sm font-medium text-orange-700">
                Voir Analytics
              </p>
            </Link>
          </div>
        </Card>
      </div>
    </div>
  )
}

export default Dashboard
