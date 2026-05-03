import { zodResolver } from '@hookform/resolvers/zod'
import { ChevronDown, Edit3, Plus, Search, Store, Trash2 } from 'lucide-react'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useOutletContext } from 'react-router-dom'
import { z } from 'zod'

import { Pagination } from '../components/ui/Pagination'
import { useRestaurants } from '../hooks/useRestaurants'
import type { AppLayoutContext } from '../components/layout/AppLayout'
import { formatCurrency } from '../shared/lib/format'
import type {
  CreateRestaurantRequest,
  RestaurantResponse,
} from '../shared/api/restaurants'

const emptyRestaurantValues = {
  name: '',
  contactEmail: '',
  city: '',
  address: '',
  phone: '',
  rating: 4.2,
}

const restaurantFormSchema = z.object({
  name: z.string().trim().min(1, 'Restaurant name is required').max(120),
  contactEmail: z
    .string()
    .trim()
    .max(255, 'Contact email must be at most 255 characters')
    .refine((value) => value === '' || z.email().safeParse(value).success, {
      message: 'Email has invalid format',
    }),
  city: z.string().trim().max(100, 'City must be at most 100 characters'),
  address: z.string().trim().max(255, 'Address must be at most 255 characters'),
  phone: z.string().trim().max(20, 'Phone must be at most 20 characters'),
  rating: z
    .number()
    .min(1, 'Rating must be at least 1')
    .max(5, 'Rating must be at most 5'),
})

type RestaurantFormValues = z.infer<typeof restaurantFormSchema>

type RestaurantTableMetrics = {
  averageCheck: number
  rating: number
  stars: string
}

type RatingFilter = 'all' | '4-plus' | '4-5-plus'
type AverageCheckFilter = 'all' | 'under-25' | '25-35' | '35-plus'

function getErrorMessage(error: Error | null): string {
  return error?.message ?? 'Unexpected loading error'
}

function getStableHash(value: string): number {
  let hash = 0

  for (const character of value) {
    hash = Math.imul(31, hash) + character.charCodeAt(0)
  }

  return Math.abs(hash)
}

function getDerivedRestaurantRating(restaurant: RestaurantResponse): number {
  const seed = [
    restaurant.id,
    restaurant.name,
    restaurant.city ?? '',
    restaurant.address ?? '',
    restaurant.contactEmail ?? '',
  ].join('|')
  const hash = getStableHash(seed)
  const rating = 3.7 + (hash % 13) / 10
  return Math.min(5, Number(rating.toFixed(1)))
}

function getRatingStars(rating: number): string {
  const filledStars = Math.round(rating)
  return `${'★'.repeat(filledStars)}${'☆'.repeat(5 - filledStars)}`
}

function getRestaurantTableMetrics(
  restaurant: RestaurantResponse,
  ratingOverride?: number,
): RestaurantTableMetrics {
  const seed = [
    restaurant.id,
    restaurant.name,
    restaurant.city ?? '',
    restaurant.address ?? '',
    restaurant.contactEmail ?? '',
  ].join('|')
  const hash = getStableHash(seed)
  const rating = ratingOverride ?? getDerivedRestaurantRating(restaurant)

  return {
    averageCheck: 18 + (hash % 2400) / 100,
    rating,
    stars: getRatingStars(rating),
  }
}

function toRestaurantRequest(values: RestaurantFormValues): CreateRestaurantRequest {
  const contactEmail = values.contactEmail.trim()
  const city = values.city.trim()
  const address = values.address.trim()
  const phone = values.phone.trim()

  return {
    name: values.name.trim(),
    active: true,
    ...(contactEmail ? { contactEmail } : {}),
    ...(city ? { city } : {}),
    ...(address ? { address } : {}),
    ...(phone ? { phone } : {}),
  }
}

