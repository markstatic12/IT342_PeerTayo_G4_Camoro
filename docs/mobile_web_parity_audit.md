# PeerTayo — Mobile ↔ Web Parity Audit
> **Date:** May 14, 2026 | **Reviewer:** Antigravity AI

---

## Executive Summary

After a screen-by-screen, feature-by-feature comparison of the web (React/Vite) and mobile (Android/Kotlin) platforms, **12 critical parity gaps** were identified. The progress audit previously marked all items as ✅ DONE, but a deeper analysis reveals significant functional and behavioral mismatches.

---

## 🔴 Critical Gaps (Missing Features / Broken Flows)

### GAP-01 · Results: "My Results" vs. Flat List (HIGH)
**Web:** `MyResultsPage` shows:
- 4-stat summary strip (Overall Avg, Evaluations Received, Total Responses, Highest Criterion)
- Left panel: evaluation card list with **search**, **filter tabs** (All / Highest / Most Recent), **Archive toggle**, 3-dot context menu (Archive / Delete per item)
- Right panel: criteria breakdown using **dot indicators** (1–5 filled dots), peer comments, "View Full Details" button that opens a **full-detail modal** with overall score hero, info box, and full criteria bar chart

**Mobile:** `ResultsFragment` shows:
- A 3-stat strip with **wrong labels**: "Received", "Average", "Improved" — "Improved" is meaningless and not present on web
- A flat `RecyclerView` of `EvaluationResultSummary` items  
- Tapping a card navigates to `ResultDetailFragment` which shows criteria as `ProgressBar` — NOT dot indicators
- **Missing:** search bar, filter tabs (All / Highest / Most Recent), archive/unarchive action, 3-dot context menu per result card
- **Missing:** the 4th stat card ("Highest Criterion")
- **Wrong terminology:** stats say "Received", "Average", "Improved" — web says "Overall Avg Score", "Evaluations Received", "Total Responses", "Highest Criterion"

### GAP-02 · Pending: Flat List vs. Grouped-by-Form (HIGH)
**Web:** `PendingEvaluationsPage` groups the flat API list **by evaluation form** client-side. Each card shows the form title, total evaluatees count, deadline, progress bar (done/total). Tapping a card shows a **detail panel** listing each evaluatee with an individual "Evaluate →" button per person, and a "Start Next" footer action.

**Mobile:** `PendingFragment` passes the raw flat items directly to `PendingAdapter` — each row is a single evaluatee assignment. The filter pills ("All", "Urgent", "This Week", "New") **do not match** the web's filter tabs ("All", "Urgent", "Missed").

- **Missing:** grouping by form  
- **Missing:** detail panel with per-evaluatee "Evaluate →" button  
- **Missing:** "Submitted This Month" stat (web has it, mobile only has Pending + Urgent)  
- **Missing:** search bar  
- **Missing:** "Missed" filter (mobile has "This Week" and "New" instead)
- **Missing:** Archive/Unarchive per pending evaluation  

### GAP-03 · Completed: Structural Mismatch (HIGH)
**Web:** `MyCompletedFormsPage` has a 3-panel structure:
- 3-stat summary strip: Total Submitted, Submitted This Month, Avg Score Given
- Left panel: form cards with search + filter (All / This Week) + Archive toggle + 3-dot menu (Archive/Delete)
- Right panel: detail with evaluatees list, per-evaluatee criteria breakdown, comment preview, "View" button opening a **full modal** with criteria cards (1–5 dots, score label)

**Mobile:** `CompletedFragment` uses a flat `RecyclerView` of `CompletedForm` items from `getCompletedForms()`. The API response model (`CompletedFormsResponse`) returns a flat list, not the grouped/per-form structure the web expects.

- **Missing:** search bar
- **Missing:** filter tabs (All / This Week)
- **Missing:** Archive/Unarchive per completed form
- **Missing:** detail panel with per-evaluatee breakdown
- **Missing:** "View" modal with criteria + score label + comment
- **Wrong stat:** "Evaluatees" and "Last" columns don't match web's "Total Submitted", "Submitted This Month", "Avg Score Given"

### GAP-04 · Forms Created: Archive, Delete, Search, Status Tabs (MEDIUM)
**Web:** `FormsCreatedPage` has:
- 4-stat strip: Total Forms, Active, Needs Attention, Closed
- Search bar
- Filter tabs: All / Active / Needs Attention / Closed
- Archives toggle button
- Per-card: View Results → icon, Archive icon, Edit icon (disabled if closed), Delete icon
- Inline overdue detection ("Needs Attention" status)

**Mobile:** `FormsFragment` + `FormsAdapter` show a simple list with no search, no filter tabs, no archive/delete actions, no status-based filtering, no overdue detection, and no "Needs Attention" concept.

### GAP-05 · Home: Greeting Logic + Recent Activity Feed (MEDIUM)
**Web:** `DashboardPage` greeting says "Hello, {firstName} 👋" with today's full date label and a real-time alert: "N evaluations due in 2 days" OR "No upcoming deadlines".

**Mobile:** `HomeFragment` says "Good morning/afternoon/evening, {name}" — different phrasing, different emoji absence, no deadline alert. The "Needs Attention" shortcut card on mobile has no web equivalent.

