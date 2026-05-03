import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo } from 'react'

import {
  createMeal,
  deleteMeal,
  getMeal,
  getMeals,
  updateMeal,
  type CreateMealRequest,
  type MealsPageRequest,
  type UpdateMealRequest,
} from '../shared/api/meals'
import { queryKeys } from '../shared/lib/queryKeys'

type UpdateMealVariables = {
  mealId: number
  request: UpdateMealRequest
}

type QueryOptions = {
  enabled?: boolean
}

export function useMeals(request: MealsPageRequest, options: QueryOptions = {}) {
  const queryClient = useQueryClient()
  const stableRequest = useMemo<MealsPageRequest>(
    () => ({
      page: request.page,
      size: request.size,
      sortBy: request.sortBy,
      ascending: request.ascending,
    }),
    [request.ascending, request.page, request.size, request.sortBy],
  )

  const mealsQuery = useQuery({
    queryKey: queryKeys.meals(stableRequest),
    queryFn: () => getMeals(stableRequest),
    enabled: options.enabled ?? true,
  })

  const createMealMutation = useMutation({
    mutationFn: (mealRequest: CreateMealRequest) => createMeal(mealRequest),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.mealsRoot })
    },
  })

  const updateMealMutation = useMutation({
    mutationFn: ({ mealId, request: mealRequest }: UpdateMealVariables) =>
      updateMeal(mealId, mealRequest),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.mealsRoot })
    },
  })

  const deleteMealMutation = useMutation({
    mutationFn: (mealId: number) => deleteMeal(mealId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.mealsRoot })
    },
  })

  return {
    mealsQuery,
    createMealMutation,
    updateMealMutation,
    deleteMealMutation,
  }
}

export function useMeal(mealId: number | null) {
  const isEnabled = mealId !== null && mealId > 0

  return useQuery({
    queryKey: queryKeys.meal(String(mealId ?? '')),
    queryFn: () => getMeal(mealId ?? 0),
    enabled: isEnabled,
  })
}
