import { httpClient } from './http'

export type MealResponse = {
  id: number
  name: string
  price: number
  cookingTime: number
  categoryId: number
  categoryName: string
  restaurantId: number
  restaurantName: string
}

export type CreateMealRequest = {
  name: string
  price: number
  cookingTime: number
  categoryId: number
  restaurantId: number
}

export type UpdateMealRequest = CreateMealRequest

export type MealSortField = 'id' | 'name' | 'price' | 'cookingTime'

export type MealsPageRequest = {
  page: number
  size: number
  sortBy: MealSortField
  ascending: boolean
}

export type PageResponse<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export async function getMeals(request: MealsPageRequest): Promise<PageResponse<MealResponse>> {
  const response = await httpClient.get<PageResponse<MealResponse>>('/api/v1/meals', {
    params: request,
  })
  return response.data
}

export async function getMeal(mealId: number): Promise<MealResponse> {
  const response = await httpClient.get<MealResponse>(`/api/v1/meals/${mealId}`)
  return response.data
}

export async function createMeal(request: CreateMealRequest): Promise<MealResponse> {
  const response = await httpClient.post<MealResponse>('/api/v1/meals', request)
  return response.data
}

export async function updateMeal(
  mealId: number,
  request: UpdateMealRequest,
): Promise<MealResponse> {
  const response = await httpClient.put<MealResponse>(`/api/v1/meals/${mealId}`, request)
  return response.data
}

export async function deleteMeal(mealId: number): Promise<void> {
  await httpClient.delete(`/api/v1/meals/${mealId}`)
}
