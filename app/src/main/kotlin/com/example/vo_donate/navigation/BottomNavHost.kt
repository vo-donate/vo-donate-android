package com.example.vo_donate.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vo_donate.di.AppModule
import com.example.vo_donate.di.viewModelFactory
import com.example.vo_donate.ui.auth.AuthScreen
import com.example.vo_donate.ui.auth.AuthViewModel
import com.example.vo_donate.ui.proposal.MakeProposalScreen
import com.example.vo_donate.ui.proposal.ProposalViewModel
import com.example.vo_donate.ui.user.UserInfoScreen
import com.example.vo_donate.ui.user.UserInfoViewModel
import com.example.vo_donate.ui.vote.VoteNavigator
import com.example.vo_donate.ui.vote.VoteViewModel

@Composable
fun BottomNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    module: AppModule
) {
    NavHost(
        navController = navController,
        startDestination = AuthDestination.route,
        modifier = modifier
    ) {
        composable(route = AuthDestination.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = viewModelFactory {
                    AuthViewModel(module.authRepository!!)
                }
            )
            AuthScreen(navController, authViewModel)
        }
        composable(route = VoteDestination.route) {
            val voteViewModel: VoteViewModel = viewModel(
                factory = viewModelFactory {
                    VoteViewModel(module.proposalRepository!!)
                }
            )
            VoteNavigator(voteViewModel)
        }
        composable(route = ProposalDestination.route) {
            val proposalViewModel: ProposalViewModel = viewModel(
                factory = viewModelFactory {
                    ProposalViewModel(module.proposalRepository!!)
                }
            )
            MakeProposalScreen(navController,proposalViewModel)
        }
        composable(route = UserInfoDestination.route) {
            val userInfoViewModel: UserInfoViewModel = viewModel(
                factory = viewModelFactory {
                    UserInfoViewModel(module.authRepository!!)
                }
            )
            UserInfoScreen(navController, userInfoViewModel)
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // re-selecting the same item
        launchSingleTop = true
        // Restore state when re-selecting a previously selected item
        restoreState = true
    }