package com.example.peertayo_mobile.data.api

import com.example.peertayo_mobile.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface EvaluationApi {

    // ── Pending Evaluations ──────────────────────────────────────────
    @GET("evaluations/pending")
    suspend fun listPendingEvaluations(
        @Query("archived") archived: Boolean = false
    ): Response<ApiResponse<PendingEvaluationsResponse>>

    // ── Submit Evaluation ────────────────────────────────────────────
    @POST("evaluations/{id}/submit")
    suspend fun submitEvaluation(
        @Path("id") id: Long,
        @Body request: SubmitEvaluationRequest
    ): Response<ApiResponse<Any>>

    // ── Submitted Summary ────────────────────────────────────────────
    @GET("evaluations/submitted/summary")
    suspend fun getSubmittedSummary(): Response<ApiResponse<SubmittedSummary>>

    // ── Completed Forms ──────────────────────────────────────────────
    @GET("evaluations/completed")
    suspend fun getCompletedForms(
        @Query("archived") archived: Boolean? = null
    ): Response<ApiResponse<CompletedFormsResponse>>

    // ── My Results ───────────────────────────────────────────────────
    @GET("evaluations/my-results")
    suspend fun getMyResults(
        @Query("archived") archived: Boolean = false
    ): Response<ApiResponse<MyResultsResponse>>

    // ── Created Evaluations (Facilitator) ────────────────────────────
    @GET("evaluations/created")
    suspend fun listCreatedEvaluations(
        @Query("archived") archived: Boolean = false
    ): Response<ApiResponse<CreatedEvaluationsResponse>>

    // ── Create Evaluation (Facilitator) ──────────────────────────────
    @POST("evaluations")
    suspend fun createEvaluation(
        @Body request: CreateEvaluationRequest
    ): Response<ApiResponse<CreateEvaluationResponse>>

    // ── User Search ──────────────────────────────────────────────────
    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String
    ): Response<ApiResponse<UserSearchResponse>>

    // ── Promote to Facilitator ───────────────────────────────────────
    @POST("auth/promote-to-facilitator")
    suspend fun promoteToFacilitator(): Response<ApiResponse<PromoteResponse>>

    // ── Notifications (GAP-10) ───────────────────────────────────────
    @GET("notifications")
    suspend fun listNotifications(): Response<ApiResponse<List<NotificationItem>>>

    @POST("notifications/mark-read")
    suspend fun markNotificationsRead(): Response<ApiResponse<Any>>

    // ── Settings & Profile (GAP Parity) ──────────────────────────────
    @GET("settings/profile")
    suspend fun getProfile(): Response<ApiResponse<Map<String, UserResponse>>>

    @PUT("settings/profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ApiResponse<AuthResponse>>

    @PUT("settings/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Any>>

    @GET("notifications/preferences")
    suspend fun getPreferences(): Response<ApiResponse<NotificationPreferences>>

    @PUT("notifications/preferences")
    suspend fun updatePreferences(
        @Body request: NotificationPreferences
    ): Response<ApiResponse<NotificationPreferences>>
}
