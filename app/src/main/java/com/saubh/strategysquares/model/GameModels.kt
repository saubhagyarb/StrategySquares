package com.saubh.strategysquares.model

data class GameRoom(
    val roomId: String = "",
    val gameState: GameState = GameState(),
    val messages: List<ChatMessage> = emptyList()
)

data class GameState(
    val gameId: String = "",
    val currentPlayer: String = "",
    val player1: Player = Player(),
    val player2: Player? = null,
    val board: List<String> = List(9) { "" },
    val winner: String = "",
    val gameStatus: GameStatus = GameStatus.WAITING,
    val scoresUpdated: Boolean = false
)

data class Player(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val score: Int = 0,
    val symbol: String = "❌",
    val symbolColor: Long = 0xFF000000
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class GameStatus {
    WAITING,
    IN_PROGRESS,
    FINISHED,
    DRAW
}
