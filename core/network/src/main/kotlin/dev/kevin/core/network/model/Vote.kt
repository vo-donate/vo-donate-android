package dev.kevin.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class VoteRequest(
    val approve : Boolean,
)

@Serializable
data class VoteResponse(
    val message : String,
)