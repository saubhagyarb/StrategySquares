package com.saubh.strategysquares.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saubh.strategysquares.ui.MainViewModel
import com.saubh.strategysquares.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameLobbyScreen(
    onCreateGame: (String) -> Unit,
    onJoinGame: (String) -> Unit,
    onViewLeaderboard: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var joinGameCode by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation when game room is created or joined
    LaunchedEffect(uiState.currentGameRoom) {
        uiState.currentGameRoom?.let { gameRoom ->
            onCreateGame(gameRoom.roomId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Lobby") },
                actions = {
                    // Leaderboard button
                    IconButton(onClick = onViewLeaderboard) {
                        Icon(
                            painter = painterResource(id = R.drawable.emoji_events),
                            contentDescription = "Leaderboard"
                        )
                    }
                    // Sign out button
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(
                            painter = painterResource(R.drawable.logout),
                            contentDescription = "Sign out"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome message
            Text(
                text = "Welcome, ${uiState.currentPlayer?.name}",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Create game button
            ElevatedButton(
                onClick = { viewModel.createGame() },
                modifier = Modifier.width(250.dp)
            ) {
                Text(
                    text = "Create New Game",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Text(
                text = "- OR -",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Join game section
            OutlinedTextField(
                value = joinGameCode,
                onValueChange = { joinGameCode = it.uppercase() },
                label = { Text("Enter Game Code") },
                singleLine = true,
                modifier = Modifier.width(250.dp)
            )

            Button(
                onClick = {
                    if (joinGameCode.isNotBlank()) {
                        viewModel.joinGame(joinGameCode)
                    }
                },
                enabled = joinGameCode.isNotBlank(),
                modifier = Modifier.width(250.dp)
            ) {
                Text(
                    text = "Join Game",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Error message
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
