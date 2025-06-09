package dev.kevin.core.preferences

import android.content.Context
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val tokenPrefs = context.getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
    }

    fun saveAccessToken(token: String) {
        tokenPrefs.edit { putString(ACCESS_TOKEN_KEY, token) }
    }

    fun getAccessToken(): String? = tokenPrefs.getString(ACCESS_TOKEN_KEY, null)

    fun clearAccessToken() {
        tokenPrefs.edit { remove(ACCESS_TOKEN_KEY) }
    }

    fun hasAccessToken(): Boolean = tokenPrefs.contains(ACCESS_TOKEN_KEY)
}