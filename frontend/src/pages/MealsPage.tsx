import { zodResolver } from '@hookform/resolvers/zod'
import { Beef, ChevronDown, Edit3, Plus, Search, Trash2 } from 'lucide-react'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useOutletContext, useSearchParams } from 'react-router-dom'
import { z } from 'zod'

import { Pagination } from '../components/ui/Pagination'
import type { AppLayoutContext } from '../components/layout/AppLayout'
import { useCategories } from '../hooks/useCategories'
import { useMeals } from '../hooks/useMeals'
import { useRestaurants } from '../hooks/useRestaurants'
import type {
  CreateMealRequest,
  MealResponse,
  MealSortField,
} from '../shared/api/meals'

const emptyMealValues = {
  name: '',
  price: 0,
  cookingTime: 0,
  categoryId: 0,
  restaurantId: 0,
}

const mealFormSchema = z.object({
  name: z.string().trim().min(1, 'Meal name is required').max(150),
  price: z.number().min(0.01, 'Price must be at least 0.01'),
  cookingTime: z.number().int().positive('Cooking time must be positive'),
  categoryId: z.number().int().positive('Category is required'),
  restaurantId: z.number().int().positive('Restaurant is required'),
})

type MealFormValues = z.infer<typeof mealFormSchema>

const sortOptions: Array<{ label: string; value: MealSortField }> = [
  { label: 'Name', value: 'name' },
  { label: 'Price', value: 'price' },
  { label: 'Cooking time', value: 'cookingTime' },
]

function getErrorMessage(error: Error | null): string {
  return error?.message ?? 'Unexpected loading error'
}

function toMealRequest(values: MealFormValues): CreateMealRequest {
  return {
    name: values.name.trim(),
    price: values.price,
    cookingTime: values.cookingTime,
    categoryId: values.categoryId,
    restaurantId: values.restaurantId,
  }
}

