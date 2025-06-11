package dev.kevin.data.repository

import dev.kevin.core.network.data.NetworkDataSource
import dev.kevin.core.network.model.LoginRequest
import dev.kevin.core.network.model.LoginResponse
import dev.kevin.core.network.model.RegisterRequest
import dev.kevin.core.network.model.RegisterResponse
import dev.kevin.core.network.model.UserBalanceResponse
import dev.kevin.core.network.model.UserInfoResponse
import dev.kevin.core.network.model.WithDrawResponse
import dev.kevin.core.network.util.onSuccess

class AuthRepositoryImpl(private val networkDataSource: NetworkDataSource) : AuthRepository {
    override suspend fun login(id: String, password: String): LoginResponse {
        networkDataSource.login(LoginRequest(id, password)).onSuccess {
            return it
        }
        return LoginResponse("Login Failed", "-1")
    }

    override suspend fun register(
        id: String,
        password: String,
        name: String,
        introduction: String
    ): RegisterResponse {
        networkDataSource.register(RegisterRequest(id, password, name, introduction)).onSuccess {
            return it
        }
        return RegisterResponse("Register Failed")
    }

    override suspend fun getUserInfo(): UserInfoResponse {
        networkDataSource.getUserInfo().onSuccess {
            return it
        }
        return UserInfoResponse("-1", "Cannot Get UserName", "-1", "Unknown User")
    }

    override suspend fun getUserBalance(): UserBalanceResponse {
        networkDataSource.getUserBalance().onSuccess {
            return it
        }
        return UserBalanceResponse("-1", "Cannot Get Balance")

    }

    override suspend fun withdraw(id: String): WithDrawResponse {
        networkDataSource.withdraw(id).onSuccess {
            return it
        }
        return WithDrawResponse("Withdraw Failed")
    }

    override fun checkInitialLoginStatus(): Boolean {
        return networkDataSource.isLoggedIn()
    }
}