import { Search } from 'lucide-react'
import { useLocation } from 'react-router-dom'

const sectionMeta = [
  { matcher: /^\/customers\/[^/]+$/, title: 'Customers', breadcrumb: 'Customer Details' },
  { matcher: /^\/customers$/, title: 'Customers', breadcrumb: 'Customers' },
  { matcher: /^\/orders\/[^/]+$/, title: 'Orders', breadcrumb: 'Order Details' },
  { matcher: /^\/orders$/, title: 'Orders', breadcrumb: 'Orders' },
  { matcher: /^\/meals\/[^/]+$/, title: 'Meals', breadcrumb: 'Meal Details' },
  { matcher: /^\/meals$/, title: 'Meals', breadcrumb: 'Meals' },
  { matcher: /^\/categories\/[^/]+$/, title: 'Categories', breadcrumb: 'Category Details' },
  { matcher: /^\/categories$/, title: 'Categories', breadcrumb: 'Categories' },
  { matcher: /^\/restaurants\/[^/]+$/, title: 'Restaurants', breadcrumb: 'Restaurant Details' },
  { matcher: /^\/restaurants$/, title: 'Restaurants', breadcrumb: 'Restaurants' },
  { matcher: /^\/$/, title: 'Dashboard', breadcrumb: 'Overview' },
]

function getSectionMeta(pathname: string) {
  return sectionMeta.find((item) => item.matcher.test(pathname)) ?? sectionMeta[sectionMeta.length - 1]
}

type TopbarProps = {
  searchValue: string
  onSearchChange: (value: string) => void
}

export function Topbar({ searchValue, onSearchChange }: TopbarProps) {
  const { pathname } = useLocation()
  const currentSection = getSectionMeta(pathname)

  return (
    <header className="topbar">
      <div className="topbar-copy">
        <h1>{currentSection.title}</h1>
        <div className="topbar-breadcrumb" aria-label="Breadcrumb">
          <span>Dashboard</span>
          <span>/</span>
          <span>{currentSection.breadcrumb}</span>
        </div>
      </div>

      <div className="topbar-actions">
        <label className="topbar-search" aria-label="Search workspace">
          <Search aria-hidden="true" size={16} />
          <input
            type="search"
            placeholder="Search anything"
            value={searchValue}
            onChange={(event) => onSearchChange(event.target.value)}
          />
        </label>
        <div className="user-profile" aria-label="Workspace profile">
          <div className="user-text">
            <span className="user-name">Restaurant Manager</span>
            <span className="user-role">Admin</span>
          </div>
          <div className="user-avatar">
            RM
          </div>
        </div>
      </div>
    </header>
  )
}
