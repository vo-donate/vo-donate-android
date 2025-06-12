package dev.kevin.core.network.api

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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NetworkApi {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/registry")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("/user")
    suspend fun getUserInfo(): Response<UserInfoResponse>

    @GET("/user/balance")
    suspend fun getUserBalance(): Response<UserBalanceResponse>

    @POST("/addProposal")
    suspend fun addProposal(@Body request: ProposalRequest): Response<ProposalResponse>

    @GET("/proposal/{id}")
    suspend fun getProposalById(@Path("id") id: String): Response<ProposalById>

    @GET("/proposals")
    suspend fun getProposals(): Response<List<ProposalListElement>>

    @POST("/proposal/{id}/vote")
    suspend fun vote(@Path("id") id: String, @Body request: VoteRequest): Response<VoteResponse>

    @POST("/proposal/{id}/donation")
    suspend fun donate(@Path("id") id: String, @Body request: DonationRequest): Response<DonationResponse>

    @GET("/proposal/{id}/withdraw")
    suspend fun withdraw(@Path("id") id: String): Response<WithDrawResponse>
}