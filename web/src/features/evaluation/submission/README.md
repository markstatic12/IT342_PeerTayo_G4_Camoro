# evaluation/submission

This slice owns all UI and service code related to a **respondent submitting evaluations**.

## Files

| File | Role |
|------|------|
| `evaluationSubmissionService.js` | API calls — `GET /evaluations/pending`, `POST /evaluations/:id/submit` |
| `PendingEvaluationsPage.jsx` | Page — list of pending forms assigned to the user, with detail panel |
| `PendingEvaluationsPage.css` | Styles for the pending list page (tokens match HTML prototype v8) |
| `EvaluateFormPage.jsx` | Page — full-screen criteria rating form for a single evaluatee |
| `EvaluateFormPage.css` | Styles for the evaluate form page (tokens match HTML prototype v8) |

## Routes

| Path | Component |
|------|-----------|
| `/pending-evaluations` | `PendingEvaluationsPage` (inside `AppLayout`) |
| `/evaluate` | `EvaluateFormPage` (standalone, outside `AppLayout` — has its own top bar) |

## Navigation

- **Sidebar** → `Pending Evaluations` → `/pending-evaluations`
- **Dashboard stat card** → `View Pending` → `/pending-evaluations`
- **PendingEvaluationsPage** → `Evaluate` / `Edit` buttons → navigates to `/evaluate` with `{ form, evaluatee }` in router state

## Backend counterpart

`SubmissionController` — `POST /{id}/submit`, `GET /pending`
