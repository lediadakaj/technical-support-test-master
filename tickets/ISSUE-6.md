# Ticket: Duplicate line item on ORD-1003, order shows two chairs instead of one

**Reported issue:** ISSUE-6: "Customer charged double order shows two chairs"

**Severity:** High. It is a customer-facing money discrepancy (the order shows
€578.00 for what should be a €289.00 single chair), it triggers a "were we charged
twice / refund" enquiry, and because ORD-1003 is a non-cancelled order the duplicate
also inflates the dashboard revenue. Scope is a single order in the current data.

**Classification:** Data issue (duplicate row). **Not** a code defect  the
application is correctly displaying what is in the database.

## Summary

ORD-1003 (order id 3) has two identical `order_items` rows for "Ergonomic Office
Chair" (€289.00 × 1 each), so the order shows two lines and a €578.00 total. The
customer ordered one. The second row is a spurious duplicate that was seeded into the
data, the application is faithfully rendering the database, so this is a data correction, not a code fix.

## Steps to reproduce

1. Open **ORD-1003** (`/orders/3`).
2. The Items table shows **two** "Ergonomic Office Chair" lines, €289.00 each.
3. Subtotal/total = **€578.00**, with no discount applied.

**Expected result:** one chair line, total €289.00.

**Actual result:** two identical chair lines, total €578.00.

## Investigation notes

The key question was *duplicate data* vs *code duplicating one row* (e.g. a JOIN
cartesian product). I checked all three layers:

1. **API**. `GET /api/orders/3` returns two item objects, both "Ergonomic Office
   Chair" €289.00 ×1; subtotal/total €578.00.
2. **Database** queried the live H2 instance:
   `SELECT id, order_id, product_name, quantity FROM order_items WHERE order_id = 3`
   returns **two rows with distinct ids: 5 and 18**, both the chair. Distinct primary
   keys means these are two genuine rows in the table, *not* one row duplicated by a
   join/fetch. This rules out a code/ORM bug.
3. **Seed file** `src/main/resources/data.sql` contains the chair for order 3
   **twice**: line 32 `(5, 3, 'Ergonomic Office Chair', 289.00, 1)` and line 45
   `(18, 3, 'Ergonomic Office Chair', 289.00, 1)`. The second is appended at the very
   end of the `order_items` INSERT, out of id sequence (after id 17), which is the
   typical fingerprint of an accidentally pasted/duplicated row.
4. **Mapping sanity check** the `Order` entity has a single eager collection
   (`items`) and a `@ManyToOne` customer, there is no second collection that could
   produce a cartesian product. Consistent with "the duplicate is real data, not a query artefact."

Conclusion: the database contains a duplicate line item for ORD-1003. The code is behaving correctly.

## Root cause

A duplicate `order_items` row in the seed data: `src/main/resources/data.sql:45`
inserts a second "Ergonomic Office Chair" (id 18) for order_id 3, in addition to the
legitimate one (id 5). Because the order has no discount, the order total is exactly
2 × €289.00 = €578.00.

## Proposed resolution

**Data correction:** removed the duplicate row from `src/main/resources/data.sql`
(deleted the `(18, 3, 'Ergonomic Office Chair', 289.00, 1)` line and moved the
statement-terminating `;` onto the previous row). ORD-1003 now has a single chair
line and a €289.00 total. No code change.

**Verification:**
- After the fix and restart:
  - `GET /api/orders/3` → **one** chair item, subtotal/total **€289.00**.
  - H2: `SELECT COUNT(*) FROM order_items WHERE order_id = 3` → **1**.
- **Revenue impact reconciled:** removing the €289 duplicate reduces dashboard
  revenue from €2383.98 to **€2094.98**, and the independent SQL query
  returns the same €2094.98, the two still agree after the correction.

## Customer-facing update

Hi, thank you for raising this, and I understand the concern. Our system was mistakenly
showing it **twice**, which is why the order total read €578.00. That second line was
a duplicate entry on our side.

I've corrected the order so it now shows the single chair and the correct €289.00 total. On the "charged twice" question, the
duplicate was on the order record. I'm confirming with our payments team right now
whether an actual double charge reached your card. If any extra €289.00 was taken,
we'll refund it in full and confirm the date. Apologies for the worry this caused.

## Prevention 

- **Guard against duplicate line items.** Either a uniqueness rule that prevents two
  identical lines on the same order (e.g. unique on `(order_id, product_name)`, with
  repeats handled by incrementing quantity instead of adding a row), or de-duplication
  in the order-creation logic. This stops the same product appearing as two separate
  €289 lines.
- **Order-total reconciliation against source.** A check (test or scheduled job) that
  an order's stored/displayed total equals the sum of its line items from an
  independent calculation and, where available, against the amount actually charged in the payment system  would flag a doubled order before a customer does.
- **Seed-data integrity check.** A startup assertion or test over `data.sql`
  (no duplicate `(order_id, product_name)` pairs unless intended) would have caught
  this specific bad row in CI rather than in production.