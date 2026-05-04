# Software Test Plan
## PeerTayo — Criteria-Based Peer Evaluation System
**Version:** 1.0  
**Branch:** `feature/vertical-slice-refactor`  
**Date:** May 2026  
**Prepared by:** Group — IT342 Camoro

---

## 1. Introduction

### 1.1 Purpose
This document defines the Software Test Plan for PeerTayo, covering all implemented functional requirements. It includes test cases, test steps, and automated test evidence for the backend API, web frontend, and mobile application.

### 1.2 Scope
| Platform | Coverage |
|----------|----------|
| Backend (Spring Boot) | All REST API endpoints — auth, evaluation, submission, results, notification, user |
| Web Frontend (React) | UI flows — login, register, dashboard, evaluation creation, submission, results |
| Mobile (Android/Kotlin) | Auth flows — login, register |

### 1.3 Test Approach
- **Backend:** Automated integration tests using JUnit 5 + MockMvc + H2 in-memory database
- **Frontend:** Manual UI testing against the running backend
- **Mobile:** Manual UI testing on Android emulator

---

## 2. Functional Requirements Coverage

| ID | Functional Requirement | Feature Slice | Test Type |
|----|------------------------|---------------|-----------|
| FR-01 | User can register with email and password | `auth/register` | Automated |
| FR-02 | User can log in with valid credentials | `auth/login` | Automated |
| FR-03 | User receives RESPONDENT role on registration | `auth/register` | Automated |
| FR-04 | User can log out and token is invalidated | `auth/token` | Automated |
| FR-05 | User can refresh session to get updated roles | `auth/token` | Automated |
| FR-06 | User can be promoted to FACILITATOR role | `auth/token` | Automated |
| FR-07 | User can sign in with Google OAuth2 | `auth/oauth2` | Manual |
| FR-08 | Facilitator can create an evaluation form | `evaluation/form` | Automated |
| FR-09 | Respondent cannot create an evaluation form (403) | `evaluation/form` | Automated |
| FR-10 | Facilitator can list their created forms | `evaluation/form` | Automated |
| FR-11 | Facilitator can update an evaluation form | `evaluation/form` | Automated |
| FR-12 | Facilitator can delete an evaluation form | `evaluation/form` | Automated |
| FR-13 | Evaluator can view pending evaluations | `evaluation/submission` | Automated |
| FR-14 | Evaluator can submit an evaluation with ratings | `evaluation/submission` | Automated |
| FR-15 | Evaluator cannot submit the same evaluation twice | `evaluation/submission` | Automated |
| FR-16 | Evaluator cannot submit after deadline | `evaluation/submission` | Automated |
| FR-17 | Evaluatee can view their own results | `evaluation/results` | Automated |
| FR-18 | Facilitator can view per-evaluatee results breakdown | `evaluation/results` | Automated |
| FR-19 | Respondent cannot view facilitator results (403) | `evaluation/results` | Automated |
| FR-20 | User receives notification when assigned to evaluation | `notification/list` | Automated |
| FR-21 | User can mark a notification as read | `notification/markread` | Automated |
| FR-22 | User can search for other users by name or email | `user/search` | Automated |
| FR-23 | Unauthenticated requests to protected endpoints return 401 | All | Automated |
| FR-24 | Dashboard shows pending evaluations and performance analytics | `dashboard` | Manual |
| FR-25 | Role-based UI — Facilitator sees Forms Created nav item | `shared/layouts` | Manual |
| FR-26 | Role-based routing — Respondent redirected from facilitator pages | `shared/routes` | Manual |

---

## 3. Test Cases

### 3.1 Authentication — Register (FR-01, FR-03)

| TC-ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-001 | Register with valid data | firstName, lastName, email, password (≥6 chars) | 201 Created, token returned, role = RESPONDENT | ✅ Pass |
| TC-002 | Register with duplicate email | Same email as existing user | 409 Conflict, error code DB-002 | ✅ Pass |
| TC-003 | Register with password < 6 chars | password = "abc" | 400 Bad Request, error code VALID-001 | ✅ Pass |
| TC-004 | Register with blank fields | All fields empty | 400 Bad Request | ✅ Pass |

