package dev.kevin.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProposalRequest(
    @SerialName("proposalText")
    val proposalText: String,
    @SerialName("voteDurationInMinutes")
    val voteDonationInMinutes: Int,
    @SerialName("donationDurationInMinutes")
    val donationDurationInMinutes: Int
)

@Serializable
data class ProposalResponse(
    @SerialName("message")
    val message: String,
    @SerialName("proposalId")
    val proposalId: String,
    @SerialName("proposalContractAddress")
    val proposalContractAddress: String
)

@Serializable
data class ProposalListElement(
    @SerialName("id")
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("contract_address")
    val contractAddress: String,
    @SerialName("vote_end_time")
    val voteEndTime: String,
    @SerialName("donationDurationInMinutes")
    val donationDurationInMinutes: Int,
    @SerialName("donation_end_time")
    val donationEndTime: String
)

@Serializable
data class ProposalById(
    @SerialName("id")
    val id: String,
    @SerialName("proposer")
    val proposer: String,
    @SerialName("proposalText")
    val proposalText: String,
    @SerialName("totalDonation")
    val totalDonation: String,
    @SerialName("finalized")
    val finalized: Boolean,
    @SerialName("voteCount")
    val voteCount: Int,
    @SerialName("voterCount")
    val voterCount: Int,
    @SerialName("balance")
    val balance: String,
    @SerialName("votePassed")
    val votePassed: Boolean,
    @SerialName("donationEndTime")
    val donationEndTime: String
)