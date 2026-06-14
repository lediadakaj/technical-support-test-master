import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchJson, formatMoney } from '../api'
import StatusBadge from '../components/StatusBadge'

export default function Orders() {
  const [orders, setOrders] = useState([])
  const [query, setQuery] = useState('')
  const [error, setError] = useState(false)

  useEffect(() => {
    const params = query ? `?orderNumber=${encodeURIComponent(query)}` : ''
    fetchJson(`/api/orders${params}`)
      .then(setOrders)
      .catch(() => setError(true))
  }, [query])

  if (error) return <p className="error-banner">Could not load orders. Please try again later.</p>

  return (
    <div>
      <h2>Orders</h2>
      <div className="toolbar">
        <label htmlFor="order-search">Order number</label>
        <input
          id="order-search"
          type="search"
          placeholder="Search by order number, e.g. ORD-1003"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>
      <table>
        <thead>
          <tr>
            <th>Order #</th>
            <th>Customer</th>
            <th>Placed on</th>
            <th>Status</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((o) => (
            <tr key={o.id}>
              <td><Link to={`/orders/${o.id}`}>{o.orderNumber}</Link></td>
              <td>{o.customerName}</td>
              <td>{o.placedOn}</td>
              <td><StatusBadge status={o.status} /></td>
              <td>{formatMoney(o.total)}</td>
            </tr>
          ))}
          {orders.length === 0 && (
            <tr><td colSpan="5" className="muted">No orders found.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
