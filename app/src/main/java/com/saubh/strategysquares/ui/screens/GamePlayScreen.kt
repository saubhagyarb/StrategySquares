package com.saubh.strategysquares.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.delay

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
            delay(1500)
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
                    .fillMaxWidth(),
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
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "TIC TAC TOE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp
                    )
                )

                IconButton(
                    onClick = {
                        // TODO: Implement share functionality
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Game Status Banner
            Text(
                text = when {
                    gameRoom?.gameState?.gameStatus == GameStatus.WAITING ->
                        "Play With Private Room"
                    gameRoom?.gameState?.gameStatus == GameStatus.IN_PROGRESS && isCurrentPlayerTurn ->
                        "Your Turn"
                    gameRoom?.gameState?.gameStatus == GameStatus.IN_PROGRESS ->
                        "${opponent?.name ?: "Opponent"}'s Turn"
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
                        gameRoom?.gameState?.gameStatus == GameStatus.FINISHED && gameRoom.gameState.winner == currentPlayer?.uid ->
                            MaterialTheme.colorScheme.tertiary
                        gameRoom?.gameState?.gameStatus == GameStatus.FINISHED ->
                            MaterialTheme.colorScheme.error
                        gameRoom?.gameState?.gameStatus == GameStatus.DRAW ->
                            MaterialTheme.colorScheme.secondary
                        isCurrentPlayerTurn ->
                            MaterialTheme.colorScheme.primary
                        else ->
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    }
                ),
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            // Players Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Player vs Player header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PlayerCard(
                        player = currentPlayer,
                        isCurrentPlayer = true,
                        isWaiting = gameRoom?.gameState?.gameStatus == GameStatus.WAITING,
                        isActive = isCurrentPlayerTurn
                    )

                    PlayerCard(
                        player = opponent,
                        isCurrentPlayer = false,
                        isWaiting = gameRoom?.gameState?.gameStatus == GameStatus.WAITING,
                        isActive = !isCurrentPlayerTurn && gameRoom?.gameState?.gameStatus == GameStatus.IN_PROGRESS
                    )
                }

                // Win Summit Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WinSummitCard(
                        title = "Win Summit",
                        value = currentPlayer?.score ?: 0,
                        isActive = isCurrentPlayerTurn
                    )

                    WinSummitCard(
                        title = "Win Summit",
                        value = opponent?.score ?: 0,
                        isActive = !isCurrentPlayerTurn
                    )
                }

                // Draw Counter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Draws: ${/*gameRoom?.gameState?.drawCount ?:*/ 0} times",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Game Board
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
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
                            color = MaterialTheme.colorScheme.surface,
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
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                                color = MaterialTheme.colorScheme.onPrimary,
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
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Play Again",
                                color = MaterialTheme.colorScheme.onPrimary,
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
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
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
private fun WinSummitCard(
    title: String,
    value: Int,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun PlayerCard(
    player: Player?,
    isCurrentPlayer: Boolean,
    isWaiting: Boolean,
    isActive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .width(IntrinsicSize.Min)
    ) {
        if (isCurrentPlayer) {
            PlayerInfoContent(player, isActive, isWaiting)
        } else {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "VS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            PlayerInfoContent(player, isActive, isWaiting)
        }
    }
}

@Composable
private fun PlayerInfoContent(
    player: Player?,
    isActive: Boolean,
    isWaiting: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player Name
        Text(
            text = when {
                player == null && isWaiting -> "Waiting..."
                player == null -> "Opponent"
                else -> player.name
            },
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Player Symbol
        Surface(
            color = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = player?.symbol ?: "?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
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
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                        color = if (symbol == "X") MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary,
                        fontSize = 32.sp
                    )
                )
            }
        }
    }
}