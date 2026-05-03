import { Beef, ClipboardList, Plus, Store, Tags, Users } from 'lucide-react'
import { useMemo } from 'react'
import { Link } from 'react-router-dom'

import { useCategories } from '../hooks/useCategories'
import { useCustomers } from '../hooks/useCustomers'
import { useMeals } from '../hooks/useMeals'
import { useOrders } from '../hooks/useOrders'
import { useRestaurants } from '../hooks/useRestaurants'
import { formatCurrency, formatDateTime } from '../shared/lib/format'

const quickActions = [
  { label: 'Create Order', to: '/orders?create=1', icon: ClipboardList, variant: 'primary' },
  { label: 'Add Customer', to: '/customers?create=1', icon: Users, variant: 'secondary' },
  { label: 'Add Meal', to: '/meals?create=1', icon: Beef, variant: 'secondary' },
]
const DASHBOARD_MEALS_PREVIEW_SIZE = 50
const ORDERS_TIMELINE_MONTHS = 6
const ORDERS_DAILY_POINTS = 7

type TimelinePoint = {
  label: string
  shortLabel: string
  count: number
}

type ChartPoint = TimelinePoint & {
  x: number
  y: number
}

function getErrorMessage(error: Error | null): string {
  return error?.message ?? 'Could not load dashboard data'
}

function formatDayKey(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function buildMonthlyTimeline(values: string[], months: number): TimelinePoint[] {
  const formatter = new Intl.DateTimeFormat('en', { month: 'short' })
  const now = new Date()
  const counts = new Map<string, number>()

  values.forEach((value) => {
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) {
      return
    }

    const key = `${date.getFullYear()}-${date.getMonth()}`
    counts.set(key, (counts.get(key) ?? 0) + 1)
  })

  return Array.from({ length: months }, (_, index) => {
    const date = new Date(now.getFullYear(), now.getMonth() - (months - index - 1), 1)
    const key = `${date.getFullYear()}-${date.getMonth()}`
    return {
      label: formatter.format(date),
      shortLabel: formatter.format(date),
      count: counts.get(key) ?? 0,
    }
  })
}

function buildDailyTimeline(values: string[], days: number): TimelinePoint[] {
  const formatter = new Intl.DateTimeFormat('en', { weekday: 'short' })
  const now = new Date()
  const counts = new Map<string, number>()

  values.forEach((value) => {
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) {
      return
    }

    counts.set(formatDayKey(date), (counts.get(formatDayKey(date)) ?? 0) + 1)
  })

  return Array.from({ length: days }, (_, index) => {
    const date = new Date(now)
    date.setHours(0, 0, 0, 0)
    date.setDate(now.getDate() - (days - index - 1))
    const key = formatDayKey(date)

    return {
      label: formatter.format(date),
      shortLabel: formatter.format(date),
      count: counts.get(key) ?? 0,
    }
  })
}

function buildLinePath(points: ChartPoint[]): string {
  return points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ')
}

