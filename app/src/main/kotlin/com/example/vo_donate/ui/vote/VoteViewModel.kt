package com.example.vo_donate.ui.vote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.kevin.data.repository.ProposalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.System
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration

class VoteViewModel(
    private val proposalRepository: ProposalRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(VoteNavigatorUiState())
    val uiState: StateFlow<VoteNavigatorUiState> = _uiState.asStateFlow()

    /** To Keep Track of Jobs Updating TimeLeft for Each Item*/
    private val itemTimeUpdaterJobs = mutableMapOf<String, Job>()

    /** To Keep Track of Donation Fee for Each Item */
    // val donationFee = MutableStateFlow
    val donationFee = MutableStateFlow<Map<String, String>>(emptyMap())

    /** To Keep Track of Items to be voted */
    val isProposalVoted = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    init {
        fetchProposalItems()
    }

    fun onVoteStatusChange(currentProposalId: String) {
        val newVoteStatusMap = isProposalVoted.value.toMutableMap()
        newVoteStatusMap[currentProposalId] = true
        isProposalVoted.value = newVoteStatusMap
        clearVoteSubmissionMessage()
    }

    fun onDonationFeeChange(currentProposalId: String, newDonationFee: String) {
        val newDonationFeeMap = donationFee.value.toMutableMap()
        newDonationFeeMap[currentProposalId] = newDonationFee
        donationFee.value = newDonationFeeMap
    }

    fun onCastVote(proposalId: String, selectedOption: ApprovalStatus) {
        if (_uiState.value.isSubmittingVote) {
            return
        }
        _uiState.update {
            it.copy(
                isSubmittingVote = true,
                voteSubmissionMessage = null
            )
        }
        viewModelScope.launch {
            try {
                val voteResponse = proposalRepository.vote(
                    proposalId,
                    isApprove = (selectedOption == ApprovalStatus.APPROVE)
                )
                if (voteResponse.message == "Vote Failed") {
                    _uiState.update {
                        it.copy(
                            isSubmittingVote = false,
                            voteSubmissionMessage = "Vote Failed"
                        )
                    }
                } else {
                    _uiState.update { currentState ->
                        val updatedVoteList = currentState.voteList.map { displayableItem ->
                            if (displayableItem.coreData.id == proposalId) {
                                onVoteStatusChange(proposalId)
                                displayableItem.copy(
                                    isVoted = true
                                )
                            } else {
                                displayableItem
                            }
                        }
                        currentState.copy(
                            voteList = updatedVoteList,
                            isSubmittingVote = false,
                            voteSubmissionMessage = "Vote for $selectedOption on the Proposal"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmittingVote = false,
                        voteSubmissionMessage = "An error occurred: ${e.message}"
                    )
                }
            }
        }
    }

    fun onSubmitDonation(proposalId: String) {
        val currentDonationFee = donationFee.value[proposalId]
        if (currentDonationFee.isNullOrBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Donation Fee cannot be empty")
            }
            return
        } else {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isSubmittingDonation = true,
                    errorMessage = null,
                    voteSubmissionMessage = null
                )
            }
            viewModelScope.launch {
                val donationResponse = proposalRepository.donate(proposalId, currentDonationFee)
                if (donationResponse.message == "Donate Failed") {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Donation Failed",
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSubmittingDonation = false,
                            errorMessage = null,
                            voteSubmissionMessage = "Your $currentDonationFee Wei Donation Successful"
                        )
                    }
                }
            }
        }
    }

    fun clearVoteSubmissionMessage() {
        _uiState.update { it.copy(voteSubmissionMessage = null) }
    }


    fun fetchProposalItems(
    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val proposalListFromRepo = proposalRepository.getProposals()

                // Cancel Previous Updater Jobs Before Processing New Items
                cancelAllTimeUpdaterJobs()

                val displayableProposalItems = proposalListFromRepo.map { coreProposalItem ->
                    val proposalDetail = proposalRepository.getProposalById(coreProposalItem.id)

                    val voteEndTime = coreProposalItem.voteEndTime
                    val donationEndTime = coreProposalItem.donationEndTime
                    val proposalStatus = getProposalType(voteEndTime, donationEndTime)

                    val timeLeft = if (proposalStatus == ProposalStatus.VOTE_IN_PROGRESS) {
                        calculateTimeLeft(voteEndTime)
                    } else {
                        calculateTimeLeft(donationEndTime)
                    }

                    val timeLeftFlow = MutableStateFlow(timeLeft to proposalStatus)

                    itemTimeUpdaterJobs[coreProposalItem.id] = launchTimeLeftUpdater(
                        itemId = coreProposalItem.id,
                        timeLimit = voteEndTime to donationEndTime,
                        timeLeftFlow = timeLeftFlow
                    )

                    onDonationFeeChange(coreProposalItem.id, "")

                    DisplayableProposalItem(
                        coreData = ProposalItem(
                            id = coreProposalItem.id,
                            title = proposalDetail.proposalText,
                            voteEndTime = coreProposalItem.voteEndTime,
                            donationEndTime = coreProposalItem.donationEndTime,
                            totalDonationFee = proposalDetail.totalDonation,
                            minimumDonationFee = proposalDetail.balance,
                            approvedVoter = proposalDetail.voteCount,
                            totalVoter = proposalDetail.voterCount,
                            votePassed = proposalDetail.votePassed,
                            proposalFinalized = proposalDetail.finalized
                        ),
                        isVoted = if (isProposalVoted.value[coreProposalItem.id] == null) {
                            false
                        } else {
                            isProposalVoted.value[coreProposalItem.id]!!
                        },
                        timeLeft = timeLeftFlow.asStateFlow()
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        voteList = displayableProposalItems.filter { it.timeLeft.value.second == ProposalStatus.VOTE_IN_PROGRESS },
                        donationList = displayableProposalItems.filter { it.timeLeft.value.second == ProposalStatus.DONATION_IN_PROGRESS })
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load proposals: ${e.message}"
                    )
                }
            }
        }
    }

    private fun launchTimeLeftUpdater(
        itemId: String,
        timeLimit: Pair<String, String>,
        timeLeftFlow: MutableStateFlow<Pair<String, ProposalStatus>>
    ): Job {
        return viewModelScope.launch(Dispatchers.Default) {
            try {
                val (voteEndTime, donationEndTime) = timeLimit
                while (isActive) {
                    val proposalStatus = getProposalType(voteEndTime, donationEndTime)
                    if (proposalStatus == ProposalStatus.VOTE_IN_PROGRESS) {
                        timeLeftFlow.value = calculateTimeLeft(voteEndTime) to proposalStatus
                    } else if (proposalStatus == ProposalStatus.DONATION_IN_PROGRESS) {
                        timeLeftFlow.value = calculateTimeLeft(donationEndTime) to proposalStatus
                    } else {
                        timeLeftFlow.value = "Time Expired" to proposalStatus
                        itemTimeUpdaterJobs.remove(itemId)
                        break
                    }
                }
                delay(1000)
            } catch (e: Exception) {
                timeLeftFlow.value = "Error" to ProposalStatus.ERROR
                itemTimeUpdaterJobs.remove(itemId)
            }
        }
    }

    private fun getProposalType(voteEndTime: String, donationEndTime: String): ProposalStatus {
        val givenTimeZone = TimeZone.currentSystemDefault()

        val voteTimeLimitInstant =
            LocalDateTime.parse(voteEndTime.replace(" ", "T")).toInstant(givenTimeZone)
        val donationTimeLimitInstant =
            LocalDateTime.parse(donationEndTime.replace(" ", "T")).toInstant(givenTimeZone)
        val currentTimeInstant = System.now()

        return if (currentTimeInstant < voteTimeLimitInstant) {
            ProposalStatus.VOTE_IN_PROGRESS
        } else if (currentTimeInstant < donationTimeLimitInstant) {
            ProposalStatus.DONATION_IN_PROGRESS
        } else {
            ProposalStatus.DONATION_ENDED
        }
    }

    private fun calculateTimeLeft(
        limitTime: String,
        finishedText: String = "Time Expired",
        errorText: String = "Invalid Time Format",
        notSetText: String = "No End Time"
    ): String {
        if (limitTime.isEmpty()) return notSetText
        return try {
            val givenTimeZone = TimeZone.currentSystemDefault()

            val limitTimeInstant =
                LocalDateTime.parse(limitTime.replace(" ", "T")).toInstant(givenTimeZone)
            val currentTimeInstant = System.now()

            if (currentTimeInstant < limitTimeInstant) {
                formatDuration(limitTimeInstant - currentTimeInstant)
            } else {
                finishedText
            }
        } catch (e: Exception) {
            errorText
        }
    }

    private fun formatDuration(duration: Duration): String {
        val absoluteDuration = duration.absoluteValue

        return when {
            absoluteDuration.inWholeDays > 0 -> "${absoluteDuration.inWholeDays} Days and ${absoluteDuration.inWholeHours - absoluteDuration.inWholeDays * 24} Left"
            absoluteDuration.inWholeHours > 0 -> "${absoluteDuration.inWholeHours} Hours and ${absoluteDuration.inWholeMinutes - absoluteDuration.inWholeHours * 60} Left"
            absoluteDuration.inWholeMinutes > 0 -> "${absoluteDuration.inWholeMinutes} Minutes and ${absoluteDuration.inWholeSeconds - absoluteDuration.inWholeMinutes * 60} Left"
            else -> "${absoluteDuration.inWholeSeconds} Seconds Left"
        }
    }

    private fun cancelAllTimeUpdaterJobs() {
        itemTimeUpdaterJobs.values.forEach { if (it.isActive) it.cancel() }
        itemTimeUpdaterJobs.clear()
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllTimeUpdaterJobs()
    }

}
