# Ticket: Discount calculation returns the discount amount instead of the discounted total

**Reported issue:** ISSUE-4: "Discounted orders show crazy wrong totals"

**Severity:** Critical. Every discounted order shows a
drastically incorrect total (ORD-1005 displays €12.99 instead of €116.91), the same
function feeds the **dashboard revenue**, so the company's headline revenue figure is
wrong, and Finance is already acting on bad numbers. It affects customer-facing
totals and internal reporting simultaneously. The fix is a one-line change.

**Classification:** Backend bug (incorrect business logic).

## Summary

`OrderService.calculateTotal` applies a discount by multiplying the subtotal by
`discountPercent / 100` — which yields the **discount amount**, and then returns that
*as the order total*. So a 10% discount makes the total 10% of the subtotal instead
of 90% of it. The same method is summed for the dashboard revenue, so revenue is
understated too.

## Steps to reproduce

1. Open **ORD-1005** (`/orders/5`): two items totalling €129.90, 10% discount.
2. Observed total: **€12.99**. Correct total: €129.90 × (100−10)/100 = **€116.91**.
3. Open **ORD-1010** (`/orders/10`): €640.00 subtotal, 15% discount → shows **€96.00**,
   should be €544.00.
4. Open the **Dashboard**: the revenue figure is understated because it sums these
   wrong totals.

**Expected result:** discounted total = subtotal − discount (i.e. subtotal × (100 −
discountPercent)/100). ORD-1005 → €116.91.

**Actual result:** total = subtotal × discountPercent/100 (the discount amount).
ORD-1005 → €12.99.

## Investigation notes

1. **Reproduced via API**, matching the report exactly:
   - `GET /api/orders/5` → `subtotal: 129.90, discountPercent: 10, total: 12.99`.
   - `GET /api/orders/10` → `subtotal: 640.00, discountPercent: 15, total: 96.00`.
2. **The numbers identify the formula.** €12.99 = €129.90 × 10 ÷ 100 and €96.00 =
   €640.00 × 15 ÷ 100. Both are exactly `subtotal × discountPercent / 100` the
   amount of the discount, not the amount payable.
3. **Located the code.** `src/main/java/com/orderdesk/service/OrderService.java`,
   `calculateTotal`:
   ```java
   total = total.multiply(BigDecimal.valueOf(order.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
   ```
   It multiplies by `discountPercent` where it should multiply by `(100 - discountPercent)`.
4. **Impact.** `calculateTotal` is used by `toSummary` and `toDetail` (every
   order list/detail total) and by `DashboardController.stats()` which sums it into
   `revenue` (`DashboardController.java:33-36`). So one bug corrupts order totals,
   the orders list, order detail, and the dashboard revenue KPI. Only orders with a
   non-null, non-zero discount are affected, undiscounted orders compute correctly because the `if` branch is skipped.
5. **Confirmed undiscounted orders are unaffected** (e.g. ORD-1001 total €64.40 equals
   its subtotal), which is why only discounted orders look "crazy".

## Root cause

`OrderService.calculateTotal` (`src/main/java/com/orderdesk/service/OrderService.java`).
A percentage discount of *p%* means the customer pays *(100 − p)%* of the subtotal.
The code instead computes *p%* of the subtotal and returns it as the total:

```java
total = total.multiply(BigDecimal.valueOf(order.getDiscountPercent()))  // p%
             .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
```

## Proposed resolution

Multiply by the *complement* of the discount:

```java
total = total.multiply(BigDecimal.valueOf(100 - order.getDiscountPercent()))
             .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
```

Now total = subtotal × (100 − p)/100, the amount actually payable. BigDecimal and
`RoundingMode.HALF_UP` at scale 2 are preserved, so currency rounding is unchanged.

**Verification:**
  - `GET /api/orders/5` → `total: 116.91` (was 12.99) ✓ matches the report's expected
    €116.91.
  - `GET /api/orders/10` → `total: 544.00` (was 96.00) ✓ (€640 × 0.85).
  - Undiscounted orders unchanged (ORD-1001 still €64.40).
- Dashboard revenue is now **€2383.98** (was €1832.06), and it matches an independent
  SQL query run in the H2 console that recomputes revenue straight from the raw tables, both return €2383.98.

## Customer-facing update

Hello thank you for contacting, I want to provide an update regarding this issue, it appears that there was a bug in how we applied percentage discounts: instead of taking the discount **off** the order, the
system was showing the **discount amount** as the total. That's why ORD-1005 showed
€12.99 instead of €116.91, and why other discounted orders (and the dashboard
revenue) looked wrong.

We've corrected the calculation. Discounted orders now show the right amount, ORD-1005 is €116.91, ORD-1010 is €544.00  and the dashboard revenue has been
recomputed and reconciled. No data was lost the underlying items and discount
percentages were always correct, only the final total was computed wrongly, so the
fix brings every order to its correct value immediately. Please let Finance know the
revenue figure is now accurate. Happy to walk through the corrected numbers with
them.

## Prevention

- **Unit test on `calculateTotal` with known values.** Assert a 10% discount on
  €129.90 yields €116.91 (and 0% / null discount yields the subtotal). 
- **Property test.** For any discounted order, assert `0 < total ≤ subtotal`
  and `total == subtotal − round(subtotal × p/100)`. The buggy version produced
  `total ≪ subtotal` for small-percentage discounts, which this invariant catches.
- **Monitoring.** A scheduled job (or dashboard self-check)
  that recomputes revenue from line items independently and alerts if it diverges from the reported figure.

## Revenue reconciliation query

A single SQL query that computes correct total revenue directly from the raw tables, non-cancelled orders only, discounts applied, rounded per order to match how the
application sums per-order totals. Run in the **H2 console** (`jdbc:h2:mem:orderdesk`):

```sql
SELECT CAST(SUM(order_total) AS DECIMAL(12,2)) AS total_revenue
FROM (
  SELECT o.id,
         ROUND(SUM(oi.unit_price * oi.quantity)
               * (100 - COALESCE(o.discount_percent, 0)) / 100.0, 2) AS order_total
  FROM orders o
  JOIN order_items oi ON oi.order_id = o.id
  WHERE o.status <> 'CANCELLED'
  GROUP BY o.id
) t;
```

**Revenue figure evolved as fixes landed (all cross-checked dashboard == SQL):**

| State | Dashboard revenue | Independent SQL | Match? |
|-------|-------------------|-----------------|--------|
| Before any fix (discount bug) | €1832.06 | - | n/a (buggy) |
| After ISSUE-4 (discount fixed) | €2383.98 | €2383.98 | ✓ |
| **After ISSUE-6 (ORD-1003 duplicate removed) FINAL** | **€2094.98** | **€2094.98** | ✓ |

The query is the independent check that catches the discount bug `(100 -discount)/100` per order, summed, instead of the buggy `discount/100` and
reflects the ORD-1003 dedup (−€289).