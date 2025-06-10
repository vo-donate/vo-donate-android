package dev.kevin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DonationRequest(
    val amount: String
)

@Serializable
data class DonationResponse(
    @SerialName("message")
    val message: String
)

