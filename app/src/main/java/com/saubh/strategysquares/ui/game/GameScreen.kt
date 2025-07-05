package com.saubh.strategysquares.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saubh.strategysquares.model.GameState
import com.saubh.strategysquares.model.GameStatus
import com.saubh.strategysquares.model.Player
import com.saubh.strategysquares.ui.components.SymbolPickerDialog
import com.saubh.strategysquares.ui.components.VictoryParticles
import com.saubh.strategysquares.util.SoundEffects

@Composable
fun GameScreen(
    gameState: GameState,
    onCellClick: (Int) -> Unit,
    onLeaveGame: () -> Unit,
    onRematchRequest: () -> Unit,
    onSymbolChange: ((String, Long) -> Unit)? = null,
    currentPlayerId: String? = null,
    soundEffects: SoundEffects? = null,
    hapticFeedback: HapticFeedback? = null,
    modifier: Modifier = Modifier
) {
    val currentPlayer = if (gameState.player1.uid == currentPlayerId) gameState.player1 else gameState.player2

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game Status
        Text(
            text = when {
                gameState.gameStatus == GameStatus.WAITING -> "Waiting for opponent..."
                gameState.gameStatus == GameStatus.IN_PROGRESS && gameState.currentPlayer == currentPlayerId ->
                    "Your turn"
                gameState.gameStatus == GameStatus.IN_PROGRESS ->
                    "Opponent's turn"
                gameState.gameStatus == GameStatus.FINISHED && gameState.winner == currentPlayerId ->
                    "You won!"
                gameState.gameStatus == GameStatus.FINISHED ->
                    "Opponent won!"
                gameState.gameStatus == GameStatus.DRAW ->
                    "Game Draw!"
                else -> ""
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Game Board
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
        ) {
            items(9) { index ->
                GameCell(
                    value = gameState.board[index],
                    color = when (gameState.board[index]) {
                        gameState.player1.symbol -> gameState.player1.symbolColor
                        gameState.player2?.symbol -> gameState.player2.symbolColor
                        else -> 0xFF000000
                    },
                    enabled = gameState.gameStatus == GameStatus.IN_PROGRESS &&
                            gameState.currentPlayer == currentPlayerId &&
                            gameState.board[index].isEmpty(),
                    onClick = { onCellClick(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Game Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onLeaveGame,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Text("Leave Game")
            }

            if (gameState.gameStatus == GameStatus.FINISHED || gameState.gameStatus == GameStatus.DRAW) {
                Button(
                    onClick = onRematchRequest,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                ) {
                    Text("Rematch")
                }
            }
        }

        // Display game code for waiting games
        if (gameState.gameStatus == GameStatus.WAITING) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Share this code with your opponent:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        gameState.gameId,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCell(
    value: String,
    color: Long,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f),
        color = MaterialTheme.colorScheme.primaryContainer.copy(
            alpha = if (enabled) 1f else 0.6f
        ),
        shape = MaterialTheme.shapes.medium,
        onClick = { if (enabled) onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = Color(color),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
