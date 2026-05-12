import { zodResolver } from '@hookform/resolvers/zod'
import { Edit3, Plus, Search, Tags, Trash2 } from 'lucide-react'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useOutletContext } from 'react-router-dom'
import { z } from 'zod'

import { useCategories } from '../hooks/useCategories'
import { useMeals } from '../hooks/useMeals'
import type { AppLayoutContext } from '../components/layout/AppLayout'
import type {
  CategoryResponse,
  CreateCategoryRequest,
} from '../shared/api/categories'

const CATEGORY_MEALS_PAGE_SIZE = 1000
const DEFAULT_CATEGORY_ICON = '🍽️'
const CATEGORY_ICONS: Record<string, string> = {
  'Asian Food': '🍜',
  Bakery: '🥐',
  Breakfast: '🍳',
  Burgers: '🍔',
  Desserts: '🍰',
  Drinks: '🥤',
  Grill: '🍖',
  Pasta: '🍝',
  Pizza: '🍕',
  Salads: '🥗',
  Seafood: '🦐',
  Sushi: '🍣',
}
const NORMALIZED_CATEGORY_ICONS = new Map(
  Object.entries(CATEGORY_ICONS).map(([categoryName, icon]) => [
    normalizeCategoryName(categoryName),
    icon,
  ]),
)

const emptyCategoryValues = {
  name: '',
}

const categoryFormSchema = z.object({
  name: z.string().trim().min(1, 'Category name is required').max(100),
})

type CategoryFormValues = z.infer<typeof categoryFormSchema>

function getErrorMessage(error: Error | null): string {
  return error?.message ?? 'Unexpected loading error'
}

function normalizeCategoryName(categoryName: string): string {
  return categoryName.trim().replace(/\s+/g, ' ').toLowerCase()
}

function getCategoryIcon(category: CategoryResponse): string {
  return NORMALIZED_CATEGORY_ICONS.get(normalizeCategoryName(category.name)) ?? DEFAULT_CATEGORY_ICON
}

function formatMealsCount(mealsCount: number): string {
  return `${mealsCount} ${mealsCount === 1 ? 'meal' : 'meals'}`
}

function toCategoryRequest(values: CategoryFormValues): CreateCategoryRequest {
  return {
    name: values.name.trim(),
  }
}

