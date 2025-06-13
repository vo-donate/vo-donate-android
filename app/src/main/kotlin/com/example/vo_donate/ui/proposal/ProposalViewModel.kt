package com.example.vo_donate.ui.proposal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kevin.data.repository.ProposalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProposalResult (
    val resultMessage: String,
    val proposalId: String,
    val proposalContractAddress: String
)

data class ProposalUiState (
    val isLoading: Boolean = false,
    val isProposalConfirmed: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val proposalResult: ProposalResult? = null
)

class ProposalViewModel (
    private val proposalRepository: ProposalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProposalUiState())
    val uiState: StateFlow<ProposalUiState> = _uiState.asStateFlow()

    val proposalText = MutableStateFlow("")
    val voteDuration = MutableStateFlow(0)
    val donationDuration = MutableStateFlow(0)

    init {
        checkInitialLoginStatus()
    }

    private fun checkInitialLoginStatus() {
        _uiState.update {
            it.copy(isAuthenticated = proposalRepository.checkInitialLoginStatus())
        }
    }

    fun onProposalTextChange(newProposalText: String) {
        proposalText.value = newProposalText
        clearErrorMessage()
    }

    fun onVoteDurationChange(newVoteDuration: Int) {
        voteDuration.value = newVoteDuration
        clearErrorMessage()
    }

    fun onDonationDurationChange(newDonationDuration: Int) {
        donationDuration.value = newDonationDuration
        clearErrorMessage()
    }

    fun submitProposal() {
        if (proposalText.value.isBlank() || voteDuration.value == 0 || donationDuration.value == 0) {
            _uiState.update {
                it.copy(errorMessage = "Proposal Text, Vote Duration, and Donation Duration cannot be empty")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = proposalRepository.addProposal(
                proposalText = proposalText.value.trim(),
                voteDurationInMinutes = voteDuration.value,
                donationDurationInMinutes = donationDuration.value
            )
            if (result.proposalId == "-1") {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error Occurred",
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        isProposalConfirmed = true,
                        errorMessage = null,
                        proposalResult = ProposalResult(
                            resultMessage = result.message,
                            proposalId = result.proposalId,
                            proposalContractAddress = result.proposalContractAddress
                        )
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        if (_uiState.value.errorMessage != null) {
            _uiState.update { it.copy(errorMessage = null) }
        }
    }

    fun clearConfirmState() {
        _uiState.update { it.copy(isProposalConfirmed = false, proposalResult = null) }
    }
}