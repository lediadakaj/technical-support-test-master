import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchJson, formatMoney } from '../api'
import StatusBadge from '../components/StatusBadge'

export default function CustomerDetail() {
  const { id } = useParams()
  const [customer, setCustomer] = useState(null)
  const [orders, setOrders] = useState([])
  const [error, setError] = useState(false)

  useEffect(() => {
    fetchJson(`/api/customers/${id}`)
      .then(setCustomer)
      .catch(() => setError(true))
    fetchJson(`/api/customers/${id}/orders`)
      .then(setOrders)
      .catch(() => setError(true))
  }, [id])

  if (error) return <p className="error-banner">Could not load this customer. Please try again later.</p>
  if (!customer) return <p className="muted">Loading…</p>

  const addressLines = customer.address
    ? customer.address.split(',').map((line) => line.trim())
    : []

  return (
    <div>
      <p><Link to="/customers">← Back to customers</Link></p>
      <h2>{customer.name}</h2>
      <div className="detail-grid">
        <div className="detail-card">
          <h3>Contact</h3>
          <p>{customer.email}</p>
          <p>{customer.phone}</p>
          <p className="muted">Customer since {customer.customerSince}</p>
        </div>
        <div className="detail-card">
          <h3>Shipping address</h3>
          {addressLines.length > 0 ? (
            addressLines.map((line) => <p key={line}>{line}</p>)
          ) : (
            <p className="muted">No address on file</p>
          )}
        </div>
      </div>
      <h3>Order history</h3>
      <table>
        <thead>
          <tr>
            <th>Order #</th>
            <th>Placed on</th>
            <th>Status</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((o) => (
            <tr key={o.id}>
              <td><Link to={`/orders/${o.id}`}>{o.orderNumber}</Link></td>
              <td>{o.placedOn}</td>
              <td><StatusBadge status={o.status} /></td>
              <td>{formatMoney(o.total)}</td>
            </tr>
          ))}
          {orders.length === 0 && (
            <tr><td colSpan="4" className="muted">No orders yet.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
