package com.thefarhany.eventapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.thefarhany.eventapp.data.model.response.UserProfile

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "event_app_session"

        // Auth keys
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_TYPE = "token_type"

        // User info keys
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_PROFILE_PICTURE = "profile_picture"

        // App preferences
        private const val KEY_FIRST_TIME = "is_first_time"
    }

    // ==================== AUTH TOKEN ====================

    /**
     * Save authentication token (JWT)
     */
    fun saveAuthToken(token: String, tokenType: String = "Bearer") {
        prefs.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_TOKEN_TYPE, tokenType)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Get authentication token
     */
    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Get full authorization header value (e.g., "Bearer eyJhbG...")
     */
    fun getAuthorizationHeader(): String? {
        val token = getAuthToken()
        val tokenType = prefs.getString(KEY_TOKEN_TYPE, "Bearer")
        return if (token != null) "$tokenType $token" else null
    }

    /**
     * Save refresh token (if your backend supports it)
     */
    fun saveRefreshToken(refreshToken: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    /**
     * Get refresh token
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    // ==================== USER SESSION ====================

    /**
     * Save complete login session with user info
     */
    fun saveLoginSession(
        email: String,
        userName: String,
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
        userId: Long? = null,
        profilePicture: String? = null
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, userName)
            firstName?.let { putString(KEY_FIRST_NAME, it) }
            lastName?.let { putString(KEY_LAST_NAME, it) }
            phoneNumber?.let { putString(KEY_PHONE_NUMBER, it) }
            userId?.let { putLong(KEY_USER_ID, it) }
            profilePicture?.let { putString(KEY_PROFILE_PICTURE, it) }
            apply()
        }
    }

    /**
     * Update user profile info (after profile update)
     */
    fun updateUserProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString(KEY_FIRST_NAME, profile.firstName)
            putString(KEY_LAST_NAME, profile.lastName)
            putString(KEY_USER_NAME, profile.userName)
            putString(KEY_USER_EMAIL, profile.email)
            putString(KEY_PHONE_NUMBER, profile.phoneNumber)
            profile.profilePicture?.let { putString(KEY_PROFILE_PICTURE, it) }
            apply()
        }
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getAuthToken() != null
    }

    /**
     * Get user ID
     */
    fun getUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, -1L)
        return if (id != -1L) id else null
    }

    /**
     * Get user email
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Get username
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * Get first name
     */
    fun getFirstName(): String? {
        return prefs.getString(KEY_FIRST_NAME, null)
    }

    /**
     * Get last name
     */
    fun getLastName(): String? {
        return prefs.getString(KEY_LAST_NAME, null)
    }

    /**
     * Get full name
     */
    fun getFullName(): String {
        val firstName = getFirstName() ?: ""
        val lastName = getLastName() ?: ""
        return "$firstName $lastName".trim().ifEmpty { getUserName() ?: "User" }
    }

    /**
     * Get phone number
     */
    fun getPhoneNumber(): String? {
        return prefs.getString(KEY_PHONE_NUMBER, null)
    }

    /**
     * Get profile picture URL
     */
    fun getProfilePicture(): String? {
        return prefs.getString(KEY_PROFILE_PICTURE, null)
    }

    // ==================== SESSION MANAGEMENT ====================

    /**
     * Clear all session data (logout)
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    /**
     * Clear only auth tokens (keep user preferences)
     */
    fun clearAuthData() {
        prefs.edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_AUTH_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_TYPE)
            apply()
        }
    }

    // ==================== APP PREFERENCES ====================

    /**
     * Check if this is first time app launch
     */
    fun isFirstTime(): Boolean {
        return prefs.getBoolean(KEY_FIRST_TIME, true)
    }

    /**
     * Set first time flag to false
     */
    fun setFirstTimeDone() {
        prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply()
    }

    // ==================== DEBUG ====================

    /**
     * Get all stored data (for debugging)
     */
    fun getAllData(): Map<String, *> {
        return prefs.all
    }
}
