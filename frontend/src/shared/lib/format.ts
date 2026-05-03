export function formatPageTitle(title: string): string {
  return `${title} | Order Tracker`
}

export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('en', {
    style: 'currency',
    currency: 'USD',
  }).format(value)
}

export function formatDateTime(value: string): string {
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}
