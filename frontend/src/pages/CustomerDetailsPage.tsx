import { ArrowLeft, ClipboardList, Mail, Phone, UserRound } from 'lucide-react'
import { useMemo } from 'react'
import { Link, useParams } from 'react-router-dom'

import { useCustomer } from '../hooks/useCustomers'
import { useOrders } from '../hooks/useOrders'
import { formatCurrency, formatDateTime } from '../shared/lib/format'

function parseRouteId(value: string | undefined): number | null {
  const parsedValue = Number(value)
  return Number.isInteger(parsedValue) && parsedValue > 0 ? parsedValue : null
}

export function CustomerDetailsPage() {
  const { customerId } = useParams()
  const parsedCustomerId = parseRouteId(customerId)
  const customerQuery = useCustomer(parsedCustomerId)
  const ordersRequest = useMemo(() => ({ withEntityGraph: true }), [])
  const { ordersQuery } = useOrders(ordersRequest, { enabled: parsedCustomerId !== null })

  const customerOrders = useMemo(() => {
    const orders = ordersQuery.data ?? []
    return parsedCustomerId === null
      ? []
      : orders.filter((order) => order.customerId === parsedCustomerId)
  }, [ordersQuery.data, parsedCustomerId])

  if (parsedCustomerId === null) {
    return (
      <section className="resource-page">
        <div className="state-panel error-state">
          <strong>Customer was not found</strong>
          <span>The selected customer link is not valid.</span>
          <Link className="secondary-action" to="/customers">Back to Customers</Link>
        </div>
      </section>
    )
  }

  const isLoading = customerQuery.isLoading || ordersQuery.isLoading
  const error = customerQuery.error ?? ordersQuery.error
  const customer = customerQuery.data

  return (
    <section className="resource-page">
      <div className="resource-header">
        <div>
          <p className="eyebrow">Customer Profile</p>
          <p>Orders placed by this customer are shown below.</p>
        </div>
        <Link className="secondary-action resource-action" to="/customers">
          <ArrowLeft aria-hidden="true" size={16} />
          Back
        </Link>
      </div>

      {isLoading && (
        <div className="state-panel">
          <div className="spinner" />
          <strong>Loading customer</strong>
          <span>Preparing profile and related orders.</span>
        </div>
      )}

      {error && (
        <div className="state-panel error-state">
          <strong>Could not load customer details</strong>
          <span>{error.message}</span>
        </div>
      )}

      {customer && !isLoading && !error && (
        <>
          <div className="detail-grid">
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><UserRound aria-hidden="true" size={24} /></div>
              <span>Full name</span>
              <strong>{customer.firstName} {customer.lastName}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Mail aria-hidden="true" size={24} /></div>
              <span>Email</span>
              <strong>{customer.email}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Phone aria-hidden="true" size={24} /></div>
              <span>Phone</span>
              <strong>{customer.phoneNumber || '—'}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><ClipboardList aria-hidden="true" size={24} /></div>
              <span>Total orders</span>
              <strong>{customerOrders.length}</strong>
            </article>
          </div>

          <article className="dashboard-card resource-card">
            <div className="card-heading compact">
              <div>
                <strong>Customer Orders</strong>
                <span>One customer can place many orders.</span>
              </div>
              <Link className="primary-action" to="/orders?create=1">Create Order</Link>
            </div>

            {customerOrders.length === 0 ? (
              <div className="state-panel">
                <ClipboardList aria-hidden="true" size={28} />
                <strong>No orders for this customer</strong>
                <span>Create an order to see it linked here.</span>
              </div>
            ) : (
              <div className="orders-table-wrap resource-table-wrap">
                <table className="orders-table resource-table">
                  <thead>
                    <tr>
                      <th>Description</th>
                      <th>Meals</th>
                      <th>Amount</th>
                      <th>Date</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {customerOrders.map((order) => (
                      <tr key={order.id}>
                        <td className="table-copy-cell">{order.description}</td>
                        <td>
                          <strong>{order.mealNames.length} meals</strong>
                          <span>{order.mealNames.join(', ') || 'No meals'}</span>
                        </td>
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
