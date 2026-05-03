import { httpClient } from './http'

export type OrderResponse = {
  id: number
  amount: number
  date: string
  description: string
  customerId: number
  customerName: string
  mealIds: number[]
  mealNames: string[]
}

export type CreateOrderRequest = {
  amount: number
  date: string
  description: string
  customerId: number
  mealIds: number[]
}

export type UpdateOrderRequest = {
  amount?: number
  date?: string
  description?: string
  customerId?: number
  mealIds?: number[]
}

export type OrdersListRequest = {
  startDate?: string
  endDate?: string
  withEntityGraph?: boolean
}

export async function getOrders(request: OrdersListRequest = {}): Promise<OrderResponse[]> {
  const response = await httpClient.get<OrderResponse[]>('/api/v1/orders', {
    params: request,
  })
  return response.data
}

export async function getOrder(orderId: number): Promise<OrderResponse> {
  const response = await httpClient.get<OrderResponse>(`/api/v1/orders/${orderId}`)
  return response.data
}

export async function createOrder(request: CreateOrderRequest): Promise<OrderResponse> {
  const response = await httpClient.post<OrderResponse>('/api/v1/orders', request)
  return response.data
}

export async function updateOrder(
  orderId: number,
  request: UpdateOrderRequest,
): Promise<OrderResponse> {
  const response = await httpClient.patch<OrderResponse>(`/api/v1/orders/${orderId}`, request)
  return response.data
}

export async function deleteOrder(orderId: number): Promise<void> {
  await httpClient.delete(`/api/v1/orders/${orderId}`)
}
