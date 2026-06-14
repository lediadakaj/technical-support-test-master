# Ticket: Customer detail page crashes (blank) for customers with no address

**Reported issue:** ISSUE-2: "Customer page is blank"

**Severity:** High for any affected customer the entire detail page is unusable
(no contact details, no order history), with no error message to guide the user.
It is time-sensitive support data (the reporter needed Liam's number for a callback
*today*). Scope is limited to customers with a missing address, but for those it is
a total page failure, and the fix is tiny and low-risk.

**Classification:** Frontend bug (unguarded null, missing-data handling).

## Summary

The customer detail page assumes every customer has an address and calls
`customer.address.split(',')` unconditionally. Liam O'Connor (customer 2) has no
address on file (`address = NULL`), so that line throws a `TypeError` during React
render, which unmounts the whole page and leaves a blank white screen with no
visible error. Customers who *do* have an address are unaffected, which is why only
Liam's page breaks.

## Steps to reproduce

1. Start the app, open the **Customers** list.
2. Click **Liam O'Connor** (URL `/customers/2`).
3. The page renders blank/white. No on-screen error. (The browser dev-tools console
   shows `TypeError: Cannot read properties of null (reading 'split')`.)
4. Open any customer that has an address (e.g. Sofia Mendes, `/customers/1`) → loads
   fine.

**Expected result:** Liam's contact details and order history load, the address area
shows a sensible placeholder when no address is on file.

**Actual result:** Entire page is blank for Liam, other customers are fine.

## Investigation notes

1. **Reproduced and isolated:** Opened Liam O'Connor from the
   Customers list → blank white page (screenshot in the screenshots folder). Then opened several *other*
   customers from the list (Sofia Mendes, and others) every one of them loaded
   normally. The blank page reproduced **only for Liam**, which pointed the
   investigation at something specific to his record rather than a global page or
   API failure. A blank SPA page with "no error" is the signature of an uncaught
   exception thrown during React render (the component tree unmounts).
2. **Backend:** Both calls the detail page makes return HTTP 200 with valid
   data:
   - `GET /api/customers/2` → `{... "name":"Liam O'Connor", "address":null, ...}`
   - `GET /api/customers/2/orders` → one order, ORD-1007.
   So this is not a server error, not a 404, and not the apostrophe in "O'Connor"
   (that value is correctly escaped end-to-end and renders fine).
3. **Data check:** `src/main/resources/data.sql:5` defines Liam with `address = NULL`.
   He is the only customer whose address is NULL, every other row has an address.
   This exactly matches "only his page is blank, others open fine."
4. **Pinpointed the crashing line:** `frontend/src/pages/CustomerDetail.jsx:24`:
   `const addressLines = customer.address.split(',').map((line) => line.trim())`.
   When `address` is `null`, `null.split(...)` throws. The address is also the only
   field the component dereferences without a guard (email/phone/customerSince are
   non-null for Liam and are rendered directly).
5. **Confirmed the mechanism:** by executing the exact logic in Node:
   - `null.split(',')` → `TypeError: Cannot read properties of null (reading 'split')`
   - guarded version → `[]` (no throw).

### Evidence (screenshots)

**1. The reported symptom, Liam's page renders completely blank** (`/customers/2`):

![Liam O'Connor customer page is blank](screenshots/issue-2.png)

**2. The root cause in the data, Liam's `address` column is `NULL`** while every
other customer has an address (`src/main/resources/data.sql:5`):

![data.sql showing Liam O'Connor with a NULL address](screenshots/issue-2.1.png)

## Root cause

`frontend/src/pages/CustomerDetail.jsx` dereferences a nullable field without a
guard. `customer.address` is nullable (the DB column is nullable and Liam's value is
`NULL`), but line 24 calls `.split(',')` on it directly. The resulting `TypeError`
propagates out of render and React unmounts the tree, producing the blank page. The
missing address itself is *valid* data, not corruption the bug is the frontend's
failure to handle it.

## Proposed resolution

Made the address rendering null-safe in `frontend/src/pages/CustomerDetail.jsx`:

- Guard the split:
  ```jsx
  const addressLines = customer.address
    ? customer.address.split(',').map((line) => line.trim())
    : []
  ```
- Render a placeholder when there is no address, instead of an empty block:
  ```jsx
  {addressLines.length > 0
    ? addressLines.map((line) => <p key={line}>{line}</p>)
    : <p className="muted">No address on file</p>}
  ```

This is the minimal change: it removes the crash and degrades gracefully, without
touching the backend or the data (a customer legitimately may have no address).

**Verification:**
- Reproduced the exact failure in isolation: `null.split(',')` throws
  `TypeError: Cannot read properties of null (reading 'split')` (the blank-page
  cause) the guarded expression returns `[]` with no throw.
- Rebuilt the frontend and restarted the app `/customers/2` now renders Liam's
  contact details and his ORD-1007 history, with "No address on file" under
  Shipping address. Customers with addresses (e.g. `/customers/1`) still render
  their address normally.

## Customer-facing update

Hi thank you for the report, I wanted to update that this results to be an issue on our side.

Liam's record simply has **no postal address saved**, and our customer page wasn't
handling that case instead of showing the page with a blank address, it failed to
load anything at all. That's why his page went white while everyone else's opened
normally.

We've fixed it: the page now loads fully even when there's no address, and shows
"No address on file" in the address area. Liam's phone number and email are
available again on his page now. If you still need it right away I am sharing it in this secure repository:
**secure repository link that holds the email adress and phone number goes here. Thank you for flagging it.

## Prevention 

- **Frontend:** Any field that maps to a nullable DB
  column (here `address`) must be guarded before method calls like `.split()`.
  Enable an ESLint rule against unguarded access on optional API fields (or adopt
  TypeScript so `address: string | null` forces the check at compile time).
- **Component test for the empty/missing data path:** A React test rendering
  `CustomerDetail` with a customer whose `address` is `null` and asserting it shows
  "No address on file" (not a crash). Testing the empty/edge state not just the happy path.
- **Error boundary:** Wrap routed pages in a React error boundary
  so a single render exception shows a friendly "Something went wrong" message
  instead of a blank screen.