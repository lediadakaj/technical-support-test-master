import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchJson } from '../api'

export default function Customers() {
  const [customers, setCustomers] = useState([])
  const [query, setQuery] = useState('')
  const [error, setError] = useState(false)

  useEffect(() => {
    const params = query ? `?name=${encodeURIComponent(query)}` : ''
    fetchJson(`/api/customers${params}`)
      .then(setCustomers)
      .catch(() => setError(true))
  }, [query])

  if (error) return <p className="error-banner">Could not load customers. Please try again later.</p>

  return (
    <div>
      <h2>Customers</h2>
      <div className="toolbar">
        <label htmlFor="customer-search">Customer name</label>
        <input
          id="customer-search"
          type="search"
          placeholder="Search by customer name…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Orders</th>
          </tr>
        </thead>
        <tbody>
          {customers.map((c) => (
            <tr key={c.id}>
              <td><Link to={`/customers/${c.id}`}>{c.name}</Link></td>
              <td>{c.email}</td>
              <td>{c.phone}</td>
              <td>{c.orderCount}</td>
            </tr>
          ))}
          {customers.length === 0 && (
            <tr><td colSpan="4" className="muted">No customers found.</td></tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
