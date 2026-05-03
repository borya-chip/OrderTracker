import { ArrowLeft, Beef, CalendarClock, ClipboardList, UserRound } from 'lucide-react'
import { useMemo } from 'react'
import { Link, useParams } from 'react-router-dom'

import { useMeals } from '../hooks/useMeals'
import { useOrder } from '../hooks/useOrders'
import type { MealResponse } from '../shared/api/meals'
import { formatCurrency, formatDateTime } from '../shared/lib/format'

const RELATED_MEALS_PAGE_SIZE = 50

function parseRouteId(value: string | undefined): number | null {
  const parsedValue = Number(value)
  return Number.isInteger(parsedValue) && parsedValue > 0 ? parsedValue : null
}

function buildOrderMeals(allMeals: MealResponse[], mealIds: number[], mealNames: string[]) {
  const mealsById = new Map(allMeals.map((meal) => [meal.id, meal]))

  return mealIds.map((mealId, index) => {
    const meal = mealsById.get(mealId)
    return {
      id: mealId,
      name: meal?.name ?? mealNames[index] ?? 'Selected meal',
      categoryName: meal?.categoryName ?? '—',
      restaurantName: meal?.restaurantName ?? '—',
      price: meal?.price,
      cookingTime: meal?.cookingTime,
    }
  })
}

export function OrderDetailsPage() {
  const { orderId } = useParams()
  const parsedOrderId = parseRouteId(orderId)
  const orderQuery = useOrder(parsedOrderId)
  const mealsRequest = useMemo(
    () => ({ page: 0, size: RELATED_MEALS_PAGE_SIZE, sortBy: 'name' as const, ascending: true }),
    [],
  )
  const { mealsQuery } = useMeals(mealsRequest, {
    enabled: parsedOrderId !== null && orderQuery.isSuccess,
  })

  const order = orderQuery.data
  const orderMeals = useMemo(() => {
    const allMeals = mealsQuery.data?.content ?? []
    return order ? buildOrderMeals(allMeals, order.mealIds, order.mealNames) : []
  }, [mealsQuery.data?.content, order])

  if (parsedOrderId === null) {
    return (
      <section className="resource-page">
        <div className="state-panel error-state">
          <strong>Order was not found</strong>
          <span>The selected order link is not valid.</span>
          <Link className="secondary-action" to="/orders">Back to Orders</Link>
        </div>
      </section>
    )
  }

  const isLoading = orderQuery.isLoading || mealsQuery.isLoading
  const error = orderQuery.error ?? mealsQuery.error

  return (
    <section className="resource-page">
      <div className="resource-header">
        <div>
          <p className="eyebrow">Order Details</p>
          <p>Customer and selected meals for this order.</p>
        </div>
        <Link className="secondary-action resource-action" to="/orders">
          <ArrowLeft aria-hidden="true" size={16} />
          Back
        </Link>
      </div>

      {isLoading && (
        <div className="state-panel">
          <div className="spinner" />
          <strong>Loading order</strong>
          <span>Preparing order and selected meals.</span>
        </div>
      )}

      {error && (
        <div className="state-panel error-state">
          <strong>Could not load order details</strong>
          <span>{error.message}</span>
        </div>
      )}

      {order && !isLoading && !error && (
        <>
          <div className="detail-grid">
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><ClipboardList aria-hidden="true" size={24} /></div>
              <span>Description</span>
              <strong>{order.description}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><UserRound aria-hidden="true" size={24} /></div>
              <span>Customer</span>
              <strong>{order.customerName}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Beef aria-hidden="true" size={24} /></div>
              <span>Meals</span>
              <strong>{order.mealIds.length}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><CalendarClock aria-hidden="true" size={24} /></div>
              <span>Date</span>
              <strong>{formatDateTime(order.date)}</strong>
            </article>
          </div>

          <article className="dashboard-card resource-card">
            <div className="card-heading compact">
              <div>
                <strong>Meals in Order</strong>
                <span>An order can include many meals, and a meal can appear in many orders.</span>
              </div>
              <span className="amount-cell">{formatCurrency(Number(order.amount))}</span>
            </div>

            {orderMeals.length === 0 ? (
              <div className="state-panel">
                <Beef aria-hidden="true" size={28} />
                <strong>No meals linked</strong>
                <span>Edit the order to select meals.</span>
              </div>
            ) : (
              <div className="orders-table-wrap resource-table-wrap">
                <table className="orders-table resource-table">
                  <thead>
                    <tr>
                      <th>Meal</th>
                      <th>Category</th>
                      <th>Restaurant</th>
                      <th>Price</th>
                      <th>Cooking</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {orderMeals.map((meal) => (
                      <tr key={meal.id}>
                        <td>
                          <strong>{meal.name}</strong>
                          <span>Selected meal</span>
                        </td>
                        <td>{meal.categoryName}</td>
                        <td className="table-copy-cell">{meal.restaurantName}</td>
                        <td className="amount-cell">
                          {meal.price === undefined ? '—' : formatCurrency(Number(meal.price))}
                        </td>
                        <td>{meal.cookingTime === undefined ? '—' : `${meal.cookingTime} min`}</td>
                        <td className="table-link-cell">
                          <Link className="status-badge processing" to={`/meals/${meal.id}`}>Details</Link>
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
