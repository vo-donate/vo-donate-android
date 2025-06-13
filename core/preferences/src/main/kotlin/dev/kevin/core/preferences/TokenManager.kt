package dev.kevin.core.preferences

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import timber.log.Timber

class TokenManager(context: Context) {
    private val tokenPrefs = context.getSharedPreferences("TokenPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
    }

    fun saveAccessToken(token: String) {
        tokenPrefs.edit {
            putString(ACCESS_TOKEN_KEY, token)
        }
        Timber.d("Token saved: $token")
    }

    fun getAccessToken(): String? = tokenPrefs.getString(ACCESS_TOKEN_KEY, null)

    fun clearAccessToken() {
        tokenPrefs.edit { remove(ACCESS_TOKEN_KEY) }
    }

    fun hasAccessToken(): Boolean = tokenPrefs.contains(ACCESS_TOKEN_KEY)
}