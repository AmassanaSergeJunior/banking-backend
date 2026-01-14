import { useState, useEffect } from 'react'
import {
  FiBarChart2,
  FiTrendingUp,
  FiTrendingDown,
  FiDollarSign,
  FiFileText,
  FiAlertTriangle,
  FiPlay,
} from 'react-icons/fi'
import { Card, Button, Alert, StatCard, Table } from '../components/common'
import { analyticsApi } from '../api/client'
import toast from 'react-hot-toast'

function AnalyticsPage() {
  const [stats, setStats] = useState(null)
  const [tax, setTax] = useState(null)
  const [audit, setAudit] = useState(null)
  const [loading, setLoading] = useState(true)
  const [selectedTab, setSelectedTab] = useState('stats')

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [statsRes, taxRes, auditRes] = await Promise.all([
        analyticsApi.getStatistics().catch(() => ({ data: null })),
        analyticsApi.getTax().catch(() => ({ data: null })),
        analyticsApi.getAudit().catch(() => ({ data: null })),
      ])

      setStats(statsRes.data)
      setTax(taxRes.data)
      setAudit(auditRes.data)
    } catch (error) {
      console.error('Error loading analytics:', error)
    } finally {
      setLoading(false)
    }
  }

  const runDemo = async () => {
    try {
      const response = await analyticsApi.demo()
      toast.success(response.data.message || 'Demo executee!')
      loadData()
    } catch (error) {
      toast.error('Erreur de demo')
    }
  }

  const tabs = [
    { id: 'stats', label: 'Statistiques', icon: FiBarChart2 },
    { id: 'tax', label: 'Rapport Fiscal', icon: FiFileText },
    { id: 'audit', label: 'Audit', icon: FiAlertTriangle },
  ]

  const auditColumns = [
    { header: 'Type', accessor: 'type' },
    { header: 'Compte', accessor: 'account' },
    { header: 'Montant', accessor: 'amount', render: (val) => `${val?.toLocaleString()} FCFA` },
    { header: 'Message', accessor: 'message' },
    {
      header: 'Severite',
      accessor: 'severity',
      render: (val) => (
        <span className={`px-2 py-1 text-xs rounded-full ${
          val === 'HIGH' ? 'bg-red-100 text-red-700' :
          val === 'MEDIUM' ? 'bg-yellow-100 text-yellow-700' :
          'bg-green-100 text-green-700'
        }`}>
          {val}
        </span>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Analytics</h1>
          {/* <p className="text-gray-500">
            Objectif 9: Pattern Visitor - Analyse et rapports
          </p> */}
        </div>
        <Button onClick={runDemo} icon={FiPlay}>
          test Visitor
        </Button>
      </div>

      {/* Info Alert */}
      <Alert variant="info">
        Le pattern Visitor permet d'ajouter de nouvelles operations (statistiques, taxes, audit)
        sur une structure d'objets (transactions) sans modifier ces objets.
        Chaque Visitor parcourt les transactions et collecte des informations specifiques.
      </Alert>

      {/* Tabs */}
      <div className="flex gap-2 border-b border-gray-200">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setSelectedTab(tab.id)}
            className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
              selectedTab === tab.id
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            <tab.icon size={18} />
            {tab.label}
          </button>
        ))}
      </div>

      {/* Statistics Tab */}
      {selectedTab === 'stats' && stats && (
        <div className="space-y-6">
          {/* Stats Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <StatCard
              title="Total Transactions"
              value={stats.totalTransactions || 0}
              icon={FiBarChart2}
              color="primary"
            />
            <StatCard
              title="Total Credits"
              value={`${(stats.totalCredits || 0).toLocaleString()} FCFA`}
              icon={FiTrendingUp}
              color="success"
            />
            <StatCard
              title="Total Debits"
              value={`${(stats.totalDebits || 0).toLocaleString()} FCFA`}
              icon={FiTrendingDown}
              color="danger"
            />
            <StatCard
              title="Flux Net"
              value={`${(stats.netFlow || 0).toLocaleString()} FCFA`}
              icon={FiDollarSign}
              color={stats.netFlow >= 0 ? 'success' : 'danger'}
            />
          </div>

          {/* Additional Stats */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card title="Repartition par Type" icon={FiBarChart2}>
              {stats.countByType ? (
                <div className="space-y-3">
                  {Object.entries(stats.countByType).map(([type, count]) => (
                    <div key={type} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <span className="text-gray-700">{type}</span>
                      <span className="font-bold">{count}</span>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500 text-center py-4">Aucune donnee</p>
              )}
            </Card>

            <Card title="Statistiques Detaillees" icon={FiBarChart2}>
              <div className="space-y-3">
                <div className="flex justify-between p-3 bg-gray-50 rounded-lg">
                  <span className="text-gray-600">Moyenne par transaction</span>
                  <span className="font-bold">{(stats.averageTransaction || 0).toLocaleString()} FCFA</span>
                </div>
                <div className="flex justify-between p-3 bg-gray-50 rounded-lg">
                  <span className="text-gray-600">Total Transferts</span>
                  <span className="font-bold">{(stats.totalTransfers || 0).toLocaleString()} FCFA</span>
                </div>
                <div className="flex justify-between p-3 bg-gray-50 rounded-lg">
                  <span className="text-gray-600">Total Frais</span>
                  <span className="font-bold">{(stats.totalFees || 0).toLocaleString()} FCFA</span>
                </div>
              </div>
            </Card>
          </div>
        </div>
      )}

      {/* Tax Tab */}
      {selectedTab === 'tax' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card title="Rapport Fiscal" icon={FiFileText}>
            {tax ? (
              <div className="space-y-4">
                <div className="p-4 bg-green-50 rounded-lg">
                  <p className="text-sm text-green-600">Revenus Imposables</p>
                  <p className="text-2xl font-bold text-green-700">
                    {(tax.taxableIncome || 0).toLocaleString()} FCFA
                  </p>
                </div>
                <div className="p-4 bg-red-50 rounded-lg">
                  <p className="text-sm text-red-600">Depenses Deductibles</p>
                  <p className="text-2xl font-bold text-red-700">
                    {(tax.taxableExpenses || 0).toLocaleString()} FCFA
                  </p>
                </div>
                <div className="p-4 bg-blue-50 rounded-lg">
                  <p className="text-sm text-blue-600">Montant Net Imposable</p>
                  <p className="text-2xl font-bold text-blue-700">
                    {(tax.netTaxableAmount || 0).toLocaleString()} FCFA
                  </p>
                </div>
                <div className="p-4 bg-purple-50 rounded-lg">
                  <p className="text-sm text-purple-600">TVA Collectee</p>
                  <p className="text-2xl font-bold text-purple-700">
                    {(tax.totalVAT || 0).toLocaleString()} FCFA
                  </p>
                </div>
              </div>
            ) : (
              <p className="text-gray-500 text-center py-8">
                Aucune donnee fiscale disponible
              </p>
            )}
          </Card>

          <Card title="Explication" icon={FiFileText}>
            <div className="space-y-4 text-sm text-gray-600">
              <p>
                Le <strong>TaxVisitor</strong> parcourt toutes les transactions
                et calcule automatiquement:
              </p>
              <ul className="list-disc list-inside space-y-2">
                <li>Les revenus imposables (depots, virements recus)</li>
                <li>Les depenses deductibles (retraits, frais)</li>
                <li>Le montant net imposable</li>
                <li>La TVA sur les transactions</li>
              </ul>
              <p>
                Ce pattern permet d'ajouter de nouveaux types de calculs fiscaux
                sans modifier les classes de transactions existantes.
              </p>
            </div>
          </Card>
        </div>
      )}

      {/* Audit Tab */}
      {selectedTab === 'audit' && (
        <div className="space-y-6">
          {/* Audit Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <StatCard
              title="Total Entrees"
              value={audit?.totalEntries || 0}
              icon={FiFileText}
              color="primary"
            />
            <StatCard
              title="Alertes"
              value={audit?.alertCount || 0}
              icon={FiAlertTriangle}
              color="danger"
            />
            <StatCard
              title="Taux de Conformite"
              value={audit?.totalEntries > 0
                ? `${(100 - (audit.alertCount / audit.totalEntries * 100)).toFixed(1)}%`
                : '100%'
              }
              icon={FiBarChart2}
              color="success"
            />
          </div>

          {/* Audit Alerts Table */}
          <Card title="Alertes d'Audit" icon={FiAlertTriangle} padding={false}>
            <Table
              columns={auditColumns}
              data={audit?.alerts || []}
              loading={loading}
              emptyMessage="Aucune alerte - Toutes les transactions sont conformes"
            />
          </Card>

          {/* Audit Explanation */}
          <Card title="A propos de l'AuditVisitor" icon={FiFileText}>
            <div className="text-sm text-gray-600 space-y-2">
              <p>
                L'<strong>AuditVisitor</strong> analyse chaque transaction et detecte:
              </p>
              <ul className="list-disc list-inside space-y-1">
                <li>Les transactions de montant eleve ( 1,000,000 FCFA)</li>
                <li>Les retraits multiples suspects</li>
                <li>Les patterns de fraude potentiels</li>
                <li>Les anomalies de compte</li>
              </ul>
            </div>
          </Card>
        </div>
      )}
    </div>
  )
}

export default AnalyticsPage
