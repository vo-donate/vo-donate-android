package dev.kevin.core.network.jwt

import dev.kevin.core.preferences.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class JwtInterceptor (private val tokenManager: TokenManager, private val onTokenExpired: () -> Unit) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Login Request without JWT
        if (originalRequest.url.encodedPath == "/login" || originalRequest.url.encodedPath == "/registry") {
            return chain.proceed(originalRequest)
        }

        val accessToken = tokenManager.getAccessToken()

        // If token is not available, proceed with the original request
        if (accessToken.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Authorization Header with JWT Token
        val authorizedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        chain.proceed(authorizedRequest).let { response ->
            // If response is 401 Unauthorized, clear token
            if (response.code == 401) {
                tokenManager.clearAccessToken()
                onTokenExpired()
            }
            return response
        }
    }
}