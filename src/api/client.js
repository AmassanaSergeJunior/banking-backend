import axios from 'axios'

const API_BASE_URL = '/api'

// Create axios instance with default config
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
})

// Request interceptor - add auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor - handle errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// ==================== AUTH API (Objectif 1 - Strategy) ====================
export const authApi = {
  login: (data) => apiClient.post('/auth/login', data),
  generateOtp: (userId) => apiClient.post(`/auth/otp/generate?userId=${userId}`),
  getMethods: () => apiClient.get('/auth/methods'),
  getPreferred: (userId) => apiClient.get(`/auth/preferred/${userId}`),
  setPreferred: (userId, method) => apiClient.put(`/auth/preferred/${userId}?method=${method}`),
}

// ==================== OPERATORS API (Objectif 2 - Factory) ====================
export const operatorsApi = {
  getAll: () => apiClient.get('/operators'),
  getOne: (type) => apiClient.get(`/operators/${type}`),
  validateAccount: (type, accountNumber, clientId, initialDeposit) =>
    apiClient.post(`/operators/${type}/validate-account?accountNumber=${accountNumber}&clientId=${clientId}&initialDeposit=${initialDeposit}`),
  calculateFee: (type, amount, transactionType) =>
    apiClient.post(`/operators/${type}/calculate-fee?amount=${amount}&transactionType=${transactionType}`),
  sendNotification: (type, phoneNumber, transactionType, amount, newBalance) =>
    apiClient.post(`/operators/${type}/send-notification?phoneNumber=${phoneNumber}&transactionType=${transactionType}&amount=${amount}&newBalance=${newBalance}`),
  transfer: (type, destinationAccount, amount, reference) =>
    apiClient.post(`/operators/${type}/transfer?destinationAccount=${destinationAccount}&amount=${amount}&reference=${reference}`),
  compareFees: (amount, transactionType) =>
    apiClient.get(`/operators/compare-fees?amount=${amount}&transactionType=${transactionType}`),
}

// ==================== TRANSACTIONS API (Objectif 3 - Builder) ====================
export const transactionsApi = {
  quick: (sourceAccount, destinationAccount, amount) =>
    apiClient.post(`/transactions/quick?sourceAccount=${sourceAccount}&destinationAccount=${destinationAccount}&amount=${amount}`),
  full: (sourceAccount, destinationAccount, amount) =>
    apiClient.post(`/transactions/full?sourceAccount=${sourceAccount}&destinationAccount=${destinationAccount}&amount=${amount}`),
  interOperator: (sourceAccount, sourceOperator, destinationAccount, destinationOperator, amount) =>
    apiClient.post(`/transactions/inter-operator?sourceAccount=${sourceAccount}&sourceOperator=${sourceOperator}&destinationAccount=${destinationAccount}&destinationOperator=${destinationOperator}&amount=${amount}`),
  international: (sourceAccount, destinationAccount, amount, sourceCurrency, targetCurrency, exchangeRate) =>
    apiClient.post(`/transactions/international?sourceAccount=${sourceAccount}&destinationAccount=${destinationAccount}&amount=${amount}&sourceCurrency=${sourceCurrency}&targetCurrency=${targetCurrency}&exchangeRate=${exchangeRate}`),
  custom: (params) => {
    const query = new URLSearchParams(params).toString()
    return apiClient.post(`/transactions/custom?${query}`)
  },
  getAll: () => apiClient.get('/transactions'),
  getOne: (reference) => apiClient.get(`/transactions/${reference}`),
  compareVariants: (amount) => apiClient.get(`/transactions/compare-variants?amount=${amount}`),
}

// ==================== SINGLETONS API (Objectif 4 - Singleton) ====================
export const singletonsApi = {
  getStats: () => apiClient.get('/singletons/stats'),
  health: () => apiClient.get('/singletons/health'),
  sendSms: (phoneNumber, message) =>
    apiClient.post(`/singletons/notifications/sms?phoneNumber=${phoneNumber}&message=${message}`),
  getNotificationStats: () => apiClient.get('/singletons/notifications/stats'),
  getNotificationHistory: () => apiClient.get('/singletons/notifications/history'),
  getAuthConfig: () => apiClient.get('/singletons/auth/config'),
  updateAuthConfig: (maxFailedAttempts, sessionTimeoutMinutes, multiFactorEnabled) =>
    apiClient.put(`/singletons/auth/config?maxFailedAttempts=${maxFailedAttempts}&sessionTimeoutMinutes=${sessionTimeoutMinutes}&multiFactorEnabled=${multiFactorEnabled}`),
  publishEvent: (type, source, message) =>
    apiClient.post(`/singletons/events/publish?type=${type}&source=${source}&message=${message}`),
  getEventsHistory: (count = 10) => apiClient.get(`/singletons/events/history?count=${count}`),
  getEventsStats: () => apiClient.get('/singletons/events/stats'),
  demoUniqueness: () => apiClient.get('/singletons/demo/uniqueness'),
}

