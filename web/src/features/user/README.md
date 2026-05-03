# user

User search UI is intentionally embedded in `features/evaluation/form/CreateEvaluationPage.jsx`
(participant assignment step) rather than having a dedicated profile page.

Sub-slices:
- `search/` — `searchUsers(query)`, `listUsers()` — called by CreateEvaluationPage

Backend counterpart: `UserManagementController` (`GET /api/v1/users`).
