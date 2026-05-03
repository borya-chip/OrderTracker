import { zodResolver } from '@hookform/resolvers/zod'
import { Edit3, Plus, Search, Tags, Trash2 } from 'lucide-react'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useOutletContext } from 'react-router-dom'
import { z } from 'zod'

import { Pagination } from '../components/ui/Pagination'
import { useCategories } from '../hooks/useCategories'
import type { AppLayoutContext } from '../components/layout/AppLayout'
import type {
  CategoryResponse,
  CreateCategoryRequest,
} from '../shared/api/categories'

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
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
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
  const normalizedFilter = topbarSearch.trim().toLowerCase()

  const filteredCategories = useMemo(() => {
    if (!normalizedFilter) {
      return categories
    }

    return categories.filter((category) =>
      category.name.toLowerCase().includes(normalizedFilter),
    )
  }, [categories, normalizedFilter])
  const totalPages = Math.max(1, Math.ceil(filteredCategories.length / pageSize))
  const currentPage = Math.min(page, totalPages)
  const paginatedCategories = useMemo(() => {
    const startIndex = (currentPage - 1) * pageSize
    return filteredCategories.slice(startIndex, startIndex + pageSize)
  }, [currentPage, filteredCategories, pageSize])

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

        {categoriesQuery.isLoading && (
          <div className="state-panel">
            <div className="spinner" />
            <strong>Loading categories</strong>
            <span>Preparing the category list.</span>
          </div>
        )}

        {categoriesQuery.isError && (
          <div className="state-panel error-state">
            <strong>Could not load categories</strong>
            <span>{getErrorMessage(categoriesQuery.error)}</span>
            <button type="button" onClick={() => void categoriesQuery.refetch()}>
              Try again
            </button>
          </div>
        )}

        {categoriesQuery.isSuccess && categories.length === 0 && (
          <div className="state-panel">
            <Tags aria-hidden="true" size={28} />
            <strong>No categories yet</strong>
            <span>Create categories before adding meals.</span>
          </div>
        )}

        {categoriesQuery.isSuccess && categories.length > 0 && filteredCategories.length === 0 && (
          <div className="state-panel">
            <Search aria-hidden="true" size={28} />
            <strong>No matches</strong>
            <span>Try another category name.</span>
          </div>
        )}

        {categoriesQuery.isSuccess && filteredCategories.length > 0 && (
          <>
            <div className="orders-table-wrap resource-table-wrap">
              <table className="orders-table resource-table">
                <thead>
                  <tr>
                    <th>Category</th>
                    <th>Usage</th>
                    <th>Details</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedCategories.map((category) => (
                    <tr key={category.id}>
                      <td>
                        <strong>{category.name}</strong>
                        <span>Meal category</span>
                      </td>
                      <td>
                        <span className="status-badge processing">Meals group</span>
                      </td>
                      <td className="table-link-cell">
                        <Link className="status-badge processing" to={`/categories/${category.id}`}>
                          Details
                        </Link>
                      </td>
                      <td className="table-actions-cell">
                        <div className="row-actions">
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
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <Pagination
              page={currentPage}
              pageSize={pageSize}
              totalItems={filteredCategories.length}
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
