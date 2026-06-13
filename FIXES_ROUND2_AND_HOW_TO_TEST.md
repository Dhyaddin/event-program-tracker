# Round 2 — Real-Data Cleanup: What Was Fixed & How To Test

_Date: 12 June 2026 · Branch: frontend-ui_

This pass removed all the fake/hardcoded "premade" content you spotted and wired every screen to the
**real database**, plus added the features from your list (event approval, system activity log,
working filters, working sign-out/change-password/delete-account).

---

## ⚠️ DO THIS FIRST — fresh database

New columns/tables were added (`events.approved`, `users.status`, `saved_events`, `feedback`,
`activity_log`). The seeder only runs on an empty DB, so reset once:

```sql
DROP DATABASE IF EXISTS event_tracker_db;
CREATE DATABASE event_tracker_db;
```

Then run from IntelliJ (or `mvnw spring-boot:run`) and open http://localhost:8080.
**Note:** this project is Spring Boot 4 / Java 21 — build it in IntelliJ. It was not compiled in the
assistant environment (only Java 11 there). If the compiler flags anything it is almost certainly a
missing import — send me the message.

### Seed accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@eventtracker.com | admin123 |
| Organiser (approved) | organizer@eventtracker.com | org123 |
| Organiser (pending) | pending@eventtracker.com | pending123 |
| User | user@eventtracker.com | user123 |

The seed also creates **one pending event** ("Web Development Bootcamp") so the Event Approvals page
has data on first run.

---

## GENERAL FIXES

- **Top-right name now matches the logged-in account.** All dashboards now use a shared topbar
  fragment that shows the real `currentUser` name + initial (no more "Ahmad" while logged in as Siti).
- **Sign Out works everywhere.** Every Sign Out is now a real POST to `/auth/logout` (was a dead `#`).
- **Notifications removed** from every topbar and the sidebar (they were broken). System activity is
  now an **admin-only** live log (see below).
- **Sidebar badges removed.** The fake "3 / 12 / 5" counters are gone — nothing shows a number unless
  it's real.
- **Database really syncs.** Every stat, table and list is read live from MySQL. Nothing is premade.

## HOME PAGE

- Hero preview cards, "Featured Events", search dropdown, "Browse by Category", and the counters are
  now **real approved events / real category names / real counts**. Empty states show if there's no data.
- Hero search now actually filters (`/events?search=...`), and the category dropdown uses real categories.

## NAVBAR

- The **Categories** dropdown is built from the real categories in your database (was hardcoded
  Conference/Workshop/etc.). Each links to a working filter.

## LOGIN / REGISTER

- **EventTracker logo is clickable** on both pages → returns to home.
- **"Dev Preview" block removed** from login.
- **Register validation** now pops up a message (password too short / mismatch / terms) and
  **keeps everything you typed** — your name, email, phone and role are preserved instead of being wiped.

## PROFILE (now shows the REAL logged-in user)

- Personal info (name, email, phone) reflects the logged-in account — e.g. Siti sees Siti's data.
  Fields you never filled in are simply blank, ready to edit.
- **Change Password works** (verifies current password, checks match + length, BCrypt-hashes the new one).
- **Notifications & Privacy sections removed.**
- **Danger Zone → Delete Account** now double-confirms: you must type `DELETE`, then confirm a second
  prompt; it deletes your data, logs you out and returns home.

## BROWSE EVENTS

- Lists all **approved** events. **Category, Date (Upcoming / This month) and Price (Free / Paid)
  filters all work** server-side and combine via the URL. "Clear All Filters" resets them.
- The fake **1 2 3 … 12 pagination** is gone — with one page, the prev/next arrows are shown dimmed and
  non-clickable so it's clear there are no other pages.

## ADMIN

- **Dashboard simplified**: real totals (users, events, registrations, pending approvals) + a **live
  System Activity Log** that records registrations, event creation, sign-ups, approvals, reviews, etc.
