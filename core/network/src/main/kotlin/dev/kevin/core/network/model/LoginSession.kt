package dev.kevin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val id: String,
    val password: String
)

@Serializable
data class LoginResponse(
    @SerialName("message")
    val message: String,
    @SerialName("token")
    val token: String
)
