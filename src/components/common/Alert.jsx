import { FiCheckCircle, FiAlertCircle, FiAlertTriangle, FiInfo, FiX } from 'react-icons/fi'

const variants = {
  success: {
    bg: 'bg-green-50 border-green-200',
    text: 'text-green-800',
    icon: FiCheckCircle,
    iconColor: 'text-green-500',
  },
  error: {
    bg: 'bg-red-50 border-red-200',
    text: 'text-red-800',
    icon: FiAlertCircle,
    iconColor: 'text-red-500',
  },
  warning: {
    bg: 'bg-yellow-50 border-yellow-200',
    text: 'text-yellow-800',
    icon: FiAlertTriangle,
    iconColor: 'text-yellow-500',
  },
  info: {
    bg: 'bg-blue-50 border-blue-200',
    text: 'text-blue-800',
    icon: FiInfo,
    iconColor: 'text-blue-500',
  },
}

function Alert({
  variant = 'info',
  title,
  children,
  onClose,
  className = '',
}) {
  const style = variants[variant]
  const Icon = style.icon

  return (
    <div
      className={`flex items-start gap-3 p-4 border rounded-lg ${style.bg} ${className}`}
    >
      <Icon className={`flex-shrink-0 mt-0.5 ${style.iconColor}`} size={20} />
      <div className="flex-1 min-w-0">
        {title && (
          <h4 className={`font-semibold ${style.text}`}>{title}</h4>
        )}
        <div className={`text-sm ${style.text} ${title ? 'mt-1' : ''}`}>
          {children}
        </div>
      </div>
      {onClose && (
        <button
          onClick={onClose}
          className={`flex-shrink-0 p-1 rounded-lg hover:bg-black/5 ${style.text}`}
        >
          <FiX size={18} />
        </button>
      )}
    </div>
  )
}

export default Alert
