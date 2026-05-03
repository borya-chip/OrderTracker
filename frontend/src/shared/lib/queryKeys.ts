import type { MealsPageRequest } from '../api/meals'
import type { OrdersListRequest } from '../api/orders'

export const queryKeys = {
  customers: ['customers'] as const,
  customer: (customerId: string) => ['customers', customerId] as const,
  ordersRoot: ['orders'] as const,
  orders: (params: OrdersListRequest) =>
    [
      'orders',
      params.startDate ?? '',
      params.endDate ?? '',
      params.withEntityGraph ?? false,
    ] as const,
  order: (orderId: string) => ['orders', orderId] as const,
  mealsRoot: ['meals'] as const,
  meals: (params: MealsPageRequest) =>
    ['meals', params.page, params.size, params.sortBy, params.ascending] as const,
  meal: (mealId: string) => ['meals', mealId] as const,
  categories: ['categories'] as const,
  category: (categoryId: string) => ['categories', categoryId] as const,
  restaurants: ['restaurants'] as const,
  restaurant: (restaurantId: string) => ['restaurants', restaurantId] as const,
}
