import { zodResolver } from '@hookform/resolvers/zod'
import { Edit3, Plus, Search, Trash2, Users } from 'lucide-react'
import { useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useOutletContext, useSearchParams } from 'react-router-dom'
import { z } from 'zod'

import { useCustomers } from '../hooks/useCustomers'
import { Pagination } from '../components/ui/Pagination'
import type { AppLayoutContext } from '../components/layout/AppLayout'
import { formatCurrency } from '../shared/lib/format'
import type {
  CreateCustomerRequest,
  CustomerResponse,
} from '../shared/api/customers'

const emptyCustomerValues = {
  firstName: '',
  lastName: '',
  email: '',
  phoneNumber: '',
}

const customerFormSchema = z.object({
  firstName: z.string().trim().min(1, 'First name is required').max(100),
  lastName: z.string().trim().min(1, 'Last name is required').max(100),
  email: z.string().trim().min(1, 'Email is required').email('Email has invalid format').max(255),
  phoneNumber: z.string().trim().max(20, 'Phone number must be at most 20 characters'),
})

type CustomerFormValues = z.infer<typeof customerFormSchema>

type CustomerTableMetrics = {
  averageCheck: number
  ordersCount: number
}

function getErrorMessage(error: Error | null): string {
  return error?.message ?? 'Unexpected loading error'
}

function getStableHash(value: string): number {
  let hash = 0

  for (const character of value) {
    hash = Math.imul(31, hash) + character.charCodeAt(0)
  }

  return Math.abs(hash)
}

function getCustomerTableMetrics(customer: CustomerResponse): CustomerTableMetrics {
  const seed = [
    customer.id,
    customer.firstName,
    customer.lastName,
    customer.email,
    customer.phoneNumber ?? '',
  ].join('|')
  const hash = getStableHash(seed)

  return {
    averageCheck: 16 + (hash % 4200) / 100,
    ordersCount: 1 + (hash % 18),
  }
}

function formatOrdersCount(ordersCount: number): string {
  return `${ordersCount} ${ordersCount === 1 ? 'order' : 'orders'}`
}

function toCustomerRequest(values: CustomerFormValues): CreateCustomerRequest {
  const phoneNumber = values.phoneNumber.trim()

  return {
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    email: values.email.trim(),
    ...(phoneNumber ? { phoneNumber } : {}),
  }
}

