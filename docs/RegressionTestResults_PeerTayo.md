# Full Regression Test Results
## PeerTayo — Criteria-Based Peer Evaluation System
**Version:** 1.0  
**Branch:** `feature/vertical-slice-refactor`  
**Test Execution Date:** May 3, 2026  
**Executed by:** Automated (JUnit 5 + MockMvc) + Manual (Browser + Android Emulator)

---

## 1. Regression Test Summary

| Platform | Test Type | Total Tests | Passed | Failed | Skipped |
|----------|-----------|-------------|--------|--------|---------|
| Backend API | Automated | 39 | **39** | 0 | 0 |
| Web Frontend | Manual | 13 | **13** | 0 | 0 |
| Mobile (Android) | Manual | 4 | **4** | 0 | 0 |
| **Total** | | **56** | **56** | **0** | **0** |

**Overall Result: ✅ PASS — No regressions detected**

---

## 2. Backend Automated Test Results

**Command executed:**
```bash
./mvnw test -Dspring.profiles.active=test
```

**Environment:**
- Java 21.0.9
- Spring Boot 3.5.0
- H2 in-memory database (test profile)
- JUnit 5 via `spring-boot-starter-test`

### 2.1 Per-Class Results

| Test Class | Slice | Tests | Passed | Failed | Time (s) |
|------------|-------|-------|--------|--------|----------|
| `LoginControllerTest` | `auth/login` | 4 | 4 | 0 | 9.31 |
| `RegisterControllerTest` | `auth/register` | 4 | 4 | 0 | 0.64 |
| `TokenControllerTest` | `auth/token` | 5 | 5 | 0 | 1.34 |
| `EvaluationFormControllerTest` | `evaluation/form` | 8 | 8 | 0 | 8.22 |
| `SubmissionControllerTest` | `evaluation/submission` | 5 | 5 | 0 | 4.51 |
| `ResultsControllerTest` | `evaluation/results` | 4 | 4 | 0 | 3.65 |
| `NotificationControllerTest` | `notification` | 4 | 4 | 0 | 3.72 |
| `UserControllerTest` | `user/search` | 4 | 4 | 0 | 3.47 |
| `PeertayoApplicationTests` | root | 1 | 1 | 0 | 0.94 |
| **TOTAL** | | **39** | **39** | **0** | **35.8** |

### 2.2 Detailed Test Results

#### auth/login — LoginControllerTest
| Test | Result |
|------|--------|
| login_success — 200 with token and RESPONDENT role | ✅ PASS |
| login_wrongPassword — 401 AUTH-001 | ✅ PASS |
| login_unknownEmail — 404 DB-001 | ✅ PASS |
| login_blankFields — 400 VALID-001 | ✅ PASS |

#### auth/register — RegisterControllerTest
| Test | Result |
|------|--------|
| register_success — 201 with RESPONDENT role | ✅ PASS |
| register_duplicateEmail — 409 DB-002 | ✅ PASS |
| register_shortPassword — 400 VALID-001 | ✅ PASS |
| register_missingFields — 400 | ✅ PASS |

#### auth/token — TokenControllerTest
| Test | Result |
|------|--------|
| getMe_success — 200 with user object | ✅ PASS |
| refresh_success — 200 with new token | ✅ PASS |
| promote_success — 200 with FACILITATOR role | ✅ PASS |
| logout_blacklistsToken — token rejected after logout | ✅ PASS |
| getMe_noToken — 401 | ✅ PASS |

#### evaluation/form — EvaluationFormControllerTest
| Test | Result |
|------|--------|
| create_asFacilitator_success — 201 ACTIVE | ✅ PASS |
| create_asRespondent_forbidden — 403 AUTH-003 | ✅ PASS |
| create_noToken_unauthorized — 401 | ✅ PASS |
| getCreated_asFacilitator — 200 array | ✅ PASS |
| getCreated_asRespondent_forbidden — 403 | ✅ PASS |
| update_success — 200 updated title | ✅ PASS |
| delete_success — 200 deleted message | ✅ PASS |
| delete_asRespondent_forbidden — 403 | ✅ PASS |

#### evaluation/submission — SubmissionControllerTest
| Test | Result |
|------|--------|
| getPending_success — 200 with assigned evaluation | ✅ PASS |
| getPending_noToken — 401 | ✅ PASS |
| submit_success — 200 submitted | ✅ PASS |
| submit_invalidRating — 400 | ✅ PASS |
| submit_alreadySubmitted — 422 EVAL-002 | ✅ PASS |

#### evaluation/results — ResultsControllerTest
| Test | Result |
|------|--------|
| getMyResults_empty — 200 overallAverage=0.0 | ✅ PASS |
| getMyResults_afterSubmission — 200 overallAverage=5.0 | ✅ PASS |
| getEvaluationResults_asFacilitator — 200 evaluatees array | ✅ PASS |
| getEvaluationResults_asRespondent_forbidden — 403 | ✅ PASS |

#### notification — NotificationControllerTest
| Test | Result |
|------|--------|
| getNotifications_success — 200 with notification | ✅ PASS |
| getNotifications_noToken — 401 | ✅ PASS |
| markAsRead_success — 200 marked as read | ✅ PASS |
| markAsRead_wrongUser — 404 | ✅ PASS |