### 3.2 Authentication — Login (FR-02)

| TC-ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-005 | Login with correct credentials | Valid email + password | 200 OK, token returned, user object with roles | ✅ Pass |
| TC-006 | Login with wrong password | Valid email + wrong password | 401 Unauthorized, error code AUTH-001 | ✅ Pass |
| TC-007 | Login with unknown email | Non-existent email | 404 Not Found, error code DB-001 | ✅ Pass |
| TC-008 | Login with blank fields | Empty email and password | 400 Bad Request, error code VALID-001 | ✅ Pass |

### 3.3 Authentication — Session Management (FR-04, FR-05, FR-06)

| TC-ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-009 | Get current user | Valid JWT in Authorization header | 200 OK, user object returned | ✅ Pass |
| TC-010 | Refresh session | Valid JWT | 200 OK, new token with current roles | ✅ Pass |
| TC-011 | Promote to Facilitator | Valid JWT (RESPONDENT) | 200 OK, roles include FACILITATOR, new token | ✅ Pass |
| TC-012 | Logout | Valid JWT | 200 OK, token blacklisted | ✅ Pass |
| TC-013 | Use blacklisted token after logout | Blacklisted JWT | 401 Unauthorized | ✅ Pass |
| TC-014 | Access protected endpoint without token | No Authorization header | 401 Unauthorized | ✅ Pass |

### 3.4 Authentication — Google OAuth2 (FR-07)

| TC-ID | Test Case | Steps | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-015 | Sign in with Google | Click "Continue with Google" → select account → redirect to /auth/callback | User logged in, redirected to /dashboard | Manual ✅ |
| TC-016 | New Google user gets RESPONDENT role | First-time Google sign-in | User created with RESPONDENT role | Manual ✅ |

### 3.5 Evaluation Form Management (FR-08 to FR-12)

| TC-ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-017 | Facilitator creates evaluation | Title, description, deadline, evaluateeIds, evaluatorIds | 201 Created, evaluation with status ACTIVE | ✅ Pass |
| TC-018 | Respondent attempts to create evaluation | Valid body, RESPONDENT token | 403 Forbidden, error code AUTH-003 | ✅ Pass |
| TC-019 | Unauthenticated create attempt | No token | 401 Unauthorized | ✅ Pass |
| TC-020 | Facilitator lists created evaluations | FACILITATOR token | 200 OK, array of evaluations | ✅ Pass |
| TC-021 | Respondent lists created evaluations | RESPONDENT token | 403 Forbidden | ✅ Pass |
| TC-022 | Facilitator updates evaluation | New title, description, deadline | 200 OK, updated evaluation returned | ✅ Pass |
| TC-023 | Facilitator deletes evaluation | Valid evaluation ID | 200 OK, "Evaluation deleted successfully" | ✅ Pass |
| TC-024 | Respondent attempts delete | RESPONDENT token | 403 Forbidden | ✅ Pass |

### 3.6 Evaluation Submission (FR-13 to FR-16)

| TC-ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-025 | Evaluator views pending evaluations | RESPONDENT token (assigned) | 200 OK, array with assigned evaluation | ✅ Pass |
| TC-026 | Unauthenticated pending request | No token | 401 Unauthorized | ✅ Pass |
| TC-027 | Evaluator submits evaluation | 10 criteria ratings (1–5), optional comment | 200 OK, "Evaluation submitted successfully" | ✅ Pass |
| TC-028 | Submit with rating out of range | rating = 10 | 400 Bad Request | ✅ Pass |
| TC-029 | Submit same evaluation twice | Second submit after first | 422 Unprocessable Entity, error code EVAL-002 | ✅ Pass |
| TC-030 | Submit after deadline | Evaluation with past deadline | 422 Unprocessable Entity, error code EVAL-001 | Manual ✅ |

### 3.7 Evaluation Results (FR-17 to FR-19)

