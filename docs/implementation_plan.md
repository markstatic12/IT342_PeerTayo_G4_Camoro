# PeerTayo Mobile Dashboard — Full Implementation

Implement the complete mobile app dashboard and all screens shown in the mockup HTML (`peertayo_mobile.html`), matching the web app's design system and connecting to the same backend API endpoints.

## Current State
- **Landing, Login, Register** are implemented using ViewBinding + XML layouts + Activities
- **MainActivity** currently shows a plain "Welcome!" screen with just a logout button
- **Data layer**: `AuthApi`, `RetrofitClient`, `AuthRepository`, `AuthModels` are working
- **Design system**: Colors, dimensions, styles, themes all match the web's dark theme

## Proposed Architecture

The app uses **Activities + ViewBinding + XML layouts** (no Jetpack Compose). We'll keep this pattern and add:

1. **DashboardActivity** — replaces `MainActivity` as the post-login hub with bottom navigation
2. **Fragment-based screens** — Each bottom nav tab loads a fragment inside `DashboardActivity`
3. **New API endpoints** — Evaluation, submission, and results APIs matching the web services
4. **Token-based auth** — SharedPreferences for JWT + auth interceptor in Retrofit

## Screens to Implement (from mockup)

| Screen | Type | Nav |
|---|---|---|
| Dashboard / Home | Fragment | Bottom Nav tab |
| Pending Evaluations | Fragment | Bottom Nav tab |
| Evaluation Form | Activity (full-screen) | From Pending |
| Submission Success | Part of Eval Form Activity | After submit |
| My Results | Fragment | Bottom Nav tab |
| Result Detail | Fragment (replaces Results) | From Results |
| My Completed Forms | Fragment | Bottom Nav tab |
| Forms Created (Facilitator) | Fragment | Bottom Nav tab (conditional) |
| Create Evaluation (Facilitator) | Activity (full-screen) | From Forms |
| Notifications | Activity (full-screen) | From topbar |
| Settings / Profile | Activity (full-screen) | From topbar |

## Proposed Changes

### Data Layer

#### [NEW] data/model/EvaluationModels.kt
Data classes for:
- `PendingEvaluation` (id, title, evaluateeName, deadline, status, criteria)
- `EvaluationResult` (id, title, overallAverage, questionAverages, totalResponses, comments)
- `CompletedForm` (id, title, evaluateeName, submittedAt)
- `CreatedEvaluation` (id, title, deadline, status, evaluatorCount, submissionCount)
- `SubmittedSummary` (totalSubmitted, submittedThisMonth)
- `SubmitEvaluationRequest` / response types

#### [NEW] data/api/EvaluationApi.kt
Retrofit interface with endpoints matching the web:
- `GET /evaluations/pending` → list pending
- `POST /evaluations/{id}/submit` → submit evaluation
- `GET /evaluations/submitted/summary` → submitted summary
- `GET /evaluations/completed` → completed forms
- `GET /evaluations/my-results` → my results
- `GET /evaluations/created` → created evaluations (facilitator)
- `POST /evaluations` → create evaluation (facilitator)
- `GET /users/search?q=...` → user search (for evaluator/evaluatee assignment)
- `POST /auth/promote-to-facilitator` → promote user role

#### [NEW] data/repository/EvaluationRepository.kt
Repository wrapping `EvaluationApi` calls.

#### [MODIFY] data/api/RetrofitClient.kt
- Add auth interceptor (reads JWT from SharedPreferences)
- Expose `evaluationApi` alongside existing `authApi`

#### [NEW] data/local/SessionManager.kt
SharedPreferences wrapper for JWT token + user info persistence.

#### [MODIFY] auth/login/LoginActivity.kt
- After successful login, save token/user via `SessionManager`
- Navigate to `DashboardActivity` instead of `MainActivity`

#### [MODIFY] auth/register/RegisterActivity.kt
- After successful register, save token/user via `SessionManager`
- Navigate to `DashboardActivity`

---

### Dashboard Activity & Navigation

#### [NEW] dashboard/DashboardActivity.kt
- Uses BottomNavigationView with 4–5 tabs (Home, Pending, Results, Done, Forms)
- `FragmentContainerView` for swapping fragments
- TopBar with logo, notification bell, profile icon
- Conditionally shows "Forms" tab for facilitators
- Handles back-stack per tab

#### [NEW] layout/activity_dashboard.xml
- CoordinatorLayout with top bar, fragment container, and bottom nav
- Matches mockup's dark design: `#080c12` bg, glass-morphism cards

---

### Fragments (Bottom Nav Tabs)

#### [NEW] dashboard/HomeFragment.kt + layout/fragment_home.xml
- Greeting card with user name, date, role pill
- Facilitator promo banner (visible if respondent only)
- 2×2 shortcut grid (Pending count, My Average, Submitted, Needs Attention)
- Facilitator overview section (Forms Health — conditional)
- Recent Activity list
- Motivational quote (API box)

#### [NEW] dashboard/PendingFragment.kt + layout/fragment_pending.xml
- Stats row (Pending, Urgent, Done counts)
- Filter pills (All, Urgent, This Week, New)
- Section dividers (Urgent / Active)
- Evaluation cards with countdown, urgency bar, View & Submit button

#### [NEW] dashboard/ResultsFragment.kt + layout/fragment_results.xml
- Stats row (Received, Average, Improved)
- Filter pills
- Result cards with score circle, criteria pills

