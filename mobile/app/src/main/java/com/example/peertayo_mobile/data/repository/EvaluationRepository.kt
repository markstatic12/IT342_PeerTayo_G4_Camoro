package com.example.peertayo_mobile.data.repository

import com.example.peertayo_mobile.data.api.EvaluationApi
import com.example.peertayo_mobile.data.model.*

class EvaluationRepository(private val api: EvaluationApi) {

    suspend fun listPendingEvaluations(): Result<List<PendingEvaluation>> = runCatching {
        val response = api.listPendingEvaluations()
        if (response.isSuccessful) {
            response.body()?.data?.evaluations ?: emptyList()
        } else {
            throw Exception("Failed to load pending evaluations")
        }
    }

    suspend fun submitEvaluation(id: Long, request: SubmitEvaluationRequest): Result<Unit> = runCatching {
        val response = api.submitEvaluation(id, request)
        if (!response.isSuccessful) {
            throw Exception("Failed to submit evaluation")
        }
    }

    suspend fun getSubmittedSummary(): Result<SubmittedSummary> = runCatching {
        val response = api.getSubmittedSummary()
        if (response.isSuccessful) {
            response.body()?.data ?: SubmittedSummary(0, 0)
        } else {
            throw Exception("Failed to load summary")
        }
    }

    suspend fun getCompletedForms(): Result<List<CompletedForm>> = runCatching {
        val response = api.getCompletedForms()
        if (response.isSuccessful) {
            response.body()?.data?.completed ?: emptyList()
        } else {
            throw Exception("Failed to load completed forms")
        }
    }

    suspend fun getMyResults(): Result<MyResults?> = runCatching {
        val response = api.getMyResults()
        if (response.isSuccessful) {
            response.body()?.data?.results
        } else {
            throw Exception("Failed to load results")
        }
    }

    suspend fun listCreatedEvaluations(): Result<List<CreatedEvaluation>> = runCatching {
        val response = api.listCreatedEvaluations()
        if (response.isSuccessful) {
            response.body()?.data?.evaluations ?: emptyList()
        } else {
            throw Exception("Failed to load created evaluations")
        }
    }

    suspend fun createEvaluation(request: CreateEvaluationRequest): Result<CreatedEvaluation?> = runCatching {
        val response = api.createEvaluation(request)
        if (response.isSuccessful) {
            response.body()?.data?.evaluation
        } else {
            throw Exception("Failed to create evaluation")
        }
    }

    suspend fun searchUsers(query: String): Result<List<UserResponse>> = runCatching {
        val response = api.searchUsers(query)
        if (response.isSuccessful) {
            response.body()?.data?.users ?: emptyList()
        } else {
            throw Exception("Failed to search users")
        }
    }

    suspend fun promoteToFacilitator(): Result<UserResponse?> = runCatching {
        val response = api.promoteToFacilitator()
        if (response.isSuccessful) {
            response.body()?.data?.user
        } else {
            throw Exception("Failed to promote to facilitator")
        }
    }
}
