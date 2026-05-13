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
    suspend fun getMyResults(): Response<ApiResponse<MyResultsResponse>>

    // ── Created Evaluations (Facilitator) ────────────────────────────
    @GET("evaluations/created")
    suspend fun listCreatedEvaluations(): Response<ApiResponse<CreatedEvaluationsResponse>>

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
}
