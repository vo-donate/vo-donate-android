package com.example.vo_donate.ui.user

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vo_donate.navigation.AuthDestination
import com.example.vo_donate.navigation.navigateSingleTopTo
import kotlinx.coroutines.launch

@Composable
fun UserInfoScreen(
    navController: NavHostController,
    userInfoViewModel: UserInfoViewModel
) {
    val uiState by userInfoViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = uiState.navigationEvent) {
        userInfoViewModel.consumeNavigationEvent()
        when (uiState.navigationEvent) {
            UserInfoNavigationEvent.NAVIGATE_TO_AUTH -> {
                navController.navigateSingleTopTo(AuthDestination.route)
            }

            UserInfoNavigationEvent.REMAIN_CURRENT -> {
                // No action needed
            }
        }
    }

    // The Scaffold is kept for overall structure and background color,
    // but its topBar parameter is removed.
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Your Information.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 32.dp),
                style = MaterialTheme.typography.labelLarge
            )
            Box(
                modifier = Modifier
                    // Apply padding provided by Scaffold (for system bars if configured)
                    // and then add additional desired screen padding.
                    .statusBarsPadding() // Add this if you want to respect the status bar area
                    .navigationBarsPadding() // Optionally add this if you want to respect the navigation bar area
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage ?: "An error occurred.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                } else if (uiState.userInfo != null) {
                    val user = uiState.userInfo!!
                    Card(
                        modifier = Modifier
                            .padding(bottom = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseOnSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                // Add specific padding for the content itself,
                                // now that TopAppBar is gone.
                                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            // Profile Header (Avatar and Name)
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.id,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            // User Details Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    UserInfoRow(
                                        icon = Icons.Default.Info,
                                        label = "Introduction",
                                        value = user.introduction
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    UserInfoRow(
                                        icon = Icons.Default.AccountBalanceWallet,
                                        label = "Balance",
                                        value = user.balance
                                    )
                                }
                            }

                        }
                    }

                } else {
                    Text(
                        text = "No user information available.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        // --- User Proposals Section ---
        UserProposalsSection(
            proposals = uiState.proposals.reversed(),
            isLoading = uiState.isLoading, // To show loading specifically for proposals if needed
            errorMessage = if (uiState.proposals.isEmpty() && !uiState.isLoading) uiState.errorMessage else null
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserProposalsSection(
    proposals: List<ProposalInfo>,
    isLoading: Boolean,
    errorMessage: String?
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()
    val tabTitles = listOf("Activated", "Finalized")

    val activeProposals = remember(proposals) { proposals.filter { !it.isProposalFinalized } }
    val finalizedProposals = remember(proposals) { proposals.filter { it.isProposalFinalized } }

    Column(
        Modifier
            .padding(horizontal = 32.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Your Proposals.",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.labelLarge
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface, // Or background
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Ensure it takes available space
        ) { pageIndex ->
            val currentList = if (pageIndex == 0) activeProposals else finalizedProposals
            val listTypeTitle = if (pageIndex == 0) "Activated" else "Finalized"

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
            ) {
                if (isLoading && proposals.isEmpty()) { // Show main loading only if proposals list is empty during initial load
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (errorMessage != null && currentList.isEmpty()) { // Show error if specific list is empty due to error
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                } else if (currentList.isEmpty()) {
                    Text(
                        text = "No $listTypeTitle found.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            currentList, key = { it.proposalId }
                        ) { proposal ->
                            ProposalItemCard(proposalInfo = proposal)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ProposalItemCard(proposalInfo: ProposalInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val statusColor = if (proposalInfo.isProposalFinalized) {
                if (proposalInfo.isProposalPassed) Color(0xFF82AF83) else MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            }

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = proposalInfo.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${proposalInfo.totalDonation} wei",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column (
                modifier = Modifier.fillMaxWidth()
            ){
                val progress = if (proposalInfo.totalVoterCount > 0) {
                    proposalInfo.voteCount.toFloat() / proposalInfo.totalVoterCount.toFloat()
                } else {
                    0f // Default to 0 progress if no votes yet
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${proposalInfo.voteCount} Approved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${proposalInfo.totalVoterCount} Total Users",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            /**
            if (proposalInfo.isProposalFinalized) {
                Spacer(modifier = Modifier.height(8.dp))
                val statusText =
                    if (proposalInfo.isProposalPassed) "Status: Passed" else "Status: Failed"
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Status: Active",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary // Or another suitable color for active
                )
            }
            */
        }
    }
}

// Re-usable InfoRow (can be moved to a common UI file if used elsewhere)
@Composable
fun ProposalInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


// UserInfoRow composable remains the same
@Composable
fun UserInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 6.dp, end = 12.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value.ifBlank { "Not provided" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}