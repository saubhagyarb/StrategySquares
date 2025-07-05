package com.saubh.strategysquares.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saubh.strategysquares.model.GameState
import com.saubh.strategysquares.model.GameStatus
import com.saubh.strategysquares.model.Player
import com.saubh.strategysquares.ui.MainViewModel
import com.saubh.strategysquares.ui.components.ChatComponent
import com.saubh.strategysquares.util.SoundEffects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayScreen(
    gameId: String,
    onLeaveGame: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameRoom = uiState.currentGameRoom
    val currentPlayer = uiState.currentPlayer

    // Add LaunchedEffect to start observing the game
    LaunchedEffect(gameId) {
        println("GamePlayScreen: Started with gameId=$gameId")
        viewModel.startObservingGame(gameId)
    }



    // Fix opponent logic
    val opponent = gameRoom?.gameState?.let { gameState ->
        when (currentPlayer?.uid) {
            gameState.player1.uid -> gameState.player2
            gameState.player2?.uid -> gameState.player1
            else -> null
        }
    }

    val isCurrentPlayerTurn = gameRoom?.gameState?.currentPlayer == currentPlayer?.uid

    LaunchedEffect(gameId) {
        println("GamePlayScreen: Launched with gameId=$gameId")
        println("Current player: ${currentPlayer?.name}, opponent: ${opponent?.name}")
        println("Game state: ${gameRoom?.gameState}")
    }

    BackHandler {
        viewModel.leaveGame()
        onLeaveGame()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Room: $gameId") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.leaveGame()
                        onLeaveGame()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game Status with more visible styling
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when {
                        gameRoom?.gameState?.gameStatus == GameStatus.WAITING ->
                            "Waiting for opponent..."
                        gameRoom?.gameState?.gameStatus == GameStatus.IN_PROGRESS && isCurrentPlayerTurn ->
                            "Your turn"
                        gameRoom?.gameState?.gameStatus == GameStatus.IN_PROGRESS ->
                            "${opponent?.name}'s turn"
                        gameRoom?.gameState?.gameStatus == GameStatus.FINISHED &&
                            gameRoom.gameState.winner == currentPlayer?.uid ->
                            "You won! 🎉"
                        gameRoom?.gameState?.gameStatus == GameStatus.FINISHED ->
                            "${opponent?.name} won!"
                        gameRoom?.gameState?.gameStatus == GameStatus.DRAW ->
                            "Game Draw!"
                        else -> ""
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Players info with improved display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Current player
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "You (${currentPlayer?.symbol})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = currentPlayer?.name ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                // Opponent with improved status display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (gameRoom?.gameState?.gameStatus == GameStatus.WAITING)
                            "Waiting for opponent..."
                        else
                            "Opponent (${opponent?.symbol ?: "O"})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = opponent?.name ?: if (gameRoom?.gameState?.gameStatus == GameStatus.WAITING)
                            "Share the game code!"
                        else
                            "Connecting...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (opponent == null)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Game board
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                val symbol = gameRoom?.gameState?.board?.getOrNull(index) ?: ""
                                val isClickable = isCurrentPlayerTurn &&
                                        gameRoom?.gameState?.gameStatus == GameStatus.IN_PROGRESS &&
                                        symbol.isEmpty()

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .background(
                                            if (isClickable)
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                            else
                                                MaterialTheme.colorScheme.surface
                                        )
                                        .clickable(
                                            enabled = isClickable,
                                            onClick = {
                                                println("Cell clicked: row=$row, col=$col, index=$index")
                                                viewModel.makeMove(index)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (symbol.isNotEmpty()) {
                                        Text(
                                            text = symbol,
                                            style = MaterialTheme.typography.displayMedium.copy(
                                                color = if (symbol == "X")
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Game controls and status messages
            AnimatedVisibility(
                visible = gameRoom?.gameState?.gameStatus == GameStatus.FINISHED ||
                        gameRoom?.gameState?.gameStatus == GameStatus.DRAW
            ) {
                Button(
                    onClick = { viewModel.requestRematch() },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Play Again")
                }
            }

            // Show game code for waiting state
            AnimatedVisibility(
                visible = gameRoom?.gameState?.gameStatus == GameStatus.WAITING
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Share this code with your opponent:")
                        Text(
                            text = gameId,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Error messages
            AnimatedVisibility(visible = uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
