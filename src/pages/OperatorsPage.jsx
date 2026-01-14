import { useState, useEffect } from 'react'
import {
  FiGrid,
  FiDollarSign,
  FiPercent,
  FiRefreshCw,
} from 'react-icons/fi'
import { Card, Button, Input, Select, Alert, Table } from '../components/common'
import { operatorsApi } from '../api/client'
import toast from 'react-hot-toast'

const operatorTypes = [
  { value: 'BANK', label: 'Banque Traditionnelle' },
  { value: 'MOBILE_MONEY', label: 'Mobile Money' },
  { value: 'MICROFINANCE', label: 'Microfinance' },
]

function OperatorsPage() {
  const [operators, setOperators] = useState([])
  const [loading, setLoading] = useState(true)
  const [feeComparison, setFeeComparison] = useState(null)
  const [calculating, setCalculating] = useState(false)

  const [feeForm, setFeeForm] = useState({
    operator: 'BANK',
    amount: '',
    transactionType: 'TRANSFER',
  })

  const [compareForm, setCompareForm] = useState({
    amount: '100000',
    transactionType: 'TRANSFER',
  })

  useEffect(() => {
    loadOperators()
  }, [])

  const loadOperators = async () => {
    try {
      const response = await operatorsApi.getAll()
      setOperators(response.data || [])
    } catch (error) {
      toast.error('Erreur de chargement des operateurs')
    } finally {
      setLoading(false)
    }
  }

  const calculateFee = async () => {
    if (!feeForm.amount) {
      toast.error('Veuillez entrer un montant')
      return
    }

    setCalculating(true)
    try {
      const response = await operatorsApi.calculateFee(
        feeForm.operator,
        feeForm.amount,
        feeForm.transactionType
      )
      toast.success(`Frais calcules: ${response.data.fee} FCFA`)
    } catch (error) {
      toast.error('Erreur de calcul des frais')
    } finally {
      setCalculating(false)
    }
  }

  const compareFees = async () => {
    setCalculating(true)
    try {
      const response = await operatorsApi.compareFees(
        compareForm.amount,
        compareForm.transactionType
      )
      setFeeComparison(response.data)
    } catch (error) {
      toast.error('Erreur de comparaison des frais')
    } finally {
      setCalculating(false)
    }
  }

  const comparisonColumns = [
    { header: 'Operateur', accessor: 'operator' },
    { header: 'Type', accessor: 'type' },
    {
      header: 'Montant',
      accessor: 'amount',
      render: (val) => `${val?.toLocaleString()} FCFA`,
    },
    {
      header: 'Frais',
      accessor: 'fee',
      render: (val) => `${val?.toLocaleString()} FCFA`,
    },
    {
      header: 'Total',
      accessor: 'total',
      render: (val) => `${val?.toLocaleString()} FCFA`,
    },
    {
      header: 'Taux',
      accessor: 'baseRate',
      render: (val) => `${(val * 100).toFixed(2)}%`,
    },
  ]

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-800">Gestion des Operateurs</h1>
        {/* <p className="text-gray-500">
          Objectif 2: Pattern Factory - Creation d'operateurs bancaires
        </p> */}
      </div>

      {/* Info Alert */}
      <Alert variant="info">
        Le pattern Factory permet de creer differents types d'operateurs (Banque, Mobile Money, Microfinance)
        avec leurs propres regles de calcul de frais et de validation.
      </Alert>

      {/* Operators List */}
      <Card title="Operateurs Disponibles" icon={FiGrid}>
        {loading ? (
          <p className="text-center py-4 text-gray-500">Chargement...</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {operators.map((op, index) => (
              <div
                key={index}
                className="p-4 border border-gray-200 rounded-lg hover:shadow-md transition-shadow"
              >
                <div className="flex items-center gap-3 mb-3">
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
                    op.type === 'BANK' ? 'bg-blue-100 text-blue-600' :
                    op.type === 'MOBILE_MONEY' ? 'bg-yellow-100 text-yellow-600' :
                    'bg-green-100 text-green-600'
                  }`}>
                    <FiGrid size={20} />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-800">{op.name || op.type}</h3>
                    <p className="text-xs text-gray-500">{op.type}</p>
                  </div>
                </div>
                <div className="space-y-1 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Taux de base:</span>
                    <span className="font-medium">{((op.baseRate || 0) * 100).toFixed(2)}%</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      {/* Fee Calculator */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card title="Calculer les Frais" icon={FiDollarSign}>
          <div className="space-y-4">
            <Select
              label="Operateur"
              options={operatorTypes}
              value={feeForm.operator}
              onChange={(e) => setFeeForm({ ...feeForm, operator: e.target.value })}
            />
            <Input
              label="Montant (FCFA)"
              type="number"
              value={feeForm.amount}
              onChange={(e) => setFeeForm({ ...feeForm, amount: e.target.value })}
              placeholder="100000"
            />
            <Select
              label="Type de Transaction"
              options={[
                { value: 'TRANSFER', label: 'Transfert' },
                { value: 'WITHDRAWAL', label: 'Retrait' },
                { value: 'DEPOSIT', label: 'Depot' },
              ]}
              value={feeForm.transactionType}
              onChange={(e) => setFeeForm({ ...feeForm, transactionType: e.target.value })}
            />
            <Button
              onClick={calculateFee}
              loading={calculating}
              className="w-full"
              icon={FiDollarSign}
            >
              Calculer les Frais
            </Button>
          </div>
        </Card>

        <Card title="Comparer les Frais" icon={FiPercent}>
          <div className="space-y-4">
            <Input
              label="Montant (FCFA)"
              type="number"
              value={compareForm.amount}
              onChange={(e) => setCompareForm({ ...compareForm, amount: e.target.value })}
              placeholder="100000"
            />
            <Select
              label="Type de Transaction"
              options={[
                { value: 'TRANSFER', label: 'Transfert' },
                { value: 'WITHDRAWAL', label: 'Retrait' },
                { value: 'DEPOSIT', label: 'Depot' },
              ]}
              value={compareForm.transactionType}
              onChange={(e) => setCompareForm({ ...compareForm, transactionType: e.target.value })}
            />
            <Button
              onClick={compareFees}
              loading={calculating}
              className="w-full"
              variant="secondary"
              icon={FiRefreshCw}
            >
              Comparer les Operateurs
            </Button>
          </div>
        </Card>
      </div>

      {/* Fee Comparison Results */}
      {feeComparison && (
        <Card title="Resultat de la Comparaison" icon={FiPercent}>
          <Table columns={comparisonColumns} data={feeComparison} />
        </Card>
      )}
    </div>
  )
}

export default OperatorsPage
