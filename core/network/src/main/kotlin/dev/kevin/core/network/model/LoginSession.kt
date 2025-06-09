package dev.kevin.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val id: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String
)
