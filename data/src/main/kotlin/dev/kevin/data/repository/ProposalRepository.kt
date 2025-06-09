package dev.kevin.data.repository

import dev.kevin.core.network.model.DonationResponse
import dev.kevin.core.network.model.ProposalById
import dev.kevin.core.network.model.ProposalListElement
import dev.kevin.core.network.model.ProposalResponse
import dev.kevin.core.network.model.VoteResponse

interface ProposalRepository {
    suspend fun addProposal(
        proposalText: String,
        voteDurationInMinutes: Int,
        donationDurationInMinutes: Int
    ): ProposalResponse

    suspend fun getProposalById(id: String): ProposalById

    suspend fun getProposals(): List<ProposalListElement>

    suspend fun donate(id: String, amount: String): DonationResponse

    suspend fun vote (id: String, isApprove: Boolean): VoteResponse
}