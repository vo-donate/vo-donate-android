package dev.kevin.core.network.model

data class RegisterRequest (
    val id: String = "test",
    val password: String = "test",
    val name: String = "홍길동",
    val introduction: String = "플랫폼에 가입하는 새로운 사용자입니다",
)

data class RegisterResponse (
    val message: String
)