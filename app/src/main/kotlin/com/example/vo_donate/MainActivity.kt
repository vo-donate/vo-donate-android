package com.example.vo_donate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.kevin.core.ui.theme.VodonateTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.vo_donate.application.InitApplication
import com.example.vo_donate.di.AppModule
import com.example.vo_donate.navigation.AuthDestination
import com.example.vo_donate.navigation.BottomNavHost
import com.example.vo_donate.navigation.bottomNavDestinations
import com.example.vo_donate.navigation.navigateSingleTopTo
import com.example.vo_donate.ui.components.BottomNav

class MainActivity : ComponentActivity() {
    private lateinit var appModule: AppModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            /**
            val authViewModel = viewModel<AuthViewModel>(
            factory = viewModelFactory{
            AuthViewModel(InitApplication.appModule.authRepository)
            }
            )
            val state = authViewModel.loginState
             */
            appModule = InitApplication.appModule

            VodonateTheme {
                VoDonateApp(appModule = appModule)
            }
        }
    }
}

@Composable
fun VoDonateApp(appModule: AppModule) {
    VodonateTheme {
        val navController = rememberNavController()
        val currentBackStack by navController.currentBackStackEntryAsState()
        val currentDestination = currentBackStack?.destination
        val currentScreen =
            bottomNavDestinations.find { it.route == currentDestination?.route } ?: AuthDestination

        Scaffold(
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth(),
            bottomBar = {
                BottomNav(
                    allScreens = bottomNavDestinations,
                    onTabSelected = { newScreen ->
                        navController.navigateSingleTopTo(newScreen.route)
                    },
                    currentScreen = currentScreen
                )
            }
        ) { innerPadding ->
            BottomNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                appModule
            )
        }
    }
}

/**
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
VodonateTheme {
VoDonateApp()
}
}
 */