export function RestaurantsPage() {
  const {
    restaurantsQuery,
    createRestaurantMutation,
    updateRestaurantMutation,
    deleteRestaurantMutation,
  } = useRestaurants()
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [editingRestaurant, setEditingRestaurant] = useState<RestaurantResponse | null>(null)
  const [restaurantToDelete, setRestaurantToDelete] = useState<RestaurantResponse | null>(null)
  const [cityFilter, setCityFilter] = useState('')
  const [ratingFilter, setRatingFilter] = useState<RatingFilter>('all')
  const [averageCheckFilter, setAverageCheckFilter] = useState<AverageCheckFilter>('all')
  const [ratingOverrides, setRatingOverrides] = useState<Record<number, number>>({})
  const { topbarSearch, setTopbarSearch } = useOutletContext<AppLayoutContext>()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RestaurantFormValues>({
    resolver: zodResolver(restaurantFormSchema),
    defaultValues: emptyRestaurantValues,
  })

  const restaurants = useMemo(() => restaurantsQuery.data ?? [], [restaurantsQuery.data])
  const cityOptions = useMemo(() => {
    const cities = restaurants
      .map((restaurant) => restaurant.city?.trim())
      .filter((city): city is string => Boolean(city))

    return Array.from(new Set(cities)).sort((firstCity, secondCity) =>
      firstCity.localeCompare(secondCity),
    )
  }, [restaurants])
  const normalizedFilter = topbarSearch.trim().toLowerCase()

  const filteredRestaurants = useMemo(() => {
    return restaurants.filter((restaurant) => {
      const metrics = getRestaurantTableMetrics(restaurant, ratingOverrides[restaurant.id])
      const searchable = [
        restaurant.name,
        restaurant.city,
        restaurant.address,
        restaurant.contactEmail,
        restaurant.phone,
      ]
        .filter((value): value is string => Boolean(value))
        .join(' ')
        .toLowerCase()

      const matchesSearch = normalizedFilter ? searchable.includes(normalizedFilter) : true
      const matchesCity = cityFilter ? restaurant.city === cityFilter : true
      const matchesRating =
        ratingFilter === '4-plus'
          ? metrics.rating >= 4
          : ratingFilter === '4-5-plus'
            ? metrics.rating >= 4.5
            : true
      const matchesAverageCheck =
        averageCheckFilter === 'under-25'
          ? metrics.averageCheck < 25
          : averageCheckFilter === '25-35'
            ? metrics.averageCheck >= 25 && metrics.averageCheck < 35
            : averageCheckFilter === '35-plus'
              ? metrics.averageCheck >= 35
              : true

      return matchesSearch && matchesCity && matchesRating && matchesAverageCheck
    })
  }, [averageCheckFilter, cityFilter, normalizedFilter, ratingFilter, ratingOverrides, restaurants])
  const totalPages = Math.max(1, Math.ceil(filteredRestaurants.length / pageSize))
  const currentPage = Math.min(page, totalPages)
  const paginatedRestaurants = useMemo(() => {
    const startIndex = (currentPage - 1) * pageSize
    return filteredRestaurants.slice(startIndex, startIndex + pageSize)
  }, [currentPage, filteredRestaurants, pageSize])

  const isSaving = createRestaurantMutation.isPending || updateRestaurantMutation.isPending
  const saveError = createRestaurantMutation.error ?? updateRestaurantMutation.error

  function openCreateDialog() {
    setEditingRestaurant(null)
    reset(emptyRestaurantValues)
    setIsDialogOpen(true)
  }

  function openEditDialog(restaurant: RestaurantResponse) {
    setEditingRestaurant(restaurant)
    reset({
      name: restaurant.name,
      contactEmail: restaurant.contactEmail ?? '',
      city: restaurant.city ?? '',
      address: restaurant.address ?? '',
      phone: restaurant.phone ?? '',
      rating: ratingOverrides[restaurant.id] ?? getDerivedRestaurantRating(restaurant),
    })
    setIsDialogOpen(true)
  }

  function closeDialog() {
    setIsDialogOpen(false)
    setEditingRestaurant(null)
    reset(emptyRestaurantValues)
  }

  function submitRestaurant(values: RestaurantFormValues) {
    const request = toRestaurantRequest(values)
    const rating = Number(values.rating.toFixed(1))

    if (editingRestaurant) {
      updateRestaurantMutation.mutate(
        { restaurantId: editingRestaurant.id, request },
        {
          onSuccess: () => {
            setRatingOverrides((currentRatings) => ({
              ...currentRatings,
              [editingRestaurant.id]: rating,
            }))
            closeDialog()
          },
        },
      )
      return
    }

    createRestaurantMutation.mutate(request, {
      onSuccess: (restaurant) => {
        setRatingOverrides((currentRatings) => ({
          ...currentRatings,
          [restaurant.id]: rating,
        }))
        closeDialog()
      },
    })
  }

  function confirmDelete() {
    if (!restaurantToDelete) {
      return
    }

    deleteRestaurantMutation.mutate(restaurantToDelete.id, {
      onSuccess: () => setRestaurantToDelete(null),
    })
  }

  return (
    <section className="resource-page">
      <article className="dashboard-card resource-card">
        <div className="resource-toolbar">
          <div className="resource-toolbar-main">
            <div className="filter-controls resource-filters">
              <label className="resource-search compact-search" aria-label="Search restaurants">
                <Search aria-hidden="true" size={17} />
                <input
                  placeholder="Search restaurants"
                  type="search"
                  value={topbarSearch}
                  onChange={(event) => {
                    setTopbarSearch(event.target.value)
                    setPage(1)
                  }}
                />
              </label>
              <label className="toolbar-select-wrap" aria-label="Filter restaurants by city">
                <select
                  className="toolbar-select"
                  aria-label="Filter restaurants by city"
                  value={cityFilter}
                  onChange={(event) => {
                    setCityFilter(event.target.value)
                    setPage(1)
                  }}
                >
                  <option value="">All cities</option>
                  {cityOptions.map((city) => (
                    <option key={city} value={city}>
                      {city}
                    </option>
                  ))}
                </select>
                <ChevronDown aria-hidden="true" className="toolbar-select-caret" size={16} />
              </label>
              <label className="toolbar-select-wrap" aria-label="Filter restaurants by rating">
                <select
                  className="toolbar-select"
                  aria-label="Filter restaurants by rating"
                  value={ratingFilter}
                  onChange={(event) => {
                    setRatingFilter(event.target.value as RatingFilter)
                    setPage(1)
                  }}
                >
                  <option value="all">All ratings</option>
                  <option value="4-plus">4.0+</option>
                  <option value="4-5-plus">4.5+</option>
                </select>
                <ChevronDown aria-hidden="true" className="toolbar-select-caret" size={16} />
              </label>
              <label className="toolbar-select-wrap" aria-label="Filter restaurants by average check">
                <select
                  className="toolbar-select"
                  aria-label="Filter restaurants by average check"
                  value={averageCheckFilter}
                  onChange={(event) => {
                    setAverageCheckFilter(event.target.value as AverageCheckFilter)
                    setPage(1)
                  }}
                >
                  <option value="all">All checks</option>
                  <option value="under-25">Under $25</option>
                  <option value="25-35">$25-$35</option>
                  <option value="35-plus">$35+</option>
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
              Add Restaurant
            </button>
          </div>
        </div>

        {restaurantsQuery.isLoading && (
          <div className="state-panel">
            <div className="spinner" />
            <strong>Loading restaurants</strong>
            <span>Preparing the restaurant list.</span>
          </div>
        )}

        {restaurantsQuery.isError && (
          <div className="state-panel error-state">
            <strong>Could not load restaurants</strong>
            <span>{getErrorMessage(restaurantsQuery.error)}</span>
            <button type="button" onClick={() => void restaurantsQuery.refetch()}>
              Try again
            </button>
          </div>
        )}

        {restaurantsQuery.isSuccess && restaurants.length === 0 && (
          <div className="state-panel">
            <Store aria-hidden="true" size={28} />
            <strong>No restaurants yet</strong>
            <span>Create a restaurant before adding meals.</span>
          </div>
        )}

        {restaurantsQuery.isSuccess && restaurants.length > 0 && filteredRestaurants.length === 0 && (
          <div className="state-panel">
            <Search aria-hidden="true" size={28} />
            <strong>No matches</strong>
            <span>Try another search or adjust the filters.</span>
          </div>
        )}

        {restaurantsQuery.isSuccess && filteredRestaurants.length > 0 && (
          <>
            <div className="orders-table-wrap resource-table-wrap">
              <table className="orders-table resource-table">
                <thead>
                  <tr>
                    <th>Restaurant</th>
                    <th>Location</th>
                    <th>Contact</th>
                    <th>Rating</th>
                    <th>Average check</th>
                    <th>Details</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedRestaurants.map((restaurant) => {
                    const metrics = getRestaurantTableMetrics(restaurant, ratingOverrides[restaurant.id])

                    return (
                      <tr key={restaurant.id}>
                        <td>
                          <strong>{restaurant.name}</strong>
                          <span>Restaurant</span>
                        </td>
                        <td className="table-copy-cell">
                          <strong>{restaurant.city || '—'}</strong>
                          <span>{restaurant.address || 'No address'}</span>
                        </td>
                        <td className="table-copy-cell">
                          <strong>{restaurant.contactEmail || '—'}</strong>
                          <span>{restaurant.phone || 'No phone'}</span>
                        </td>
                        <td className="metric-cell">
                          <strong className="rating-value">
                            <span aria-hidden="true">{metrics.stars}</span>
                            {metrics.rating.toFixed(1)}
                          </strong>
                        </td>
                        <td className="metric-cell">
                          <strong>{formatCurrency(metrics.averageCheck)}</strong>
                          <span>Per order</span>
                        </td>
                        <td className="table-link-cell">
                          <Link className="status-badge processing" to={`/restaurants/${restaurant.id}`}>
                            Details
                          </Link>
                        </td>
                        <td className="table-actions-cell">
                          <div className="row-actions">
                            <button
                              className="table-icon-button"
                              type="button"
                              aria-label={`Edit ${restaurant.name}`}
                              onClick={() => openEditDialog(restaurant)}
                            >
                              <Edit3 aria-hidden="true" size={15} />
                            </button>
                            <button
                              className="table-icon-button danger"
                              type="button"
                              aria-label={`Delete ${restaurant.name}`}
                              onClick={() => setRestaurantToDelete(restaurant)}
                            >
                              <Trash2 aria-hidden="true" size={15} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>

            <Pagination
              page={currentPage}
              pageSize={pageSize}
              totalItems={filteredRestaurants.length}
              onPageChange={setPage}
              onPageSizeChange={(nextPageSize) => {
                setPageSize(nextPageSize)
                setPage(1)
              }}
            />
          </>
        )}
      </article>

      {isDialogOpen && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="restaurant-form-title">
            <div className="modal-header">
              <div>
                <p className="eyebrow">Restaurant</p>
                <h3 id="restaurant-form-title">
                  {editingRestaurant ? 'Edit restaurant' : 'Create restaurant'}
                </h3>
              </div>
              <button className="table-icon-button" type="button" aria-label="Close" onClick={closeDialog}>
                ×
              </button>
            </div>

            <form className="entity-form" onSubmit={handleSubmit(submitRestaurant)}>
              <label>
                <span>Name</span>
                <input type="text" {...register('name')} />
                {errors.name && <small>{errors.name.message}</small>}
              </label>

              <label>
                <span>Contact email</span>
                <input type="email" {...register('contactEmail')} />
                {errors.contactEmail && <small>{errors.contactEmail.message}</small>}
              </label>

              <label>
                <span>City</span>
                <input type="text" {...register('city')} />
                {errors.city && <small>{errors.city.message}</small>}
              </label>

              <label>
                <span>Address</span>
                <input type="text" {...register('address')} />
                {errors.address && <small>{errors.address.message}</small>}
              </label>

              <label>
                <span>Phone</span>
                <input type="tel" {...register('phone')} />
                {errors.phone && <small>{errors.phone.message}</small>}
              </label>

              <label>
                <span>Rating</span>
                <input
                  type="number"
                  min="1"
                  max="5"
                  step="0.1"
                  {...register('rating', { valueAsNumber: true })}
                />
                {errors.rating && <small>{errors.rating.message}</small>}
              </label>

              {saveError && <div className="form-error">{getErrorMessage(saveError)}</div>}

              <div className="modal-actions">
                <button className="secondary-action" type="button" onClick={closeDialog}>
                  Cancel
                </button>
                <button className="primary-action" type="submit" disabled={isSaving}>
                  {isSaving ? 'Saving...' : 'Save restaurant'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {restaurantToDelete && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal-card confirm-card" role="dialog" aria-modal="true" aria-labelledby="delete-title">
            <div className="modal-header">
              <div>
                <p className="eyebrow">Confirm delete</p>
                <h3 id="delete-title">Delete restaurant?</h3>
              </div>
            </div>
            <p>{restaurantToDelete.name} will be removed from the restaurant list.</p>
            {deleteRestaurantMutation.error && (
              <div className="form-error">{getErrorMessage(deleteRestaurantMutation.error)}</div>
            )}
            <div className="modal-actions">
              <button className="secondary-action" type="button" onClick={() => setRestaurantToDelete(null)}>
                Cancel
              </button>
              <button
                className="primary-action danger-action"
                type="button"
                disabled={deleteRestaurantMutation.isPending}
                onClick={confirmDelete}
              >
                {deleteRestaurantMutation.isPending ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
