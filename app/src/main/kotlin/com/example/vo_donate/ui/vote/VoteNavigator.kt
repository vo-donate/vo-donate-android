package com.example.vo_donate.ui.vote

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vo_donate.ui.components.RefreshFloatingButton
import kotlinx.coroutines.launch
import kotlin.text.isNotBlank

enum class ItemCardType { VOTE, DONATION }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VoteNavigator(
    viewModel: VoteViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val tabTitles = listOf("Active Voting", "Active Donations")

    LaunchedEffect(uiState.voteSubmissionMessage) {
        viewModel.fetchProposalItems()
        uiState.voteSubmissionMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearVoteSubmissionMessage()
        }
    }

    Scaffold(
        floatingActionButton = {
            RefreshFloatingButton(
                isLoading = uiState.isLoading,
                onClick = { viewModel.fetchProposalItems() },
                modifier = Modifier.padding(end = 16.dp)
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Vote To Donate.",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 32.dp),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(14.dp))
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    when (pageIndex) {
                        0 -> ProposalListContent( // Renamed for clarity
                            items = uiState.voteList, // Use voteList from UiState
                            isLoading = uiState.isLoading && uiState.voteList.isEmpty(),
                            errorMessage = uiState.errorMessage,
                            listTypeTitle = "Voting Proposals",
                            itemCardType = ItemCardType.VOTE,
                            viewModel = viewModel
                        )

                        1 -> ProposalListContent( // Renamed for clarity
                            items = uiState.donationList, // Use donationList from UiState
                            isLoading = uiState.isLoading && uiState.donationList.isEmpty(),
                            errorMessage = uiState.errorMessage,
                            listTypeTitle = "Donation Proposals",
                            itemCardType = ItemCardType.DONATION,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProposalListContent(
    items: List<DisplayableProposalItem>,
    isLoading: Boolean,
    errorMessage: String?,
    listTypeTitle: String,
    itemCardType: ItemCardType,
    viewModel: VoteViewModel // Pass ViewModel for actions
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        contentAlignment = Alignment.TopStart
    ) { // Added padding
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(4.dp)
            )
        } else if (items.isEmpty()) {
            Text(
                text = "No items found for '$listTypeTitle'.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge, // Style from MakeProposalScreen
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(4.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    horizontal = 4.dp,
                    vertical = 8.dp
                ), // Style from MakeProposalScreen
                verticalArrangement = Arrangement.spacedBy(12.dp) // Style from MakeProposalScreen
            ) {
                items.reversed()
                    .filter {
                        it.timeLeft.value.second == ProposalStatus.VOTE_IN_PROGRESS ||
                                (it.timeLeft.value.second == ProposalStatus.DONATION_IN_PROGRESS && it.coreData.votePassed)
                    }
                    .forEach {
                        item {
                            when (itemCardType) {
                                ItemCardType.VOTE -> VotePhaseCard(it, viewModel)
                                ItemCardType.DONATION -> DonationPhaseCard(it, viewModel)
                            }
                        }
                    }
            }
        }
    }
}

@Composable
fun VotePhaseCard(
    displayableItem: DisplayableProposalItem,
    viewModel: VoteViewModel
) {
    val voteItem = displayableItem.coreData
    val timeLeft by displayableItem.timeLeft.collectAsStateWithLifecycle()
    val voteStatus = displayableItem.isVoted
    val selectedOption by displayableItem.approvalStatus.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle() // For isSubmittingVote
    // var selectedOptionListener = ApprovalStatus.NOT_VOTE

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = voteItem.title.ifBlank { "Untitled Proposal" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Voting Ends: ${timeLeft.first}", // Combine label and value
                style = MaterialTheme.typography.bodySmall,
                color = if (timeLeft.first == "Time Expired") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = if (timeLeft.first == "Time Expired") FontStyle.Italic else FontStyle.Normal
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)) // Clip the section for visual grouping
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) // Subtle background
                    .padding(12.dp)
            ) {
                Text(
                    text = "Current Tally",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val progress = if (voteItem.totalVoter > 0) {
                    voteItem.approvedVoter.toFloat() / voteItem.totalVoter.toFloat()
                } else {
                    0f // Default to 0 progress if no votes yet
                }

                LinearProgressIndicator(
                    progress = { progress }, // Pass the calculated progress
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary, // Color for the progress portion
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), // Color for the background track
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${voteItem.approvedVoter} Approved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${voteItem.totalVoter} Total Users",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            InfoRowWithIcon(
                icon = Icons.Default.Info, // Example
                label = "Fee to Qualify:", // Clearer label
                value = voteItem.minimumDonationFee,
                iconTint = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (voteStatus == false && timeLeft.second == ProposalStatus.VOTE_IN_PROGRESS) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HowToVote, // Example Icon
                        contentDescription = "Cast your vote",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cast Your Vote:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                voteItem.voteOption.forEach { option ->
                    val isSelected = (selectedOption == option)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .selectable(
                                selected = isSelected,
                                onClick = {
                                    if (!uiState.isSubmittingVote) {
                                        displayableItem.approvalStatus.value = option
                                    }
                                }
                            )
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else Color.Transparent
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (!uiState.isSubmittingVote) {
                                    displayableItem.approvalStatus.value = option
                                }
                            },
                            enabled = !uiState.isSubmittingVote,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.scale(0.85f)
                        )
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        selectedOption.let { currentSelection ->
                            viewModel.onCastVote(voteItem.id, currentSelection)
                        }
                    },
                    enabled = !uiState.isSubmittingVote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (uiState.isSubmittingVote /* && relevant for this item */) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("SUBMITTING...")
                    } else {
                        Text("SUBMIT VOTE", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (voteStatus == true && timeLeft.second == ProposalStatus.VOTE_IN_PROGRESS) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = "You already voted",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = if (timeLeft.first == "Time Expired") "Voting has ended for this proposal."
                        else "Voting is not currently active.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DonationPhaseCard(
    displayableItem: DisplayableProposalItem,
    viewModel: VoteViewModel
) {
    val voteItem = displayableItem.coreData
    val timeLeft by displayableItem.timeLeft.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Retrieve the current input for cards
    val currentDonationInput by viewModel.donationFee.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = voteItem.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            InfoRow("Donation Ends:", timeLeft.first)
            InfoRow("Total Raised:", voteItem.totalDonationFee) // As specified

            Spacer(modifier = Modifier.height(12.dp))

            if (timeLeft.second != ProposalStatus.DONATION_ENDED) {
                OutlinedTextField( // Style from MakeProposalScreen
                    value = currentDonationInput[displayableItem.coreData.id]!!,
                    onValueChange = { newValue ->
                        // Allow only numbers and one decimal point
                        if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            viewModel.onDonationFeeChange(displayableItem.coreData.id, newValue)
                        }
                    },
                    label = { Text("Donation Amount (e.g., wei)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors( // Style
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (currentDonationInput[displayableItem.coreData.id]!!.isNotBlank()) {
                            viewModel.onSubmitDonation(voteItem.id) // ViewModel handles using the stored fee
                        }
                    },
                    enabled = !uiState.isSubmittingDonation, // Assuming isSubmittingVote can be global or donation specific
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    // Assuming a global `isSubmittingVote` or you might need a specific `isSubmittingDonation`
                    if (uiState.isSubmittingDonation /* && relevant for this donation */) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PROCESSING...")
                    } else {
                        Text("DONATE NOW")
                    }
                }
            } else {
                Text(
                    "Donation period has ended.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) { // Helper for consistent styling
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        ) // Style
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        ) // Style
    }
}

@Composable
fun InfoRowWithIcon(
    icon: ImageVector? = null,
    label: String,
    value: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "$label ", // Add space for separation
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f) // Adjust weight if icon is present
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = valueColor,
            modifier = Modifier.weight(0.55f),
            textAlign = TextAlign.End // Align value to the end for better readability
        )
    }
}

