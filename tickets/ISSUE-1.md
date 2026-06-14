# Ticket: Orders page search does not match customer names (works by order number only)

**Reported issue:** ISSUE-1: "Order search is completely broken"

**Severity:** Low (as a defect): the feature works correctly as designed; no
data is wrong or lost, and a working alternative exists. There is a genuine
*usability* gap that is worth a **Medium**-priority enhancement (see below),
because the expectation it violates is common and the report shows real user
friction.

**Classification:** User error / expectation mismatch. Secondary:
a UX-enhancement opportunity.

## Summary

The search box on the Orders page searches by **order number only**, exactly as
its label and placeholder state. The reporter typed a **customer name** ("Sofia")
into it and got "No orders found". Nothing is broken, the box was never meant to
match names. The orders reappear when the search is cleared because clearing the
search removes the order-number filter, filtering isn't faulty.

## Steps to reproduce

1. Start the app and open the **Orders** page.
2. Type `Sofia` into the "Order number" search box → table shows "No orders found."
3. Clear the box → all orders (including Sofia Mendes') are listed again.
4. Type an actual order number instead, e.g. `ORD-1004` or just `1004` → the
   matching order is returned correctly.

**Expected result (reporter's expectation):** typing a customer's name finds that
customer's orders.

**Actual result:** typing a name finds nothing; typing an order number works. The
field is labelled "Order number" with placeholder "Search by order number, e.g.
ORD-1003".

## Investigation notes

Investigated front-to-back:

1. **Frontend**: `frontend/src/pages/Orders.jsx` sends the query as
   `?orderNumber=<text>` and the input is labelled "Order number" with the
   placeholder "Search by order number, e.g. ORD-1003". So the UI advertises an
   order-number search.
2. **Backend**: `OrderController.list()` (`src/main/java/com/orderdesk/web/OrderController.java:46`)
   calls `orderRepository.findByOrderNumberContainingIgnoreCaseOrderByOrderNumber(...)`.
   The Spring Data method name resolves to `WHERE order_number LIKE %?% (ignore case)`, it only ever looks at the `order_number` column. There is no join to customer
   name anywhere in the query path.
3. **Data**: Sofia Mendes is customer id 1 (`data.sql:4`) with orders ORD-1001,
   ORD-1004 and ORD-1012. None of those order numbers contain the string "Sofia".
4. **Empirical proof** against the running instance:
   - `GET /api/orders?orderNumber=Sofia` → `[]`
   - `GET /api/orders?orderNumber=ORD-1004` → returns ORD-1004 (Sofia Mendes)
   - `GET /api/orders?orderNumber=1004` → returns ORD-1004 (partial match works)
   - `GET /api/orders` (no filter) → Sofia's three orders are all present
   This rules out a data problem, a filtering bug, and a case-sensitivity bug: the
   search behaves correctly for what it actually is, an order-number search.
5. **Reporter follow-up** confirmed the same "No orders found" for *other* names
   too, consistent with "names are never matched", not with a one-off data glitch.
6. **Workaround proven** the **Customers** page
   (`frontend/src/pages/Customers.jsx`) *does* search by name (`?name=`) and the
   customer detail page renders order history from `/api/customers/{id}/orders`.
   Verified: `GET /api/customers?name=Sofia` → Sofia (orderCount 3), and
   `GET /api/customers/1/orders` → her three orders (ORD-1001, ORD-1004, ORD-1012).
   So the name-based lookup the reporter wanted already exists, just on a different
   page.
7. **Edge cases on the order-number search** (ruling out a subtler defect):
   `?orderNumber=ord-1004` (lowercase) and `?orderNumber=%20%201004%20%20` (padded)
   both return ORD-1004 confirming the case-insensitive + trim behaviour works, so
   there is no hidden bug in the search that actually exists.

## Root cause

There is no code defect. The Orders search is an order-number filter by design:

- `frontend/src/pages/Orders.jsx:11-16` — builds `?orderNumber=` from the input.
- `src/main/java/com/orderdesk/web/OrderController.java:46-50` — filters via
  `findByOrderNumberContainingIgnoreCaseOrderByOrderNumber`, which matches the
  `order_number` column only.

The report stems from an expectation mismatch: the reporter expected a name search
from a control that is explicitly an order-number search.

## Proposed resolution

**Resolved as "not a defect / working as designed."** No code change made, because
silently turning the order-number box into a name search would alter intended,
clearly-labelled behaviour without a product decision.

**How I verified the conclusion:** the four API calls above demonstrate the search
works correctly for order numbers and do not claim to match names; the unfiltered
list and the Customers-page name search both confirm Sofia's data is present and
findable. Reproduced on the running instance.

**Follow-up (separate enhancement, ~Medium priority):** users clearly
expect to find orders by customer name from the Orders page. Two low-risk options:
1. *Cheapest UX fix:* relabel/repurpose the field, keep it order-number only but
   make that unmistakable, and add a hint linking to the Customers page for name
   lookups.
2. *Better UX:* broaden the search to match **order number OR
   customer name** (e.g. add a repository query joining `Customer.name` with a
   `LIKE`, and generalise the request param to `q`). This is a small
   change and would satisfy the reported expectation.

I have intentionally left this as a recommendation rather than shipping it under a
"bug" so the behaviour change is a conscious product choice.

## Customer-facing update

Hi, thank you for reaching out and flagging towards this, I'm providing some insights to this ocurrence: your orders are safe and what's happening is that the search box on the **Orders** page only matches the
**order number** (for example `ORD-1004`), not the customer's name. That's why
typing "Sofia" shows "No orders found" even though her orders are right there in
the full list.

Two ways to get what you need today:
- To find someone's orders **by name**, use the **Customers** page: search
  "Sofia", click her name, and you'll see all of her orders.
- On the Orders page, search by the **order number** instead (a partial number
  like `1004` works too).

I've also logged a suggestion internally to let the Orders search match customer names
directly, since that's clearly what you (reasonably) expected  I'll let you know
if/when that ships. In the meantime the Customers page will get you there. Let me
know if you'd like further assistance or an additional screen-share session by our team to walk through it.

## Prevention (Part D.2)

The useful prevention
here is to protect against a **genuine future** search regression (the failure the
reporter believed they were seeing) and against silent behaviour drift:

- **Regression test pinning the intended scope.** A controller/repository test that
  asserts `?orderNumber=ORD-1004` returns that order and `?orderNumber=<an existing
  customer name>` returns an empty list. This documents the order-number-only
  contract in code, and if someone later adds name search it forces a conscious
  update to the test rather than an accidental behaviour change.
- **Zero-result-rate monitoring.** Track the proportion of order searches returning
  no results. A sudden spike would surface an *actual* search outage quickly and let
  us distinguish a real break from this "works-as-designed, wrong field" situation.