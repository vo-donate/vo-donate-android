package com.example.vo_donate.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun RefreshFloatingButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = {
            if (!isLoading) {
                onClick()
            }
        },
        modifier = modifier
            .alpha(if (isLoading) 0.6f else 1.0f), // Dim if loading, to indicate disabled state
        containerColor = MaterialTheme.colorScheme.secondaryContainer, // Or primaryContainer
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer  // Or onPrimaryContainer
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp), // Adjust size as needed
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh proposals"
            )
        }
    }
}