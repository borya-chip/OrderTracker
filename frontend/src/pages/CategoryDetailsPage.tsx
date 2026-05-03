import { ArrowLeft, Beef, Tags } from 'lucide-react'
import { useMemo } from 'react'
import { Link, useParams } from 'react-router-dom'

import { useCategory } from '../hooks/useCategories'
import { useMeals } from '../hooks/useMeals'
import { formatCurrency } from '../shared/lib/format'

const RELATED_MEALS_PAGE_SIZE = 50

function parseRouteId(value: string | undefined): number | null {
  const parsedValue = Number(value)
  return Number.isInteger(parsedValue) && parsedValue > 0 ? parsedValue : null
}

export function CategoryDetailsPage() {
  const { categoryId } = useParams()
  const parsedCategoryId = parseRouteId(categoryId)
  const categoryQuery = useCategory(parsedCategoryId)
  const mealsRequest = useMemo(
    () => ({ page: 0, size: RELATED_MEALS_PAGE_SIZE, sortBy: 'name' as const, ascending: true }),
    [],
  )
  const { mealsQuery } = useMeals(mealsRequest, { enabled: parsedCategoryId !== null })

  const categoryMeals = useMemo(() => {
    const meals = mealsQuery.data?.content ?? []
    return parsedCategoryId === null
      ? []
      : meals.filter((meal) => meal.categoryId === parsedCategoryId)
  }, [mealsQuery.data?.content, parsedCategoryId])

  if (parsedCategoryId === null) {
    return (
      <section className="resource-page">
        <div className="state-panel error-state">
          <strong>Category was not found</strong>
          <span>The selected category link is not valid.</span>
          <Link className="secondary-action" to="/categories">Back to Categories</Link>
        </div>
      </section>
    )
  }

  const isLoading = categoryQuery.isLoading || mealsQuery.isLoading
  const error = categoryQuery.error ?? mealsQuery.error
  const category = categoryQuery.data

  return (
    <section className="resource-page">
      <div className="resource-header">
        <div>
          <p className="eyebrow">Meal Category</p>
          <p>Meals assigned to this category are shown below.</p>
        </div>
        <Link className="secondary-action resource-action" to="/categories">
          <ArrowLeft aria-hidden="true" size={16} />
          Back
        </Link>
      </div>

      {isLoading && (
        <div className="state-panel">
          <div className="spinner" />
          <strong>Loading category</strong>
          <span>Preparing category and related meals.</span>
        </div>
      )}

      {error && (
        <div className="state-panel error-state">
          <strong>Could not load category details</strong>
          <span>{error.message}</span>
        </div>
      )}

      {category && !isLoading && !error && (
        <>
          <div className="detail-grid compact-detail-grid">
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Tags aria-hidden="true" size={24} /></div>
              <span>Category</span>
              <strong>{category.name}</strong>
            </article>
            <article className="dashboard-card detail-card">
              <div className="detail-icon"><Beef aria-hidden="true" size={24} /></div>
              <span>Meals in category</span>
              <strong>{categoryMeals.length}</strong>
            </article>
          </div>

          <article className="dashboard-card resource-card">
            <div className="card-heading compact">
              <div>
                <strong>Category Meals</strong>
                <span>One category can group many meals.</span>
              </div>
              <Link className="primary-action" to="/meals?create=1">Add Meal</Link>
            </div>

            {categoryMeals.length === 0 ? (
              <div className="state-panel">
                <Beef aria-hidden="true" size={28} />
                <strong>No meals in this category</strong>
                <span>Add a meal and select this category.</span>
              </div>
            ) : (
              <div className="orders-table-wrap resource-table-wrap">
                <table className="orders-table resource-table">
                  <thead>
                    <tr>
                      <th>Meal</th>
                      <th>Restaurant</th>
                      <th>Price</th>
                      <th>Cooking</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {categoryMeals.map((meal) => (
                      <tr key={meal.id}>
                        <td>
                          <strong>{meal.name}</strong>
                          <span>{meal.categoryName}</span>
                        </td>
                        <td className="table-copy-cell">{meal.restaurantName}</td>
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
