import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { FiUser, FiLogOut, FiBell, FiSettings } from 'react-icons/fi'

function Navbar() {
  const { user, logout } = useAuth()

  return (
    <nav className="fixed top-0 left-0 right-0 h-16 bg-white border-b border-gray-200 z-50">
      <div className="flex items-center justify-between h-full px-6">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-br from-primary-600 to-primary-800 rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-xl">B</span>
          </div>
          <div>
            <h1 className="text-lg font-bold text-gray-800">BanqueApp</h1>
            <p className="text-xs text-gray-500">TP INF461</p>
          </div>
        </Link>

        {/* Center - Title */}
        <div className="hidden md:block">
          <h2 className="text-sm text-gray-600">
            Systeme Bancaire Multi-Operateurs - Design Patterns
          </h2>
        </div>

        {/* Right - User menu */}
        <div className="flex items-center gap-4">
          {/* Notifications */}
          <button className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors">
            <FiBell size={20} />
          </button>

          {/* Settings */}
          <button className="p-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors">
            <FiSettings size={20} />
          </button>

          {/* User dropdown */}
          <div className="flex items-center gap-3 pl-4 border-l border-gray-200">
            <div className="text-right">
              <p className="text-sm font-medium text-gray-800">
                {user?.userId || 'Utilisateur'}
              </p>
              <p className="text-xs text-gray-500">
                {user?.authenticationType || 'Connecte'}
              </p>
            </div>
            <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
              <FiUser className="text-primary-600" size={20} />
            </div>
            <button
              onClick={logout}
              className="p-2 text-gray-600 hover:bg-red-50 hover:text-red-600 rounded-lg transition-colors"
              title="Deconnexion"
            >
              <FiLogOut size={20} />
            </button>
          </div>
        </div>
      </div>
    </nav>
  )
}

export default Navbar