#### [NEW] dashboard/ResultDetailFragment.kt + layout/fragment_result_detail.xml
- Overall score headline (4.3 / 5.0 with delta)
- Criteria breakdown bars (10 criteria)
- Anonymous feedback comments

#### [NEW] dashboard/CompletedFragment.kt + layout/fragment_completed.xml
- Stats row (Submitted, Evaluatees, Last date)
- Filter pills
- Completed form cards with user avatar, evaluatee name, timestamp, ✓ Done badge

#### [NEW] dashboard/FormsFragment.kt + layout/fragment_forms.xml (Facilitator)
- Header with "Forms Created" title + "＋ Create" button
- Stats row (Forms, Responses, Attention)
- Filter pills (All, Active, Alert, Closed)
- Section dividers (Needs Attention / Active)
- Form cards with progress bars, evaluatee pills, View/Edit buttons

---

### Full-Screen Activities

#### [NEW] evaluation/EvaluateFormActivity.kt + layout/activity_evaluate_form.xml
- Top bar with back arrow + "Evaluation Form" title
- Header card (title, evaluatee, deadline warning)
- 10 rating rows (1-5 buttons each)
- Optional comment textarea
- Progress counter "X of 10 answered"
- Submit button (disabled until all 10 answered)

#### [NEW] evaluation/EvaluateSuccessActivity.kt (or fragment within EvaluateForm)
- Success icon, "Submitted!" heading
- Confirmation message with evaluatee name
- "Back to Pending" button

#### [NEW] evaluation/CreateEvaluationActivity.kt + layout/activity_create_evaluation.xml (Facilitator)
- 2-step stepper (Criteria → Details)
- Step 1: Shows 10 predefined criteria
- Step 2: Title, Description, Evaluators (chip search), Evaluatees (chip search), Deadline
- Back / Publish buttons

#### [NEW] notification/NotificationActivity.kt + layout/activity_notifications.xml
- Header with unread count + "Mark all read"
- Notification items with colored left border, message, type chip, time

#### [NEW] settings/SettingsActivity.kt + layout/activity_settings.xml
- Profile section (avatar, name, email, role pill)
- Account section (First Name, Last Name, Email — read-only)
- Security section (Change Password)
- App section (Notifications, Privacy Policy, Log Out)

---

### Resource Files

#### [NEW] res/menu/bottom_nav_menu.xml
Menu items for Home, Pending, Results, Done, Forms

#### [NEW] res/drawable/ (various)
- `bg_card_glass.xml` — glass card background (rounded, semi-transparent)
- `bg_greeting_card.xml` — gradient background for greeting
- `bg_shortcut_card.xml` — shortcut card background
- `bg_rating_btn.xml` / `bg_rating_btn_selected.xml` — rating button states
- `bg_bottom_nav.xml` — bottom nav background
- `bg_progress_track.xml` / `bg_progress_fill_*.xml` — progress bars
- `ic_home.xml`, `ic_pending.xml`, `ic_results.xml`, `ic_done.xml`, `ic_forms.xml` — nav icons
- Various card/badge/pill backgrounds

#### [MODIFY] res/values/colors.xml
Add the mockup's cyan/orange palette alongside existing blue palette:
- `cyan_primary` (#00d4d4), `orange_accent` (#E8845A), etc.

#### [MODIFY] res/values/styles.xml
Add dashboard-specific styles (glass cards, badges, stat numbers)

#### [MODIFY] res/values/strings.xml
Add all dashboard-related strings

#### [MODIFY] AndroidManifest.xml
Register new activities (DashboardActivity, EvaluateFormActivity, CreateEvaluationActivity, NotificationActivity, SettingsActivity)

---

### ViewModels

#### [NEW] dashboard/DashboardViewModel.kt
Loads all dashboard data: pending evals, submitted summary, my results, created evals (if facilitator). Exposes LiveData for each.

#### [NEW] evaluation/PendingViewModel.kt
Loads and filters pending evaluations.

#### [NEW] evaluation/ResultsViewModel.kt
Loads my results + result details.

#### [NEW] evaluation/EvaluateFormViewModel.kt
Manages form state, rating selections, submit action.

#### [NEW] evaluation/FormsViewModel.kt (Facilitator)
Lists created evaluations, create/publish new forms.

---

## Open Questions

> [!IMPORTANT]
> **Backend connectivity**: The mobile currently points to `http://10.0.2.2:8080/api/v1/`. Is the backend actually running and accessible? Should I keep this URL or update it?

> [!IMPORTANT]
> **Token persistence**: The current login flow navigates to `MainActivity` but doesn't persist the JWT token. I'll add `SharedPreferences`-based session management. Is that acceptable, or do you prefer EncryptedSharedPreferences?

> [!NOTE]
> **Design palette**: The mockup uses a **cyan (#00d4d4) + orange (#E8845A)** palette while the existing web/mobile uses **blue (#3B82F6)**. The mockup is the target UI — I'll implement the cyan/orange palette as shown. The web's blue palette stays intact for the existing landing/login/register screens.

## Verification Plan

### Automated Tests
- Build the project with `./gradlew assembleDebug` to verify compilation
- Verify all XML layouts inflate correctly

### Manual Verification
- Visual comparison with the mockup HTML by running on emulator
- Navigate through all bottom nav tabs
- Test the evaluation form flow (rate all 10 criteria → submit → success)
- Test facilitator promotion flow
- Test notification and settings screens
