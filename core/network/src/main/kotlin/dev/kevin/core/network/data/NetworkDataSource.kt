package dev.kevin.core.network.data

import dev.kevin.core.network.model.DonationRequest
import dev.kevin.core.network.model.DonationResponse
import dev.kevin.core.network.model.LoginRequest
import dev.kevin.core.network.model.LoginResponse
import dev.kevin.core.network.model.ProposalById
import dev.kevin.core.network.model.ProposalListElement
import dev.kevin.core.network.model.ProposalRequest
import dev.kevin.core.network.model.ProposalResponse
import dev.kevin.core.network.model.RegisterRequest
import dev.kevin.core.network.model.RegisterResponse
import dev.kevin.core.network.model.UserBalanceResponse
import dev.kevin.core.network.model.UserInfoResponse
import dev.kevin.core.network.model.VoteRequest
import dev.kevin.core.network.model.VoteResponse
import dev.kevin.core.network.model.WithDrawResponse
import dev.kevin.core.network.util.NetworkError
import dev.kevin.core.network.util.NetworkResult

interface NetworkDataSource {
    suspend fun login(request: LoginRequest): NetworkResult<LoginResponse, NetworkError>

    suspend fun register(request: RegisterRequest): NetworkResult<RegisterResponse, NetworkError>

    suspend fun getUserInfo(): NetworkResult<UserInfoResponse, NetworkError>

    suspend fun getUserBalance(): NetworkResult<UserBalanceResponse, NetworkError>

    suspend fun addProposal(request: ProposalRequest): NetworkResult<ProposalResponse, NetworkError>

    suspend fun getProposalById(id: String): NetworkResult<ProposalById, NetworkError>

    suspend fun getProposals(): NetworkResult<List<ProposalListElement>, NetworkError>

    suspend fun vote(id: String, request: VoteRequest): NetworkResult<VoteResponse, NetworkError>

    suspend fun donate(id: String, request: DonationRequest): NetworkResult<DonationResponse, NetworkError>

    suspend fun withdraw(id: String): NetworkResult<WithDrawResponse, NetworkError>
}