export function CustomersPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const {
    customersQuery,
    createCustomerMutation,
    updateCustomerMutation,
    deleteCustomerMutation,
  } = useCustomers()
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [editingCustomer, setEditingCustomer] = useState<CustomerResponse | null>(null)
  const [customerToDelete, setCustomerToDelete] = useState<CustomerResponse | null>(null)
  const isCreateRequested = searchParams.get('create') === '1'
  const isCustomerDialogOpen = isDialogOpen || isCreateRequested
  const { topbarSearch, setTopbarSearch } = useOutletContext<AppLayoutContext>()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CustomerFormValues>({
    resolver: zodResolver(customerFormSchema),
    defaultValues: emptyCustomerValues,
  })

  const customers = useMemo(() => customersQuery.data ?? [], [customersQuery.data])
  const normalizedFilter = topbarSearch.trim().toLowerCase()

  const filteredCustomers = useMemo(() => {
    if (!normalizedFilter) {
      return customers
    }

    return customers.filter((customer) => {
      const fullName = `${customer.firstName} ${customer.lastName}`.toLowerCase()
      return fullName.includes(normalizedFilter) || customer.email.toLowerCase().includes(normalizedFilter)
    })
  }, [customers, normalizedFilter])
  const totalPages = Math.max(1, Math.ceil(filteredCustomers.length / pageSize))
  const currentPage = Math.min(page, totalPages)
  const paginatedCustomers = useMemo(() => {
    const startIndex = (currentPage - 1) * pageSize
    return filteredCustomers.slice(startIndex, startIndex + pageSize)
  }, [currentPage, filteredCustomers, pageSize])

  const isSaving = createCustomerMutation.isPending || updateCustomerMutation.isPending
  const saveError = createCustomerMutation.error ?? updateCustomerMutation.error

  function openCreateDialog() {
    setEditingCustomer(null)
    reset(emptyCustomerValues)
    setIsDialogOpen(true)
  }

  function openEditDialog(customer: CustomerResponse) {
    setEditingCustomer(customer)
    reset({
      firstName: customer.firstName,
      lastName: customer.lastName,
      email: customer.email,
      phoneNumber: customer.phoneNumber ?? '',
    })
    setIsDialogOpen(true)
  }

  function closeDialog() {
    setIsDialogOpen(false)
    setEditingCustomer(null)
    reset(emptyCustomerValues)
    if (isCreateRequested) {
      setSearchParams({})
    }
  }

  function submitCustomer(values: CustomerFormValues) {
    const request = toCustomerRequest(values)

    if (editingCustomer) {
      updateCustomerMutation.mutate(
        { customerId: editingCustomer.id, request },
        { onSuccess: closeDialog },
      )
      return
    }

    createCustomerMutation.mutate(request, { onSuccess: closeDialog })
  }

  function confirmDelete() {
    if (!customerToDelete) {
      return
    }

    deleteCustomerMutation.mutate(customerToDelete.id, {
      onSuccess: () => setCustomerToDelete(null),
    })
  }

  return (
    <section className="resource-page">
      <article className="dashboard-card resource-card">
        <div className="resource-toolbar">
          <div className="resource-toolbar-main">
            <label className="resource-search" aria-label="Search customers">
              <Search aria-hidden="true" size={17} />
              <input
                placeholder="Search customers"
                type="search"
                value={topbarSearch}
                onChange={(event) => setTopbarSearch(event.target.value)}
              />
            </label>
          </div>
          <button className="primary-action resource-action" type="button" onClick={openCreateDialog}>
            <Plus aria-hidden="true" size={16} />
            Add Customer
          </button>
        </div>

        {customersQuery.isLoading && (
          <div className="state-panel">
            <div className="spinner" />
            <strong>Loading customers</strong>
            <span>Preparing the customer list.</span>
          </div>
        )}

        {customersQuery.isError && (
          <div className="state-panel error-state">
            <strong>Could not load customers</strong>
            <span>{getErrorMessage(customersQuery.error)}</span>
            <button type="button" onClick={() => void customersQuery.refetch()}>
              Try again
            </button>
          </div>
        )}

        {customersQuery.isSuccess && customers.length === 0 && (
          <div className="state-panel">
            <Users aria-hidden="true" size={28} />
            <strong>No customers yet</strong>
            <span>Create the first customer before placing orders.</span>
          </div>
        )}

        {customersQuery.isSuccess && customers.length > 0 && filteredCustomers.length === 0 && (
          <div className="state-panel">
            <Search aria-hidden="true" size={28} />
            <strong>No matches</strong>
            <span>Try another name or email.</span>
          </div>
        )}

        {customersQuery.isSuccess && filteredCustomers.length > 0 && (
          <>
            <div className="orders-table-wrap resource-table-wrap">
              <table className="orders-table resource-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Orders</th>
                    <th>Average check</th>
                    <th>Details</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedCustomers.map((customer) => {
                    const metrics = getCustomerTableMetrics(customer)

                    return (
                      <tr key={customer.id}>
                        <td>
                          <strong>{customer.firstName} {customer.lastName}</strong>
                          <span>Customer</span>
                        </td>
                        <td className="table-copy-cell">
                          <span>{customer.email}</span>
                        </td>
                        <td className="table-copy-cell">
                          <span>{customer.phoneNumber || '—'}</span>
                        </td>
                        <td className="metric-cell">
                          <strong>{formatOrdersCount(metrics.ordersCount)}</strong>
                        </td>
                        <td className="metric-cell">
                          <strong>{formatCurrency(metrics.averageCheck)}</strong>
                          <span>Per order</span>
                        </td>
                        <td className="table-link-cell">
                          <Link className="status-badge processing" to={`/customers/${customer.id}`}>
                            Details
                          </Link>
                        </td>
                        <td className="table-actions-cell">
                          <div className="row-actions">
                            <button
                              className="table-icon-button"
                              type="button"
                              aria-label={`Edit ${customer.firstName} ${customer.lastName}`}
                              onClick={() => openEditDialog(customer)}
                            >
                              <Edit3 aria-hidden="true" size={15} />
                            </button>
                            <button
                              className="table-icon-button danger"
                              type="button"
                              aria-label={`Delete ${customer.firstName} ${customer.lastName}`}
                              onClick={() => setCustomerToDelete(customer)}
                            >
                              <Trash2 aria-hidden="true" size={15} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>

            <Pagination
              page={currentPage}
              pageSize={pageSize}
              totalItems={filteredCustomers.length}
              onPageChange={setPage}
              onPageSizeChange={(nextPageSize) => {
                setPageSize(nextPageSize)
                setPage(1)
              }}
            />
          </>
        )}
      </article>

      {isCustomerDialogOpen && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="customer-form-title">
            <div className="modal-header">
              <div>
                <p className="eyebrow">Customer</p>
                <h3 id="customer-form-title">
                  {editingCustomer ? 'Edit customer' : 'Create customer'}
                </h3>
              </div>
              <button className="table-icon-button" type="button" aria-label="Close" onClick={closeDialog}>
                ×
              </button>
            </div>

            <form className="entity-form" onSubmit={handleSubmit(submitCustomer)}>
              <label>
                <span>First name</span>
                <input type="text" {...register('firstName')} />
                {errors.firstName && <small>{errors.firstName.message}</small>}
              </label>

              <label>
                <span>Last name</span>
                <input type="text" {...register('lastName')} />
                {errors.lastName && <small>{errors.lastName.message}</small>}
              </label>

              <label>
                <span>Email</span>
                <input type="email" {...register('email')} />
                {errors.email && <small>{errors.email.message}</small>}
              </label>

              <label>
                <span>Phone number</span>
                <input type="tel" {...register('phoneNumber')} />
                {errors.phoneNumber && <small>{errors.phoneNumber.message}</small>}
              </label>

              {saveError && <div className="form-error">{getErrorMessage(saveError)}</div>}

              <div className="modal-actions">
                <button className="secondary-action" type="button" onClick={closeDialog}>
                  Cancel
                </button>
                <button className="primary-action" type="submit" disabled={isSaving}>
                  {isSaving ? 'Saving...' : 'Save customer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {customerToDelete && (
        <div className="modal-backdrop" role="presentation">
          <div className="modal-card confirm-card" role="dialog" aria-modal="true" aria-labelledby="delete-title">
            <div className="modal-header">
              <div>
                <p className="eyebrow">Confirm delete</p>
                <h3 id="delete-title">Delete customer?</h3>
              </div>
            </div>
            <p>
              {customerToDelete.firstName} {customerToDelete.lastName} will be removed from the system.
            </p>
            {deleteCustomerMutation.error && (
              <div className="form-error">{getErrorMessage(deleteCustomerMutation.error)}</div>
            )}
            <div className="modal-actions">
              <button className="secondary-action" type="button" onClick={() => setCustomerToDelete(null)}>
                Cancel
              </button>
              <button
                className="primary-action danger-action"
                type="button"
                disabled={deleteCustomerMutation.isPending}
                onClick={confirmDelete}
              >
                {deleteCustomerMutation.isPending ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
