import { useState, useEffect } from 'react'
import {
  FiRepeat,
  FiSend,
  FiArrowRight,
  FiGlobe,
  FiSettings,
  FiList,
} from 'react-icons/fi'
import { Card, Button, Input, Select, Alert, Table, Modal } from '../components/common'
import { transactionsApi } from '../api/client'
import toast from 'react-hot-toast'

function TransactionsPage() {
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [selectedTab, setSelectedTab] = useState('quick')
  const [showDetails, setShowDetails] = useState(null)
  const [comparison, setComparison] = useState(null)

  const [quickForm, setQuickForm] = useState({
    sourceAccount: 'CM001',
    destinationAccount: 'CM002',
    amount: '50000',
  })

  const [interOpForm, setInterOpForm] = useState({
    sourceAccount: 'CM001',
    sourceOperator: 'BANK',
    destinationAccount: 'MM001',
    destinationOperator: 'MOBILE_MONEY',
    amount: '25000',
  })

  const [intlForm, setIntlForm] = useState({
    sourceAccount: 'CM001',
    destinationAccount: 'FR001',
    amount: '100000',
    sourceCurrency: 'XAF',
    targetCurrency: 'EUR',
    exchangeRate: '0.00152',
  })

  useEffect(() => {
    loadTransactions()
  }, [])

  const loadTransactions = async () => {
    try {
      const response = await transactionsApi.getAll()
      setTransactions(response.data || [])
    } catch (error) {
      console.error('Error loading transactions:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleQuickTransaction = async () => {
    setSubmitting(true)
    try {
      const response = await transactionsApi.quick(
        quickForm.sourceAccount,
        quickForm.destinationAccount,
        quickForm.amount
      )
      toast.success('Transaction rapide effectuee!')
      setShowDetails(response.data)
      loadTransactions()
    } catch (error) {
      toast.error('Erreur lors de la transaction')
    } finally {
      setSubmitting(false)
    }
  }

  const handleFullTransaction = async () => {
    setSubmitting(true)
    try {
      const response = await transactionsApi.full(
        quickForm.sourceAccount,
        quickForm.destinationAccount,
        quickForm.amount
      )
      toast.success('Transaction complete effectuee!')
      setShowDetails(response.data)
      loadTransactions()
    } catch (error) {
      toast.error('Erreur lors de la transaction')
    } finally {
      setSubmitting(false)
    }
  }

  const handleInterOpTransaction = async () => {
    setSubmitting(true)
    try {
      const response = await transactionsApi.interOperator(
        interOpForm.sourceAccount,
        interOpForm.sourceOperator,
        interOpForm.destinationAccount,
        interOpForm.destinationOperator,
        interOpForm.amount
      )
      toast.success('Transaction inter-operateur effectuee!')
      setShowDetails(response.data)
      loadTransactions()
    } catch (error) {
      toast.error('Erreur lors de la transaction')
    } finally {
      setSubmitting(false)
    }
  }

  const handleInternationalTransaction = async () => {
    setSubmitting(true)
    try {
      const response = await transactionsApi.international(
        intlForm.sourceAccount,
        intlForm.destinationAccount,
        intlForm.amount,
        intlForm.sourceCurrency,
        intlForm.targetCurrency,
        intlForm.exchangeRate
      )
      toast.success('Transaction internationale effectuee!')
      setShowDetails(response.data)
      loadTransactions()
    } catch (error) {
      toast.error('Erreur lors de la transaction')
    } finally {
      setSubmitting(false)
    }
  }

  const compareVariants = async () => {
    try {
      const response = await transactionsApi.compareVariants(quickForm.amount)
      setComparison(response.data)
    } catch (error) {
      toast.error('Erreur de comparaison')
    }
  }

  const tabs = [
    { id: 'quick', label: 'Rapide', icon: FiSend },
    { id: 'interop', label: 'Inter-Operateur', icon: FiArrowRight },
    { id: 'international', label: 'International', icon: FiGlobe },
    { id: 'history', label: 'Historique', icon: FiList },
  ]

  const historyColumns = [
    { header: 'Reference', accessor: 'reference' },
    { header: 'Type', accessor: 'type' },
    { header: 'Source', accessor: 'sourceAccount' },
    { header: 'Destination', accessor: 'destinationAccount' },
    {
      header: 'Montant',
      accessor: 'amount',
      render: (val) => `${val?.toLocaleString()} FCFA`,
    },
    {
      header: 'Statut',
      accessor: 'status',
      render: (val) => (
        <span className={`px-2 py-1 text-xs rounded-full ${
          val === 'COMPLETED' ? 'bg-green-100 text-green-700' :
          val === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
          'bg-red-100 text-red-700'
        }`}>
          {val}
        </span>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-800">Transactions</h1>
        {/* <p className="text-gray-500">
          Objectif 3: Pattern Builder - Construction flexible de transactions
        </p> */}
      </div>

      {/* Info Alert */}
      <Alert variant="info">
        Le pattern Builder permet de construire des transactions complexes etape par etape,
        avec differentes variantes (rapide, complete, inter-operateur, internationale).
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

      {/* Quick Transaction */}
      {selectedTab === 'quick' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card title="Transaction Rapide / Complete" icon={FiSend}>
            <div className="space-y-4">
              <Input
                label="Compte Source"
                value={quickForm.sourceAccount}
                onChange={(e) => setQuickForm({ ...quickForm, sourceAccount: e.target.value })}
                placeholder="CM001"
              />
              <Input
                label="Compte Destination"
                value={quickForm.destinationAccount}
                onChange={(e) => setQuickForm({ ...quickForm, destinationAccount: e.target.value })}
                placeholder="CM002"
              />
              <Input
                label="Montant (FCFA)"
                type="number"
                value={quickForm.amount}
                onChange={(e) => setQuickForm({ ...quickForm, amount: e.target.value })}
                placeholder="50000"
              />
              <div className="flex gap-3">
                <Button
                  onClick={handleQuickTransaction}
                  loading={submitting}
                  className="flex-1"
                >
                  Transaction Rapide
                </Button>
                <Button
                  onClick={handleFullTransaction}
                  loading={submitting}
                  variant="secondary"
                  className="flex-1"
                >
                  Transaction Complete
                </Button>
              </div>
              <Button
                onClick={compareVariants}
                variant="outline"
                className="w-full"
                icon={FiSettings}
              >
                Comparer les Variantes
              </Button>
            </div>
          </Card>

          {comparison && (
            <Card title="Comparaison des Variantes" icon={FiSettings}>
              <div className="space-y-4">
                <div className="p-4 bg-blue-50 rounded-lg">
                  <h4 className="font-medium text-blue-800">Transaction Rapide</h4>
                  <p className="text-sm text-blue-600">
                    Commissions: {comparison.quick?.totalCommissions?.toLocaleString()} FCFA
                  </p>
                  <p className="text-sm text-blue-600">
                    Montant final: {comparison.quick?.finalAmount?.toLocaleString()} FCFA
                  </p>
                  <p className="text-xs text-blue-500 mt-1">
                    Etapes: {comparison.quick?.steps}
                  </p>
                </div>
                <div className="p-4 bg-green-50 rounded-lg">
                  <h4 className="font-medium text-green-800">Transaction Complete</h4>
                  <p className="text-sm text-green-600">
                    Commissions: {comparison.full?.totalCommissions?.toLocaleString()} FCFA
                  </p>
                  <p className="text-sm text-green-600">
                    Montant final: {comparison.full?.finalAmount?.toLocaleString()} FCFA
                  </p>
                  <p className="text-xs text-green-500 mt-1">
                    Etapes: {comparison.full?.steps}
                  </p>
                </div>
                <div className="text-center text-sm text-gray-500">
                  Difference de commission: {comparison.commissionDifference?.toLocaleString()} FCFA
                </div>
              </div>
            </Card>
          )}
        </div>
      )}

      {/* Inter-Operator Transaction */}
      {selectedTab === 'interop' && (
        <Card title="Transaction Inter-Operateur" icon={FiArrowRight}>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <h4 className="font-medium text-gray-700">Source</h4>
              <Input
                label="Compte"
                value={interOpForm.sourceAccount}
                onChange={(e) => setInterOpForm({ ...interOpForm, sourceAccount: e.target.value })}
              />
              <Select
                label="Operateur"
                options={[
                  { value: 'BANK', label: 'Banque' },
                  { value: 'MOBILE_MONEY', label: 'Mobile Money' },
                  { value: 'MICROFINANCE', label: 'Microfinance' },
                ]}
                value={interOpForm.sourceOperator}
                onChange={(e) => setInterOpForm({ ...interOpForm, sourceOperator: e.target.value })}
              />
            </div>
            <div className="space-y-4">
              <h4 className="font-medium text-gray-700">Destination</h4>
              <Input
                label="Compte"
                value={interOpForm.destinationAccount}
                onChange={(e) => setInterOpForm({ ...interOpForm, destinationAccount: e.target.value })}
              />
              <Select
                label="Operateur"
                options={[
                  { value: 'BANK', label: 'Banque' },
                  { value: 'MOBILE_MONEY', label: 'Mobile Money' },
                  { value: 'MICROFINANCE', label: 'Microfinance' },
                ]}
                value={interOpForm.destinationOperator}
                onChange={(e) => setInterOpForm({ ...interOpForm, destinationOperator: e.target.value })}
              />
            </div>
          </div>
          <div className="mt-4 space-y-4">
            <Input
              label="Montant (FCFA)"
              type="number"
              value={interOpForm.amount}
              onChange={(e) => setInterOpForm({ ...interOpForm, amount: e.target.value })}
            />
            <Button
              onClick={handleInterOpTransaction}
              loading={submitting}
              className="w-full"
              icon={FiArrowRight}
            >
              Executer le Transfert Inter-Operateur
            </Button>
          </div>
        </Card>
      )}

      {/* International Transaction */}
      {selectedTab === 'international' && (
        <Card title="Transaction Internationale" icon={FiGlobe}>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Input
              label="Compte Source"
              value={intlForm.sourceAccount}
              onChange={(e) => setIntlForm({ ...intlForm, sourceAccount: e.target.value })}
            />
            <Input
              label="Compte Destination"
              value={intlForm.destinationAccount}
              onChange={(e) => setIntlForm({ ...intlForm, destinationAccount: e.target.value })}
            />
            <Input
              label="Montant"
              type="number"
              value={intlForm.amount}
              onChange={(e) => setIntlForm({ ...intlForm, amount: e.target.value })}
            />
            <Input
              label="Devise Source"
              value={intlForm.sourceCurrency}
              onChange={(e) => setIntlForm({ ...intlForm, sourceCurrency: e.target.value })}
            />
            <Input
              label="Devise Cible"
              value={intlForm.targetCurrency}
              onChange={(e) => setIntlForm({ ...intlForm, targetCurrency: e.target.value })}
            />
            <Input
              label="Taux de Change"
              value={intlForm.exchangeRate}
              onChange={(e) => setIntlForm({ ...intlForm, exchangeRate: e.target.value })}
            />
          </div>
          <Button
            onClick={handleInternationalTransaction}
            loading={submitting}
            className="w-full mt-4"
            icon={FiGlobe}
          >
            Executer le Transfert International
          </Button>
        </Card>
      )}

      {/* History */}
      {selectedTab === 'history' && (
        <Card title="Historique des Transactions" icon={FiList} padding={false}>
          <Table
            columns={historyColumns}
            data={transactions}
            loading={loading}
            emptyMessage="Aucune transaction trouvee"
          />
        </Card>
      )}

      {/* Transaction Details Modal */}
      <Modal
        isOpen={!!showDetails}
        onClose={() => setShowDetails(null)}
        title="Details de la Transaction"
        size="lg"
      >
        {showDetails && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-gray-500">Reference</p>
                <p className="font-medium">{showDetails.reference}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Type</p>
                <p className="font-medium">{showDetails.type}</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Montant</p>
                <p className="font-medium">{showDetails.amount?.toLocaleString()} FCFA</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Commissions</p>
                <p className="font-medium">{showDetails.totalCommissions?.toLocaleString()} FCFA</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Montant Final</p>
                <p className="font-medium text-green-600">{showDetails.finalAmount?.toLocaleString()} FCFA</p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Statut</p>
                <p className="font-medium">{showDetails.status}</p>
              </div>
            </div>
            {showDetails.executionLogs && (
              <div>
                <p className="text-sm text-gray-500 mb-2">Logs d'execution</p>
                <div className="bg-gray-50 p-3 rounded-lg text-sm font-mono max-h-40 overflow-y-auto">
                  {showDetails.executionLogs.map((log, i) => (
                    <p key={i} className="text-gray-700">{log}</p>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

export default TransactionsPage
