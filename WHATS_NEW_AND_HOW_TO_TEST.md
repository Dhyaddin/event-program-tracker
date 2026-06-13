# What's New — Limitations Completed & How To Test

_Date: 12 June 2026 · Branch: frontend-ui_

This update implements the four "known limitations" from the system reference:
**Saved Events**, **Organiser Approval**, **Server-side Category Filter**, and **Feedback/Ratings**.

---

## ⚠️ READ FIRST — refresh your database once

This update adds **two new tables** (`saved_events`, `feedback`) and a new **`status`** column on
`users`. `ddl-auto=update` will create these automatically, **but** the `DataInitializer` only seeds
when the database is empty, so to get the new seed data (including a *pending* organiser) you should
start from a fresh schema.

In MySQL, run once:

```sql
DROP DATABASE IF EXISTS event_tracker_db;
CREATE DATABASE event_tracker_db;
```

Then start the app — Hibernate rebuilds all tables and `DataInitializer` reseeds.

```bash
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
# open http://localhost:8080
```

### Seed accounts

| Role | Email | Password | Notes |
|------|-------|----------|-------|
| Admin | admin@eventtracker.com | admin123 | |
| Organiser (approved) | organizer@eventtracker.com | org123 | can create events |
| Organiser (pending) | pending@eventtracker.com | pending123 | **awaiting approval** |
| User | user@eventtracker.com | user123 | |

---

## 1. Organiser Approval Workflow

**Rule:** new organisers can log in but **cannot create events** until an admin approves them.
USERs are auto-approved; new sign-ups with the *Organiser* role start as **PENDING**.

**Test:**
1. Log in as **pending@eventtracker.com**. The dashboard shows a yellow *"awaiting admin approval"*
   banner, and the **Create Event** actions are hidden / show a "Pending" lock in the sidebar.
   Try visiting `/events/create` directly → you are redirected with an error message.
2. Log out, log in as **admin@eventtracker.com**. The dashboard shows a *"1 organiser awaiting approval"*
   prompt. Open **Approvals** in the sidebar → you see *Farah Pending*. Click **Approve**.
3. Log back in as **pending@eventtracker.com** → the banner is gone and **Create Event** now works.
4. (Optional) Register a brand-new account choosing the *Organiser* role → it appears in Approvals as PENDING.

---

## 2. Saved Events (bookmarks)

**Test:**
1. Log in as **user@eventtracker.com**. Open any event → click **Save** (top of the event page).
   The button turns solid and reads *Saved*.
2. Go to **Saved Events** in the sidebar → the event is listed (live data, real count).
3. Click the bookmark icon on the saved card (or **Save** again on the event page) → it is removed.
   Verify the `saved_events` table in MySQL gains/loses the row.

---

## 3. Feedback / Ratings

**Rule:** only users who **registered** for an event can leave a 1–5 star review (one per event, editable).

**Test:**
1. As **user@eventtracker.com**, open an event and **Register** for it.
2. Scroll to **Reviews & Ratings** → choose a star rating, optionally add a comment, **Submit Review**.
3. The review appears in the list and the **average rating** (with stars) shows in the section header.
4. Submit again → it **updates** your existing review rather than adding a duplicate.
5. Log in as a different user who has **not** registered → the form is replaced by a note explaining
   only registered attendees can review. Anonymous visitors are prompted to sign in.

---

## 4. Server-side Category Filter

**Test:**
1. Open **Browse Events**. The **Category** sidebar now lists the real categories from the database
   (Technology, Business, Arts & Culture, Sports & Fitness, …).
2. Click a category → the URL becomes `/events?category=Business` and only matching events show.
   The toolbar reads *"Showing N event(s) in Business"* with a **(clear)** link.
3. The **Search** box now submits to the server and filters by title (`/events?search=...`).

---

## Files changed / added

**New backend:** `entity/SavedEvent`, `entity/Feedback`, `repository/SavedEventRepository`,
`repository/FeedbackRepository`, `service/SavedEventService(+Impl)`, `service/FeedbackService(+Impl)`.

**Modified backend:** `entity/User` (status field + enum), `repository/UserRepository` &
`repository/EventRepository` (new queries), `service/UserService(+Impl)` (approval),
`service/EventService(+Impl)` (category), `controller/EventController` (save, feedback, category,
create-gating, FK-safe delete), `controller/AdminPagesController` (approvals + FK-safe user delete),
`controller/UserPagesController` (saved events), `controller/DashboardController` (pending count),
`controller/GlobalControllerAdvice` (currentUser + isPendingOrganiser), `config/DataInitializer`
(statuses + pending organiser seed).

**Modified templates:** `fragments/sidebar` (Approvals link, gated Create), `events/event-details`
(save, reviews, alerts), `events/event-list` (category links, search, results count),
`user/saved-events` (live data), `dashboard/dashboard-organiser` (pending banner + gated create),
`dashboard/dashboard-admin` (approvals prompt). **New template:** `admin/approvals.html`.

---

## Note on building

This project targets **Spring Boot 4 / Java 21**, so build and run it from **IntelliJ** (or your
local Maven). It was not compiled in this assistant session because that environment only has Java 11.
The changes follow the existing code patterns; if the compiler flags anything, it is most likely a
missing import — paste the error and it can be fixed quickly.