export function DashboardPage() {
  const ordersRequest = useMemo(() => ({ withEntityGraph: true }), [])
  const mealsRequest = useMemo(
    () => ({ page: 0, size: DASHBOARD_MEALS_PREVIEW_SIZE, sortBy: 'name' as const, ascending: true }),
    [],
  )
  const { customersQuery } = useCustomers()
  const { ordersQuery } = useOrders(ordersRequest)
  const { mealsQuery } = useMeals(mealsRequest)
  const { categoriesQuery } = useCategories()
  const { restaurantsQuery } = useRestaurants()

  const mealsPage = mealsQuery.data
  const customers = useMemo(() => customersQuery.data ?? [], [customersQuery.data])
  const orders = useMemo(() => ordersQuery.data ?? [], [ordersQuery.data])
  const meals = useMemo(() => mealsPage?.content ?? [], [mealsPage?.content])
  const categories = useMemo(() => categoriesQuery.data ?? [], [categoriesQuery.data])
  const restaurants = useMemo(() => restaurantsQuery.data ?? [], [restaurantsQuery.data])

  const isLoading =
    customersQuery.isLoading ||
    ordersQuery.isLoading ||
    mealsQuery.isLoading ||
    categoriesQuery.isLoading ||
    restaurantsQuery.isLoading
  const error =
    customersQuery.error ??
    ordersQuery.error ??
    mealsQuery.error ??
    categoriesQuery.error ??
    restaurantsQuery.error

  const stats = [
    {
      label: 'Total Orders',
      value: orders.length,
      hint: 'Placed orders',
      icon: ClipboardList,
    },
    {
      label: 'Total Customers',
      value: customers.length,
      hint: 'Registered customers',
      icon: Users,
    },
    {
      label: 'Total Meals',
      value: mealsPage?.totalElements ?? meals.length,
      hint: 'Menu items',
      icon: Beef,
    },
    {
      label: 'Total Restaurants',
      value: restaurants.length,
      hint: 'Restaurant records',
      icon: Store,
    },
  ]

  const categoryMealCounts = useMemo(() => {
    const counts = new Map<number, number>()

    meals.forEach((meal) => {
      counts.set(meal.categoryId, (counts.get(meal.categoryId) ?? 0) + 1)
    })

    return counts
  }, [meals])

  const recentOrders = useMemo(
    () =>
      [...orders]
        .sort((left, right) => new Date(right.date).getTime() - new Date(left.date).getTime())
        .slice(0, 5),
    [orders],
  )

  const mealsByCategory = useMemo(() => {
    const totalMeals = meals.length

    return categories
      .map((category) => {
        const count = categoryMealCounts.get(category.id) ?? 0
        const width = totalMeals === 0 ? 0 : Math.round((count / totalMeals) * 100)

        return {
          id: category.id,
          name: category.name,
          count,
          width,
        }
      })
      .sort((left, right) => right.count - left.count)
      .slice(0, 5)
  }, [categories, categoryMealCounts, meals.length])

  const orderDates = useMemo(() => orders.map((order) => order.date), [orders])

  const monthlyOrders = useMemo(
    () => buildMonthlyTimeline(orderDates, ORDERS_TIMELINE_MONTHS),
    [orderDates],
  )

  const dailyOrders = useMemo(
    () => buildDailyTimeline(orderDates, ORDERS_DAILY_POINTS),
    [orderDates],
  )

  const monthlyMax = Math.max(...monthlyOrders.map((point) => point.count), 1)
  const dailyMax = Math.max(...dailyOrders.map((point) => point.count), 1)

  const ordersLinePoints = useMemo<ChartPoint[]>(() => {
    const width = 560
    const height = 180
    const step = monthlyOrders.length > 1 ? width / (monthlyOrders.length - 1) : width

    return monthlyOrders.map((point, index) => ({
      ...point,
      x: index * step,
      y: height - (point.count / monthlyMax) * 140 - 16,
    }))
  }, [monthlyMax, monthlyOrders])

  const ordersLinePath = useMemo(() => buildLinePath(ordersLinePoints), [ordersLinePoints])
  const totalOrderAmount = useMemo(
    () => orders.reduce((sum, order) => sum + Number(order.amount), 0),
    [orders],
  )
  const averageOrderAmount = orders.length === 0 ? 0 : totalOrderAmount / orders.length
  const topCategory = mealsByCategory[0]

  if (isLoading) {
    return (
      <div className="dashboard-grid">
        <section className="dashboard-main">
          <div className="state-panel">
            <div className="spinner" />
            <strong>Loading dashboard</strong>
            <span>Preparing totals, recent orders, and category overview.</span>
          </div>
        </section>
      </div>
    )
  }

  if (error) {
    return (
      <div className="dashboard-grid">
        <section className="dashboard-main">
          <div className="state-panel error-state">
            <strong>Could not load dashboard</strong>
            <span>{getErrorMessage(error)}</span>
          </div>
        </section>
      </div>
    )
  }

  return (
    <div className="dashboard-grid">
      <section className="dashboard-main">
        <div className="stats-grid">
          {stats.map((item) => {
            const Icon = item.icon

            return (
              <article className="stat-card" key={item.label}>
                <div className="stat-icon">
                  <Icon aria-hidden="true" size={22} />
                </div>
                <div>
                  <span>{item.label}</span>
                  <strong>{item.value}</strong>
                </div>
                <small>{item.hint}</small>
              </article>
            )
          })}
        </div>

        <div className="analytics-grid">
          <article className="dashboard-card analytics-card orders-overview-card">
            <div className="card-heading">
              <div>
                <span>Order volume</span>
                <strong>{orders.length}</strong>
              </div>
              <span className="status-badge processing">Last {ORDERS_TIMELINE_MONTHS} months</span>
            </div>

            {orders.length === 0 ? (
              <div className="state-panel dashboard-mini-state">
                <ClipboardList aria-hidden="true" size={24} />
                <strong>No order trend yet</strong>
                <span>Create orders to populate the timeline.</span>
              </div>
            ) : (
              <>
                <div className="chart-metrics">
                  <div>
                    <span>Average order</span>
                    <strong>{formatCurrency(averageOrderAmount)}</strong>
                  </div>
                  <div>
                    <span>Peak month</span>
                    <strong>{Math.max(...monthlyOrders.map((point) => point.count))} orders</strong>
                  </div>
                </div>
                <div className="line-chart dashboard-line-chart" aria-hidden="true">
                  <svg viewBox="0 0 560 180" preserveAspectRatio="none">
                    <path className="chart-line income" d={ordersLinePath} />
                    {ordersLinePoints.map((point) => (
                      <circle key={point.label} className="chart-dot" cx={point.x} cy={point.y} r="5" />
                    ))}
                  </svg>
                </div>
                <div className="chart-axis">
                  {monthlyOrders.map((point) => (
                    <span key={point.label}>{point.shortLabel}</span>
                  ))}
                </div>
              </>
            )}
          </article>

          <article className="dashboard-card activity-card">
            <div className="card-heading compact">
              <div>
                <strong>Orders by Day</strong>
                <span>Real orders from the last seven days.</span>
              </div>
              <ClipboardList aria-hidden="true" size={18} />
            </div>

            {orders.length === 0 ? (
              <div className="state-panel dashboard-mini-state">
                <ClipboardList aria-hidden="true" size={24} />
                <strong>No daily activity yet</strong>
                <span>Order activity appears here as new orders are added.</span>
              </div>
            ) : (
              <>
                <div className="bar-chart dashboard-bar-chart" aria-hidden="true">
                  {dailyOrders.map((point) => {
                    const height = Math.max(18, Math.round((point.count / dailyMax) * 124))

                    return (
                      <div className="bar-item" key={point.label}>
                        <small>{point.count}</small>
                        <div className={point.count === dailyMax ? 'bar active' : 'bar'} style={{ height }} />
                        <span>{point.shortLabel}</span>
                      </div>
                    )
                  })}
                </div>
                <div className="activity-summary">
                  <div>
                    <span>Today</span>
                    <strong>{dailyOrders[dailyOrders.length - 1]?.count ?? 0} orders</strong>
                  </div>
                  <div>
                    <span>Seven-day total</span>
                    <strong>{dailyOrders.reduce((sum, point) => sum + point.count, 0)} orders</strong>
                  </div>
                </div>
              </>
            )}
          </article>
        </div>

        <article className="dashboard-card recent-orders-card">
          <div className="card-heading compact">
            <div>
              <strong>Recent Orders</strong>
              <span>Recent customer activity from live order data.</span>
            </div>
            <Link className="secondary-action" to="/orders">
              Open Orders
            </Link>
          </div>

          {recentOrders.length === 0 ? (
            <div className="state-panel">
              <ClipboardList aria-hidden="true" size={28} />
              <strong>No orders yet</strong>
              <span>Create the first order to see it on the dashboard.</span>
            </div>
          ) : (
            <div className="orders-table-wrap">
              <table className="orders-table">
                <thead>
                  <tr>
                    <th>Customer</th>
                    <th>Meals</th>
                    <th>Amount</th>
                    <th>Date</th>
                    <th>Details</th>
                  </tr>
                </thead>
                <tbody>
                  {recentOrders.map((order) => (
                    <tr key={order.id}>
                      <td>
                        <strong>{order.customerName}</strong>
                        <span>{order.description}</span>
                      </td>
                      <td>
                        <strong>{order.mealNames.length} meals</strong>
                        <span>{order.mealNames.join(', ') || 'No meals'}</span>
                      </td>
                      <td className="amount-cell">{formatCurrency(Number(order.amount))}</td>
                      <td>{formatDateTime(order.date)}</td>
                      <td className="table-link-cell">
                        <Link className="status-badge processing" to={`/orders/${order.id}`}>
                          Details
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </article>
      </section>

      <aside className="dashboard-aside">
        <article className="dashboard-card order-types-card">
          <div className="card-heading compact">
            <div>
              <strong>Meals by Category</strong>
              <span>Current menu distribution.</span>
            </div>
            <Tags aria-hidden="true" size={18} />
          </div>

          {categories.length === 0 || meals.length === 0 ? (
            <div className="state-panel dashboard-mini-state">
              <Beef aria-hidden="true" size={24} />
              <strong>No menu distribution yet</strong>
              <span>Add categories and meals to build this view.</span>
            </div>
          ) : (
            <>
              <div className="chart-metrics compact">
                <div>
                  <span>Top category</span>
                  <strong>{topCategory?.name ?? '—'}</strong>
                </div>
                <div>
                  <span>Tracked meals</span>
                  <strong>{meals.length}</strong>
                </div>
              </div>
              <div className="order-type-list">
                {mealsByCategory.map((item) => (
                  <div className="order-type-row" key={item.id}>
                    <div>
                      <span>{item.name}</span>
                      <small>{item.count} meals</small>
                    </div>
                    <strong>{item.width}%</strong>
                    <div className="progress-track">
                      <div className="progress-fill" style={{ width: `${item.width}%` }} />
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </article>

        <article className="dashboard-card order-types-card">
          <div className="card-heading compact">
            <div>
              <strong>Quick Actions</strong>
              <span>Shortcuts to the most common manager tasks.</span>
            </div>
            <Plus aria-hidden="true" size={18} />
          </div>
          <div className="order-type-list">
            {quickActions.map((action) => {
              const Icon = action.icon
              const className = action.variant === 'primary' ? 'primary-action' : 'secondary-action'

              return (
                <Link className={className} key={action.label} style={{ gap: '8px' }} to={action.to}>
                  <Icon aria-hidden="true" size={16} />
                  {action.label}
                </Link>
              )
            })}
          </div>
        </article>
      </aside>
    </div>
  )
}
