import { httpClient } from './http'

export type RestaurantResponse = {
  id: number
  name: string
  contactEmail: string | null
  city: string | null
  address: string | null
  phone: string | null
  active: boolean | null
}

export type CreateRestaurantRequest = {
  name: string
  contactEmail?: string
  city?: string
  address?: string
  phone?: string
  active?: boolean
}

export type UpdateRestaurantRequest = CreateRestaurantRequest

export type RestaurantSearchMode = 'jpql' | 'native'

export type RestaurantCategorySearchRequest = {
  categoryName: string
  minMealPrice: string
  maxMealPrice: string
  mode: RestaurantSearchMode
}

export async function getRestaurants(): Promise<RestaurantResponse[]> {
  const response = await httpClient.get<RestaurantResponse[]>('/api/v1/restaurants')
  return response.data
}

export async function getRestaurant(restaurantId: number): Promise<RestaurantResponse> {
  const response = await httpClient.get<RestaurantResponse>(
    `/api/v1/restaurants/${restaurantId}`,
  )
  return response.data
}

export async function createRestaurant(
  request: CreateRestaurantRequest,
): Promise<RestaurantResponse> {
  const response = await httpClient.post<RestaurantResponse>('/api/v1/restaurants', request)
  return response.data
}

export async function updateRestaurant(
  restaurantId: number,
  request: UpdateRestaurantRequest,
): Promise<RestaurantResponse> {
  const response = await httpClient.put<RestaurantResponse>(
    `/api/v1/restaurants/${restaurantId}`,
    request,
  )
  return response.data
}

export async function deleteRestaurant(restaurantId: number): Promise<void> {
  await httpClient.delete(`/api/v1/restaurants/${restaurantId}`)
}

export async function searchRestaurantsByCategory(
  request: RestaurantCategorySearchRequest,
): Promise<RestaurantResponse[]> {
  const searchParams = new URLSearchParams({
    categoryName: request.categoryName,
    minMealPrice: request.minMealPrice,
    maxMealPrice: request.maxMealPrice,
  })
  const response = await httpClient.get<RestaurantResponse[]>(
    `/api/v1/restaurants/search/category/${request.mode}?${searchParams.toString()}`,
  )
  return response.data
}
