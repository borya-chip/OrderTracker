import { useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'

import { Sidebar } from './Sidebar'
import { Topbar } from './Topbar'

export type AppLayoutContext = {
  topbarSearch: string
  setTopbarSearch: (value: string) => void
}

export function AppLayout() {
  const { pathname } = useLocation()
  const [searchByPath, setSearchByPath] = useState<Record<string, string>>({})
  const topbarSearch = searchByPath[pathname] ?? ''

  return (
    <div className="app-shell">
      <Sidebar />
      <div className="app-main">
        <div className="app-content">
          <Topbar
            searchValue={topbarSearch}
            onSearchChange={(value) => {
              setSearchByPath((current) => ({ ...current, [pathname]: value }))
            }}
          />
          <main className="page-surface">
            <Outlet
              context={{
                topbarSearch,
                setTopbarSearch: (value: string) => {
                  setSearchByPath((current) => ({ ...current, [pathname]: value }))
                },
              } satisfies AppLayoutContext}
            />
          </main>
        </div>
      </div>
    </div>
  )
}
