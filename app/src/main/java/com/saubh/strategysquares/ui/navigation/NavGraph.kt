package com.saubh.strategysquares.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.saubh.strategysquares.ui.screens.GameLobbyScreen
import com.saubh.strategysquares.ui.screens.GamePlayScreen
import com.saubh.strategysquares.ui.screens.LeaderboardScreen
import com.saubh.strategysquares.ui.screens.SignInScreen

sealed class Screen(val route: String) {
    object SignIn : Screen("signIn")
    object GameLobby : Screen("gameLobby")
    object GamePlay : Screen("gamePlay/{gameId}") {
        fun createRoute(gameId: String) = "gamePlay/$gameId"
    }
    object Leaderboard : Screen("leaderboard")
}

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onSignInClick: () -> Unit,
    isSignedIn: Boolean = false
) {
    // Set start destination based on sign-in state
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            navController.navigate(Screen.GameLobby.route) {
                popUpTo(Screen.SignIn.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.SignIn.route,
        modifier = modifier
    ) {
        composable(Screen.SignIn.route) {
            SignInScreen(
                onSignInClick = onSignInClick,
                onSignInSuccess = {
                    navController.navigate(Screen.GameLobby.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.GameLobby.route) {
            GameLobbyScreen(
                onCreateGame = { gameId ->
                    navController.navigate(Screen.GamePlay.createRoute(gameId))
                },
                onJoinGame = { gameId ->
                    navController.navigate(Screen.GamePlay.createRoute(gameId))
                },
                onViewLeaderboard = {
                    navController.navigate(Screen.Leaderboard.route)
                }
            )
        }

        composable(Screen.GamePlay.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            GamePlayScreen(
                gameId = gameId,
                onLeaveGame = { navController.navigate(Screen.GameLobby.route) }
            )
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                onBack = { navController.navigateUp() }
            )
        }
    }
}
