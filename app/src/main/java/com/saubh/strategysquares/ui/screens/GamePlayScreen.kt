package com.saubh.strategysquares.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import com.saubh.strategysquares.R
import com.saubh.strategysquares.model.GameState
import com.saubh.strategysquares.model.GameStatus
import com.saubh.strategysquares.model.Player
import com.saubh.strategysquares.ui.MainViewModel
import kotlinx.coroutines.delay

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

    // Animation state
    var previousBoard by remember { mutableStateOf<List<String>>(emptyList()) }
    var showAnimation by remember { mutableStateOf(false) }

    // Lottie animation setup
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fire_anim))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1,
        isPlaying = showAnimation,
        speed = 1f,
        restartOnPlay = true
    )

    // Detect board changes and trigger animation
    LaunchedEffect(gameRoom?.gameState?.board) {
        val currentBoard = gameRoom?.gameState?.board ?: emptyList()
        if (currentBoard != previousBoard && currentBoard.any { it.isNotEmpty() }) {
            showAnimation = true
            delay(1500) // Animation duration
            showAnimation = false
        }
        previousBoard = currentBoard
    }

    // Opponent logic
    val opponent = gameRoom?.gameState?.let { gameState ->
        when (currentPlayer?.uid) {
            gameState.player1.uid -> gameState.player2
            gameState.player2?.uid -> gameState.player1
            else -> null
        }
    }

    val isCurrentPlayerTurn = gameRoom?.gameState?.currentPlayer == currentPlayer?.uid

    LaunchedEffect(gameId) {
        viewModel.startObservingGame(gameId)
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Main game content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Game Status
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

                // Players info
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
                        Text(
                            text = "Score: ${currentPlayer?.score ?: 0}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Opponent
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

                // Game controls
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

                // Game code for waiting state
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

            // Lottie animation overlay
            AnimatedVisibility(
                visible = showAnimation,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}