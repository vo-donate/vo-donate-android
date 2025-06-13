package com.example.vo_donate.navigation

sealed interface BottomNavDestination {
    val route: String
}

data object AuthDestination : BottomNavDestination {
    override val route = "auth"
}

data object VoteDestination : BottomNavDestination {
    override val route = "vote"
}

data object ProposalDestination : BottomNavDestination {
    override val route = "proposal"
}

data object UserInfoDestination : BottomNavDestination {
    override val route = "user"
}

val bottomNavDestinations = listOf(AuthDestination, VoteDestination, ProposalDestination, UserInfoDestination)
