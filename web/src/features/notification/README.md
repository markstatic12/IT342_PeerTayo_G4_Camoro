# notification

Notification UI is intentionally embedded in `features/dashboard/DashboardPage.jsx`
(Recent Activity panel) rather than having a dedicated page.

Sub-slices:
- `list/`     — `listNotifications()` — fetched by DashboardPage on mount
- `markread/` — `markNotificationAsRead(id)` — called when a notification is interacted with

Backend counterparts: `ListNotificationController`, `MarkReadController`.
