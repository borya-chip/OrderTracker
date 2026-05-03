import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import {
  createCustomer,
  deleteCustomer,
  getCustomer,
  getCustomers,
  updateCustomer,
  type CreateCustomerRequest,
  type UpdateCustomerRequest,
} from '../shared/api/customers'
import { queryKeys } from '../shared/lib/queryKeys'

type UpdateCustomerVariables = {
  customerId: number
  request: UpdateCustomerRequest
}

type QueryOptions = {
  enabled?: boolean
}

export function useCustomers(options: QueryOptions = {}) {
  const queryClient = useQueryClient()

  const customersQuery = useQuery({
    queryKey: queryKeys.customers,
    queryFn: getCustomers,
    enabled: options.enabled ?? true,
  })

  const createCustomerMutation = useMutation({
    mutationFn: (request: CreateCustomerRequest) => createCustomer(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.customers })
    },
  })

  const updateCustomerMutation = useMutation({
    mutationFn: ({ customerId, request }: UpdateCustomerVariables) =>
      updateCustomer(customerId, request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.customers })
    },
  })

  const deleteCustomerMutation = useMutation({
    mutationFn: (customerId: number) => deleteCustomer(customerId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.customers })
    },
  })

  return {
    customersQuery,
    createCustomerMutation,
    updateCustomerMutation,
    deleteCustomerMutation,
  }
}

export function useCustomer(customerId: number | null) {
  const isEnabled = customerId !== null && customerId > 0

  return useQuery({
    queryKey: queryKeys.customer(String(customerId ?? '')),
    queryFn: () => getCustomer(customerId ?? 0),
    enabled: isEnabled,
  })
}
