package com.example.vo_donate.ui.proposal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vo_donate.navigation.AuthDestination
import com.example.vo_donate.navigation.VoteDestination
import com.example.vo_donate.navigation.navigateSingleTopTo

// Assuming ProposalViewModel and its states are in this package or imported
// from com.example.vo_donate.ui.proposal

@Composable
fun MakeProposalScreen(
    navHostController: NavHostController,
    viewModel: ProposalViewModel // Or provide via factory
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val proposalText by viewModel.proposalText.collectAsStateWithLifecycle()
    val voteDuration by viewModel.voteDuration.collectAsStateWithLifecycle()
    val donationDuration by viewModel.donationDuration.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Handle navigation if not authenticated
    LaunchedEffect(key1 = uiState.isAuthenticated, key2 = uiState.isProposalConfirmed) {
        if (!uiState.isAuthenticated) {
            // Navigate to AuthScreen and clear back stack up to the start destination of the current graph
            navHostController.navigateSingleTopTo(AuthDestination.route)
        }
        if (uiState.isProposalConfirmed) {
            navHostController.navigateSingleTopTo(VoteDestination.route)
            viewModel.clearConfirmState()
        }
    }

    // Handle proposal submission result
    LaunchedEffect(uiState.proposalResult) {
        uiState.proposalResult?.let { result ->
            Toast.makeText(
                context,
                result.resultMessage,
                Toast.LENGTH_LONG
            ).show()
            viewModel.clearConfirmState()
            // Optionally navigate away or clear fields after successful submission
            // For now, we just show a toast. ViewModel doesn't clear fields automatically.
            // To clear fields, you could call viewModel.onProposalTextChange("") etc.
            // or navigate back: navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Only render the content if authenticated, otherwise the LaunchedEffect will navigate away
        if (uiState.isAuthenticated) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Make Your Proposal.",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)) {
                        OutlinedTextField(
                            value = proposalText,
                            onValueChange = {
                                viewModel.onProposalTextChange(it)
                                viewModel.clearErrorMessage()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Proposal Description") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "Proposal Description"
                                )
                            },
                            placeholder = { Text("Clearly describe your proposal...") },
                            minLines = 4,
                            maxLines = 10,
                            enabled = !uiState.isLoading
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = if (voteDuration == 0) "" else voteDuration.toString(),
                            onValueChange = {
                                viewModel.onVoteDurationChange(it.toIntOrNull() ?: 0)
                                viewModel.clearErrorMessage()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Vote Duration (minutes)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.HowToVote,
                                    contentDescription = "Vote Duration"
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = if (donationDuration == 0) "" else donationDuration.toString(),
                            onValueChange = {
                                viewModel.onDonationDurationChange(it.toIntOrNull() ?: 0)
                                viewModel.clearErrorMessage()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Donation Duration (minutes)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = "Donation Duration"
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
                }

                uiState.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus() // Dismiss keyboard
                        viewModel.submitProposal()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    enabled = !uiState.isLoading && proposalText.isNotBlank() && voteDuration > 0 && donationDuration > 0,
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("SUBMIT PROPOSAL", fontSize = 16.sp)
                }
            }
        } else {
            // Optional: Show a loading indicator or placeholder while waiting for auth check/navigation
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp) // Subtle indicator
                Text(
                    "Checking authentication...",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 50.dp)
                )
            }
        }
    }
}


/**
@Composable
fun MakeProposalScreen (
navHostController: NavHostController,
proposalViewModel: ProposalViewModel,
modifier: Modifier = Modifier
){
Column(
modifier = modifier
.fillMaxSize()
.padding(16.dp),
horizontalAlignment = Alignment.CenterHorizontally
) {
Text(
text = "Make a Proposal.",
fontSize = 36.sp,
fontWeight = FontWeight.Bold,
color = MaterialTheme.colorScheme.primary,
modifier = Modifier
.padding(top = 32.dp, bottom = 32.dp)
.align(Alignment.Start),
style = MaterialTheme.typography.labelLarge
)
}

}
 */