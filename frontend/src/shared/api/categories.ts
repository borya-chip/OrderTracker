import { httpClient } from './http'

export type CategoryResponse = {
  id: number
  name: string
}

export type CreateCategoryRequest = {
  name: string
}

export type UpdateCategoryRequest = CreateCategoryRequest

export async function getCategories(): Promise<CategoryResponse[]> {
  const response = await httpClient.get<CategoryResponse[]>('/api/v1/categories')
  return response.data
}

export async function getCategory(categoryId: number): Promise<CategoryResponse> {
  const response = await httpClient.get<CategoryResponse>(`/api/v1/categories/${categoryId}`)
  return response.data
}

export async function createCategory(
  request: CreateCategoryRequest,
): Promise<CategoryResponse> {
  const response = await httpClient.post<CategoryResponse>('/api/v1/categories', request)
  return response.data
}

export async function updateCategory(
  categoryId: number,
  request: UpdateCategoryRequest,
): Promise<CategoryResponse> {
  const response = await httpClient.put<CategoryResponse>(
    `/api/v1/categories/${categoryId}`,
    request,
  )
  return response.data
}

export async function deleteCategory(categoryId: number): Promise<void> {
  await httpClient.delete(`/api/v1/categories/${categoryId}`)
}
