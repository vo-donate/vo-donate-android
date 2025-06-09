package dev.kevin.data.repository

import dev.kevin.core.network.data.NetworkDataSource
import dev.kevin.core.network.model.DonationRequest
import dev.kevin.core.network.model.DonationResponse
import dev.kevin.core.network.model.ProposalById
import dev.kevin.core.network.model.ProposalListElement
import dev.kevin.core.network.model.ProposalRequest
import dev.kevin.core.network.model.ProposalResponse
import dev.kevin.core.network.model.VoteRequest
import dev.kevin.core.network.model.VoteResponse
import dev.kevin.core.network.util.onSuccess

class ProposalRepositoryImpl(private val networkDataSource: NetworkDataSource) :
    ProposalRepository {
    override suspend fun addProposal(
        proposalText: String,
        voteDurationInMinutes: Int,
        donationDurationInMinutes: Int
    ): ProposalResponse {
        networkDataSource.addProposal(
            ProposalRequest(
                proposalText,
                voteDurationInMinutes,
                donationDurationInMinutes
            )
        ).onSuccess {
            return it
        }
        return ProposalResponse("Add Proposal Failed", "-1", "-1")
    }

    override suspend fun getProposalById(id: String): ProposalById {
        networkDataSource.getProposalById(id).onSuccess {
            return it
        }
        return ProposalById("-1", "-1", "-1", "-1", false, -1, -1, -1, false, "-1")
    }

    override suspend fun getProposals(): List<ProposalListElement> {
        networkDataSource.getProposals().onSuccess {
            return it
        }
        return emptyList()
    }

    override suspend fun donate(id: String, amount: String): DonationResponse {
        networkDataSource.donate(id, DonationRequest(amount)).onSuccess {
            return it
        }
        return DonationResponse("Donate Failed")
    }

    override suspend fun vote(id: String, isApprove: Boolean): VoteResponse {
        networkDataSource.vote(id, VoteRequest(isApprove)).onSuccess {
            return it
        }
        return VoteResponse("Vote Failed")
    }
}