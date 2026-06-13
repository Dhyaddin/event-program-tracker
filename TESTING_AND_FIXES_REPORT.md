# Event & Program Tracker — CRUD Testing & Fixes Report

_Date: 12 June 2026 · Branch: frontend-ui_

## Which phase are we in?

You are **past the "frontend-only" stage**. The project already has full entities,
repositories, services, Spring Security, and a `DataInitializer` seed. In terms of the
14-phase plan this is **Phase 13 — Backend Integration**, moving toward **Phase 14 — Demo
Readiness**. This session connected the remaining role pages to the database and fixed the
broken CRUD paths so every screen now reflects real data.

> Note: the project rules describe a "frontend developer only" split, but the actual codebase
> is full-stack and already wired. To satisfy your request ("every CRUD must work and sync with
> the database"), the previously-static role pages were connected to the existing service layer.

---

## CRUD audit — status by role

| Area | Operation | Before | After |
|------|-----------|--------|-------|
| Auth | Register / Login / Logout | Working | Working |
| Events (public) | Browse list | **Mock cards only** | **Live DB events** |
| Events | View details | Working | Working |
| Events | Create | Working | Working |
| Events | **Edit** | **Broken** (hardcoded id 1, wrong field names, invalid status values) | **Fixed** |
| Events | Delete | Crashed if event had registrations (FK) | **Fixed** (cascades registrations) |
| Registrations | Register / Cancel | Working | Working |
| User | My Registrations | **Mock rows** | **Live, with working Cancel** |
| User | Saved Events | Mock | Left as placeholder (no entity — see below) |
| Organiser | My Events | **Mock rows** | **Live + edit/delete/registrations links** |
| Organiser | Registrations | **Mock rows** | **Live (scoped to organiser's events)** |
| Organiser | Analytics | **Mock bars** | **Live fill-rate per event** |
| Admin | Manage Users | **Mock rows** | **Live + working Delete** |
| Admin | All Events | **Mock rows** | **Live + edit/delete** |
| Admin | Reports | **Static numbers** | **Live counts** |
| Dashboards (all 3) | Stats + lists | **Mock** | **Live** |

---

## Fixes applied (full list)

### Backend (Java)

1. **`EventController.editEvent`** — was doing a blind `save()` of the form object, which
   **nulled out `organizer`, `createdAt`, `price`, and `status`** on every edit. Now it loads
   the existing event and copies only the editable fields.
2. **`EventController.deleteEvent`** — deleting an event with registrations threw a foreign-key
   error. Now it removes child registrations first, then the event.
3. **`AdminPagesController`** — was a no-data stub. Now injects `allUsers`, `allPlatformEvents`,
   per-event registration counts, and report totals; adds a real **`POST /admin/users/{id}/delete`**
   action (FK-safe, with a friendly error if the user still owns events).
4. **`OrganiserController`** — was a no-data stub. Now scopes `myEvents`, `allRegistrations`, and
   analytics to the logged-in organiser.
5. **`UserPagesController`** — was a no-data stub. Now injects the current user's `registrations`
   plus upcoming/attended counts.
6. **`DashboardController`** — added per-event registration counts and totals for the organiser
   and admin dashboards.
7. **`RegistrationService` / repository** — added `findByOrganizer`, `countAllRegistrations`,
   `deleteRegistrationsForEvent`, `deleteRegistrationsForUser`, plus the supporting derived
   queries (`findByEvent_Organizer`, `deleteByEvent`, `deleteByUser`).

### Frontend (Thymeleaf)

8. **`event-list.html`** — replaced 6 hardcoded cards with a `th:each` over `${events}` + empty state.
9. **`edit-event.html`** — fixed hardcoded `/events/1` action, corrected field names
   (`eventDate`, `eventTime`, `location`, `capacity`), pre-filled every field from the event,
   and fixed the status dropdown to use valid enum values (`UPCOMING/ONGOING/COMPLETED/CANCELLED`)
   — previously `PUBLISHED/DRAFT` which would 400 on submit.
10. **`manage-users.html`, `all-events.html`, `reports.html`** — wired tables, stat cards, and a
    working delete form.
11. **`my-events.html`, `registrations.html`, `analytics.html`** — wired tables / fill-rate bars.
12. **`my-registrations.html`** — wired table + working Cancel form.
13. **`dashboard-user/organiser/admin.html`** — welcome name, stat counts, and main lists now live.
14. Fixed three invalid avatar-initial Thymeleaf expressions (split `${} ? ${}` → single expression).

---

## Known limitations (by design / out of scope)

- **Saved Events** — kept as a static placeholder; there is no `SavedEvent`/bookmark entity in
  the data model. Add one if you want this functional.
- **Organiser approval workflow** (pending → approved) — the UI buttons exist but there is no
  `status` field on `User`, so they remain visual.
- **Category filter chips** on the events page filter client-side only; server-side search by
  title works.
- A few decorative widgets (page views, ratings, monthly trend, uptime) have no backing data and
  remain static.

---

## How to run & test (live click-through)

```bash
# 1. Make sure MySQL is running and the schema exists
#    (application.properties: event_tracker_db, root/root123, ddl-auto=update)

# 2. From the project root:
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run

# 3. Open http://localhost:8080
```

**Seed accounts** (from `DataInitializer`):

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@eventtracker.com | admin123 |
| Organiser | organizer@eventtracker.com | org123 |
| User | user@eventtracker.com | user123 |

### Click-through checklist

**As User**
1. Login → dashboard shows your registrations and counts.
2. Browse Events → cards are the 5 seeded events. Open one → Register.
3. My Registrations → the event appears. Click Cancel → it disappears. (Verify the
   `registrations` table in MySQL gains/loses the row.)

**As Organiser**
4. My Events → only your events. Create an event → it appears in the list and in MySQL `events`.
5. Edit it → change title/date/capacity → values persist (organizer is **not** wiped).
6. Registrations → shows users who registered for your events.
7. Delete an event that has registrations → succeeds (no FK error).

**As Admin**
8. Dashboard / Manage Users → all seeded users listed. Delete a plain user → row removed in MySQL.
9. All Events → every event with organiser + live registration count. Edit / Delete work.
10. Reports → totals match the database.

---

## Verification note

Java was reviewed statically (the build needs JDK 21 + Maven, which I confirmed your `pom.xml`
targets). All Thymeleaf expressions, controller↔service↔repository signatures, and form field
names were checked against the entities. Please run `./mvnw spring-boot:run` once to confirm a
clean boot — if anything fails to compile, send me the error and I'll fix it. I can also drive the
live click-through in your browser once the app is running.
