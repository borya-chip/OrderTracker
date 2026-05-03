import { ArrowLeft, Beef, Clock, Store, Tags } from 'lucide-react'
import { useMemo } from 'react'
import { Link, useParams } from 'react-router-dom'

import { useMeal } from '../hooks/useMeals'
import { useOrders } from '../hooks/useOrders'
import { formatCurrency, formatDateTime } from '../shared/lib/format'

function parseRouteId(value: string | undefined): number | null {
  const parsedValue = Number(value)
  return Number.isInteger(parsedValue) && parsedValue > 0 ? parsedValue : null
}

export function MealDetailsPage() {
  const { mealId } = useParams()
  const parsedMealId = parseRouteId(mealId)
  const mealQuery = useMeal(parsedMealId)
  const ordersRequest = useMemo(() => ({ withEntityGraph: true }), [])
  const { ordersQuery } = useOrders(ordersRequest, { enabled: parsedMealId !== null })

  const mealOrders = useMemo(() => {
    const orders = ordersQuery.data ?? []
    return parsedMealId === null
      ? []
      : orders.filter((order) => order.mealIds.includes(parsedMealId))
  }, [ordersQuery.data, parsedMealId])

  if (parsedMealId === null) {
    return (
      <section className="resource-page">
        <div className="state-panel error-state">
          <strong>Meal was not found</strong>
          <span>The selected meal link is not valid.</span>
          <Link className="secondary-action" to="/meals">Back to Meals</Link>
        </div>
      </section>
    )
  }

  const isLoading = mealQuery.isLoading || ordersQuery.isLoading
  const error = mealQuery.error ?? ordersQuery.error
  const meal = mealQuery.data

  return (
    <section className="resource-page">
      <div className="resource-header">
        <div>
          <p className="eyebrow">Meal Details</p>
          <p>Category, restaurant, and orders where this meal appears.</p>
        </div>
        <Link className="secondary-action resource-action" to="/meals">
          <ArrowLeft aria-hidden="true" size={16} />
          Back
        </Link>
      </div>

      {isLoading && (
        <div className="state-panel">
          <div className="spinner" />
          <strong>Loading meal</strong>
          <span>Preparing meal and related orders.</span>
        </div>
      )}

      {error && (
        <div className="state-panel error-state">
          <strong>Could not load meal details</strong>
          <span>{error.message}</span>
        </div>
      )}

      {meal && !isLoading && !error && (
        <>
          <div className="detail-grid">
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Beef aria-hidden="true" size={24} /></div>
              <span>Meal</span>
              <strong>{meal.name}</strong>
              <small>{formatCurrency(Number(meal.price))}</small>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Tags aria-hidden="true" size={24} /></div>
              <span>Category</span>
              <strong>{meal.categoryName}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Store aria-hidden="true" size={24} /></div>
              <span>Restaurant</span>
              <strong>{meal.restaurantName}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Clock aria-hidden="true" size={24} /></div>
              <span>Cooking time</span>
              <strong>{meal.cookingTime} min</strong>
            </article>
          </div>

          <article className="dashboard-card resource-card">
            <div className="card-heading compact">
              <div>
                <strong>Orders With This Meal</strong>
                <span>A meal can appear in many orders.</span>
              </div>
              <span className="status-badge processing">{mealOrders.length} orders</span>
            </div>

            {mealOrders.length === 0 ? (
              <div className="state-panel">
                <Beef aria-hidden="true" size={28} />
                <strong>No orders include this meal</strong>
                <span>Create or edit an order to select this meal.</span>
              </div>
            ) : (
              <div className="orders-table-wrap resource-table-wrap">
                <table className="orders-table resource-table">
                  <thead>
                    <tr>
                      <th>Customer</th>
                      <th>Description</th>
                      <th>Amount</th>
                      <th>Date</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {mealOrders.map((order) => (
                      <tr key={order.id}>
                        <td>
                          <strong>{order.customerName}</strong>
                          <span>{order.mealNames.join(', ')}</span>
                        </td>
                        <td className="table-copy-cell">{order.description}</td>
                        <td className="amount-cell">{formatCurrency(Number(order.amount))}</td>
                        <td>{formatDateTime(order.date)}</td>
                        <td className="table-link-cell">
                          <Link className="status-badge processing" to={`/orders/${order.id}`}>Details</Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </article>
        </>
      )}
    </section>
  )
}
