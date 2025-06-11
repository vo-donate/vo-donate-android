package dev.kevin.core.network.data

import dev.kevin.core.android.di.IoDispatcher
import dev.kevin.core.network.di.NetworkModule
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Response
import timber.log.Timber
import java.io.IOException


class NetworkDataSourceImpl(
    private val networkModule: NetworkModule,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NetworkDataSource {
    private val networkApi = networkModule.provideNetworkApi()
    private val tokenManager = networkModule.provideTokenManager()

    override suspend fun login(request: LoginRequest): NetworkResult<LoginResponse, NetworkError> {
        return withContext(ioDispatcher) {
            safeApiCallWithHandlingToken(
                call = { networkApi.login(request) },
                extractToken = { it.token }
            )
        }
    }

    override suspend fun register(request: RegisterRequest): NetworkResult<RegisterResponse, NetworkError> {
        return withContext(ioDispatcher) {
            safeApiCallWithHandlingToken(
                call = { networkApi.register(request) },
                extractToken = { _ -> null }
            )
        }
    }

    override suspend fun getUserInfo(): NetworkResult<UserInfoResponse, NetworkError> {
        return withContext(ioDispatcher) {
            safeApiCall {
                networkApi.getUserInfo()
            }
        }
    }

    override suspend fun getUserBalance(): NetworkResult<UserBalanceResponse, NetworkError> {
        return withContext(ioDispatcher) {
            safeApiCall {
                networkApi.getUserBalance()
            }
        }
    }

    override suspend fun addProposal(request: ProposalRequest): NetworkResult<ProposalResponse, NetworkError> {
        return withContext(ioDispatcher) {
            safeApiCall {
                networkApi.addProposal(request)
            }
        }
    }

    override suspend fun getProposalById(id: String): NetworkResult<ProposalById, NetworkError> {
        return withContext(ioDispatcher) {
            safeApiCall {
                networkApi.getProposalById(id)
            }
        }
    }

    override suspend fun getProposals(): NetworkResult<List<ProposalListElement>, NetworkError> {
        return withContext(ioDispatcher) {
            safeApiCall {
                networkApi.getProposals()
            }
        }
    }

    override suspend fun donate(
        id: String,
        request: DonationRequest
    ): NetworkResult<DonationResponse, NetworkError> {
        return withContext(ioDispatcher) {
            safeApiCall {
                networkApi.donate(id, request)
            }
        }
    }

    override suspend fun vote(
        id: String,
        voteRequest: VoteRequest
    ): NetworkResult<VoteResponse, NetworkError> {
        return withContext(ioDispatcher) {
            safeApiCall {
                networkApi.vote(id, voteRequest)
            }
        }
    }

    override suspend fun withdraw(id: String): NetworkResult<WithDrawResponse, NetworkError> {
        return withContext(ioDispatcher) {
            safeApiCall {
                networkApi.withdraw(id)
            }
        }
    }

    override fun isLoggedIn(): Boolean {
        return networkModule.hasAccessToken()
    }

    private suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T, NetworkError> {
        return try {
            val response = call()
            Timber.d("Response: $response")
            if (response.isSuccessful) {
                response.let {
                    val body = response.body()
                    if (body != null) {
                        NetworkResult.Success(body)
                    } else {
                        NetworkResult.Error(NetworkError.UNKNOWN, "Response body is null")
                    }
                }
            } else {
                mapResponseCodeToNetworkError(
                    response.code(),
                    response.message(),
                    response.errorBody()?.string()
                )
            }
        } catch (e: IOException) {
            // Network exceptions (no internet, timeout, etc.)
            NetworkResult.Error(NetworkError.REQUEST_FAILED, e.message ?: "Network request failed")
        } catch (e: Exception) {
            // Other unexpected exceptions during the call or processing
            NetworkResult.Error(NetworkError.UNKNOWN, e.message ?: "An unknown error occurred")
        }
    }

    private suspend fun <T : Any> safeApiCallWithHandlingToken(
        call: suspend () -> Response<T>,
        extractToken: (T) -> String?
    ): NetworkResult<T, NetworkError> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                response.let { response ->
                    val body = response.body()
                    if (body != null) {
                        extractToken(body).takeUnless { it.isNullOrEmpty() }?.let { token ->
                            tokenManager.saveAccessToken(token)
                        }
                        NetworkResult.Success(body)
                    } else {
                        NetworkResult.Error(NetworkError.UNKNOWN, "Response body is null")
                    }
                }
            } else {
                mapResponseCodeToNetworkError(
                    response.code(),
                    response.message(),
                    response.errorBody()?.string()
                )
            }
        } catch (e: IOException) {
            // Network exceptions (no internet, timeout, etc.)
            NetworkResult.Error(NetworkError.REQUEST_FAILED, e.message ?: "Network request failed")
        } catch (e: Exception) {
            // Other unexpected exceptions during the call or processing
            NetworkResult.Error(NetworkError.UNKNOWN, e.message ?: "An unknown error occurred")
        }
    }

    private fun mapResponseCodeToNetworkError(
        code: Int,
        responseMessage: String?,
        errorBody: String?
    ): NetworkResult.Error<NetworkError> {
        val detailMessage = buildString {
            append("Error Code: $code")
            responseMessage?.takeIf { it.isNotBlank() }?.let { append(" - Message: $it") }
            errorBody?.takeIf { it.isNotBlank() }?.let { append(" - Details: $it") }
        }
        return when (code) {
            401 -> NetworkResult.Error(NetworkError.UNAUTHORIZED, detailMessage.ifEmpty { null })
            408 -> NetworkResult.Error(NetworkError.REQUEST_FAILED, detailMessage.ifEmpty { null })
            409 -> NetworkResult.Error(NetworkError.CONFLICT, detailMessage.ifEmpty { null })
            413 -> NetworkResult.Error(
                NetworkError.PAYLOAD_TOO_LARGE,
                detailMessage.ifEmpty { null })

            in 500..599 -> NetworkResult.Error(
                NetworkError.SERVER_ERROR,
                detailMessage.ifEmpty { null })

            else -> NetworkResult.Error(NetworkError.UNKNOWN, detailMessage.ifEmpty { null })
        }
    }
}