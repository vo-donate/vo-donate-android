package dev.kevin.data.repository

import dev.kevin.core.network.model.LoginResponse
import dev.kevin.core.network.model.RegisterResponse
import dev.kevin.core.network.model.UserBalanceResponse
import dev.kevin.core.network.model.UserInfoResponse
import dev.kevin.core.network.model.WithDrawResponse

interface AuthRepository {
    suspend fun login(id: String, password: String): LoginResponse

    suspend fun register(id: String, password: String, name: String, introduction: String): RegisterResponse

    suspend fun getUserInfo() : UserInfoResponse

    suspend fun getUserBalance() : UserBalanceResponse

    suspend fun withdraw(id: String) : WithDrawResponse

}