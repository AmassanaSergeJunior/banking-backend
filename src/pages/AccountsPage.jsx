import { useState, useEffect } from 'react'
import {
  FiCreditCard,
  FiPlus,
  FiDollarSign,
  FiArrowUp,
  FiArrowDown,
  FiSend,
  FiPlay,
} from 'react-icons/fi'
import { Card, Button, Input, Select, Alert, Table, Modal } from '../components/common'
import { accountsApi } from '../api/client'
import toast from 'react-hot-toast'

function AccountsPage() {
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [showOperationModal, setShowOperationModal] = useState(null)
  const [selectedAccount, setSelectedAccount] = useState(null)

  const [createForm, setCreateForm] = useState({
    holder: 'Jean Dupont',
    type: 'COURANT',
    initialBalance: '100000',
    withInterest: false,
    interestRate: '0.05',
    withOverdraft: false,
    overdraftLimit: '50000',
    withInsurance: false,
    insuranceType: 'VIE',
    withLoyalty: false,
    withFees: false,
    feeType: 'MONTHLY',
    withNotifications: false,
    phone: '+237690123456',
    email: 'jean@mail.com',
  })

  const [operationForm, setOperationForm] = useState({
    amount: '',
    toAccount: '',
  })

  useEffect(() => {
    loadAccounts()
  }, [])

  const loadAccounts = async () => {
    try {
      const response = await accountsApi.getAll()
      setAccounts(response.data || [])
    } catch (error) {
      console.error('Error loading accounts:', error)
    } finally {
      setLoading(false)
    }
  }

  const createAccount = async () => {
    setSubmitting(true)
    try {
      // Check if any decorator is selected
      const hasDecorators = createForm.withInterest || createForm.withOverdraft ||
        createForm.withInsurance || createForm.withLoyalty ||
        createForm.withFees || createForm.withNotifications

      let response
      if (hasDecorators) {
        response = await accountsApi.createWithFeatures({
          holder: createForm.holder,
          type: createForm.type,
          initialBalance: parseFloat(createForm.initialBalance),
          withInterest: createForm.withInterest,
          interestRate: createForm.withInterest ? parseFloat(createForm.interestRate) : null,
          withOverdraft: createForm.withOverdraft,
          overdraftLimit: createForm.withOverdraft ? parseFloat(createForm.overdraftLimit) : null,
          withInsurance: createForm.withInsurance,
          insuranceType: createForm.withInsurance ? createForm.insuranceType : null,
          withLoyalty: createForm.withLoyalty,
          withFees: createForm.withFees,
          feeType: createForm.withFees ? createForm.feeType : null,
          withNotifications: createForm.withNotifications,
          phone: createForm.withNotifications ? createForm.phone : null,
          email: createForm.withNotifications ? createForm.email : null,
        })
      } else {
        response = await accountsApi.create({
          holder: createForm.holder,
          type: createForm.type,
          initialBalance: parseFloat(createForm.initialBalance),
        })
      }

      toast.success(`Compte cree: ${response.data.accountNumber}`)
      setShowCreateModal(false)
      loadAccounts()
    } catch (error) {
      toast.error('Erreur de creation')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDeposit = async () => {
    setSubmitting(true)
    try {
      await accountsApi.deposit(selectedAccount.accountNumber, {
        amount: parseFloat(operationForm.amount),
      })
      toast.success('Depot effectue!')
      setShowOperationModal(null)
      loadAccounts()
    } catch (error) {
      toast.error('Erreur de depot')
    } finally {
      setSubmitting(false)
    }
  }

  const handleWithdraw = async () => {
    setSubmitting(true)
    try {
      await accountsApi.withdraw(selectedAccount.accountNumber, {
        amount: parseFloat(operationForm.amount),
      })
      toast.success('Retrait effectue!')
      setShowOperationModal(null)
      loadAccounts()
    } catch (error) {
      toast.error('Erreur de retrait')
    } finally {
      setSubmitting(false)
    }
  }

  const handleTransfer = async () => {
    setSubmitting(true)
    try {
      await accountsApi.transfer(selectedAccount.accountNumber, {
        toAccount: operationForm.toAccount,
        amount: parseFloat(operationForm.amount),
      })
      toast.success('Transfert effectue!')
      setShowOperationModal(null)
      loadAccounts()
    } catch (error) {
      toast.error('Erreur de transfert')
    } finally {
      setSubmitting(false)
    }
  }

  const runDemo = async () => {
    setSubmitting(true)
    try {
      const response = await accountsApi.demo()
      toast.success(response.data.message || 'Demo executee!')
      loadAccounts()
    } catch (error) {
      toast.error('Erreur de demo')
    } finally {
      setSubmitting(false)
    }
  }

  const accountColumns = [
    { header: 'Numero', accessor: 'accountNumber' },
    { header: 'Titulaire', accessor: 'holder' },
    { header: 'Type', accessor: 'type' },
    {
      header: 'Solde',
      accessor: 'balance',
      render: (val) => `${val?.toLocaleString()} FCFA`,
    },
    {
      header: 'Decorateurs',
      accessor: 'decoratorCount',
      render: (val) => (
        <span className="px-2 py-1 bg-primary-100 text-primary-700 rounded-full text-xs">
          {val || 0} options
        </span>
      ),
    },
    {
      header: 'Actions',
      accessor: 'actions',
      render: (_, row) => (
        <div className="flex gap-2">
          <button
            onClick={() => {
              setSelectedAccount(row)
              setOperationForm({ amount: '', toAccount: '' })
              setShowOperationModal('deposit')
            }}
            className="p-1 text-green-600 hover:bg-green-50 rounded"
            title="Depot"
          >
            <FiArrowDown size={16} />
          </button>
          <button
            onClick={() => {
              setSelectedAccount(row)
              setOperationForm({ amount: '', toAccount: '' })
              setShowOperationModal('withdraw')
            }}
            className="p-1 text-orange-600 hover:bg-orange-50 rounded"
            title="Retrait"
          >
            <FiArrowUp size={16} />
          </button>
          <button
            onClick={() => {
              setSelectedAccount(row)
              setOperationForm({ amount: '', toAccount: '' })
              setShowOperationModal('transfer')
            }}
            className="p-1 text-blue-600 hover:bg-blue-50 rounded"
            title="Transfert"
          >
            <FiSend size={16} />
          </button>
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Gestion des Comptes</h1>
          {/* <p className="text-gray-500">
            Objectif 8: Pattern Decorator - Comptes avec options dynamiques
          </p> */}
        </div>
        <div className="flex gap-3">
          <Button onClick={runDemo} variant="secondary" loading={submitting} icon={FiPlay}>
            test Decorator
          </Button>
          <Button onClick={() => setShowCreateModal(true)} icon={FiPlus}>
            Nouveau Compte
          </Button>
        </div>
      </div>

      {/* Info Alert */}
      <Alert variant="info">
        Le pattern Decorator permet d'ajouter dynamiquement des fonctionnalites a un compte:
        interets, decouvert, assurance, fidelite, frais, notifications. Chaque option est un decorateur.
      </Alert>

      {/* Accounts Table */}
      <Card title="Liste des Comptes" icon={FiCreditCard} padding={false}>
        <Table
          columns={accountColumns}
          data={accounts}
          loading={loading}
          emptyMessage="Aucun compte. Cliquez sur 'Nouveau Compte' pour commencer."
        />
      </Card>

      {/* Create Account Modal */}
      <Modal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        title="Creer un Nouveau Compte"
        size="lg"
      >
        <div className="space-y-6">
          {/* Basic Info */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Input
              label="Titulaire"
              value={createForm.holder}
              onChange={(e) => setCreateForm({ ...createForm, holder: e.target.value })}
            />
            <Select
              label="Type de Compte"
              options={[
                { value: 'COURANT', label: 'Compte Courant' },
                { value: 'EPARGNE', label: 'Compte Epargne' },
                { value: 'PROFESSIONNEL', label: 'Compte Professionnel' },
              ]}
              value={createForm.type}
              onChange={(e) => setCreateForm({ ...createForm, type: e.target.value })}
            />
            <Input
              label="Solde Initial (FCFA)"
              type="number"
              value={createForm.initialBalance}
              onChange={(e) => setCreateForm({ ...createForm, initialBalance: e.target.value })}
            />
          </div>

          {/* Decorators */}
          <div className="border-t pt-4">
            <h4 className="font-medium text-gray-700 mb-4">Options (Decorateurs)</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Interest */}
              <div className="p-4 border rounded-lg">
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={createForm.withInterest}
                    onChange={(e) => setCreateForm({ ...createForm, withInterest: e.target.checked })}
                    className="w-4 h-4 text-primary-600 rounded"
                  />
                  <span className="font-medium">Interets</span>
                </label>
                {createForm.withInterest && (
                  <Input
                    label="Taux (%)"
                    type="number"
                    step="0.01"
                    value={createForm.interestRate}
                    onChange={(e) => setCreateForm({ ...createForm, interestRate: e.target.value })}
                    className="mt-2"
                  />
                )}
              </div>

              {/* Overdraft */}
              <div className="p-4 border rounded-lg">
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={createForm.withOverdraft}
                    onChange={(e) => setCreateForm({ ...createForm, withOverdraft: e.target.checked })}
                    className="w-4 h-4 text-primary-600 rounded"
                  />
                  <span className="font-medium">Decouvert Autorise</span>
                </label>
                {createForm.withOverdraft && (
                  <Input
                    label="Limite (FCFA)"
                    type="number"
                    value={createForm.overdraftLimit}
                    onChange={(e) => setCreateForm({ ...createForm, overdraftLimit: e.target.value })}
                    className="mt-2"
                  />
                )}
              </div>

              {/* Insurance */}
              <div className="p-4 border rounded-lg">
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={createForm.withInsurance}
                    onChange={(e) => setCreateForm({ ...createForm, withInsurance: e.target.checked })}
                    className="w-4 h-4 text-primary-600 rounded"
                  />
                  <span className="font-medium">Assurance</span>
                </label>
                {createForm.withInsurance && (
                  <Select
                    label="Type"
                    options={[
                      { value: 'VIE', label: 'Assurance Vie' },
                      { value: 'DECES', label: 'Assurance Deces' },
                      { value: 'COMPLETE', label: 'Complete' },
                    ]}
                    value={createForm.insuranceType}
                    onChange={(e) => setCreateForm({ ...createForm, insuranceType: e.target.value })}
                    className="mt-2"
                  />
                )}
              </div>

              {/* Loyalty */}
              <div className="p-4 border rounded-lg">
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={createForm.withLoyalty}
                    onChange={(e) => setCreateForm({ ...createForm, withLoyalty: e.target.checked })}
                    className="w-4 h-4 text-primary-600 rounded"
                  />
                  <span className="font-medium">Programme Fidelite</span>
                </label>
                <p className="text-xs text-gray-500 mt-1">Gagnez des points sur chaque operation</p>
              </div>

              {/* Fees */}
              <div className="p-4 border rounded-lg">
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={createForm.withFees}
                    onChange={(e) => setCreateForm({ ...createForm, withFees: e.target.checked })}
                    className="w-4 h-4 text-primary-600 rounded"
                  />
                  <span className="font-medium">Frais de Gestion</span>
                </label>
                {createForm.withFees && (
                  <Select
                    label="Type"
                    options={[
                      { value: 'MONTHLY', label: 'Mensuel' },
                      { value: 'YEARLY', label: 'Annuel' },
                      { value: 'PER_TRANSACTION', label: 'Par Transaction' },
                    ]}
                    value={createForm.feeType}
                    onChange={(e) => setCreateForm({ ...createForm, feeType: e.target.value })}
                    className="mt-2"
                  />
                )}
              </div>

              {/* Notifications */}
              <div className="p-4 border rounded-lg">
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={createForm.withNotifications}
                    onChange={(e) => setCreateForm({ ...createForm, withNotifications: e.target.checked })}
                    className="w-4 h-4 text-primary-600 rounded"
                  />
                  <span className="font-medium">Notifications</span>
                </label>
                {createForm.withNotifications && (
                  <div className="mt-2 space-y-2">
                    <Input
                      label="Telephone"
                      value={createForm.phone}
                      onChange={(e) => setCreateForm({ ...createForm, phone: e.target.value })}
                    />
                    <Input
                      label="Email"
                      type="email"
                      value={createForm.email}
                      onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })}
                    />
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t">
            <Button variant="secondary" onClick={() => setShowCreateModal(false)}>
              Annuler
            </Button>
            <Button onClick={createAccount} loading={submitting}>
              Creer le Compte
            </Button>
          </div>
        </div>
      </Modal>

      {/* Operation Modal */}
      <Modal
        isOpen={!!showOperationModal}
        onClose={() => setShowOperationModal(null)}
        title={
          showOperationModal === 'deposit' ? 'Depot' :
          showOperationModal === 'withdraw' ? 'Retrait' : 'Transfert'
        }
      >
        {selectedAccount && (
          <div className="space-y-4">
            <div className="p-3 bg-gray-50 rounded-lg">
              <p className="text-sm text-gray-500">Compte</p>
              <p className="font-medium">{selectedAccount.accountNumber}</p>
              <p className="text-sm">Solde: {selectedAccount.balance?.toLocaleString()} FCFA</p>
            </div>

            <Input
              label="Montant (FCFA)"
              type="number"
              value={operationForm.amount}
              onChange={(e) => setOperationForm({ ...operationForm, amount: e.target.value })}
            />

            {showOperationModal === 'transfer' && (
              <Input
                label="Compte Destinataire"
                value={operationForm.toAccount}
                onChange={(e) => setOperationForm({ ...operationForm, toAccount: e.target.value })}
              />
            )}

            <div className="flex justify-end gap-3">
              <Button variant="secondary" onClick={() => setShowOperationModal(null)}>
                Annuler
              </Button>
              <Button
                onClick={
                  showOperationModal === 'deposit' ? handleDeposit :
                  showOperationModal === 'withdraw' ? handleWithdraw : handleTransfer
                }
                loading={submitting}
              >
                Confirmer
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}

export default AccountsPage
