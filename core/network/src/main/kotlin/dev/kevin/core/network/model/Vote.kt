package dev.kevin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoteRequest(
    val approve : Boolean,
)

@Serializable
data class VoteResponse(
    @SerialName("message")
    val message : String,
)