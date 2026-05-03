import {
  Beef,
  ClipboardList,
  GalleryVerticalEnd,
  LayoutDashboard,
  ListChecks,
  Store,
  Tags,
  Users,
} from 'lucide-react'
import { NavLink } from 'react-router-dom'

const navigationItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/customers', label: 'Customers', icon: Users },
  { to: '/orders', label: 'Orders', icon: ClipboardList },
  { to: '/meals', label: 'Meals', icon: Beef },
  { to: '/categories', label: 'Categories', icon: Tags },
  { to: '/restaurants', label: 'Restaurants', icon: Store },
]

export function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-mark">
          <GalleryVerticalEnd aria-hidden="true" size={20} />
        </div>
        <div>
          <strong>Order Tracker</strong>
        </div>
      </div>

      <nav className="sidebar-nav" aria-label="Main navigation">
        {navigationItems.map((item) => {
          const Icon = item.icon

          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            >
              <Icon aria-hidden="true" size={18} />
              <span>{item.label}</span>
            </NavLink>
          )
        })}
      </nav>

      <div className="sidebar-card">
        <div className="sidebar-card-icon">
          <ListChecks aria-hidden="true" size={18} />
        </div>
        <strong>Manager workspace</strong>
        <span>Manage orders, meals, customers, categories, and restaurants from one place.</span>
      </div>
    </aside>
  )
}
