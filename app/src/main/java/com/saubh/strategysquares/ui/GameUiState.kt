package com.saubh.strategysquares.ui

import com.saubh.strategysquares.model.GameRoom
import com.saubh.strategysquares.model.Player

data class GameUiState(
    val isSignedIn: Boolean = false,
    val currentPlayer: Player? = null,
    val currentGameRoom: GameRoom? = null,
    val leaderboardPlayers: List<Player> = emptyList(),
    val error: String? = null
)
