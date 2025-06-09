package dev.kevin.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class DonationRequest(
    val amount: String
)

@Serializable
data class DonationResponse(
    val message: String
)

