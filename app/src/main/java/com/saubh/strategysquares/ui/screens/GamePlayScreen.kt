package com.saubh.strategysquares.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import com.saubh.strategysquares.R
import com.saubh.strategysquares.model.GameState
import com.saubh.strategysquares.model.GameStatus
import com.saubh.strategysquares.model.Player
import com.saubh.strategysquares.ui.MainViewModel
import com.saubh.strategysquares.ui.theme.*
import kotlinx.coroutines.delay

// Custom colors matching the design
private val DarkText = Color(0xFF2C2C2C)
private val LightGray = Color(0xFFF5F5F5)

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main game content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.leaveGame()
                        onLeaveGame()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DarkText
                    )
                }

                Text(
                    text = "TIC TAC TOE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkText,
                        fontSize = 20.sp
                    )
                )

                // Share button for game code
                IconButton(
                    onClick = {
                        // TODO: Implement share functionality
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = DarkText
                    )
                }
            }

            // Game Status Banner
            Surface(
                color = when {
                    gameRoom?.gameState?.gameStatus == GameStatus.FINISHED && gameRoom.gameState.winner == currentPlayer?.uid -> MaterialTheme.colorScheme.tertiaryContainer
                    gameRoom?.gameState?.gameStatus == GameStatus.FINISHED -> MaterialTheme.colorScheme.errorContainer
                    gameRoom?.gameState?.gameStatus == GameStatus.DRAW -> MaterialTheme.colorScheme.secondaryContainer
                    isCurrentPlayerTurn -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.secondary
                },
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        gameRoom?.gameState?.gameStatus == GameStatus.WAITING ->
                            "Play With Private Room"
                        gameRoom?.gameState?.gameStatus == GameStatus.IN_PROGRESS && isCurrentPlayerTurn ->
                            "Your Turn"
                        gameRoom?.gameState?.gameStatus == GameStatus.IN_PROGRESS ->
                            "${opponent?.name}'s Turn"
                        gameRoom?.gameState?.gameStatus == GameStatus.FINISHED &&
                                gameRoom.gameState.winner == currentPlayer?.uid ->
                            "You Won! 🎉"
                        gameRoom?.gameState?.gameStatus == GameStatus.FINISHED ->
                            "You Lost!"
                        gameRoom?.gameState?.gameStatus == GameStatus.DRAW ->
                            "It's a Draw!"
                        else -> "Loading..."
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = when {
                            gameRoom?.gameState?.gameStatus == GameStatus.FINISHED && gameRoom.gameState.winner == currentPlayer?.uid -> MaterialTheme.colorScheme.onTertiaryContainer
                            gameRoom?.gameState?.gameStatus == GameStatus.FINISHED -> MaterialTheme.colorScheme.onErrorContainer
                            gameRoom?.gameState?.gameStatus == GameStatus.DRAW -> MaterialTheme.colorScheme.onSecondaryContainer
                            isCurrentPlayerTurn -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> Color.White
                        }
                    ),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Players Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current Player
                PlayerCard(
                    player = currentPlayer,
                    isCurrentPlayer = true,
                    isWaiting = gameRoom?.gameState?.gameStatus == GameStatus.WAITING,
                    isActive = isCurrentPlayerTurn
                )

                // VS Circle
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                // Opponent
                PlayerCard(
                    player = opponent,
                    isCurrentPlayer = false,
                    isWaiting = gameRoom?.gameState?.gameStatus == GameStatus.WAITING,
                    isActive = !isCurrentPlayerTurn && gameRoom?.gameState?.gameStatus == GameStatus.IN_PROGRESS
                )
            }

            // Game Board
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                val symbol = gameRoom?.gameState?.board?.getOrNull(index) ?: ""
                                val isClickable = isCurrentPlayerTurn &&
                                        gameRoom?.gameState?.gameStatus == GameStatus.IN_PROGRESS &&
                                        symbol.isEmpty()

                                GameCell(
                                    symbol = symbol,
                                    isClickable = isClickable,
                                    onClick = { viewModel.makeMove(index) },
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            when (gameRoom?.gameState?.gameStatus) {
                GameStatus.WAITING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Game Room Code",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = DarkText.copy(alpha = 0.7f)
                                    )
                                )
                                Text(
                                    text = gameId,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = {
                                // TODO: Implement share room code
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Share Room Code",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                GameStatus.FINISHED, GameStatus.DRAW -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.requestRematch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Play Again",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.leaveGame()
                                onLeaveGame()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Home",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                else -> {}
            }

            // Error messages
            AnimatedVisibility(visible = uiState.error != null) {
                Surface(
                    color = Color.Red.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
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

@Composable
private fun PlayerCard(
    player: Player?,
    isCurrentPlayer: Boolean,
    isWaiting: Boolean,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(IntrinsicSize.Min) // Ensure the column takes minimum width
    ) {
        // Player Avatar
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.White,
                    CircleShape
                )
                .border(
                    width = if (isActive) 3.dp else 1.dp,
                    color = if (isActive) Color.White else Color.Gray.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Player",
                tint = if (isActive) Color.White else DarkText,
                modifier = Modifier.size(30.dp)
            )
        }

        // Player Symbol Badge
        Surface(
            color = if (isCurrentPlayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(
                text = if (isWaiting && !isCurrentPlayer) "?" else (player?.symbol ?: "?"),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        // Player Name
        Text(
            text = when {
                isCurrentPlayer -> "You"
                isWaiting -> "Waiting..."
                else -> player?.name ?: "Opponent"
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = DarkText
            ),
            maxLines = 1,

        )

        // Player Score
        if (!isWaiting && player != null) {
            Text(
                text = "${player.score} pts",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = DarkText.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
private fun GameCell(
    symbol: String,
    isClickable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (symbol.isEmpty()) Color.White else Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .clickable(enabled = isClickable, onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (symbol.isNotEmpty()) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (symbol == "X") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                        fontSize = 32.sp
                    )
                )
            }
        }
    }
}