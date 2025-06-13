package dev.kevin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest (
    @SerialName("id")
    val id: String,
    @SerialName("password")
    val password: String,
    @SerialName("name")
    val name: String,
    @SerialName("introduction")
    val introduction: String = "플랫폼에 가입하는 새로운 사용자입니다",
)

@Serializable
data class RegisterResponse (
    @SerialName("message")
    val message: String
)