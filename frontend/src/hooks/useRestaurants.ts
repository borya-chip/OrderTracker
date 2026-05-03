import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import {
  createRestaurant,
  deleteRestaurant,
  getRestaurant,
  getRestaurants,
  searchRestaurantsByCategory,
  updateRestaurant,
  type CreateRestaurantRequest,
  type RestaurantCategorySearchRequest,
  type UpdateRestaurantRequest,
} from '../shared/api/restaurants'
import { queryKeys } from '../shared/lib/queryKeys'

type UpdateRestaurantVariables = {
  restaurantId: number
  request: UpdateRestaurantRequest
}

type QueryOptions = {
  enabled?: boolean
}

export function useRestaurants(options: QueryOptions = {}) {
  const queryClient = useQueryClient()

  const restaurantsQuery = useQuery({
    queryKey: queryKeys.restaurants,
    queryFn: getRestaurants,
    enabled: options.enabled ?? true,
  })

  const createRestaurantMutation = useMutation({
    mutationFn: (request: CreateRestaurantRequest) => createRestaurant(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.restaurants })
    },
  })

  const updateRestaurantMutation = useMutation({
    mutationFn: ({ restaurantId, request }: UpdateRestaurantVariables) =>
      updateRestaurant(restaurantId, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.restaurants })
    },
  })

  const deleteRestaurantMutation = useMutation({
    mutationFn: (restaurantId: number) => deleteRestaurant(restaurantId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.restaurants })
    },
  })

  const searchRestaurantsMutation = useMutation({
    mutationFn: (request: RestaurantCategorySearchRequest) =>
      searchRestaurantsByCategory(request),
  })

  return {
    restaurantsQuery,
    createRestaurantMutation,
    updateRestaurantMutation,
    deleteRestaurantMutation,
    searchRestaurantsMutation,
  }
}

export function useRestaurant(restaurantId: number | null) {
  const isEnabled = restaurantId !== null && restaurantId > 0

  return useQuery({
    queryKey: queryKeys.restaurant(String(restaurantId ?? '')),
    queryFn: () => getRestaurant(restaurantId ?? 0),
    enabled: isEnabled,
  })
}
