import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchJson, formatMoney } from '../api'
import StatusBadge from '../components/StatusBadge'

export default function OrderDetail() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [error, setError] = useState(false)
  const [notes, setNotes] = useState([])
  const [noteText, setNoteText] = useState('')
  const [noteError, setNoteError] = useState(false)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    fetchJson(`/api/orders/${id}`)
      .then(setOrder)
      .catch(() => setError(true))
    fetchJson(`/api/orders/${id}/notes`)
      .then(setNotes)
      .catch(() => {})
  }, [id])

  function saveNote(e) {
    e.preventDefault()
    setNoteError(false)
    setSaving(true)
    fetch(`/api/orders/${id}/notes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ body: noteText }),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`Request failed with status ${response.status}`)
        }
        return response.json()
      })
      .then((note) => {
        setNotes((current) => [...current, note])
        setNoteText('')
      })
      .catch(() => setNoteError(true))
      .finally(() => setSaving(false))
  }

  if (error) return <p className="error-banner">Could not load this order. Please try again later.</p>
  if (!order) return <p className="muted">Loading…</p>

  return (
    <div>
      <p><Link to="/orders">← Back to orders</Link></p>
      <h2>
        {order.orderNumber} <StatusBadge status={order.status} />
      </h2>
      <div className="detail-grid">
        <div className="detail-card">
          <h3>Order info</h3>
          <p>Customer: <Link to={`/customers/${order.customerId}`}>{order.customerName}</Link></p>
          <p>Placed on {order.placedOn}</p>
          {order.deliveredOn && <p>Delivered on {order.deliveredOn}</p>}
        </div>
        <div className="detail-card">
          <h3>Totals</h3>
          <p>Subtotal: {formatMoney(order.subtotal)}</p>
          {order.discountPercent != null && <p>Discount: {order.discountPercent}%</p>}
          <p className="total-line">Total: {formatMoney(order.total)}</p>
        </div>
      </div>
      <h3>Items</h3>
      <table>
        <thead>
          <tr>
            <th>Product</th>
            <th>Unit price</th>
            <th>Qty</th>
            <th>Line total</th>
          </tr>
        </thead>
        <tbody>
          {order.items.map((item, index) => (
            <tr key={index}>
              <td>{item.productName}</td>
              <td>{formatMoney(item.unitPrice)}</td>
              <td>{item.quantity}</td>
              <td>{formatMoney(item.lineTotal)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <h3>Internal notes</h3>
      <div className="notes">
        {notes.map((note) => (
          <div className="note" key={note.id}>
            <p>{note.body}</p>
            <p className="muted">{note.author} — {note.createdAt}</p>
          </div>
        ))}
        {notes.length === 0 && <p className="muted">No notes on this order yet.</p>}
        <form className="note-form" onSubmit={saveNote}>
          <textarea
            value={noteText}
            onChange={(e) => setNoteText(e.target.value)}
            placeholder="Add an internal note about this order…"
            rows="3"
            required
          />
          {noteError && (
            <p className="error-banner">Could not save the note. Please try again later.</p>
          )}
          <button type="submit" disabled={saving}>
            {saving ? 'Saving…' : 'Save note'}
          </button>
        </form>
      </div>
    </div>
  )
}
