package dev.kevin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProposalRequest(
    val proposalText: String,
    val voteDonationInMinutes: Int,
    val donationDurationInMinutes: Int
)

@Serializable
data class ProposalResponse(
    val message: String,
    val proposalId: String,
    val proposalContractAddress: String
)

@Serializable
data class ProposalListElement(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("contract_address")
    val contractAddress: String,
    @SerialName("vote_end_time")
    val voteEndTime: String,
    val donationDurationInMinutes: Int,
    @SerialName("donation_end_time")
    val donationEndTime: String
)

@Serializable
data class ProposalById(
    val id: String,
    val proposer: String,
    val proposalText: String,
    val totalDonation: String,
    val finalized: Boolean,
    val voteCount: Int,
    val voterCount: Int,
    val balance: Int,
    val votePassed: Boolean,
    val donationEndTime: String
)