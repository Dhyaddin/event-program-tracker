# Round 4 — Fixes & How To Test

_Date: 13 June 2026 · Branch: frontend-ui_

One new table (`event_comments`) is added; `ddl-auto=update` creates it automatically — **no full DB
reset needed**. Just rebuild/restart in IntelliJ.

---

## ADMIN

- **No Danger Zone for admins.** The Delete Account section is hidden on the admin profile, and the
  server blocks an admin from deleting their own account.
- **Admins can no longer edit or delete events.** On All Events the row action is now **View** only.
- **Admins comment instead of editing.** On any event page an admin sees an **Admin Notes** box —
  they leave a note suggesting changes; the **organiser** sees those notes on their event and can
  decide whether to apply them by editing. (Admins still approve/reject *pending* events as before.)

## ORGANISER — event lifecycle

- **Status now has real effects** (set it on Edit Event):
  - **Cancelled** → the event disappears from Browse Events / Home for everyone, so no one can
    register. A cancellation is recorded in the admin activity log (with how many registrants were
    affected), and registered users see an **"Event cancelled"** notice on My Registrations.
  - **Completed** → the event disappears from Browse Events, and everyone who was registered is
    automatically marked **Attended**.
- **Cover image feedback.** After create/edit you get a message confirming the cover image uploaded
  (or an error if it failed, e.g. over 5MB). The live preview on the form already shows the chosen
  image before you submit, and the saved image now appears on the event and all cards.

## USER — reviews

- **Reviews are only possible after an event is completed.** Before completion, a registered user
  sees "You can review this event once it has been marked completed." The server enforces this too.
- On **My Registrations**, completed events show as **Attended** with a ⭐ **Review** action that opens
  the event so they can rate it; cancelled events show an **Event cancelled** notice.

---

## How to test

**Admin can't edit / can comment**
1. Log in as admin → All Events → each row only has **View**. Open an event → you see **Admin Notes**;
   type a note → Send. (No Edit button for admin.) Profile has **no Danger Zone**.
2. Log in as the **organiser who owns that event** → open it → you see the admin's note, and you have
   the **Edit Event** button (others' events have no edit button).

**Cancelled event**
1. As the owning organiser, Edit the event → Status = **Cancelled** → Save.
2. Browse Events (as anyone) → the event is gone; nobody can register.
3. A user who had registered → My Registrations shows **Event cancelled**.
4. Admin dashboard activity log shows the cancellation.

**Completed event + review flow**
1. Have a USER register for an event first.
2. As the owning organiser, Edit → Status = **Completed** → Save.
3. The user's My Registrations now shows **Attended** + a ⭐ Review action. Opening the event lets them
   submit a rating. (Try reviewing a non-completed event — it's blocked with a message.)
4. The completed event no longer appears in Browse Events.

**Image upload**
1. Create/Edit an event with a cover image → success message confirms the upload; the image shows on
   the event page and cards. Editing without choosing a new file keeps the old image.

---

## Files changed this round

**Backend (new):** `entity/EventComment`, `repository/EventCommentRepository`,
`service/EventCommentService(+Impl)`.
**Backend (modified):** `controller/EventController` (owner-only edit/delete, admin comment endpoint,
review-after-completion guard, completed→attended + cancelled logging, image upload feedback),
`controller/AdminPagesController` (comment cascade on reject), `controller/ProfileController`
(block admin self-delete), `service/EventService(+Impl)` (hide completed/cancelled from public),
`service/RegistrationService(+Impl)` (markAttendedForEvent).
**Frontend (modified):** `events/event-details` (admin notes + review gating + role-based CTA),
`admin/all-events` (view-only), `profile/profile` (no danger zone for admin),
`user/my-registrations` (attended/review/cancelled notices).
