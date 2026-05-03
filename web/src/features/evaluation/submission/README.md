# evaluation/submission

This slice is intentionally service-only.

The submission UI is not a standalone page — respondents access pending evaluations
directly from the Dashboard (`features/dashboard/DashboardPage.jsx`), which calls
`listPendingEvaluations()` and `submitEvaluation()` from this service.

The backend counterpart is `SubmissionController` (`POST /{id}/submit`, `GET /pending`).
