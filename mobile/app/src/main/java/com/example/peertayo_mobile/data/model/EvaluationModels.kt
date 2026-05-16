package com.example.peertayo_mobile.data.model

import com.google.gson.annotations.SerializedName

// ── Pending Evaluations ──────────────────────────────────────────────

data class PendingEvaluationsResponse(
    @SerializedName("evaluations") val evaluations: List<PendingEvaluation>
)

data class PendingEvaluation(
    @SerializedName("id") val id: Long,
    @SerializedName("evaluationId") val evaluationId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("evaluateeName") val evaluateeName: String,
    @SerializedName("evaluateeId") val evaluateeId: Long,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("creatorName") val creatorName: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("criteria") val criteria: List<Criterion>?
)

data class Criterion(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?
)

// ── Submit Evaluation ────────────────────────────────────────────────

data class SubmitEvaluationRequest(
    @SerializedName("ratings") val ratings: List<RatingEntry>,
    @SerializedName("comment") val comment: String?
)

data class RatingEntry(
    @SerializedName("criteriaId") val criteriaId: Long,
    @SerializedName("score") val score: Int
)

// ── Submitted Summary ────────────────────────────────────────────────

data class SubmittedSummary(
    @SerializedName("totalSubmitted") val totalSubmitted: Int,
    @SerializedName("submittedThisMonth") val submittedThisMonth: Int
)

// ── My Results ───────────────────────────────────────────────────────

data class MyResultsResponse(
    @SerializedName("results") val results: MyResults?
)

data class MyResults(
    @SerializedName("overallAverage") val overallAverage: Double?,
    @SerializedName("totalResponses") val totalResponses: Int?,
    @SerializedName("questionAverages") val questionAverages: List<QuestionAverage>?,
    @SerializedName("evaluations") val evaluations: List<EvaluationResultSummary>?,
    @SerializedName("comments") val comments: List<String>?
)

data class QuestionAverage(
    @SerializedName("criteriaId") val criteriaId: Long,
    @SerializedName("criteriaName") val criteriaName: String?,
    @SerializedName("average") val average: Double
)

data class EvaluationResultSummary(
    @SerializedName("evaluationId") val evaluationId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("createdByName") val createdByName: String?,
    @SerializedName("overallAverage") val overallAverage: Double?,
    @SerializedName("totalResponses") val totalResponses: Int?,
    @SerializedName("submittedAt") val submittedAt: String?,
    @SerializedName("questionAverages") val questionAverages: List<QuestionAverage>?,
    @SerializedName("comments") val comments: List<String>?
)

// ── Completed Forms ──────────────────────────────────────────────────

data class CompletedFormsResponse(
    @SerializedName("completed") val completed: List<CompletedForm>?
)

data class CompletedForm(
    @SerializedName("id") val id: Long,
    @SerializedName("evaluationId") val evaluationId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("evaluateeName") val evaluateeName: String,
    @SerializedName("evaluateeId") val evaluateeId: Long?,
    @SerializedName("submittedAt") val submittedAt: String?,
    @SerializedName("comment") val comment: String?
)

// ── Created Evaluations (Facilitator) ────────────────────────────────

data class CreatedEvaluationsResponse(
    @SerializedName("evaluations") val evaluations: List<CreatedEvaluation>
)

data class CreatedEvaluation(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("evaluatorCount") val evaluatorCount: Int?,
    @SerializedName("evaluateeCount") val evaluateeCount: Int?,
    @SerializedName("submissionCount") val submissionCount: Int?,
    @SerializedName("totalExpectedSubmissions") val totalExpectedSubmissions: Int?,
    @SerializedName("roleUpgraded") val roleUpgraded: Boolean?,
    @SerializedName("permanentlyClosed") val permanentlyClosed: Boolean?
)

data class CreateEvaluationRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("deadline") val deadline: String,
    @SerializedName("evaluatorIds") val evaluatorIds: List<Long>,
    @SerializedName("evaluateeIds") val evaluateeIds: List<Long>
)

data class CreateEvaluationResponse(
    @SerializedName("evaluation") val evaluation: CreatedEvaluation?
)

// ── User Search ──────────────────────────────────────────────────────

data class UserSearchResponse(
    @SerializedName("users") val users: List<UserResponse>?
)

// ── Promote to Facilitator ───────────────────────────────────────────

data class PromoteResponse(
    @SerializedName("user") val user: UserResponse?
)

// ── Notification ─────────────────────────────────────────────────────

data class NotificationItem(
    val message: String,
    val type: String,      // "EVALUATION_ASSIGNED", "DEADLINE_EXTENDED", "ZERO_SUBMISSION", "ACTIVITY"
    val timeAgo: String,
    val isRead: Boolean = false
)