#### user — UserControllerTest
| Test | Result |
|------|--------|
| listUsers_success — 200 array | ✅ PASS |
| searchUsers_byName — 200 matching user | ✅ PASS |
| searchUsers_noMatch — 200 empty array | ✅ PASS |
| listUsers_noToken — 401 | ✅ PASS |

---

## 3. Web Frontend Manual Test Results

**Environment:** Chrome (latest), frontend at `localhost:5173`, backend at `localhost:8080`

| TC-ID | Test Case | Result | Notes |
|-------|-----------|--------|-------|
| TC-043 | Dashboard loads for authenticated user | ✅ PASS | Stat cards, activity feed render correctly |
| TC-044 | Pending evaluations shown on dashboard | ✅ PASS | Count updates after evaluation assigned |
| TC-045 | Facilitator sees "Forms Created" in sidebar | ✅ PASS | Appears after promotion |
| TC-046 | Respondent does not see "Forms Created" | ✅ PASS | Hidden for RESPONDENT role |
| TC-047 | Respondent redirected from /forms-created | ✅ PASS | Upgrade modal shown |
| TC-048 | Create evaluation form (2-step wizard) | ✅ PASS | Form created, redirected to list |
| TC-015 | Google OAuth2 sign-in | ✅ PASS | Redirects to /dashboard after auth |
| TC-016 | New Google user gets RESPONDENT role | ✅ PASS | Role shown as "Respondent" in TopBar |
| — | Session refresh on page reload | ✅ PASS | Roles stay current after reload |
| — | Logout modal UI | ✅ PASS | Modal matches system design |
| — | Role badge in sidebar for facilitators | ✅ PASS | "You're a Facilitator" badge visible |
| — | Evaluation results page loads | ✅ PASS | Evaluatee breakdown table renders |
| — | Step scroll-to-top on evaluation wizard | ✅ PASS | Page scrolls to top on step change |

---

## 4. Mobile Manual Test Results

**Environment:** Android Emulator (API 34), backend at `10.0.2.2:8080`

| TC-ID | Test Case | Result | Notes |
|-------|-----------|--------|-------|
| — | Register new user | ✅ PASS | Account created, navigates to MainActivity |
| — | Login with valid credentials | ✅ PASS | Token stored, welcome screen shown |
| — | Login with wrong password | ✅ PASS | Error toast displayed |
| — | Logout clears token | ✅ PASS | Redirected to LoginActivity |

---

## 5. Bugs and Regressions Found

### 5.1 Bugs Found During Regression

| Bug ID | Description | Severity | Status |
|--------|-------------|----------|--------|
| BUG-001 | `loginWithToken()` (OAuth2 callback) double-wrapped user object — `user.roles` was `undefined` | High | ✅ Fixed |
| BUG-002 | `refreshCurrentUser()` same double-wrap issue — roles lost after page reload | High | ✅ Fixed |
| BUG-003 | `login()` in `AuthService` not `@Transactional` — roles collection potentially unloaded | Medium | ✅ Fixed |
| BUG-004 | Silent `ensureFacilitatorRole()` in `EvaluationService.create()` — implicit role escalation | High | ✅ Fixed |
| BUG-005 | Stale JWT after role promotion — old token lacked `ROLE_FACILITATOR` authority | High | ✅ Fixed |
| BUG-006 | Duplicate redirect URI in Google Cloud Console causing `Error 400: invalid_request` | Medium | ✅ Fixed (manual) |
| BUG-007 | `window.scrollTo()` had no effect — wrong scroll container targeted | Low | ✅ Fixed |
| BUG-008 | Sidebar/TopBar font inconsistency — `<button>` elements not inheriting `Plus Jakarta Sans` | Low | ✅ Fixed |

### 5.2 Test Failures During Development (Resolved)

| Failure | Root Cause | Fix Applied |
|---------|------------|-------------|
| `TokenControllerTest.refresh_success` → 401 | `TokenBlacklistService` in-memory state persisted across `@Transactional` rollbacks — blacklisted token from `logout` test affected `refresh` test | Used unique email per test to get unique tokens |
| `EvaluationFormControllerTest.create_asFacilitator_success` → 422 | Test used same user as both evaluator and evaluatee — self-evaluation blocked by business rule | Used distinct users for evaluator and evaluatee |
| `SubmissionControllerTest` all → `PathNotFoundException` | `registerAndGetUserId` + `registerAndGetToken` called twice for same email — second call returned 409 with no `data` | Refactored `TestHelper` to return both `id` and `token` in a single `register()` call |

---

## 6. Regression Verdict

| Area | Pre-Refactor | Post-Refactor | Regression? |
|------|-------------|---------------|-------------|
| User registration | ✅ Working | ✅ Working | None |
| User login | ✅ Working | ✅ Working | None |
| Google OAuth2 | ✅ Working | ✅ Working | None |
| Session refresh | ✅ Working | ✅ Working | None |
| Role promotion | ✅ Working | ✅ Working | None |
| Evaluation creation | ✅ Working | ✅ Working | None |
| Evaluation submission | ✅ Working | ✅ Working | None |
| Results viewing | ✅ Working | ✅ Working | None |
| Notifications | ✅ Working | ✅ Working | None |
| User search | ✅ Working | ✅ Working | None |
| RBAC enforcement | ✅ Working | ✅ Working | None |
| Frontend routing | ✅ Working | ✅ Working | None |
| Mobile auth | ✅ Working | ✅ Working | None |

**Conclusion: Zero regressions. All 56 tests pass. All functional requirements verified.**
