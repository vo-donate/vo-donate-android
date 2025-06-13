package com.example.vo_donate.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kevin.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserInfo(
    val id: String,
    val name: String,
    val walletAddress: String,
    val introduction: String,
    val balance: String = "0"
)

data class ProposalInfo(
    val proposalId: String,
    val title: String,
    val isProposalFinalized: Boolean,
    val isProposalPassed: Boolean,
    val voteCount: Int,
    val totalVoterCount: Int,
    val totalDonation: String,
)

data class UserInfoUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userInfo: UserInfo? = null,
    val proposals: List<ProposalInfo> = emptyList(),
    val errorMessage: String? = null,
    val navigationEvent: UserInfoNavigationEvent = UserInfoNavigationEvent.NAVIGATE_TO_AUTH
)

enum class UserInfoNavigationEvent {
    NAVIGATE_TO_AUTH,
    REMAIN_CURRENT
}

class UserInfoViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserInfoUiState())
    val uiState: StateFlow<UserInfoUiState> = _uiState.asStateFlow()

    fun consumeNavigationEvent() {
        checkInitialLoginStatus()
        if (uiState.value.isLoggedIn) {
            updateUserInfo()
        }
    }

    private fun updateUserInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val infoResult = authRepository.getUserInfo()
            val balanceResult = authRepository.getUserBalance()

            if (infoResult.id == "-1" || balanceResult.walletAddress == "-1") {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        errorMessage = "Error Occurred",
                        navigationEvent = UserInfoNavigationEvent.NAVIGATE_TO_AUTH
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userInfo = UserInfo(
                            id = infoResult.id,
                            name = infoResult.name,
                            walletAddress = balanceResult.walletAddress,
                            introduction = infoResult.introduction,
                            balance = balanceResult.balance.plus(" wei")
                        ),
                    )
                }
                // When Update Success, Update Proposals
                updateUserProposals()
            }
        }
    }

    private fun updateUserProposals() {
        viewModelScope.launch {
            val currentUserId = _uiState.value.userInfo?.id
            if (currentUserId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "User ID not found for fetching proposals."
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }
            val proposalsById = authRepository.getProposalsByUserId(currentUserId)

            _uiState.update { currentState ->
                if (proposalsById.isNotEmpty()) {
                    val updatedProposalsList = proposalsById.map { proposal ->
                        ProposalInfo(
                            proposalId = proposal.id,
                            title = proposal.proposalText,
                            isProposalFinalized = proposal.finalized,
                            isProposalPassed = proposal.votePassed,
                            voteCount = proposal.voteCount,
                            totalVoterCount = proposal.voterCount,
                            totalDonation = proposal.totalDonation
                        )
                    }
                    currentState.copy(
                        proposals = updatedProposalsList,
                        isLoading = false
                    )
                } else {
                    currentState.copy(
                        isLoading = false,
                        errorMessage = "You have not make any proposals"
                    )
                }
            }

        }
    }

    private fun checkInitialLoginStatus() {
        _uiState.update {
            if (authRepository.checkInitialLoginStatus()) {
                it.copy(
                    isLoggedIn = true,
                    errorMessage = null,
                    navigationEvent = UserInfoNavigationEvent.REMAIN_CURRENT
                )
            } else {
                it.copy(
                    isLoggedIn = false,
                    navigationEvent = UserInfoNavigationEvent.NAVIGATE_TO_AUTH
                )
            }
        }
    }
}