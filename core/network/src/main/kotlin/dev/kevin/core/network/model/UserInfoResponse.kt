package dev.kevin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
    val id: String,
    val name: String,
    @SerialName("wallet_address")
    val walletAddress: String,
    val introduction: String
)

@Serializable
data class UserBalanceResponse(
    @SerialName("wallet_address")
    val walletAddress: String,
    val balance: String
)