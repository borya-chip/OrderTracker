import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useMemo } from 'react'

import {
  createOrder,
  deleteOrder,
  getOrder,
  getOrders,
  updateOrder,
  type CreateOrderRequest,
  type OrdersListRequest,
  type UpdateOrderRequest,
} from '../shared/api/orders'
import { queryKeys } from '../shared/lib/queryKeys'

type UpdateOrderVariables = {
  orderId: number
  request: UpdateOrderRequest
}

type QueryOptions = {
  enabled?: boolean
}

export function useOrders(request: OrdersListRequest, options: QueryOptions = {}) {
  const queryClient = useQueryClient()
  const stableRequest = useMemo<OrdersListRequest>(
    () => ({
      ...(request.startDate ? { startDate: request.startDate } : {}),
      ...(request.endDate ? { endDate: request.endDate } : {}),
      ...(request.withEntityGraph === undefined
        ? {}
        : { withEntityGraph: request.withEntityGraph }),
    }),
    [request.endDate, request.startDate, request.withEntityGraph],
  )

  const ordersQuery = useQuery({
    queryKey: queryKeys.orders(stableRequest),
    queryFn: () => getOrders(stableRequest),
    enabled: options.enabled ?? true,
  })

  const createOrderMutation = useMutation({
    mutationFn: (orderRequest: CreateOrderRequest) => createOrder(orderRequest),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.ordersRoot })
    },
  })

  const updateOrderMutation = useMutation({
    mutationFn: ({ orderId, request: orderRequest }: UpdateOrderVariables) =>
      updateOrder(orderId, orderRequest),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.ordersRoot })
    },
  })

  const deleteOrderMutation = useMutation({
    mutationFn: (orderId: number) => deleteOrder(orderId),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.ordersRoot })
    },
  })

  return {
    ordersQuery,
    createOrderMutation,
    updateOrderMutation,
    deleteOrderMutation,
  }
}

export function useOrder(orderId: number | null) {
  const isEnabled = orderId !== null && orderId > 0

  return useQuery({
    queryKey: queryKeys.order(String(orderId ?? '')),
    queryFn: () => getOrder(orderId ?? 0),
    enabled: isEnabled,
  })
}
