import { ArrowLeft, Beef, Mail, MapPin, Phone, Store } from 'lucide-react'
import { useMemo } from 'react'
import { Link, useParams } from 'react-router-dom'

import { useMeals } from '../hooks/useMeals'
import { useRestaurant } from '../hooks/useRestaurants'
import { formatCurrency } from '../shared/lib/format'

const RELATED_MEALS_PAGE_SIZE = 50

function parseRouteId(value: string | undefined): number | null {
  const parsedValue = Number(value)
  return Number.isInteger(parsedValue) && parsedValue > 0 ? parsedValue : null
}

export function RestaurantDetailsPage() {
  const { restaurantId } = useParams()
  const parsedRestaurantId = parseRouteId(restaurantId)
  const restaurantQuery = useRestaurant(parsedRestaurantId)
  const mealsRequest = useMemo(
    () => ({ page: 0, size: RELATED_MEALS_PAGE_SIZE, sortBy: 'name' as const, ascending: true }),
    [],
  )
  const { mealsQuery } = useMeals(mealsRequest, { enabled: parsedRestaurantId !== null })

  const restaurantMeals = useMemo(() => {
    const meals = mealsQuery.data?.content ?? []
    return parsedRestaurantId === null
      ? []
      : meals.filter((meal) => meal.restaurantId === parsedRestaurantId)
  }, [mealsQuery.data?.content, parsedRestaurantId])

  if (parsedRestaurantId === null) {
    return (
      <section className="resource-page">
        <div className="state-panel error-state">
          <strong>Restaurant was not found</strong>
          <span>The selected restaurant link is not valid.</span>
          <Link className="secondary-action" to="/restaurants">Back to Restaurants</Link>
        </div>
      </section>
    )
  }

  const isLoading = restaurantQuery.isLoading || mealsQuery.isLoading
  const error = restaurantQuery.error ?? mealsQuery.error
  const restaurant = restaurantQuery.data

  return (
    <section className="resource-page">
      <div className="resource-header">
        <div>
          <p className="eyebrow">Restaurant Profile</p>
          <p>Meals offered by this restaurant are shown below.</p>
        </div>
        <Link className="secondary-action resource-action" to="/restaurants">
          <ArrowLeft aria-hidden="true" size={16} />
          Back
        </Link>
      </div>

      {isLoading && (
        <div className="state-panel">
          <div className="spinner" />
          <strong>Loading restaurant</strong>
          <span>Preparing restaurant and related meals.</span>
        </div>
      )}

      {error && (
        <div className="state-panel error-state">
          <strong>Could not load restaurant details</strong>
          <span>{error.message}</span>
        </div>
      )}

      {restaurant && !isLoading && !error && (
        <>
          <div className="detail-grid">
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Store aria-hidden="true" size={24} /></div>
              <span>Restaurant</span>
              <strong>{restaurant.name}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><MapPin aria-hidden="true" size={24} /></div>
              <span>Location</span>
              <strong>{restaurant.city || '—'}</strong>
              <small>{restaurant.address || 'No address'}</small>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Mail aria-hidden="true" size={24} /></div>
              <span>Email</span>
              <strong>{restaurant.contactEmail || '—'}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Phone aria-hidden="true" size={24} /></div>
              <span>Phone</span>
              <strong>{restaurant.phone || '—'}</strong>
            </article>
          </div>

          <article className="dashboard-card resource-card">
            <div className="card-heading compact">
              <div>
                <strong>Restaurant Meals</strong>
                <span>One restaurant can offer many meals.</span>
              </div>
              <span className={restaurant.active ? 'status-badge completed' : 'status-badge cancelled'}>
                {restaurant.active ? 'Active' : 'Inactive'}
              </span>
            </div>

            {restaurantMeals.length === 0 ? (
              <div className="state-panel">
                <Beef aria-hidden="true" size={28} />
                <strong>No meals for this restaurant</strong>
                <span>Add a meal and select this restaurant.</span>
              </div>
            ) : (
              <div className="orders-table-wrap resource-table-wrap">
                <table className="orders-table resource-table">
                  <thead>
                    <tr>
                      <th>Meal</th>
                      <th>Category</th>
                      <th>Price</th>
                      <th>Cooking</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {restaurantMeals.map((meal) => (
                      <tr key={meal.id}>
                        <td>
                          <strong>{meal.name}</strong>
                          <span>{meal.restaurantName}</span>
                        </td>
                        <td>
                          <span className="status-badge processing">{meal.categoryName}</span>
                        </td>
                        <td className="amount-cell">{formatCurrency(Number(meal.price))}</td>
                        <td>{meal.cookingTime} min</td>
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
