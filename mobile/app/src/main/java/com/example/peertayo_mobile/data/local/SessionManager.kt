package com.example.peertayo_mobile.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "peertayo_session"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
    }

    fun saveSession(token: String, userId: Long, firstName: String, lastName: String, email: String, role: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_FIRST_NAME, firstName)
            .putString(KEY_LAST_NAME, lastName)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun getFirstName(): String = prefs.getString(KEY_FIRST_NAME, "") ?: ""

    fun getLastName(): String = prefs.getString(KEY_LAST_NAME, "") ?: ""

    fun getFullName(): String = "${getFirstName()} ${getLastName()}".trim()

    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""

    fun getRole(): String = prefs.getString(KEY_ROLE, "RESPONDENT") ?: "RESPONDENT"

    fun isFacilitator(): Boolean {
        val roles = getRole().uppercase()
        return roles.contains("FACILITATOR") || roles.contains("ADMIN")
    }

    fun saveUser(firstName: String, lastName: String, email: String) {
        prefs.edit()
            .putString(KEY_FIRST_NAME, firstName)
            .putString(KEY_LAST_NAME, lastName)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun updateRole(role: String) {
        prefs.edit().putString(KEY_ROLE, role).apply()
    }

    /** Alias for updateRole — used after facilitator promotion. */
    fun saveRole(role: String) = updateRole(role)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
