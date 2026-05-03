import { httpClient } from './http'

export type CustomerResponse = {
  id: number
  firstName: string
  lastName: string
  email: string
  phoneNumber: string | null
}

export type CreateCustomerRequest = {
  firstName: string
  lastName: string
  email: string
  phoneNumber?: string
}

export type UpdateCustomerRequest = CreateCustomerRequest

export async function getCustomers(): Promise<CustomerResponse[]> {
  const response = await httpClient.get<CustomerResponse[]>('/api/v1/customers')
  return response.data
}

export async function getCustomer(customerId: number): Promise<CustomerResponse> {
  const response = await httpClient.get<CustomerResponse>(`/api/v1/customers/${customerId}`)
  return response.data
}

export async function createCustomer(
  request: CreateCustomerRequest,
): Promise<CustomerResponse> {
  const response = await httpClient.post<CustomerResponse>('/api/v1/customers', request)
  return response.data
}

export async function updateCustomer(
  customerId: number,
  request: UpdateCustomerRequest,
): Promise<CustomerResponse> {
  const response = await httpClient.put<CustomerResponse>(
    `/api/v1/customers/${customerId}`,
    request,
  )
  return response.data
}

export async function deleteCustomer(customerId: number): Promise<void> {
  await httpClient.delete(`/api/v1/customers/${customerId}`)
}