export function CategoriesPage() {
  const {
    categoriesQuery,
    createCategoryMutation,
    updateCategoryMutation,
    deleteCategoryMutation,
  } = useCategories()
  const mealsRequest = useMemo(
    () => ({ page: 0, size: CATEGORY_MEALS_PAGE_SIZE, sortBy: 'name' as const, ascending: true }),
    [],
  )
  const { mealsQuery } = useMeals(mealsRequest)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [editingCategory, setEditingCategory] = useState<CategoryResponse | null>(null)
  const [categoryToDelete, setCategoryToDelete] = useState<CategoryResponse | null>(null)
  const { topbarSearch, setTopbarSearch } = useOutletContext<AppLayoutContext>()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CategoryFormValues>({
    resolver: zodResolver(categoryFormSchema),
    defaultValues: emptyCategoryValues,
  })

  const categories = useMemo(() => categoriesQuery.data ?? [], [categoriesQuery.data])
  const meals = useMemo(() => mealsQuery.data?.content ?? [], [mealsQuery.data?.content])
  const mealsCountByCategoryId = useMemo(() => {
    const counts = new Map<number, number>()

    for (const meal of meals) {
      counts.set(meal.categoryId, (counts.get(meal.categoryId) ?? 0) + 1)
    }

    return counts
  }, [meals])
  const normalizedFilter = topbarSearch.trim().toLowerCase()

  const filteredCategories = useMemo(() => {
    if (!normalizedFilter) {
      return categories
    }

    return categories.filter((category) =>
      category.name.toLowerCase().includes(normalizedFilter),
    )
  }, [categories, normalizedFilter])

  const isSaving = createCategoryMutation.isPending || updateCategoryMutation.isPending
  const saveError = createCategoryMutation.error ?? updateCategoryMutation.error

  function openCreateDialog() {
    setEditingCategory(null)
    reset(emptyCategoryValues)
    setIsDialogOpen(true)
  }

  function openEditDialog(category: CategoryResponse) {
    setEditingCategory(category)
    reset({ name: category.name })
    setIsDialogOpen(true)
  }

  function closeDialog() {
    setIsDialogOpen(false)
    setEditingCategory(null)
    reset(emptyCategoryValues)
  }

  function submitCategory(values: CategoryFormValues) {
    const request = toCategoryRequest(values)

    if (editingCategory) {
      updateCategoryMutation.mutate(
        { categoryId: editingCategory.id, request },
        { onSuccess: closeDialog },
      )
      return
    }

    createCategoryMutation.mutate(request, { onSuccess: closeDialog })
  }

  function confirmDelete() {
    if (!categoryToDelete) {
      return
    }

    deleteCategoryMutation.mutate(categoryToDelete.id, {
      onSuccess: () => setCategoryToDelete(null),
    })
  }

  return (
    <section className="resource-page">
      <article className="dashboard-card resource-card">
        <div className="resource-toolbar">
          <div className="resource-toolbar-main">
            <label className="resource-search" aria-label="Search categories">
              <Search aria-hidden="true" size={17} />
              <input
                placeholder="Search categories"
                type="search"
                value={topbarSearch}
                onChange={(event) => setTopbarSearch(event.target.value)}
              />
            </label>
          </div>
          <button className="primary-action resource-action" type="button" onClick={openCreateDialog}>
            <Plus aria-hidden="true" size={16} />
            Add Category
          </button>
        </div>

        {(categoriesQuery.isLoading || mealsQuery.isLoading) && (
          <div className="state-panel">
            <div className="spinner" />
            <strong>Loading categories</strong>
            <span>Preparing categories and meal counts.</span>
          </div>
        )}

        {(categoriesQuery.isError || mealsQuery.isError) && (
          <div className="state-panel error-state">
            <strong>Could not load categories</strong>
            <span>{getErrorMessage(categoriesQuery.error ?? mealsQuery.error)}</span>
            <button
              type="button"
              onClick={() => {
                void categoriesQuery.refetch()
                void mealsQuery.refetch()
              }}
            >
              Try again
            </button>
          </div>
        )}

        {categoriesQuery.isSuccess && mealsQuery.isSuccess && categories.length === 0 && (
          <div className="state-panel">
            <Tags aria-hidden="true" size={28} />
            <strong>No categories yet</strong>
            <span>Create categories before adding meals.</span>
          </div>
        )}

        {categoriesQuery.isSuccess &&
          mealsQuery.isSuccess &&
          categories.length > 0 &&
          filteredCategories.length === 0 && (
          <div className="state-panel">
            <Search aria-hidden="true" size={28} />
            <strong>No matches</strong>
            <span>Try another category name.</span>
          </div>
        )}

        {categoriesQuery.isSuccess && mealsQuery.isSuccess && filteredCategories.length > 0 && (
          <div className="category-card-grid">
            {filteredCategories.map((category) => {
              const mealsCount = mealsCountByCategoryId.get(category.id) ?? 0

              return (
                <article className="category-card-item" key={category.id}>
                  <div className="category-card-topline">
                    <div className="category-card-icon" aria-hidden="true">
                      {getCategoryIcon(category)}
                    </div>
                    <div className="row-actions category-card-actions">
                      <button
                        className="table-icon-button"
                        type="button"
                        aria-label={`Edit ${category.name}`}
                        onClick={() => openEditDialog(category)}
                      >
                        <Edit3 aria-hidden="true" size={15} />
                      </button>
                      <button
                        className="table-icon-button danger"
                        type="button"
                        aria-label={`Delete ${category.name}`}
                        onClick={() => setCategoryToDelete(category)}
                      >
                        <Trash2 aria-hidden="true" size={15} />
                      </button>
                    </div>
                  </div>
                  <div className="category-card-copy">
                    <strong>{category.name}</strong>
                    <span>{formatMealsCount(mealsCount)}</span>
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </article>

      {isDialogOpen && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="category-form-title">
            <div className="modal-header">
              <div>
                <p className="eyebrow">Category</p>
                <h3 id="category-form-title">
                  {editingCategory ? 'Edit category' : 'Create category'}
                </h3>
              </div>
              <button className="table-icon-button" type="button" aria-label="Close" onClick={closeDialog}>
                ×
              </button>
            </div>

            <form className="entity-form" onSubmit={handleSubmit(submitCategory)}>
              <label>
                <span>Name</span>
                <input type="text" {...register('name')} />
                {errors.name && <small>{errors.name.message}</small>}
              </label>

              {saveError && <div className="form-error">{getErrorMessage(saveError)}</div>}

              <div className="modal-actions">
                <button className="secondary-action" type="button" onClick={closeDialog}>
                  Cancel
                </button>
                <button className="primary-action" type="submit" disabled={isSaving}>
                  {isSaving ? 'Saving...' : 'Save category'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {categoryToDelete && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal-card confirm-card" role="dialog" aria-modal="true" aria-labelledby="delete-title">
            <div className="modal-header">
              <div>
                <p className="eyebrow">Confirm delete</p>
                <h3 id="delete-title">Delete category?</h3>
              </div>
            </div>
            <p>{categoryToDelete.name} will be removed from the category list.</p>
            {deleteCategoryMutation.error && (
              <div className="form-error">{getErrorMessage(deleteCategoryMutation.error)}</div>
            )}
            <div className="modal-actions">
              <button className="secondary-action" type="button" onClick={() => setCategoryToDelete(null)}>
                Cancel
              </button>
              <button
                className="primary-action danger-action"
                type="button"
                disabled={deleteCategoryMutation.isPending}
                onClick={confirmDelete}
              >
                {deleteCategoryMutation.isPending ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
