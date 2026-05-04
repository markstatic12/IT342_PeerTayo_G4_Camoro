# Automated Test Evidence
## PeerTayo — Criteria-Based Peer Evaluation System
**Branch:** `feature/vertical-slice-refactor`  
**Generated:** May 3, 2026  
**Test Command:** `mvnw.cmd test -Dspring.profiles.active=test`

> **Note:** Test execution screenshots are provided separately as image files.  
> This document contains the test logs, coverage reports, and automated test results.

---

## 1. Test Logs
> Source: `backend/target/surefire-reports/*.txt`

```
-------------------------------------------------------------------------------
Test set: edu.cit.camoro.peertayo.auth.login.LoginControllerTest
-------------------------------------------------------------------------------
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 13.82 s
-- in edu.cit.camoro.peertayo.auth.login.LoginControllerTest

-------------------------------------------------------------------------------
Test set: edu.cit.camoro.peertayo.auth.register.RegisterControllerTest
-------------------------------------------------------------------------------
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.714 s
-- in edu.cit.camoro.peertayo.auth.register.RegisterControllerTest

-------------------------------------------------------------------------------
Test set: edu.cit.camoro.peertayo.auth.token.TokenControllerTest
-------------------------------------------------------------------------------
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.458 s
-- in edu.cit.camoro.peertayo.auth.token.TokenControllerTest

-------------------------------------------------------------------------------
Test set: edu.cit.camoro.peertayo.evaluation.form.EvaluationFormControllerTest
-------------------------------------------------------------------------------
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 8.153 s
-- in edu.cit.camoro.peertayo.evaluation.form.EvaluationFormControllerTest

-------------------------------------------------------------------------------
Test set: edu.cit.camoro.peertayo.evaluation.submission.SubmissionControllerTest
-------------------------------------------------------------------------------
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.676 s
-- in edu.cit.camoro.peertayo.evaluation.submission.SubmissionControllerTest

-------------------------------------------------------------------------------
Test set: edu.cit.camoro.peertayo.evaluation.results.ResultsControllerTest
-------------------------------------------------------------------------------
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.827 s
-- in edu.cit.camoro.peertayo.evaluation.results.ResultsControllerTest

-------------------------------------------------------------------------------
Test set: edu.cit.camoro.peertayo.notification.NotificationControllerTest
-------------------------------------------------------------------------------
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.688 s
-- in edu.cit.camoro.peertayo.notification.NotificationControllerTest

-------------------------------------------------------------------------------
Test set: edu.cit.camoro.peertayo.PeertayoApplicationTests
-------------------------------------------------------------------------------
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.838 s
-- in edu.cit.camoro.peertayo.PeertayoApplicationTests

-------------------------------------------------------------------------------
Test set: edu.cit.camoro.peertayo.user.UserControllerTest
-------------------------------------------------------------------------------
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.425 s
-- in edu.cit.camoro.peertayo.user.UserControllerTest
```

---

## 2. Automated Test Results
> Source: Maven Surefire Plugin — `backend/target/surefire-reports/`

### 2.1 Summary

| Test Class | Tests | Failures | Errors | Skipped | Time (s) |
|------------|-------|----------|--------|---------|----------|
| `auth.login.LoginControllerTest` | 4 | 0 | 0 | 0 | 13.82 |
| `auth.register.RegisterControllerTest` | 4 | 0 | 0 | 0 | 0.71 |
| `auth.token.TokenControllerTest` | 5 | 0 | 0 | 0 | 1.46 |
| `evaluation.form.EvaluationFormControllerTest` | 8 | 0 | 0 | 0 | 8.15 |
| `evaluation.submission.SubmissionControllerTest` | 5 | 0 | 0 | 0 | 4.68 |
| `evaluation.results.ResultsControllerTest` | 4 | 0 | 0 | 0 | 3.83 |
| `notification.NotificationControllerTest` | 4 | 0 | 0 | 0 | 3.69 |
| `PeertayoApplicationTests` | 1 | 0 | 0 | 0 | 1.84 |
| `user.UserControllerTest` | 4 | 0 | 0 | 0 | 3.43 |
| **TOTAL** | **39** | **0** | **0** | **0** | **40.61** |

**Overall: ✅ BUILD SUCCESS — 39/39 tests passed**

### 2.2 Individual Test Methods

#### auth.login.LoginControllerTest
| Method | Result |
|--------|--------|
| `login_success` | ✅ PASS |
| `login_wrongPassword` | ✅ PASS |
| `login_unknownEmail` | ✅ PASS |
| `login_blankFields` | ✅ PASS |

#### auth.register.RegisterControllerTest
| Method | Result |
|--------|--------|
| `register_success` | ✅ PASS |
| `register_duplicateEmail` | ✅ PASS |
| `register_shortPassword` | ✅ PASS |
| `register_missingFields` | ✅ PASS |

