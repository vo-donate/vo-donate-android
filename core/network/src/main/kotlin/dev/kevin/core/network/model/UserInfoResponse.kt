package dev.kevin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoResponse(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("wallet_address")
    val walletAddress: String,
    @SerialName("introduction")
    val introduction: String
)

@Serializable
data class UserBalanceResponse(
    @SerialName("wallet_address")
    val walletAddress: String,
    @SerialName("balance")
    val balance: String
)