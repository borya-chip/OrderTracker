import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import {
  createCategory,
  deleteCategory,
  getCategory,
  getCategories,
  updateCategory,
  type CreateCategoryRequest,
  type UpdateCategoryRequest,
} from '../shared/api/categories'
import { queryKeys } from '../shared/lib/queryKeys'

type UpdateCategoryVariables = {
  categoryId: number
  request: UpdateCategoryRequest
}

type QueryOptions = {
  enabled?: boolean
}

export function useCategories(options: QueryOptions = {}) {
  const queryClient = useQueryClient()

  const categoriesQuery = useQuery({
    queryKey: queryKeys.categories,
    queryFn: getCategories,
    enabled: options.enabled ?? true,
  })

  const createCategoryMutation = useMutation({
    mutationFn: (request: CreateCategoryRequest) => createCategory(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.categories })
    },
  })

  const updateCategoryMutation = useMutation({
    mutationFn: ({ categoryId, request }: UpdateCategoryVariables) =>
      updateCategory(categoryId, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.categories })
    },
  })

  const deleteCategoryMutation = useMutation({
    mutationFn: (categoryId: number) => deleteCategory(categoryId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.categories })
    },
  })

  return {
    categoriesQuery,
    createCategoryMutation,
    updateCategoryMutation,
    deleteCategoryMutation,
  }
}

export function useCategory(categoryId: number | null) {
  const isEnabled = categoryId !== null && categoryId > 0

  return useQuery({
    queryKey: queryKeys.category(String(categoryId ?? '')),
    queryFn: () => getCategory(categoryId ?? 0),
    enabled: isEnabled,
  })
}
