# Triage
# All six reports in the queue at once

**Scenario:** assume ISSUE-1 … ISSUE-6 all landed simultaneously. Below I assign each
a **severity**, then a **work order**, which weighs impact × scope × urgency × effort,  not
severity alone. 

**Severity scale:** 
- Critical = wrong money or data integrity at scale / company-wide reporting 
- High = a feature or page fully broken for affected users 
- Medium = degraded but usable
- Low = cosmetic, non-defect, or working-as-designed.

## Summary table

| Order | Issue | Title | Severity | Class | Effort |
|------:|-------|-------|----------|-------|--------|
| 1 | ISSUE-4 | Discount math inverts totals + revenue KPI | **Critical** | Backend bug | 1 line |
| 2 | ISSUE-6 | Duplicate line item → "charged double" | **High** | Data issue | 1 row |
| 3 | ISSUE-5 | Notes can't be saved (all users/orders) | **High** | Frontend bug | 1 line |
| 4 | ISSUE-3 | Order detail 500s (delivered, no date) | **High** | Backend bug | 1 line + data |
| 5 | ISSUE-2 | Customer page blank (no address) | **High** | Frontend bug | 1 line |
| 6 | ISSUE-1 | "Order search broken" | **Low** | Not a defect | reply only |


> The table is sorted by work order.

## Justification (impact · scope · urgency · effort)

**1. ISSUE-4 (Critical).**
- *Impact:* it's wrong money. Every discounted order shows a
badly incorrect total (ORD-1005 €12.99 vs €116.91), and the **same** function feeds
the dashboard revenue, so the company's headline KPI is wrong and Finance is already
acting on it. 
- *Scope:* all discounted orders + internal reporting simultaneously the only company-wide item. *Urgency:* high; financial reporting is in play. 
- *Effort:*
one-line fix. Highest impact at lowest cost → unambiguous first.

**2. ISSUE-6 (High).** 
- *Impact:* a customer-facing money discrepancy (€578 for a
€289 order) plus a possible **real double charge / refund** owed, financial harm to
a specific customer. 
- *Scope:* one order in current data, but it's money and it's
external. 
- *Urgency:* very high, the customer is upset and "wants an answer today."
- *Effort:* trivial (delete the duplicate row) plus a billing-team check that can run in
parallel. Money + waiting customer + tiny effort → clear it right after the critical.

**3.  ISSUE-5 (High).** 
- *Impact:* an entire feature (note saving) is 100% broken.
- *Scope:* the broadest functional outage left, every user, every order and it blocks shift handovers the team relies on operationally. 
- *Urgency:* high but internal (no external customer is stuck waiting on it). 
- *Effort:* one line. Largest blast radius among the remaining items, so it ranks above the single-page failures.

**4. ISSUE-3 (High).** 
- *Impact:* the order detail page fully 500s, blocking a live
"when was it delivered?" customer enquiry. 
- *Scope:* one order today, but it's a
recurring *class* any future order marked delivered without a date will hit the same
crash. 
- *Urgency:* high (customer waiting). 
- *Effort:* one-line guard (+ optional data
correction / constraint). Ranked above ISSUE-2 because of the recurring-class risk.

**5.  ISSUE-2 (High).** 
- *Impact:* the customer page renders completely blank, blocking
a callback. 
- *Scope:* narrowest of the High items — only customers with no address
(one in current data). 
- *Urgency:* high (agent needs the number today). 
- *Effort:* one line. Same severity as ISSUE-3 but smaller blast radius, so it follows it.

**6. ISSUE-1 (Low).** 
- *Impact:* none, the order search works exactly as designed
(it searches order numbers, not names). *Scope:* n/a; it's an expectation mismatch,
not a defect. 
- *Urgency:* low. 
- *Effort:* a clear explanatory reply (use the order
number, or the Customers page for name search) plus logging a UX enhancement.
Lowest priority: nothing is broken.

## Execution note

Five of the six are effectively one-line/one-row changes, so in practice the High
items would be batched and shipped quickly once ordered. The two money-touching items
(ISSUE-4, ISSUE-6) change the dashboard revenue, so after those land I re-ran the
Part D.1 reconciliation: corrected revenue settles at **€2094.98**, with the
dashboard and the independent SQL query in agreement (see ISSUE-4 → Stretch Part D.1).
ISSUE-6 also carries a non-engineering action (payments-team verification of an actual
double charge) that I'd open in parallel rather than block the queue on.