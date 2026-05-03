import { createBrowserRouter, Navigate } from 'react-router-dom'

import { AppLayout } from '../components/layout/AppLayout'
import { CategoriesPage } from '../pages/CategoriesPage'
import { CategoryDetailsPage } from '../pages/CategoryDetailsPage'
import { CustomerDetailsPage } from '../pages/CustomerDetailsPage'
import { CustomersPage } from '../pages/CustomersPage'
import { DashboardPage } from '../pages/DashboardPage'
import { MealDetailsPage } from '../pages/MealDetailsPage'
import { MealsPage } from '../pages/MealsPage'
import { OrderDetailsPage } from '../pages/OrderDetailsPage'
import { OrdersPage } from '../pages/OrdersPage'
import { RestaurantDetailsPage } from '../pages/RestaurantDetailsPage'
import { RestaurantsPage } from '../pages/RestaurantsPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'customers', element: <CustomersPage /> },
      { path: 'customers/:customerId', element: <CustomerDetailsPage /> },
      { path: 'orders', element: <OrdersPage /> },
      { path: 'orders/new', element: <Navigate to="/orders?create=1" replace /> },
      { path: 'orders/:orderId', element: <OrderDetailsPage /> },
      { path: 'orders/:orderId/edit', element: <Navigate to="/orders" replace /> },
      { path: 'meals', element: <MealsPage /> },
      { path: 'meals/new', element: <Navigate to="/meals?create=1" replace /> },
      { path: 'meals/:mealId', element: <MealDetailsPage /> },
      { path: 'meals/:mealId/edit', element: <Navigate to="/meals" replace /> },
      { path: 'categories', element: <CategoriesPage /> },
      { path: 'categories/:categoryId', element: <CategoryDetailsPage /> },
      { path: 'restaurants', element: <RestaurantsPage /> },
      { path: 'restaurants/:restaurantId', element: <RestaurantDetailsPage /> },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])