| TC-ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-031 | Evaluatee views own results (no submissions) | RESPONDENT token | 200 OK, overallAverage = 0.0, totalResponses = 0 | ✅ Pass |
| TC-032 | Evaluatee views own results (after submission) | RESPONDENT token, after evaluator submits | 200 OK, overallAverage = 5.0, totalResponses = 1 | ✅ Pass |
| TC-033 | Facilitator views evaluation results | FACILITATOR token, valid evaluation ID | 200 OK, evaluatees array with scores | ✅ Pass |
| TC-034 | Respondent attempts to view facilitator results | RESPONDENT token | 403 Forbidden | ✅ Pass |

### 3.8 Notifications (FR-20, FR-21)

| TC-ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-035 | List notifications after being assigned | RESPONDENT token (assigned to evaluation) | 200 OK, notification "You have a new evaluation assigned." | ✅ Pass |
| TC-036 | Unauthenticated notification list | No token | 401 Unauthorized | ✅ Pass |
| TC-037 | Mark notification as read | Valid notification ID, owner token | 200 OK, "Notification marked as read" | ✅ Pass |
| TC-038 | Mark another user's notification | Different user's token | 404 Not Found | ✅ Pass |

### 3.9 User Search (FR-22, FR-23)

| TC-ID | Test Case | Input | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-039 | List users (no query) | Authenticated token | 200 OK, array of up to 20 users | ✅ Pass |
| TC-040 | Search users by name | q=alice | 200 OK, matching user returned | ✅ Pass |
| TC-041 | Search with no match | q=xyz_nobody | 200 OK, empty array | ✅ Pass |
| TC-042 | Unauthenticated user list | No token | 401 Unauthorized | ✅ Pass |

### 3.10 Frontend UI (FR-24 to FR-26)

| TC-ID | Test Case | Steps | Expected Result | Status |
|-------|-----------|-------|-----------------|--------|
| TC-043 | Dashboard loads for authenticated user | Log in → navigate to /dashboard | Dashboard renders with stat cards, activity feed | Manual ✅ |
| TC-044 | Pending evaluations shown on dashboard | Log in as evaluator → /dashboard | Pending Evaluations count > 0 | Manual ✅ |
| TC-045 | Facilitator sees "Forms Created" in sidebar | Log in as facilitator → check sidebar | "Forms Created" nav item visible | Manual ✅ |
| TC-046 | Respondent does not see "Forms Created" | Log in as respondent → check sidebar | "Forms Created" nav item hidden | Manual ✅ |
| TC-047 | Respondent redirected from /forms-created | Navigate to /forms-created as respondent | Redirected to /dashboard with upgrade modal | Manual ✅ |
| TC-048 | Create evaluation form (2-step wizard) | Facilitator → Forms Created → New Form → fill details → Create | Evaluation created, redirected to /forms-created | Manual ✅ |

---

## 4. Test Scripts / Test Steps

### 4.1 Manual Test Script — Full Auth Flow

**Precondition:** Backend running on `localhost:8080`, frontend on `localhost:5173`

**Steps:**
1. Navigate to `http://localhost:5173/register`
2. Fill in: First Name = "Juan", Last Name = "Cruz", Email = "juan@peertayo.test", Password = "pass123"
3. Click "Create Account"
4. **Expected:** Redirected to `/dashboard`, TopBar shows "Respondent"
5. Click "Sign Out" → confirm in modal
6. **Expected:** Redirected to `/login`
7. Log in with the same credentials
8. **Expected:** Redirected to `/dashboard`, session restored

### 4.2 Manual Test Script — Facilitator Promotion and Evaluation Creation

**Precondition:** Logged in as a Respondent

**Steps:**
1. Click "Create Now →" on the dashboard carousel
2. **Expected:** Upgrade modal appears — "Become a Facilitator"
3. Click "Yes, make me a Facilitator"
4. **Expected:** Redirected to `/forms-created/new`, TopBar now shows "Facilitator", sidebar shows "Forms Created"
5. Step 1 — review criteria → click "Next Step"
6. Step 2 — fill Title, Deadline, Description, add at least one Evaluator and one Evaluatee
7. Click "Create Evaluation"
8. **Expected:** Redirected to `/forms-created`, new form appears in the list

### 4.3 Manual Test Script — Evaluation Submission

**Precondition:** Logged in as a user who has been assigned as an Evaluator

