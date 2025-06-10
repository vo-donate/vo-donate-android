package dev.kevin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WithDrawResponse(
    @SerialName("message")
    val message: String
)
