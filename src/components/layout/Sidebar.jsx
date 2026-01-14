import { NavLink } from 'react-router-dom'
import {
  FiHome,
  FiKey,
  FiGrid,
  FiRepeat,
  FiDatabase,
  FiMessageSquare,
  FiBell,
  FiCreditCard,
  FiBarChart2,
  FiActivity,
} from 'react-icons/fi'

const menuItems = [
  {
    path: '/',
    icon: FiHome,
    label: 'Bienvenue',
    description: 'Vue d\'ensemble',
  },
  {
    //path: '/login',
    icon: FiKey,
    //icon: FiEdit,
    //label: 'Obj 1: Auth',
    label: 'Creer un compte',
    //pattern: 'Strategy',
    //description: 'Authentification multi-methodes',
    description: 'commencer pas la creation de compte',
    hideWhenAuth: true,
  },
  {
    path: '/operators',
    icon: FiGrid,
    label: 'Obj 2: Operateurs',
    pattern: 'Factory',
    description: 'Gestion des operateurs',
  },
  {
    path: '/transactions',
    icon: FiRepeat,
    //icon: FiGrid,
    label: 'Obj 3: Transactions',
    pattern: 'Builder',
    description: 'Creation de transactions',
  },
  {
    path: '/singletons',
    icon: FiDatabase,
    label: 'Obj 4: Singletons',
    pattern: 'Singleton',
    description: 'Services uniques',
  },
  {
    path: '/sms',
    icon: FiMessageSquare,
    label: 'Obj 5: SMS',
    pattern: 'Adapter',
    description: 'Envoi SMS multi-providers',
  },
  {
    path: '/notifications',
    icon: FiBell,
    label: 'Obj 7: Notifications',
    pattern: 'Composite',
    description: 'Notifications composees',
  },

  {
    path: '/accounts',
    icon: FiCreditCard,
    label: 'Obj 8: Comptes',
    pattern: 'Decorator',
    description: 'Comptes avec options',
  },
  {
    path: '/analytics',
    icon: FiBarChart2,
    label: 'Obj 9: Analytics',
    pattern: 'Visitor',
    description: 'Rapports et statistiques',
  },
  {
    path: '/events',
    icon: FiActivity,
    label: 'Obj 10: Events',
    pattern: 'Observer',
    description: 'Evenements temps reel',
  },
]

function Sidebar() {
  return (
    <aside className="fixed left-0 top-16 bottom-0 w-64 bg-white border-r border-gray-200 overflow-y-auto">
      <div className="p-4">
        <h3 className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-4">
          Menu 
        </h3>

        <nav className="space-y-1">
          {menuItems.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `sidebar-link ${isActive ? 'active' : ''}`
              }
            >
              <item.icon size={20} />
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <span className="truncate">{item.label}</span>
                  {item.pattern && (
                    <span className="text-[10px] px-1.5 py-0.5 bg-primary-100 text-primary-700 rounded font-medium">
                      {item.pattern}
                    </span>
                  )}
                </div>
                <p className="text-xs text-gray-400 truncate">
                  {item.description}
                </p>
              </div>
            </NavLink>
          ))}
        </nav>
      </div>

      {/* Footer info */}
      <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-200 bg-gray-50">
        <div className="text-xs text-gray-500">
          {/* <p className="font-medium text-gray-700">TP INF461 - UY1</p> */}
          {/* <p>10 Design Patterns</p>
          <p>79 API Endpoints</p> */}
        </div>
      </div>
    </aside>
  )
}

export default Sidebar