**Steps:**
1. Navigate to `/dashboard`
2. **Expected:** "Pending Evaluations" stat card shows count > 0
3. Click "View Pending"
4. Select an evaluation from the list
5. Rate each of the 10 criteria (1–5 scale)
6. Optionally add a comment
7. Click "Submit"
8. **Expected:** Success message, evaluation removed from pending list

### 4.4 Manual Test Script — Google OAuth2 Sign-In

**Precondition:** Google OAuth2 configured, no duplicate redirect URIs in Google Cloud Console

**Steps:**
1. Navigate to `/login`
2. Click "Continue with Google"
3. Select a Google account
4. **Expected:** Redirected to `/auth/callback`, then to `/dashboard`
5. **Expected:** TopBar shows user's Google name and "Respondent" role

---

## 5. Automated Test Cases

### 5.1 Test Infrastructure

| Component | Technology |
|-----------|-----------|
| Test Framework | JUnit 5 (via `spring-boot-starter-test`) |
| HTTP Testing | Spring MockMvc |
| Database | H2 in-memory (test profile) |
| JSON Assertions | JsonPath |
| Security Testing | `spring-security-test` |
| Test Profile | `application-test.properties` |

### 5.2 Test Class Summary

| Test Class | Location | Tests | All Pass |
|------------|----------|-------|----------|
| `LoginControllerTest` | `auth/login/` | 4 | ✅ |
| `RegisterControllerTest` | `auth/register/` | 4 | ✅ |
| `TokenControllerTest` | `auth/token/` | 5 | ✅ |
| `EvaluationFormControllerTest` | `evaluation/form/` | 8 | ✅ |
| `SubmissionControllerTest` | `evaluation/submission/` | 5 | ✅ |
| `ResultsControllerTest` | `evaluation/results/` | 4 | ✅ |
| `NotificationControllerTest` | `notification/` | 4 | ✅ |
| `UserControllerTest` | `user/` | 4 | ✅ |
| `PeertayoApplicationTests` | root | 1 | ✅ |
| **Total** | | **39** | **✅ 39/39** |

### 5.3 Running the Automated Tests

```bash
# From the backend/ directory
./mvnw test -Dspring.profiles.active=test
```

**Expected output:**
```
Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 5.4 Key Automated Test Scenarios

#### Auth — Register Success (TC-001)
```java
// POST /api/v1/auth/register
// Body: { firstName, lastName, email, password }
// Assert: 201, data.token not empty, data.user.roles[0] = "RESPONDENT"
```

#### Evaluation — RBAC Enforcement (TC-018)
```java
// POST /api/v1/evaluations with RESPONDENT token
// Assert: 403, error.code = "AUTH-003"
```

#### Submission — Double Submit Prevention (TC-029)
```java
// POST /api/v1/evaluations/{id}/submit (twice with same token)
// Second call Assert: 422, error.code = "EVAL-002"
```

#### Results — Score Calculation (TC-032)
```java
// After evaluator submits all ratings = 5
// GET /api/v1/evaluations/my-results
// Assert: data.results.overallAverage = 5.0, totalResponses = 1
```

---

## 6. Test Environment

| Item | Value |
|------|-------|
| Backend URL | `http://localhost:8080` |
| Frontend URL | `http://localhost:5173` |
| Test DB | H2 in-memory (`jdbc:h2:mem:peertayo_test`) |
| Java Version | 17 |
| Spring Boot | 3.5.0 |
| Node.js | 18+ |
| Browser | Chrome (latest) |

---

## 7. Pass/Fail Criteria

| Criterion | Threshold |
|-----------|-----------|
| Automated tests pass rate | 100% (39/39) |
| Critical functional requirements covered | 100% (FR-01 to FR-26) |
| No regression in existing features | Zero regressions |
| API response format consistent | All responses follow `ApiResponse<T>` envelope |
| RBAC enforcement | All facilitator endpoints return 403 for respondents |

---

## 8. Known Limitations

| Item | Note |
|------|------|
| Google OAuth2 | Cannot be automated — requires browser interaction and Google account |
| Deadline-based submission rejection (TC-030) | Tested manually by setting a past deadline directly in DB |
| Mobile tests | Manual only — no automated UI tests for Android |
| Frontend unit tests | Not implemented — UI tested manually against running backend |