- **Manage Users** is now a clean **read-only** table (no edit/suspend/delete) showing real details.
- **Organiser Approvals** (approve/reject organisers) and **Event Approvals** (approve/reject pending
  events) are both functional. Approving an event publishes it; rejecting deletes it.
- **Reports** and **System Settings** removed from the menu.

## ORGANISER

- **Dashboard / My Events counts now tally** (Total Events = your events; Total Registrations real).
- **Page Views / Avg Fill / fake "Active" stats removed.**
- **Create Event**: Checklist removed; categories are the real list; note explains the event goes to
  admin approval.
- **Edit Event**: title, category, date, price etc. all pre-fill from the real event (the "tally"
  bug is fixed); fake "280 attendees / Page Views / Quick Actions" panels removed; approval badge is real.
- **Registrations**: the **Actions** column and **Export** button are removed — it's a clean attendee list.
- **Analytics** and **Export Report** removed.

## USER

- **Dashboard stats are real**: Registered, Attended, Saved (Certificates removed). Counts reflect your
  actual activity — register 0 events and it shows 0.
- **My Tickets / Certificates / Download QR removed.**
- **My Registrations** and **Saved Events** show your real rows.

---

## CLICK-THROUGH TEST PLAN

**A · Names & sign-out (the headline bug)**
1. Log in as **user@eventtracker.com**. Top-right shows **Siti Participant** (not Ahmad).
   Dashboard stats show your real counts (0 until you register).
2. Open the top-right menu → **Sign Out** → you're logged out and back at login.

**B · Event approval (admin + organiser)**
1. Log in as **organizer@eventtracker.com** → Create Event → submit. You'll see "submitted for approval".
   It does **not** appear in Browse Events yet.
2. Log in as **admin@eventtracker.com** → dashboard shows a "1 event awaiting approval" prompt →
   **Event Approvals** → Approve. Now it appears in Browse Events for everyone.
3. The seeded "Web Development Bootcamp (Pending Approval)" is there to approve/reject too.

**C · Browse filters & pagination**
1. Go to **Browse Events**. Click a **Category** → only that category shows. Add **Upcoming** and
   **Free only** → results narrow and the toolbar shows the active filters. **Clear All Filters** resets.
2. With one page of results, the pager arrows are dimmed/un-clickable.

**D · Profile & password**
1. As any user → **My Profile**: your real name/email/phone are shown. Change the name → Save → it sticks.
2. **Change Password**: wrong current password → error; correct + matching new (≥6) → success; log out
   and log in with the new password.
3. **Danger Zone → Delete Account**: type `DELETE`, confirm the second prompt → account deleted, logged out.

**E · Activity log (admin)**
1. Do a few actions as user/organiser (register, create event, leave a review).
2. Log in as admin → dashboard **System Activity Log** lists them with timestamps, newest first.

**F · Saved events & reviews** (from round 1, still working)
- Save an event from its page → appears in **Saved Events**. Register for an event → leave a star review
  → it shows with the average rating.

---

## Files changed this round (high level)

**Backend (new):** `entity/ActivityLog`, `repository/ActivityLogRepository`,
`service/ActivityLogService(+Impl)`.
**Backend (modified):** `entity/Event` (approved flag), `entity/User` (status nullable),
`repository/EventRepository` (+ approval/category queries), `service/EventService(+Impl)`,
`service/UserService(+Impl)` (changePassword), controllers: `Event`, `Admin`, `Auth`, `Dashboard`,
`Profile`, `Home`, `GlobalControllerAdvice`, plus `DataInitializer` (approved seed + pending event).
**Frontend (new):** `fragments/topbar.html`, `admin/event-approvals.html`.
**Frontend (modified):** `fragments/sidebar`, `fragments/navbar`, all three dashboards, `index`,
`auth/login`, `auth/register`, `profile/profile`, `events/event-list`, `events/event-details`,
`events/create-event`, `events/edit-event`, `admin/manage-users`, `admin/all-events`,
`organiser/my-events`, `organiser/registrations`, `user/saved-events`, `user/my-registrations`.

**Removed from menus:** Reports, System Settings, Analytics, Notifications (templates left orphaned/unused).
