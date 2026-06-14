# OrderDesk — Technical Support Challenge

Welcome! **OrderDesk** is a small internal order-management portal used by our
(fictional) support team to look up customers and their orders, and to keep
internal notes on orders.

It is a single full-stack project:

| Layer    | Technology                                                     |
|----------|----------------------------------------------------------------|
| Backend  | Spring Boot 4.1, Java 25                                       |
| Frontend | React 19 + Vite 8, embedded in the same Gradle project         |
| Database | H2 (in-memory) — schema + demo data recreated on every startup |
| Build    | Gradle 9.5 (wrapper included)                                  |

Several users have reported problems with the portal (see
[Reported issues](#2-reported-issues) below). Your job is to investigate each
report like a real support engineer would: reproduce it, figure out what is
actually going on, write a proper ticket, and resolve it.

---

## 1. Running the project locally

The project runs the same way on **Windows, macOS and Linux**.

### Prerequisites

You need a **Java Development Kit (JDK), version 25** — and nothing else.
Gradle and Node.js are downloaded automatically by the build:

- The **Gradle Wrapper** (`gradlew` / `gradlew.bat`, included in the repo)
  downloads Gradle 9.5 on first run.
- The build downloads a private **Node.js 24 (LTS)** runtime and uses it to
  build the React app — it will not touch any Node.js you may already have.

To install JDK 25, download **Temurin 25 (LTS)** for your operating system
from <https://adoptium.net/temurin/releases/?version=25> and follow the
installer, or use your package manager:

| OS | Example |
|----|---------|
| Windows | `winget install EclipseAdoptium.Temurin.25.JDK` |
| macOS | `brew install --cask temurin@25` |
| Linux / any | `sdk install java 25-tem` (via [SDKMAN!](https://sdkman.io)), or your distro's package for OpenJDK 25 |

Then verify in a new terminal:

```
java -version
```

It must report version 25.x. If it reports an older version, make sure JDK 25
is first on your `PATH` (or set `JAVA_HOME` to the JDK 25 install directory).

> Already have a different JDK (17+) installed and don't want to change it?
> That works too: the build uses Gradle's toolchain support and will download
> a JDK 25 automatically for compiling and running the app.

You need an internet connection for the first build.

### Start the application

From the project root:

```
./gradlew bootRun        (macOS / Linux)
gradlew.bat bootRun      (Windows — use .\gradlew.bat in PowerShell)
```

The first run takes a few minutes (it downloads Gradle, dependencies, Node
and npm packages). When the log shows `Started OrderDeskApplication`, open:

- **App:** <http://localhost:8080>
- **H2 database console:** <http://localhost:8080/h2-console>
  — JDBC URL `jdbc:h2:mem:orderdesk`, user `sa`, empty password.

The database is **in-memory**: every restart drops everything and re-creates
the schema and demo records from
[`src/main/resources/data.sql`](src/main/resources/data.sql), so you can
never permanently break the data.

Tip for faster restarts once the frontend has been built at least once:

```
./gradlew bootRun -PskipFrontend
```

### Optional: frontend dev mode (hot reload)

Only needed if you want hot reload while changing frontend code, and it
requires your own Node.js 20+ install:

```
cd frontend
npm install
npm run dev
```

This serves the React app on <http://localhost:5173> and proxies `/api/*`
calls to the backend on `:8080` (keep `bootRun` running in another terminal).
Otherwise, just rebuild with `./gradlew bootRun` after frontend changes.

### Project layout

```
├── build.gradle.kts            # Gradle build (backend + embedded frontend build)
├── src/main/java/com/orderdesk # Spring Boot backend (REST API)
├── src/main/resources          # application.properties, data.sql (demo data)
├── frontend/                   # React app (Vite), built into the jar by Gradle
└── tickets/                    # ← your tickets go here
```

---

## 2. Reported issues

Below are six reports exactly as they arrived from users.

### ISSUE-1 — "Order search is completely broken"

> I'm trying to find Sofia Mendes' orders. I went to the **Orders** page,
> typed `Sofia` into the search box and it says *"No orders found"*. That
> can't be right — I can literally see her orders in the list when I clear
> the search! Searching is broken, please fix ASAP.

### ISSUE-2 — "Customer page is blank"

> When I open the customer **Liam O'Connor** from the Customers list, the
> whole page goes blank. Nothing loads, no error, just a white page. Other
> customers open fine. I need his contact details for a callback today.

### ISSUE-3 — "Order ORD-1007 won't open"

> Clicking on order **ORD-1007** just shows *"Could not load this order.
> Please try again later."* — I tried again later, same thing. Every other
> order I tried opens normally. The customer is asking when their headphones
> were delivered and I can't tell them!

### ISSUE-4 — "Discounted orders show crazy wrong totals"

> Order **ORD-1005** has two items that add up to **€129.90** and a 10%
> discount, so the total should be **€116.91** — but the portal shows
> **€12.99**! Finance says other discounted orders look wrong too, and the
> revenue number on the dashboard seems way off. This is urgent.

### ISSUE-5 — "Can't save notes on orders"

> The internal notes feature stopped working. Whatever I type, on whatever
> order, clicking **Save note** always shows *"Could not save the note"*.
> The existing notes show up fine, so it's not all broken — just saving.
> We rely on these notes for handovers between shifts.

### ISSUE-6 — "Customer charged double — order shows two chairs"

> Jonas Weber called in upset: order **ORD-1003** shows **two** lines of
> "Ergonomic Office Chair" and a total of **€578.00**, but he insists he
> ordered exactly **one** chair for €289.00. Did we charge him twice? Where
> did the second line come from? He wants an answer today.

---

## 3. Your tasks

### Part A — Investigate and write tickets

For **each** of the six issues:

1. **Investigate** it the way you would in production support. You have the
   complete running system and its source code at your disposal; how you
   approach each report is entirely up to you.
2. **Write a ticket** describing it. Copy
   [`TICKET_TEMPLATE.md`](TICKET_TEMPLATE.md) into the [`tickets/`](tickets)
   folder (one file per issue, e.g. `tickets/ISSUE-1.md`) and fill in every
   section — including the **customer-facing update**: the reply you would
   actually send to the person who reported it, in plain language.
   Be precise — "it's broken" is not a root cause.

### Part B — Triage

Create `tickets/TRIAGE.md`. Assume all six reports landed in your queue at
the same time. Assign each a severity, decide the order you would work on
them, and justify it in a few sentences (impact, scope, urgency, effort).

### Part C — Resolve your own tickets

For each ticket:

- If you conclude it is a genuine defect, fix it in the code (or data) and
  explain the fix in the ticket's *Proposed resolution* section, including
  how you verified it.
- If you conclude it is **not** a defect, say so explicitly, explain what is
  actually happening, and write what you would communicate back to the user.

### Part D — Stretch goals (optional, senior bonus)

- Using the H2 console, write a **single SQL query** that computes the
  correct total revenue (non-cancelled orders, discounts applied), and check
  it against the dashboard after your fixes.
- For each defect you fixed, propose **one preventive measure** (a database
  constraint, validation, test, or monitoring/alerting) that would have
  caught it before users did.

## 4. What we evaluate

- Whether your conclusions about each report are correct.
- The quality of your tickets: can someone else reproduce the problem,
  understand the root cause, and trust the evidence you present?
- The soundness of your triage reasoning.
- Whether your fixes are minimal, correct, and verified — and how clearly
  you explain them.
- Whether your customer-facing replies would actually help the person who
  reported the issue.

We care at least as much about *how you investigate and what you conclude*
as about the fixes themselves — your tickets are where we see that.

Good luck!
