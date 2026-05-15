package com.example.peertayo_mobile.data.model

import com.google.gson.annotations.SerializedName

// ── Profile Update ───────────────────────────────────────────────────

data class UpdateProfileRequest(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String
)

// ── Password Change ──────────────────────────────────────────────────

data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

// ── Preferences (Aligned with Backend) ───────────────────────────────

data class NotificationPreferences(
    @SerializedName("evaluationAssigned") val evaluationAssigned: Boolean = true,
    @SerializedName("deadlineReminder") val deadlineReminder: Boolean = true,
    @SerializedName("resultsPublished") val resultsPublished: Boolean = true,
    @SerializedName("formCreated") val formCreated: Boolean = true,
    @SerializedName("submissionReceived") val submissionReceived: Boolean = true,
    @SerializedName("systemAnnouncements") val systemAnnouncements: Boolean = true
)

// ── Roles & Permissions ──────────────────────────────────────────────

data class PermissionItem(
    val action: String,
    val isAllowed: Boolean
)
