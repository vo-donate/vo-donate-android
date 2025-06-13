package com.example.vo_donate.ui.vote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ProposalItem(
    val id: String,
    val title: String,
    val voteEndTime: String,
    val donationEndTime: String,
    val totalDonationFee: String,
    val minimumDonationFee: String,
    val approvedVoter: Int,
    val totalVoter: Int,
    val votePassed: Boolean,
    val proposalFinalized: Boolean,
    val voteOption: List<ApprovalStatus> = listOf(
        ApprovalStatus.APPROVE, ApprovalStatus.DISAPPROVE,
    ),
)

data class DisplayableProposalItem(
    val coreData: ProposalItem,
    val timeLeft: StateFlow<Pair<String, ProposalStatus>>,
    val approvalStatus: MutableStateFlow<ApprovalStatus> = MutableStateFlow(ApprovalStatus.NOT_VOTE),
    val isVoted: Boolean = false
)

data class VoteNavigatorUiState(
    val isLoading: Boolean = false,
    val isSubmittingVote: Boolean = false,
    val isSubmittingDonation: Boolean = false,
    val errorMessage: String? = null,
    val voteSubmissionMessage: String? = null,
    val voteList: List<DisplayableProposalItem> = emptyList(),
    val donationList: List<DisplayableProposalItem> = emptyList(),
)

enum class ApprovalStatus {
    APPROVE, DISAPPROVE, NOT_VOTE
}

enum class ProposalStatus {
    DONATION_IN_PROGRESS, VOTE_IN_PROGRESS, DONATION_ENDED, ERROR
}