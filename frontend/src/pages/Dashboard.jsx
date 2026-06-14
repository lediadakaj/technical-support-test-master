import { useEffect, useState } from 'react'
import { fetchJson, formatMoney } from '../api'

export default function Dashboard() {
  const [stats, setStats] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    fetchJson('/api/dashboard')
      .then(setStats)
      .catch(() => setError(true))
  }, [])

  if (error) return <p className="error-banner">Could not load dashboard data. Please try again later.</p>
  if (!stats) return <p className="muted">Loading…</p>

  return (
    <div>
      <h2>Dashboard</h2>
      <div className="stat-grid">
        <div className="stat-card">
          <span className="stat-value">{stats.totalCustomers}</span>
          <span className="stat-label">Customers</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{stats.totalOrders}</span>
          <span className="stat-label">Orders</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{stats.pendingOrders}</span>
          <span className="stat-label">Pending orders</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{formatMoney(stats.totalRevenue)}</span>
          <span className="stat-label">Total revenue</span>
        </div>
      </div>
      <p className="muted">
        Demo data is reset every time the application restarts.
      </p>
    </div>
  )
}
