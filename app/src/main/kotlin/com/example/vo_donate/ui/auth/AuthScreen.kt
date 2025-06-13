package com.example.vo_donate.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.vo_donate.navigation.VoteDestination
import com.example.vo_donate.navigation.navigateSingleTopTo
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuthScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val pagerState =
        rememberPagerState(initialPage = 0, pageCount = { 2 }) // 0 for Login, 1 for Register
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = uiState.navigationEvent) {
        when (uiState.navigationEvent) {
            AuthNavigationEvent.NAVIGATE_TO_MAIN -> {
                navController.navigateSingleTopTo(VoteDestination.route)
            }

            AuthNavigationEvent.NAVIGATE_TO_LOGIN -> {
                // This might be redundant if AuthScreen is the login destination
                // but could be used if logging out from another screen
                coroutineScope.launch {
                    pagerState.animateScrollToPage(0) // Go to login page
                }
            }

            AuthNavigationEvent.NAVIGATE_TO_REGISTER -> {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(1)
                }
            }
        }
        authViewModel.consumeNavigationEvent() // Consume the event
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()), // Make content scrollable if it overflows
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome.",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))

            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Login") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Register") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))


            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = !uiState.isAuthenticated // Disable swipe if authenticated (only register visible)
            ) { page ->
                when (page) {
                    0 -> { // Login Page
                        // Only show Login Card if not authenticated
                        AnimatedVisibility(visible = !uiState.isAuthenticated) {
                            AuthCard {
                                LoginContent(uiState = uiState, viewModel = authViewModel)
                            }
                        }
                        // If authenticated, and pager is somehow on page 0, show a placeholder or nothing
                        if (uiState.isAuthenticated) {
                            Spacer(modifier = Modifier.height(200.dp)) // Placeholder height
                        }
                    }

                    1 -> { // Register Page
                        AuthCard {
                            RegisterContent(
                                uiState = uiState,
                                viewModel = authViewModel,
                                isOnlyRegisterVisible = uiState.isAuthenticated
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AuthCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun LoginContent(
    uiState: AuthScreenUiState,
    viewModel: AuthViewModel
) {
    val id by viewModel.id.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Text(
        "Login to Your Account",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = id,
        onValueChange = { viewModel.onIdChange(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("ID") },
        leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = "ID") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        enabled = !uiState.isLoading
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = password,
        onValueChange = { viewModel.onPasswordChange(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        enabled = !uiState.isLoading
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = {
            focusManager.clearFocus() // Dismiss keyboard
            viewModel.login()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isLoading,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text("LOGIN", fontSize = 16.sp)
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun RegisterContent(
    uiState: AuthScreenUiState,
    viewModel: AuthViewModel,
    isOnlyRegisterVisible: Boolean // To control the title based on the context
) {
    val id by viewModel.id.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val introduction by viewModel.introduction.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Text(
        if (isOnlyRegisterVisible) "Complete Your Registration" else "Create a New Account",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(24.dp))

    // Name and Introduction are always part of registration form
    OutlinedTextField(
        value = name,
        onValueChange = { viewModel.onNameChange(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Full Name") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Full Name") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        enabled = !uiState.isLoading
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = id,
        onValueChange = { viewModel.onIdChange(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("ID") },
        leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = "ID") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        enabled = !uiState.isLoading
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { viewModel.onPasswordChange(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        enabled = !uiState.isLoading
    )
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = introduction,
        onValueChange = { viewModel.onIntroductionChange(it) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Introduction") },
        leadingIcon = {
            Icon(
                Icons.Default.Person,
                contentDescription = "Introduction"
            )
        }, // Choose a better icon
        singleLine = false,
        maxLines = 3,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        enabled = !uiState.isLoading
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(
        onClick = {
            focusManager.clearFocus() // Dismiss keyboard
            viewModel.register()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isLoading,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text("REGISTER", fontSize = 16.sp)
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { viewModel.onLoginClick() },
        modifier = Modifier.fillMaxWidth(),
        enabled = !uiState.isLoading,
        contentPadding = PaddingValues(vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Text("Want to Login >", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
    }
}