// ==================== SMS API (Objectif 5 - Adapter) ====================
export const smsApi = {
  send: (phoneNumber, message) =>
    apiClient.post(`/sms/send?phoneNumber=${phoneNumber}&message=${message}`),
  sendWithProvider: (provider, phoneNumber, message) =>
    apiClient.post(`/sms/send/${provider}?phoneNumber=${phoneNumber}&message=${message}`),
  sendWithFallback: (phoneNumber, message) =>
    apiClient.post(`/sms/send-fallback?phoneNumber=${phoneNumber}&message=${message}`),
  bulk: (data) => apiClient.post('/sms/bulk', data),
  getStatus: (provider, messageId) => apiClient.get(`/sms/status/${provider}/${messageId}`),
  getBalances: () => apiClient.get('/sms/balances'),
  getAvailability: () => apiClient.get('/sms/availability'),
  getStatistics: () => apiClient.get('/sms/statistics'),
  getSummary: () => apiClient.get('/sms/summary'),
  getProviders: () => apiClient.get('/sms/providers'),
  detectProvider: (phoneNumber) => apiClient.get(`/sms/detect-provider?phoneNumber=${phoneNumber}`),
}

// ==================== TEMPLATE TRANSACTIONS API (Objectif 6 - Template Method) ====================
export const templateApi = {
  deposit: (operatorType, account, amount, description) =>
    apiClient.post(`/transactions/${operatorType}/deposit?account=${account}&amount=${amount}&description=${description}`),
  withdraw: (operatorType, account, amount, description) =>
    apiClient.post(`/transactions/${operatorType}/withdraw?account=${account}&amount=${amount}&description=${description}`),
  transfer: (operatorType, sourceAccount, destAccount, amount, description) =>
    apiClient.post(`/transactions/${operatorType}/transfer?sourceAccount=${sourceAccount}&destAccount=${destAccount}&amount=${amount}&description=${description}`),
  process: (operatorType, data) => apiClient.post(`/transactions/${operatorType}/process`, data),
  getAllStats: () => apiClient.get('/transactions/stats'),
  getStats: (operatorType) => apiClient.get(`/transactions/${operatorType}/stats`),
  getHistory: (operatorType) => apiClient.get(`/transactions/${operatorType}/history`),
  getOperators: () => apiClient.get('/transactions/operators'),
}

// ==================== NOTIFICATIONS API (Objectif 7 - Composite) ====================
export const notificationsApi = {
  sendSms: (data) => apiClient.post('/notifications/sms', data),
  sendEmail: (data) => apiClient.post('/notifications/email', data),
  sendPush: (data) => apiClient.post('/notifications/push', data),
  sendMultiChannel: (data) => apiClient.post('/notifications/multi-channel', data),
  broadcastSms: (data) => apiClient.post('/notifications/broadcast/sms', data),
  broadcastEmail: (data) => apiClient.post('/notifications/broadcast/email', data),
  sendTemplate: (templateId, data) => apiClient.post(`/notifications/template/${templateId}`, data),
  getTemplates: () => apiClient.get('/notifications/templates'),
  getStats: () => apiClient.get('/notifications/stats'),
  getHistory: (limit = 20) => apiClient.get(`/notifications/history?limit=${limit}`),
  reset: () => apiClient.post('/notifications/reset'),
  demo: () => apiClient.get('/notifications/demo'),
}

// ==================== ACCOUNTS API (Objectif 8 - Decorator) ====================
export const accountsApi = {
  create: (data) => apiClient.post('/accounts', data),
  createWithFeatures: (data) => apiClient.post('/accounts/with-features', data),
  deposit: (accountNumber, data) => apiClient.post(`/accounts/${accountNumber}/deposit`, data),
  withdraw: (accountNumber, data) => apiClient.post(`/accounts/${accountNumber}/withdraw`, data),
  transfer: (accountNumber, data) => apiClient.post(`/accounts/${accountNumber}/transfer`, data),
  getOne: (accountNumber) => apiClient.get(`/accounts/${accountNumber}`),
  getAll: () => apiClient.get('/accounts'),
  demo: () => apiClient.get('/accounts/demo'),
}

// ==================== ANALYTICS API (Objectif 9 - Visitor) ====================
export const analyticsApi = {
  getStatistics: () => apiClient.get('/analytics/statistics'),
  getTax: () => apiClient.get('/analytics/tax'),
  getAudit: () => apiClient.get('/analytics/audit'),
  demo: () => apiClient.get('/analytics/demo'),
}

// ==================== EVENTS API (Objectif 10 - Observer) ====================
export const eventsApi = {
  getObservers: () => apiClient.get('/events/observers'),
  getHistory: (limit = 20) => apiClient.get(`/events/history?limit=${limit}`),
  getStatistics: () => apiClient.get('/events/statistics'),
  getAlerts: () => apiClient.get('/events/alerts'),
  demo: () => apiClient.get('/events/demo'),
}

export default apiClient
