import { zodResolver } from '@hookform/resolvers/zod'
import { CalendarRange, ChevronDown, ClipboardList, Edit3, Plus, Search, Trash2 } from 'lucide-react'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { Link, useOutletContext, useSearchParams } from 'react-router-dom'
import { z } from 'zod'

import { Pagination } from '../components/ui/Pagination'
import type { AppLayoutContext } from '../components/layout/AppLayout'
import { useCustomers } from '../hooks/useCustomers'
import { useMeals } from '../hooks/useMeals'
import { useOrders } from '../hooks/useOrders'
import type { MealResponse } from '../shared/api/meals'
import type {
   CreateOrderRequest,
   OrderResponse,
} from '../shared/api/orders'

const emptyOrderValues = {
   amount: 0,
   date: '',
   description: '',
   customerId: 0,
   mealIds: [] as number[],
}

const orderFormSchema = z.object({
   amount: z.number().min(0.01, 'Amount must be at least 0.01'),
   date: z.string().trim().min(1, 'Order date and time is required'),
   description: z.string().trim().min(1, 'Description is required').max(255),
   customerId: z.number().int().positive('Customer is required'),
   mealIds: z.array(z.number().int().positive()).min(1, 'Select at least one meal'),
})

type OrderFormValues = z.infer<typeof orderFormSchema>

type AppliedDateRange = {
   startDate?: string
   endDate?: string
}

type DatePreset = 'today' | 'last7Days' | 'last30Days' | 'thisMonth' | null

const ORDER_FORM_MEALS_PAGE_SIZE = 50

function getErrorMessage(error: Error | null): string {
   return error?.message ?? 'Unexpected loading error'
}

function toOrderRequest(values: OrderFormValues): CreateOrderRequest {
   return {
      amount: values.amount,
      date: values.date,
      description: values.description.trim(),
      customerId: values.customerId,
      mealIds: values.mealIds,
   }
}

function formatDateTime(value: string): string {
   if (!value) {
      return '—'
   }

   return new Intl.DateTimeFormat('en', {
      dateStyle: 'medium',
      timeStyle: 'short',
   }).format(new Date(value))
}

function formatDateTimeInput(value: string): string {
   return value ? value.slice(0, 16) : ''
}

function formatMealNames(order: OrderResponse): string {
   if (order.mealNames.length === 0) {
      return 'No meals'
   }

   return order.mealNames.join(', ')
}

function getSelectedMeals(mealOptions: MealResponse[], selectedMealIds: number[]): string {
   return mealOptions
      .filter((meal) => selectedMealIds.includes(meal.id))
      .map((meal) => meal.name)
      .join(', ')
}

function formatDateForFilter(value: Date): string {
   const year = value.getFullYear()
   const month = String(value.getMonth() + 1).padStart(2, '0')
   const day = String(value.getDate()).padStart(2, '0')

   return `${year}-${month}-${day}`
}

function shiftDate(value: Date, days: number): Date {
   const nextDate = new Date(value)
   nextDate.setDate(nextDate.getDate() + days)
   return nextDate
}

function getPresetLabel(preset: DatePreset): string {
   switch (preset) {
      case 'today':
         return 'Today'
      case 'last7Days':
         return 'Last 7 days'
      case 'last30Days':
         return 'Last 30 days'
      case 'thisMonth':
         return 'This month'
      default:
         return 'Date'
   }
}

