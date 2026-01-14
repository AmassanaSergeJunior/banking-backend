import { createContext, useContext, useState, useCallback } from 'react'
import toast from 'react-hot-toast'

const NotificationContext = createContext(null)

export function NotificationProvider({ children }) {
  const [notifications, setNotifications] = useState([])

  const addNotification = useCallback((notification) => {
    const id = Date.now()
    const newNotification = {
      id,
      timestamp: new Date(),
      ...notification,
    }

    setNotifications((prev) => [newNotification, ...prev].slice(0, 50))

    // Also show toast
    switch (notification.type) {
      case 'success':
        toast.success(notification.message)
        break
      case 'error':
        toast.error(notification.message)
        break
      case 'warning':
        toast(notification.message, { icon: '!' })
        break
      default:
        toast(notification.message)
    }

    return id
  }, [])

  const removeNotification = useCallback((id) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id))
  }, [])

  const clearAll = useCallback(() => {
    setNotifications([])
  }, [])

  const success = useCallback((message, title = '') => {
    return addNotification({ type: 'success', message, title })
  }, [addNotification])

  const error = useCallback((message, title = '') => {
    return addNotification({ type: 'error', message, title })
  }, [addNotification])

  const warning = useCallback((message, title = '') => {
    return addNotification({ type: 'warning', message, title })
  }, [addNotification])

  const info = useCallback((message, title = '') => {
    return addNotification({ type: 'info', message, title })
  }, [addNotification])

  const value = {
    notifications,
    addNotification,
    removeNotification,
    clearAll,
    success,
    error,
    warning,
    info,
  }

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  )
}

export function useNotification() {
  const context = useContext(NotificationContext)
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider')
  }
  return context
}

export default NotificationContext