#### auth.token.TokenControllerTest
| Method | Result |
|--------|--------|
| `getMe_success` | ✅ PASS |
| `refresh_success` | ✅ PASS |
| `promote_success` | ✅ PASS |
| `logout_blacklistsToken` | ✅ PASS |
| `getMe_noToken` | ✅ PASS |

#### evaluation.form.EvaluationFormControllerTest
| Method | Result |
|--------|--------|
| `create_asFacilitator_success` | ✅ PASS |
| `create_asRespondent_forbidden` | ✅ PASS |
| `create_noToken_unauthorized` | ✅ PASS |
| `getCreated_asFacilitator` | ✅ PASS |
| `getCreated_asRespondent_forbidden` | ✅ PASS |
| `update_success` | ✅ PASS |
| `delete_success` | ✅ PASS |
| `delete_asRespondent_forbidden` | ✅ PASS |

#### evaluation.submission.SubmissionControllerTest
| Method | Result |
|--------|--------|
| `getPending_success` | ✅ PASS |
| `getPending_noToken` | ✅ PASS |
| `submit_success` | ✅ PASS |
| `submit_invalidRating` | ✅ PASS |
| `submit_alreadySubmitted` | ✅ PASS |

#### evaluation.results.ResultsControllerTest
| Method | Result |
|--------|--------|
| `getMyResults_empty` | ✅ PASS |
| `getMyResults_afterSubmission` | ✅ PASS |
| `getEvaluationResults_asFacilitator` | ✅ PASS |
| `getEvaluationResults_asRespondent_forbidden` | ✅ PASS |

#### notification.NotificationControllerTest
| Method | Result |
|--------|--------|
| `getNotifications_success` | ✅ PASS |
| `getNotifications_noToken` | ✅ PASS |
| `markAsRead_success` | ✅ PASS |
| `markAsRead_wrongUser` | ✅ PASS |

#### user.UserControllerTest
| Method | Result |
|--------|--------|
| `listUsers_success` | ✅ PASS |
| `searchUsers_byName` | ✅ PASS |
| `searchUsers_noMatch` | ✅ PASS |
| `listUsers_noToken` | ✅ PASS |

#### PeertayoApplicationTests
| Method | Result |
|--------|--------|
| `contextLoads` | ✅ PASS |

---

## 3. Coverage Report
> Source: JaCoCo Maven Plugin — `backend/target/site/jacoco/`  
> HTML report: `backend/target/site/jacoco/index.html`  
> Raw data: `backend/target/site/jacoco/jacoco.csv`

### 3.1 Coverage by Feature Slice

| Package (Feature Slice) | Instructions Covered | Instructions Missed | Line Coverage | Method Coverage |
|-------------------------|---------------------|---------------------|---------------|-----------------|
| `auth.login` | 45 / 45 | 0 | 100% | 100% |
| `auth.register` | 79 / 79 | 0 | 100% | 100% |
| `auth.token` | 141 / 159 | 18 | ~89% | ~82% |
| `auth.shared` | 48 / 48 | 0 | 100% | 100% |
| `auth.management` | 60 / 64 | 4 | ~94% | 100% |
| `auth.security` | 322 / 401 | 79 | ~80% | ~82% |
| `auth.entity` | 15 / 15 | 0 | 100% | 100% |
| `evaluation.form` | 342 / 382 | 40 | ~90% | ~85% |
| `evaluation.submission` | 209 / 231 | 22 | ~90% | ~87% |
| `evaluation.results` | 282 / 334 | 52 | ~84% | ~79% |
| `evaluation.config` | 28 / 44 | 16 | ~64% | ~43% |
| `notification.list` | 45 / 50 | 5 | ~90% | ~75% |
| `notification.markread` | 49 / 59 | 10 | ~83% | ~60% |
| `shared.config` | 119 / 120 | 1 | ~99% | ~83% |
| `shared.security` | 162 / 162 | 0 | 100% | 100% |
| `shared.exception` | 91 / 105 | 14 | ~87% | ~87% |
| `shared.response` | 80 / 118 | 38 | ~68% | ~67% |

### 3.2 Classes with 0% Coverage (Not Tested)

| Class | Reason |
|-------|--------|
| `auth.oauth2.GoogleOAuth2Service` | Google OAuth2 requires browser interaction — cannot be automated |
| `auth.security.OAuth2LoginSuccessHandler` | Same — OAuth2 callback not testable with MockMvc |

These two classes are excluded from automated testing by design. They are covered by manual testing (TC-015, TC-016).

### 3.3 Coverage Notes

- **Core auth flows** (`login`, `register`, `shared`) achieve **100% instruction coverage**
- **Evaluation slices** average **~88% line coverage** — uncovered lines are primarily error-handling branches for edge cases not triggered in the test suite
- **OAuth2 classes** are 0% automated coverage — intentional, covered manually
- **Overall project coverage** is approximately **82% instruction coverage** across all tested classes

### 3.4 How to View the Full HTML Report

Open in any browser:
```
backend/target/site/jacoco/index.html
```

The report is organized by package (matching the VSA feature slices) and allows drill-down to individual class and method coverage with line-by-line highlighting.