export function OrdersPage() {
   const [searchParams, setSearchParams] = useSearchParams()
   const [page, setPage] = useState(1)
   const [pageSize, setPageSize] = useState(10)
   const [appliedDateRange, setAppliedDateRange] = useState<AppliedDateRange>({})
   const [activeDatePreset, setActiveDatePreset] = useState<DatePreset>(null)
   const [isDateMenuOpen, setIsDateMenuOpen] = useState(false)
   const [isDialogOpen, setIsDialogOpen] = useState(false)
   const [editingOrder, setEditingOrder] = useState<OrderResponse | null>(null)
   const [orderToDelete, setOrderToDelete] = useState<OrderResponse | null>(null)
   const isCreateRequested = searchParams.get('create') === '1'
   const isOrderDialogOpen = isDialogOpen || isCreateRequested
   const { topbarSearch, setTopbarSearch } = useOutletContext<AppLayoutContext>()
   const dateMenuRef = useRef<HTMLDivElement | null>(null)

   const ordersRequest = useMemo(
      () => ({
         ...appliedDateRange,
         withEntityGraph: true,
      }),
      [appliedDateRange],
   )
   const {
      ordersQuery,
      createOrderMutation,
      updateOrderMutation,
      deleteOrderMutation,
   } = useOrders(ordersRequest)
   const { customersQuery } = useCustomers({ enabled: isOrderDialogOpen })
   const mealsRequest = useMemo(
      () => ({
         page: 0,
         size: ORDER_FORM_MEALS_PAGE_SIZE,
         sortBy: 'name' as const,
         ascending: true,
      }),
      [],
   )
   const { mealsQuery } = useMeals(mealsRequest, { enabled: isOrderDialogOpen })

   const {
      control,
      register,
      handleSubmit,
      reset,
      formState: { errors },
   } = useForm<OrderFormValues>({
      resolver: zodResolver(orderFormSchema),
      defaultValues: emptyOrderValues,
   })

   const orders = useMemo(() => ordersQuery.data ?? [], [ordersQuery.data])
   const customers = useMemo(() => customersQuery.data ?? [], [customersQuery.data])
   const mealOptions = useMemo(() => mealsQuery.data?.content ?? [], [mealsQuery.data?.content])
   const watchedMealIds = useWatch({ control, name: 'mealIds' })
   const selectedMealIds = useMemo(() => watchedMealIds ?? [], [watchedMealIds])
   const selectedMealSummary = useMemo(
      () => getSelectedMeals(mealOptions, selectedMealIds),
      [mealOptions, selectedMealIds],
   )
   const normalizedSearch = topbarSearch.trim().toLowerCase()

   const filteredOrders = useMemo(() => {
      if (!normalizedSearch) {
         return orders
      }

      return orders.filter((order) => {
         const searchable = [
            order.customerName,
            order.description,
            order.mealNames.join(' '),
         ].join(' ').toLowerCase()

         return searchable.includes(normalizedSearch)
      })
   }, [orders, normalizedSearch])
   const totalPages = Math.max(1, Math.ceil(filteredOrders.length / pageSize))
   const currentPage = Math.min(page, totalPages)
   const paginatedOrders = useMemo(() => {
      const startIndex = (currentPage - 1) * pageSize
      return filteredOrders.slice(startIndex, startIndex + pageSize)
   }, [currentPage, filteredOrders, pageSize])
   const supportingDataLoading = customersQuery.isLoading || mealsQuery.isLoading
   const supportingDataError = customersQuery.error ?? mealsQuery.error
   const isSaving = createOrderMutation.isPending || updateOrderMutation.isPending
   const saveError = createOrderMutation.error ?? updateOrderMutation.error
   useEffect(() => {
      if (!isDateMenuOpen) {
         return undefined
      }

      function handlePointerDown(event: MouseEvent) {
         if (!dateMenuRef.current || !(event.target instanceof Node)) {
            return
         }

         if (!dateMenuRef.current.contains(event.target)) {
            setIsDateMenuOpen(false)
         }
      }

      document.addEventListener('mousedown', handlePointerDown)

      return () => {
         document.removeEventListener('mousedown', handlePointerDown)
      }
   }, [isDateMenuOpen])

   function openCreateDialog() {
      setEditingOrder(null)
      reset(emptyOrderValues)
      setIsDialogOpen(true)
   }

   function openEditDialog(order: OrderResponse) {
      setEditingOrder(order)
      reset({
         amount: Number(order.amount),
         date: formatDateTimeInput(order.date),
         description: order.description,
         customerId: order.customerId,
         mealIds: order.mealIds,
      })
      setIsDialogOpen(true)
   }

   function closeDialog() {
      setIsDialogOpen(false)
      setEditingOrder(null)
      reset(emptyOrderValues)
      if (isCreateRequested) {
         setSearchParams({})
      }
   }

   function submitOrder(values: OrderFormValues) {
      const request = toOrderRequest(values)

      if (editingOrder) {
         updateOrderMutation.mutate(
            { orderId: editingOrder.id, request },
            { onSuccess: closeDialog },
         )
         return
      }

      createOrderMutation.mutate(request, { onSuccess: closeDialog })
   }

   function confirmDelete() {
      if (!orderToDelete) {
         return
      }

      deleteOrderMutation.mutate(orderToDelete.id, {
         onSuccess: () => setOrderToDelete(null),
      })
   }

   function clearDateFilter() {
      setAppliedDateRange({})
      setActiveDatePreset(null)
      setPage(1)
      setIsDateMenuOpen(false)
   }

   function applyDatePreset(preset: Exclude<DatePreset, null>) {
      const today = new Date()
      const endDate = formatDateForFilter(today)
      let startDate = endDate

      if (preset === 'last7Days') {
         startDate = formatDateForFilter(shiftDate(today, -7))
      } else if (preset === 'last30Days') {
         startDate = formatDateForFilter(shiftDate(today, -30))
      } else if (preset === 'thisMonth') {
         startDate = formatDateForFilter(new Date(today.getFullYear(), today.getMonth(), 1))
      }

      setAppliedDateRange({ startDate, endDate })
      setActiveDatePreset(preset)
      setPage(1)
      setIsDateMenuOpen(false)
   }

   return (
      <section className="resource-page">
         <article className="dashboard-card resource-card">
            <div className="resource-toolbar">
               <div className="resource-toolbar-main">
                  <div className="filter-controls resource-filters">
                     <label className="resource-search compact-search" aria-label="Search orders">
                        <Search aria-hidden="true" size={17} />
                        <input
                           placeholder="Search orders"
                           type="search"
                           value={topbarSearch}
                           onChange={(event) => setTopbarSearch(event.target.value)}
                        />
                     </label>
                     <div className="toolbar-menu" ref={dateMenuRef}>
                        <button
                           className="toolbar-menu-trigger"
                           type="button"
                           aria-haspopup="menu"
                           aria-expanded={isDateMenuOpen}
                           onClick={() => setIsDateMenuOpen((current) => !current)}
                        >
                           <CalendarRange aria-hidden="true" size={16} />
                           <span>{getPresetLabel(activeDatePreset)}</span>
                           <ChevronDown aria-hidden="true" className="toolbar-select-caret" size={16} />
                        </button>
                        {isDateMenuOpen && (
                           <div className="toolbar-menu-panel" role="menu" aria-label="Date presets">
                              <button className="toolbar-menu-item" type="button" role="menuitem" onClick={() => applyDatePreset('today')}>
                                 Today
                              </button>
                              <button className="toolbar-menu-item" type="button" role="menuitem" onClick={() => applyDatePreset('last7Days')}>
                                 Last 7 days
                              </button>
                              <button className="toolbar-menu-item" type="button" role="menuitem" onClick={() => applyDatePreset('last30Days')}>
                                 Last 30 days
                              </button>
                              <button className="toolbar-menu-item" type="button" role="menuitem" onClick={() => applyDatePreset('thisMonth')}>
                                 This month
                              </button>
                              <button className="toolbar-menu-item danger" type="button" role="menuitem" onClick={clearDateFilter}>
                                 Clear
                              </button>
                           </div>
                        )}
                     </div>
                  </div>
               </div>
               <div className="resource-toolbar-end">
                  <button
                     className="primary-action resource-action toolbar-primary-action"
                     type="button"
                     onClick={openCreateDialog}
                  >
                     <Plus aria-hidden="true" size={16} />
                     Create Order
                  </button>
               </div>
            </div>

            {ordersQuery.isLoading && (
               <div className="state-panel">
                  <div className="spinner" />
                  <strong>Loading orders</strong>
                  <span>Preparing the order list.</span>
               </div>
            )}

            {ordersQuery.isError && (
               <div className="state-panel error-state">
                  <strong>Could not load orders</strong>
                  <span>{getErrorMessage(ordersQuery.error)}</span>
                  <button type="button" onClick={() => void ordersQuery.refetch()}>
                     Try again
                  </button>
               </div>
            )}

            {ordersQuery.isSuccess && orders.length === 0 && (
               <div className="state-panel">
                  <ClipboardList aria-hidden="true" size={28} />
                  <strong>No orders yet</strong>
                  <span>Create an order by choosing a customer and one or more meals.</span>
               </div>
            )}

            {ordersQuery.isSuccess && orders.length > 0 && filteredOrders.length === 0 && (
               <div className="state-panel">
                  <Search aria-hidden="true" size={28} />
                  <strong>No matches</strong>
                  <span>Try another customer, description, or meal name.</span>
               </div>
            )}

            {ordersQuery.isSuccess && filteredOrders.length > 0 && (
               <>
                  <div className="orders-table-wrap resource-table-wrap">
                     <table className="orders-table resource-table">
                        <thead>
                           <tr>
                              <th>Customer</th>
                              <th>Meals</th>
                              <th>Amount</th>
                              <th>Date</th>
                              <th>Description</th>
                              <th>Actions</th>
                           </tr>
                        </thead>
                        <tbody>
                           {paginatedOrders.map((order) => (
                              <tr key={order.id}>
                                 <td>
                                    <strong>{order.customerName}</strong>
                                    <span>{formatDateTime(order.date)}</span>
                                 </td>
                                 <td>
                                    <strong>{order.mealNames.length} meals</strong>
                                    <span>{formatMealNames(order)}</span>
                                 </td>
                                 <td className="amount-cell">${Number(order.amount).toFixed(2)}</td>
                                 <td>{formatDateTime(order.date)}</td>
                                 <td className="table-copy-cell">{order.description}</td>
                                 <td className="table-actions-cell">
                                    <div className="row-actions">
                                       <Link className="status-badge processing" to={`/orders/${order.id}`}>
                                          Details
                                       </Link>
                                       <button
                                          className="table-icon-button"
                                          type="button"
                                          aria-label={`Edit order for ${order.customerName}`}
                                          onClick={() => openEditDialog(order)}
                                       >
                                          <Edit3 aria-hidden="true" size={15} />
                                       </button>
                                       <button
                                          className="table-icon-button danger"
                                          type="button"
                                          aria-label={`Delete order for ${order.customerName}`}
                                          onClick={() => setOrderToDelete(order)}
                                       >
                                          <Trash2 aria-hidden="true" size={15} />
                                       </button>
                                    </div>
                                 </td>
                              </tr>
                           ))}
                        </tbody>
                     </table>
                  </div>

                  <Pagination
                     page={currentPage}
                     pageSize={pageSize}
                     totalItems={filteredOrders.length}
                     onPageChange={setPage}
                     onPageSizeChange={(nextPageSize) => {
                        setPageSize(nextPageSize)
                        setPage(1)
                     }}
                  />
               </>
            )}
         </article>

         {isOrderDialogOpen && (
            <div className="modal-backdrop" role="presentation">
               <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="order-form-title">
                  <div className="modal-header">
                     <div>
                        <p className="eyebrow">Order</p>
                        <h3 id="order-form-title">{editingOrder ? 'Edit order' : 'Create order'}</h3>
                     </div>
                     <button className="table-icon-button" type="button" aria-label="Close" onClick={closeDialog}>
                        ×
                     </button>
                  </div>

                  {supportingDataError && (
                     <div className="form-error">{getErrorMessage(supportingDataError)}</div>
                  )}

                  <form className="entity-form" onSubmit={handleSubmit(submitOrder)}>
                     <label>
                        <span>Customer</span>
                        <select disabled={supportingDataLoading} {...register('customerId', { valueAsNumber: true })}>
                           <option value={0}>Select customer</option>
                           {customers.map((customer) => (
                              <option key={customer.id} value={customer.id}>
                                 {customer.firstName} {customer.lastName}
                              </option>
                           ))}
                        </select>
                        {errors.customerId && <small>{errors.customerId.message}</small>}
                     </label>

                     <label>
                        <span>Meals</span>
                        <Controller
                           name="mealIds"
                           control={control}
                           render={({ field }) => (
                              <select
                                 multiple
                                 className="multi-select"
                                 disabled={supportingDataLoading}
                                 value={field.value.map(String)}
                                 onBlur={field.onBlur}
                                 onChange={(event) => {
                                    const selectedValues = Array.from(event.currentTarget.selectedOptions)
                                       .map((option) => Number(option.value))
                                    field.onChange(selectedValues)
                                 }}
                              >
                                 {mealOptions.map((meal) => (
                                    <option key={meal.id} value={meal.id}>
                                       {meal.name} · ${Number(meal.price).toFixed(2)}
                                    </option>
                                 ))}
                              </select>
                           )}
                        />
                        {selectedMealSummary && <span className="field-hint">{selectedMealSummary}</span>}
                        {errors.mealIds && <small>{errors.mealIds.message}</small>}
                     </label>

                     <label>
                        <span>Amount</span>
                        <input type="number" min="0.01" step="0.01" {...register('amount', { valueAsNumber: true })} />
                        {errors.amount && <small>{errors.amount.message}</small>}
                     </label>

                     <label>
                        <span>Date and time</span>
                        <input type="datetime-local" {...register('date')} />
                        {errors.date && <small>{errors.date.message}</small>}
                     </label>

                     <label>
                        <span>Description</span>
                        <input type="text" {...register('description')} />
                        {errors.description && <small>{errors.description.message}</small>}
                     </label>

                     {saveError && <div className="form-error">{getErrorMessage(saveError)}</div>}

                     <div className="modal-actions">
                        <button className="secondary-action" type="button" onClick={closeDialog}>
                           Cancel
                        </button>
                        <button
                           className="primary-action"
                           type="submit"
                           disabled={isSaving || supportingDataLoading}
                        >
                           {isSaving ? 'Saving...' : 'Save order'}
                        </button>
                     </div>
                  </form>
               </div>
            </div>
         )}

         {orderToDelete && (
            <div className="modal-backdrop" role="presentation">
               <div className="modal-card confirm-card" role="dialog" aria-modal="true" aria-labelledby="delete-title">
                  <div className="modal-header">
                     <div>
                        <p className="eyebrow">Confirm delete</p>
                        <h3 id="delete-title">Delete order?</h3>
                     </div>
                  </div>
                  <p>Order for {orderToDelete.customerName} on {formatDateTime(orderToDelete.date)} will be removed.</p>
                  {deleteOrderMutation.error && (
                     <div className="form-error">{getErrorMessage(deleteOrderMutation.error)}</div>
                  )}
                  <div className="modal-actions">
                     <button className="secondary-action" type="button" onClick={() => setOrderToDelete(null)}>
                        Cancel
                     </button>
                     <button
                        className="primary-action danger-action"
                        type="button"
                        disabled={deleteOrderMutation.isPending}
                        onClick={confirmDelete}
                     >
                        {deleteOrderMutation.isPending ? 'Deleting...' : 'Delete'}
                     </button>
                  </div>
               </div>
            </div>
         )}
      </section>
   )
}