**Web Recent Activity** is dynamically computed from API data (submitted count, pending count, active forms count, results). Mobile has a static `tvRecentEmpty` placeholder with no dynamic activity feed.

### GAP-06 · Promote to Facilitator Flow (MEDIUM)
**Web:** Tapping "Create Now →" in the carousel when not a facilitator shows a **Facilitator Upgrade Modal** with a description and a "Yes, make me a Facilitator" button that calls `promoteToFacilitator()` API.

**Mobile:** `HomeFragment` sets `promoBanner.visibility = View.VISIBLE` and `btnPromote.setOnClickListener { /* TODO: Call promote API */ }` — the promote API call is NOT implemented. It's a TODO stub.

### GAP-07 · Home: Carousel Promo Banner Logic is Inverted (MEDIUM)
**Web:** The carousel is shown to **all** users; the "Create Now →" button checks if the user is a facilitator and either navigates to `/forms-created/new` or opens the upgrade modal.

**Mobile:** The `promoBanner` is only shown when `!sessionManager.isFacilitator()`. Facilitators see nothing in the promo area. The carousel slides (Feature Awareness / Productivity / Insights) with auto-advance are **not implemented** on mobile at all.

### GAP-08 · Results Detail: Criteria Visualization (LOW-MEDIUM)
**Web:** Criteria are displayed as **5 colored dot indicators** (filled/empty) with a per-criterion color and a numeric score.

**Mobile:** `ResultDetailFragment` uses `ProgressBar` with `progress = (score / 5f * 100).toInt()`. This is a horizontal bar, not dot indicators. The web's dot-based visualization is the intended UX.

### GAP-09 · Pending: "Submitted This Month" Stat Missing (LOW)
**Web:** The 3rd stat card on the Pending page shows "Submitted This Month" (from `getSubmittedSummary()`).
**Mobile:** The 3rd stat is "Done" — this is meaningless for a pending evaluations screen.

### GAP-10 · Notifications: Static Placeholder (LOW)
**Web:** No dedicated Notifications page exists (notifications are embedded in the dashboard header as a bell icon).
**Mobile:** `NotificationActivity` exists but has only "Empty state placeholder, Mark All Read" — no real notifications are fetched, and the `NotificationItem` model uses hardcoded fields rather than API data.

### GAP-11 · Forms: Missing `submissionProgress` in Mobile Model (LOW)
**Web:** Each form card shows a progress bar displaying `ev.submissionProgress` (e.g., "3/10").
**Mobile:** `CreatedEvaluation` model has `submissionCount` and `totalExpectedSubmissions` as separate fields — not a formatted `submissionProgress` string. The `FormsAdapter` must compute this.

### GAP-12 · Terminology Mismatch (LOW)
| Screen | Web Text | Mobile Text |
|--------|----------|-------------|
| Results stats | "Overall Avg Score", "Evaluations Received", "Total Responses", "Highest Criterion" | "Received", "Average", "Improved" |
| Pending stats | "Pending Count", "Urgent", "Submitted This Month" | "Pending", "Urgent", "Done" |
| Pending filters | "All", "Urgent", "Missed" | "All", "Urgent", "This Week", "New" |
| Completed stats | "Total Submitted", "Submitted This Month", "Avg Score Given" | "Submitted", "Evaluatees", "Last" |
| Greeting | "Hello, {firstName} 👋" | "Good morning/afternoon/evening, {name}" |

---

## ✅ Items That Are Consistent

| Feature | Status |
|---------|--------|
| Login → Dashboard navigation | ✅ Consistent |
| Registration flow | ✅ Consistent |
| Evaluate Form (10-criteria rating + comment) | ✅ Consistent |
| Create Evaluation form fields | ✅ Consistent |
| Settings / Profile display | ✅ Consistent |
| Bottom navigation tabs | ✅ Consistent |
| Color palette (cyan/orange/blue/green) | ✅ Consistent |
| Typography (Plus Jakarta Sans) | ✅ Consistent |

---

## Priority Refactor Plan

| Priority | Gap | Files to Change |
|----------|-----|-----------------|
| P0 | GAP-06: Implement promote API | `HomeFragment.kt` |
| P0 | GAP-05: Dynamic Recent Activity | `HomeFragment.kt`, `fragment_home.xml` |
| P1 | GAP-01: Results stats + filter + archive | `ResultsFragment.kt`, `fragment_results.xml`, `ResultsViewModel.kt` |
| P1 | GAP-02: Pending grouping + detail panel | `PendingFragment.kt`, `PendingViewModel.kt`, `fragment_pending.xml` |
| P1 | GAP-03: Completed stats + filters | `CompletedFragment.kt`, `fragment_completed.xml` |
| P2 | GAP-04: Forms search + filter + actions | `FormsFragment.kt`, `fragment_forms.xml`, `FormsAdapter.kt` |
| P2 | GAP-07: Promo banner logic fix | `HomeFragment.kt`, `fragment_home.xml` |
| P3 | GAP-08: Criteria dot indicators | `fragment_result_detail.xml`, `ResultDetailFragment.kt` |
| P3 | GAP-12: Terminology fixes | `strings.xml`, layout XML files |
