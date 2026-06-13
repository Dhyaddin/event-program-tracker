# Round 3 — Fixes & How To Test

_Date: 13 June 2026 · Branch: frontend-ui_

No database reset needed this round (no new columns). Just rebuild/restart in IntelliJ.
Uploaded cover images are saved to an `uploads/` folder created at the project root.

---

## 1. Admin → All Events (`/admin/events`)

- **Stat cards are now real**: Total Events, **Published** (approved), **Pending Approval**.
  The fake "Active 34 / Pending 12 / Rejected 5 / +7 this week" numbers are gone.
- **The empty table is fixed.** It was caused by the filter tabs hiding every row (rows had no
  status to match). Now every row shows by default, each row carries its real status, and the
  **All / Published / Pending** tabs actually filter.
- Each row shows the **organiser** who created the event and a real **Published / Pending** badge.
- Admin has **no register option** on events — they only view/manage. (Register is a participant action.)

## 2. Roles & ownership

- **Only participant (USER) accounts can register** for events. Organisers and admins see
  "Only participant accounts can register" instead of a Register button, and the server blocks it too.
- **Organisers can only edit/delete events they created.** When an organiser opens an event they
  don't own, there's no Edit button, and trying `/events/{id}/edit` by URL redirects with a message.
  Admins can edit/delete any event.
- On an event page, an **Edit Event** button appears only for the owning organiser (or admin).

## 3. Cover image upload (now actually works)

- Create/Edit Event forms now upload the file (`multipart/form-data`). The image is saved to disk and
  its path stored on the event.
- The uploaded cover now **appears** on: the event detail hero, the Browse Events cards, the home
  Featured Events cards, the Saved Events cards, and the admin All Events list thumbnail.
- On Edit, leaving the file empty **keeps** the existing image (it's no longer wiped on save).

## 4. Publish settings

- The **Visibility** dropdown (Public/Private/Unlisted) was removed — it had no backing and did nothing.
  Public visibility is controlled by the admin **approval** workflow.
- **Status** (Upcoming/Ongoing/Completed/Cancelled) is kept and is saved to the event.

## 5. Register password rule

- Passwords must now be **at least 6 characters AND contain both letters and numbers**.
  "aaaaa" / "aaaaaa" are rejected (no digit); "abc123" is accepted. Enforced both client-side
  (popup, keeps your typed values) and server-side (shows an error, keeps your values).

---

## Test steps

**Admin All Events**
1. Log in as admin → **All Events**. Cards show real Total/Published/Pending. The table lists all
   events with organiser + status. Click **Pending** → only pending events; **Published** → only published.

**Ownership**
1. Log in as **organizer@eventtracker.com**, open one of **your** events → you see **Edit Event**; edit works.
2. Open an event you don't own (or another organiser's) → no Edit button; you can't register either.
3. Log in as **user@eventtracker.com** → you can **Register**; organisers/admins cannot.

**Image upload**
1. As an approved organiser (or admin) → Create Event → choose a cover image → submit.
2. Open the event → the photo shows in the hero; it also shows on Browse Events / Home cards
   (once approved) and in admin All Events.
3. Edit the event without choosing a new file → the old image stays.

**Password**
1. Register a new account with password `aaaaaa` → blocked ("must include both letters and numbers"),
   your other fields stay filled.
2. Use `abc123` → accepted.

---

## Files changed this round

**Backend (new):** `config/WebConfig`, `service/FileStorageService(+Impl)`.
**Backend (modified):** `controller/EventController` (image upload on create/edit, ownership checks,
register restricted to USER, `canEdit`/`canRegister` flags), `controller/AdminPagesController`
(published/pending counts), `controller/AuthController` (password letters+numbers), `config/SecurityConfig`
(`/uploads/**` public), `application.properties` (upload settings).
**Frontend (modified):** `admin/all-events`, `events/event-details`, `events/event-list`,
`events/create-event`, `events/edit-event`, `index`, `user/saved-events`, `auth/register`.
