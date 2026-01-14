import { useState, useEffect } from 'react'
import {
  FiBell,
  FiMail,
  FiSmartphone,
  FiMonitor,
  FiSend,
  FiUsers,
  FiPlay,
} from 'react-icons/fi'
import { Card, Button, Input, Select, Alert, Table, StatCard } from '../components/common'
import { notificationsApi } from '../api/client'
import toast from 'react-hot-toast'

function NotificationsPage() {
  const [stats, setStats] = useState(null)
  const [templates, setTemplates] = useState([])
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [selectedTab, setSelectedTab] = useState('single')

  const [smsForm, setSmsForm] = useState({
    phoneNumber: '+237690123456',
    message: 'Test notification SMS',
  })

  const [emailForm, setEmailForm] = useState({
    email: 'test@example.com',
    subject: 'Test Notification',
    body: 'Ceci est un test de notification email.',
  })

  const [pushForm, setPushForm] = useState({
    deviceToken: 'device-token-123',
    title: 'Notification',
    body: 'Test push notification',
  })

  const [multiForm, setMultiForm] = useState({
    name: 'Client Test',
    phone: '+237690123456',
    email: 'test@example.com',
    deviceToken: 'device-123',
    message: 'Notification multi-canal',
  })

  const [broadcastForm, setBroadcastForm] = useState({
    name: 'Broadcast Test',
    recipients: '+237690123456,+237677889900',
    message: 'Message broadcast',
    type: 'sms',
  })

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [statsRes, templatesRes, historyRes] = await Promise.all([
        notificationsApi.getStats().catch(() => ({ data: null })),
        notificationsApi.getTemplates().catch(() => ({ data: [] })),
        notificationsApi.getHistory(10).catch(() => ({ data: [] })),
      ])

      setStats(statsRes.data)
      setTemplates(templatesRes.data || [])
      setHistory(historyRes.data || [])
    } catch (error) {
      console.error('Error loading notifications data:', error)
    } finally {
      setLoading(false)
    }
  }

  const sendSms = async () => {
    setSending(true)
    try {
      await notificationsApi.sendSms(smsForm)
      toast.success('SMS envoye!')
      loadData()
    } catch (error) {
      toast.error('Erreur d\'envoi')
    } finally {
      setSending(false)
    }
  }

  const sendEmail = async () => {
    setSending(true)
    try {
      await notificationsApi.sendEmail(emailForm)
      toast.success('Email envoye!')
      loadData()
    } catch (error) {
      toast.error('Erreur d\'envoi')
    } finally {
      setSending(false)
    }
  }

  const sendPush = async () => {
    setSending(true)
    try {
      await notificationsApi.sendPush(pushForm)
      toast.success('Push envoye!')
      loadData()
    } catch (error) {
      toast.error('Erreur d\'envoi')
    } finally {
      setSending(false)
    }
  }

  const sendMultiChannel = async () => {
    setSending(true)
    try {
      await notificationsApi.sendMultiChannel(multiForm)
      toast.success('Notifications multi-canal envoyees!')
      loadData()
    } catch (error) {
      toast.error('Erreur d\'envoi')
    } finally {
      setSending(false)
    }
  }

  const sendBroadcast = async () => {
    setSending(true)
    try {
      const recipients = broadcastForm.recipients.split(',').map((r) => r.trim())
      if (broadcastForm.type === 'sms') {
        await notificationsApi.broadcastSms({
          name: broadcastForm.name,
          recipients,
          message: broadcastForm.message,
        })
      } else {
        await notificationsApi.broadcastEmail({
          name: broadcastForm.name,
          recipients,
          subject: 'Broadcast',
          body: broadcastForm.message,
        })
      }
      toast.success('Broadcast envoye!')
      loadData()
    } catch (error) {
      toast.error('Erreur de broadcast')
    } finally {
      setSending(false)
    }
  }

  const runDemo = async () => {
    setSending(true)
    try {
      const response = await notificationsApi.demo()
      toast.success(response.data.message || 'Demo executee!')
      loadData()
    } catch (error) {
      toast.error('Erreur de demo')
    } finally {
      setSending(false)
    }
  }

  const tabs = [
    { id: 'single', label: 'Notification Simple', icon: FiBell },
    { id: 'multi', label: 'Multi-Canal', icon: FiMonitor },
    { id: 'broadcast', label: 'Broadcast', icon: FiUsers },
  ]

  const historyColumns = [
    { header: 'Type', accessor: 'type' },
    { header: 'Destinataire', accessor: 'recipient' },
    {
      header: 'Succes',
      accessor: 'success',
      render: (val) => (
        <span className={val ? 'text-green-600' : 'text-red-600'}>
          {val ? 'Oui' : 'Non'}
        </span>
      ),
    },
    { header: 'Message', accessor: 'message' },
  ]

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Notifications</h1>
          {/* <p className="text-gray-500">
            Objectif 7: Pattern Composite - Notifications composees
          </p> */}
        </div>
        <Button onClick={runDemo} loading={sending} icon={FiPlay}>
          test Composite
        </Button>
      </div>

      {/* Info Alert */}
      <Alert variant="info">
        Le pattern Composite permet de traiter des notifications individuelles et des groupes
        de notifications de maniere uniforme. On peut combiner SMS, Email et Push.
      </Alert>

      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <StatCard
            title="Total Envoyes"
            value={stats.totalSent || 0}
            icon={FiSend}
            color="success"
          />
          <StatCard
            title="SMS"
            value={stats.sentByType?.SMS || 0}
            icon={FiSmartphone}
            color="primary"
          />
          <StatCard
            title="Emails"
            value={stats.sentByType?.EMAIL || 0}
            icon={FiMail}
            color="warning"
          />
          <StatCard
            title="Push"
            value={stats.sentByType?.PUSH || 0}
            icon={FiMonitor}
            color="info"
          />
        </div>
      )}

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

      {/* Single Notifications */}
      {selectedTab === 'single' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <Card title="SMS" icon={FiSmartphone}>
            <div className="space-y-4">
              <Input
                label="Telephone"
                value={smsForm.phoneNumber}
                onChange={(e) => setSmsForm({ ...smsForm, phoneNumber: e.target.value })}
              />
              <Input
                label="Message"
                value={smsForm.message}
                onChange={(e) => setSmsForm({ ...smsForm, message: e.target.value })}
              />
              <Button onClick={sendSms} loading={sending} className="w-full">
                Envoyer SMS
              </Button>
            </div>
          </Card>

          <Card title="Email" icon={FiMail}>
            <div className="space-y-4">
              <Input
                label="Email"
                type="email"
                value={emailForm.email}
                onChange={(e) => setEmailForm({ ...emailForm, email: e.target.value })}
              />
              <Input
                label="Sujet"
                value={emailForm.subject}
                onChange={(e) => setEmailForm({ ...emailForm, subject: e.target.value })}
              />
              <Input
                label="Corps"
                value={emailForm.body}
                onChange={(e) => setEmailForm({ ...emailForm, body: e.target.value })}
              />
              <Button onClick={sendEmail} loading={sending} className="w-full">
                Envoyer Email
              </Button>
            </div>
          </Card>

          <Card title="Push" icon={FiMonitor}>
            <div className="space-y-4">
              <Input
                label="Device Token"
                value={pushForm.deviceToken}
                onChange={(e) => setPushForm({ ...pushForm, deviceToken: e.target.value })}
              />
              <Input
                label="Titre"
                value={pushForm.title}
                onChange={(e) => setPushForm({ ...pushForm, title: e.target.value })}
              />
              <Input
                label="Corps"
                value={pushForm.body}
                onChange={(e) => setPushForm({ ...pushForm, body: e.target.value })}
              />
              <Button onClick={sendPush} loading={sending} className="w-full">
                Envoyer Push
              </Button>
            </div>
          </Card>
        </div>
      )}

      {/* Multi-Channel */}
      {selectedTab === 'multi' && (
        <Card title="Notification Multi-Canal (Composite)" icon={FiMonitor}>
          <p className="text-gray-600 mb-4">
            Envoyez une notification sur tous les canaux simultanement (SMS + Email + Push).
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="Nom"
              value={multiForm.name}
              onChange={(e) => setMultiForm({ ...multiForm, name: e.target.value })}
            />
            <Input
              label="Telephone"
              value={multiForm.phone}
              onChange={(e) => setMultiForm({ ...multiForm, phone: e.target.value })}
            />
            <Input
              label="Email"
              value={multiForm.email}
              onChange={(e) => setMultiForm({ ...multiForm, email: e.target.value })}
            />
            <Input
              label="Device Token"
              value={multiForm.deviceToken}
              onChange={(e) => setMultiForm({ ...multiForm, deviceToken: e.target.value })}
            />
          </div>
          <Input
            label="Message"
            value={multiForm.message}
            onChange={(e) => setMultiForm({ ...multiForm, message: e.target.value })}
            className="mt-4"
          />
          <Button onClick={sendMultiChannel} loading={sending} className="w-full mt-4" icon={FiSend}>
            Envoyer sur tous les canaux
          </Button>
        </Card>
      )}

      {/* Broadcast */}
      {selectedTab === 'broadcast' && (
        <Card title="Broadcast (Groupe)" icon={FiUsers}>
          <p className="text-gray-600 mb-4">
            Envoyez une notification a plusieurs destinataires en une seule operation.
          </p>
          <div className="space-y-4">
            <Input
              label="Nom du groupe"
              value={broadcastForm.name}
              onChange={(e) => setBroadcastForm({ ...broadcastForm, name: e.target.value })}
            />
            <Select
              label="Type"
              options={[
                { value: 'sms', label: 'SMS' },
                { value: 'email', label: 'Email' },
              ]}
              value={broadcastForm.type}
              onChange={(e) => setBroadcastForm({ ...broadcastForm, type: e.target.value })}
            />
            <Input
              label="Destinataires (separes par des virgules)"
              value={broadcastForm.recipients}
              onChange={(e) => setBroadcastForm({ ...broadcastForm, recipients: e.target.value })}
              placeholder="+237690123456,+237677889900"
            />
            <Input
              label="Message"
              value={broadcastForm.message}
              onChange={(e) => setBroadcastForm({ ...broadcastForm, message: e.target.value })}
            />
            <Button onClick={sendBroadcast} loading={sending} className="w-full" icon={FiUsers}>
              Envoyer le Broadcast
            </Button>
          </div>
        </Card>
      )}

      {/* History */}
      <Card title="Historique Recent" icon={FiBell} padding={false}>
        <Table
          columns={historyColumns}
          data={history}
          loading={loading}
          emptyMessage="Aucune notification recente"
        />
      </Card>
    </div>
  )
}

export default NotificationsPage