export function MealsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [sortBy, setSortBy] = useState<MealSortField>('name')
  const [ascending, setAscending] = useState(true)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [editingMeal, setEditingMeal] = useState<MealResponse | null>(null)
  const [mealToDelete, setMealToDelete] = useState<MealResponse | null>(null)
  const isCreateRequested = searchParams.get('create') === '1'
  const isMealDialogOpen = isDialogOpen || isCreateRequested
  const { topbarSearch, setTopbarSearch } = useOutletContext<AppLayoutContext>()

  const mealPageRequest = useMemo(
    () => ({ page, size, sortBy, ascending }),
    [page, size, sortBy, ascending],
  )
  const {
    mealsQuery,
    createMealMutation,
    updateMealMutation,
    deleteMealMutation,
  } = useMeals(mealPageRequest)
  const { categoriesQuery } = useCategories({ enabled: isMealDialogOpen })
  const { restaurantsQuery } = useRestaurants({ enabled: isMealDialogOpen })

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<MealFormValues>({
    resolver: zodResolver(mealFormSchema),
    defaultValues: emptyMealValues,
  })

  const mealsPage = mealsQuery.data
  const meals = useMemo(() => mealsPage?.content ?? [], [mealsPage?.content])
  const categories = useMemo(() => categoriesQuery.data ?? [], [categoriesQuery.data])
  const restaurants = useMemo(() => restaurantsQuery.data ?? [], [restaurantsQuery.data])
  const normalizedFilter = topbarSearch.trim().toLowerCase()

  const filteredMeals = useMemo(() => {
    if (!normalizedFilter) {
      return meals
    }

    return meals.filter((meal) => {
      const searchable = [
        meal.name,
        meal.categoryName,
        meal.restaurantName,
        String(meal.price),
      ].join(' ').toLowerCase()

      return searchable.includes(normalizedFilter)
    })
  }, [meals, normalizedFilter])

  const isSaving = createMealMutation.isPending || updateMealMutation.isPending
  const saveError = createMealMutation.error ?? updateMealMutation.error
  const supportingDataLoading = categoriesQuery.isLoading || restaurantsQuery.isLoading
  const supportingDataError = categoriesQuery.error ?? restaurantsQuery.error

  function openCreateDialog() {
    setEditingMeal(null)
    reset(emptyMealValues)
    setIsDialogOpen(true)
  }

  function openEditDialog(meal: MealResponse) {
    setEditingMeal(meal)
    reset({
      name: meal.name,
      price: meal.price,
      cookingTime: meal.cookingTime,
      categoryId: meal.categoryId,
      restaurantId: meal.restaurantId,
    })
    setIsDialogOpen(true)
  }

  function closeDialog() {
    setIsDialogOpen(false)
    setEditingMeal(null)
    reset(emptyMealValues)
    if (isCreateRequested) {
      setSearchParams({})
    }
  }

  function submitMeal(values: MealFormValues) {
    const request = toMealRequest(values)

    if (editingMeal) {
      updateMealMutation.mutate(
        { mealId: editingMeal.id, request },
        { onSuccess: closeDialog },
      )
      return
    }

    createMealMutation.mutate(request, { onSuccess: closeDialog })
  }

  function confirmDelete() {
    if (!mealToDelete) {
      return
    }

    deleteMealMutation.mutate(mealToDelete.id, {
      onSuccess: () => setMealToDelete(null),
    })
  }

  function updatePageSize(nextSize: number) {
    setSize(nextSize)
    setPage(0)
  }

  function updateSort(nextSortBy: MealSortField) {
    setSortBy(nextSortBy)
    setPage(0)
  }

  function toggleAscending() {
    setAscending((current) => !current)
    setPage(0)
  }

  return (
    <section className="resource-page">
      <article className="dashboard-card resource-card">
        <div className="resource-toolbar">
          <div className="resource-toolbar-main">
            <div className="filter-controls resource-filters">
              <label className="resource-search compact-search" aria-label="Search meals">
                <Search aria-hidden="true" size={17} />
                <input
                  placeholder="Search meals"
                  type="search"
                  value={topbarSearch}
                  onChange={(event) => setTopbarSearch(event.target.value)}
                />
              </label>
              <label className="toolbar-select-wrap" aria-label="Sort meals by">
                <select
                  className="toolbar-select"
                  aria-label="Sort meals by"
                  value={sortBy}
                  onChange={(event) => updateSort(event.target.value as MealSortField)}
                >
                  {sortOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <ChevronDown aria-hidden="true" className="toolbar-select-caret" size={16} />
              </label>
              <label className="toolbar-select-wrap" aria-label="Sort order">
                <select
                  className="toolbar-select"
                  aria-label="Sort order"
                  value={ascending ? 'ascending' : 'descending'}
                  onChange={(event) => {
                    const nextAscending = event.target.value === 'ascending'
                    if (nextAscending !== ascending) {
                      toggleAscending()
                    }
                  }}
                >
                  <option value="ascending">Ascending</option>
                  <option value="descending">Descending</option>
                </select>
                <ChevronDown aria-hidden="true" className="toolbar-select-caret" size={16} />
              </label>
            </div>
          </div>
          <div className="resource-toolbar-end">
            <button
              className="primary-action resource-action toolbar-primary-action"
              type="button"
              onClick={openCreateDialog}
            >
              <Plus aria-hidden="true" size={16} />
              Add Meal
            </button>
          </div>
        </div>

        {mealsQuery.isLoading && (
          <div className="state-panel">
            <div className="spinner" />
            <strong>Loading meals</strong>
            <span>Preparing the meal list.</span>
          </div>
        )}

        {mealsQuery.isError && (
          <div className="state-panel error-state">
            <strong>Could not load meals</strong>
            <span>{getErrorMessage(mealsQuery.error)}</span>
            <button type="button" onClick={() => void mealsQuery.refetch()}>
              Try again
            </button>
          </div>
        )}

        {mealsQuery.isSuccess && meals.length === 0 && (
          <div className="state-panel">
            <Beef aria-hidden="true" size={28} />
            <strong>No meals yet</strong>
            <span>Create categories and restaurants first, then add meals.</span>
          </div>
        )}

        {mealsQuery.isSuccess && meals.length > 0 && filteredMeals.length === 0 && (
          <div className="state-panel">
            <Search aria-hidden="true" size={28} />
            <strong>No matches</strong>
            <span>Try another meal, category, restaurant, or price filter.</span>
          </div>
        )}

        {mealsQuery.isSuccess && filteredMeals.length > 0 && (
          <>
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
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredMeals.map((meal) => (
                    <tr key={meal.id}>
                      <td>
                        <strong>{meal.name}</strong>
                        <span>Menu item</span>
                      </td>
                      <td>
                        <span className="status-badge processing">{meal.categoryName}</span>
                      </td>
                      <td className="table-copy-cell">{meal.restaurantName}</td>
                      <td className="amount-cell">${Number(meal.price).toFixed(2)}</td>
                      <td>{meal.cookingTime} min</td>
                      <td className="table-link-cell">
                        <Link className="status-badge processing" to={`/meals/${meal.id}`}>
                          Details
                        </Link>
                      </td>
                      <td className="table-actions-cell">
                        <div className="row-actions">
                          <button
                            className="table-icon-button"
                            type="button"
                            aria-label={`Edit ${meal.name}`}
                            onClick={() => openEditDialog(meal)}
                          >
                            <Edit3 aria-hidden="true" size={15} />
                          </button>
                          <button
                            className="table-icon-button danger"
                            type="button"
                            aria-label={`Delete ${meal.name}`}
                            onClick={() => setMealToDelete(meal)}
                          >
                            <Trash2 aria-hidden="true" size={15} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <Pagination
              page={(mealsPage?.number ?? page) + 1}
              pageSize={size}
              totalItems={mealsPage?.totalElements ?? filteredMeals.length}
              onPageChange={(nextPage) => setPage(nextPage - 1)}
              onPageSizeChange={updatePageSize}
            />
          </>
        )}
      </article>

      {isMealDialogOpen && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="meal-form-title">
            <div className="modal-header">
              <div>
                <p className="eyebrow">Meal</p>
                <h3 id="meal-form-title">{editingMeal ? 'Edit meal' : 'Create meal'}</h3>
              </div>
              <button className="table-icon-button" type="button" aria-label="Close" onClick={closeDialog}>
                ×
              </button>
            </div>

            {supportingDataError && (
              <div className="form-error">{getErrorMessage(supportingDataError)}</div>
            )}

            <form className="entity-form" onSubmit={handleSubmit(submitMeal)}>
              <label>
                <span>Name</span>
                <input type="text" {...register('name')} />
                {errors.name && <small>{errors.name.message}</small>}
              </label>

              <label>
                <span>Price</span>
                <input type="number" min="0.01" step="0.01" {...register('price', { valueAsNumber: true })} />
                {errors.price && <small>{errors.price.message}</small>}
              </label>

              <label>
                <span>Cooking time, minutes</span>
                <input type="number" min="1" step="1" {...register('cookingTime', { valueAsNumber: true })} />
                {errors.cookingTime && <small>{errors.cookingTime.message}</small>}
              </label>

              <label>
                <span>Category</span>
                <select disabled={supportingDataLoading} {...register('categoryId', { valueAsNumber: true })}>
                  <option value={0}>Select category</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
                {errors.categoryId && <small>{errors.categoryId.message}</small>}
              </label>

              <label>
                <span>Restaurant</span>
                <select disabled={supportingDataLoading} {...register('restaurantId', { valueAsNumber: true })}>
                  <option value={0}>Select restaurant</option>
                  {restaurants.map((restaurant) => (
                    <option key={restaurant.id} value={restaurant.id}>
                      {restaurant.name}
                    </option>
                  ))}
                </select>
                {errors.restaurantId && <small>{errors.restaurantId.message}</small>}
              </label>

              {saveError && <div className="form-error">{getErrorMessage(saveError)}</div>}

              <div className="modal-actions">
                <button className="secondary-action" type="button" onClick={closeDialog}>
                  Cancel
                </button>
                <button
                  className="primary-action"
                  type="submit"
                  disabled={isSaving || supportingDataLoading}
                >
                  {isSaving ? 'Saving...' : 'Save meal'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {mealToDelete && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal-card confirm-card" role="dialog" aria-modal="true" aria-labelledby="delete-title">
            <div className="modal-header">
              <div>
                <p className="eyebrow">Confirm delete</p>
                <h3 id="delete-title">Delete meal?</h3>
              </div>
            </div>
            <p>{mealToDelete.name} will be removed from the meal list.</p>
            {deleteMealMutation.error && (
              <div className="form-error">{getErrorMessage(deleteMealMutation.error)}</div>
            )}
            <div className="modal-actions">
              <button className="secondary-action" type="button" onClick={() => setMealToDelete(null)}>
                Cancel
              </button>
              <button
                className="primary-action danger-action"
                type="button"
                disabled={deleteMealMutation.isPending}
                onClick={confirmDelete}
              >
                {deleteMealMutation.isPending ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
