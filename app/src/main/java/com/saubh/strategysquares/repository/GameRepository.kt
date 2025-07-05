package com.saubh.strategysquares.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.saubh.strategysquares.model.ChatMessage
import com.saubh.strategysquares.model.GameRoom
import com.saubh.strategysquares.model.GameState
import com.saubh.strategysquares.model.GameStatus
import com.saubh.strategysquares.model.Player
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GameRepository @Inject constructor(
    @Named("firebaseDatabase") private val database: FirebaseDatabase
) {
    private val gamesRef = database.getReference("games")
    private val playersRef = database.getReference("players")

    suspend fun createGame(player: Player): String {
        val gameId = generateGameId()
         val initialBoard = List(9) { "" }
        val gameState = GameState(
            gameId = gameId,
            currentPlayer = player.uid,
            player1 = player,
            board = initialBoard,
            gameStatus = GameStatus.WAITING
        )
        val gameRoom = GameRoom(roomId = gameId, gameState = gameState)
        gamesRef.child(gameId).setValue(gameRoom).await()
        return gameId
    }

    suspend fun joinGame(gameId: String, player: Player) {
        try {
            val gameRef = gamesRef.child(gameId)
            val gameSnapshot = gameRef.get().await()
            val gameRoom = gameSnapshot.getValue(GameRoom::class.java) ?: throw IllegalStateException("Game not found")

            println("Joining game: $gameId, player: ${player.name}")
            println("Current game state: ${gameRoom.gameState}")

            if (gameRoom.gameState.player2 != null) {
                throw IllegalStateException("Game is full")
            }

            // If the player is trying to join their own game, throw an error
            if (gameRoom.gameState.player1.uid == player.uid) {
                throw IllegalStateException("Cannot join your own game")
            }

            // Update player2 with O symbol and different color
            val player2 = player.copy(
                symbol = "⭕",
                symbolColor = 0xFFF44336 // Red color for O
            )

            val updatedGameState = gameRoom.gameState.copy(
                player2 = player2,
                gameStatus = GameStatus.IN_PROGRESS,
                currentPlayer = gameRoom.gameState.player1.uid // First player starts
            )

            println("Updated game state: $updatedGameState")
            gameRef.child("gameState").setValue(updatedGameState).await()
            println("Successfully joined game")
        } catch (e: Exception) {
            println("Error joining game: ${e.message}")
            throw e
        }
    }

    fun observeGame(gameId: String): Flow<GameRoom> = callbackFlow {
        println("Starting to observe game: $gameId")

        val gameRef = gamesRef.child(gameId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val gameRoom = snapshot.getValue(GameRoom::class.java)
                    if (gameRoom != null) {
                        println("Game update received: ${gameRoom.gameState.gameStatus}")
                        println("Players: ${gameRoom.gameState.player1.name}, ${gameRoom.gameState.player2?.name}")
                        trySend(gameRoom)
                    } else {
                        println("Game room is null for id: $gameId")
                    }
                } catch (e: Exception) {
                    println("Error processing game update: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Game observation cancelled: ${error.message}")
                close(error.toException())
            }
        }

        gameRef.addValueEventListener(listener)

        // Remove the listener when the flow is cancelled
        awaitClose {
            println("Removing game observer for: $gameId")
            gameRef.removeEventListener(listener)
        }
    }

    suspend fun makeMove(gameId: String, position: Int, playerId: String) {
        try {
            println("Repository: Making move - gameId=$gameId, position=$position, playerId=$playerId")

            val gameRef = gamesRef.child(gameId)
            val gameSnapshot = gameRef.get().await()
            val gameRoom = gameSnapshot.getValue(GameRoom::class.java)

            if (gameRoom == null) {
                println("Repository: Error - Game room not found")
                return
            }

            val gameState = gameRoom.gameState
            println("Repository: Current game state - currentPlayer=${gameState.currentPlayer}, status=${gameState.gameStatus}")
            println("Repository: Current board state - ${gameState.board}")

            // Validate move
            if (gameState.currentPlayer != playerId) {
                println("Repository: Error - Not player's turn")
                return
            }

            if (position < 0 || position >= gameState.board.size) {
                println("Repository: Error - Invalid position")
                return
            }

            if (gameState.board[position].isNotEmpty()) {
                println("Repository: Error - Position already occupied")
                return
            }

            if (gameState.gameStatus != GameStatus.IN_PROGRESS) {
                println("Repository: Error - Game not in progress")
                return
            }

            // Make the move
            val symbol = if (playerId == gameState.player1.uid) "❌" else "⭕"
            val newBoard = gameState.board.toMutableList().apply {
                set(position, symbol)
            }

            // Check for win or draw
            val hasWon = checkWinner(newBoard, symbol)
            val isDraw = if (!hasWon) newBoard.none { it.isEmpty() } else false

            // Switch to next player
            val nextPlayer = if (playerId == gameState.player1.uid)
                gameState.player2?.uid
            else
                gameState.player1.uid

            // Create updated game state BEFORE score updates
            var updatedGameState = gameState.copy(
                board = newBoard,
                currentPlayer = if (hasWon || isDraw) "" else (nextPlayer ?: gameState.currentPlayer),
                winner = if (hasWon) playerId else "",
                gameStatus = when {
                    hasWon -> GameStatus.FINISHED
                    isDraw -> GameStatus.DRAW
                    else -> GameStatus.IN_PROGRESS
                }
            )

            // ADD THIS BLOCK: Update scores only if game is won and scores haven't been updated yet
            if (hasWon && !gameState.scoresUpdated) {
                // Update winner score
                updatePlayerScore(playerId, 1)

                // Update loser score
                val opponentId = if (playerId == gameState.player1.uid) {
                    gameState.player2?.uid
                } else {
                    gameState.player1.uid
                }
                opponentId?.let { updatePlayerScore(it, -1) }

                // Set flag to prevent duplicate updates
                updatedGameState = updatedGameState.copy(scoresUpdated = true)
            }

            // Write the final updated state to database
            gameRef.child("gameState").setValue(updatedGameState).await()


        } catch (e: Exception) {
            println("Repository: Error making move - ${e.message}")
            throw e
        }
    }

    suspend fun rematchGame(gameId: String) {
        val gameRef = gamesRef.child(gameId)
        val gameSnapshot = gameRef.get().await()
        val gameRoom = gameSnapshot.getValue(GameRoom::class.java) ?: return

        val updatedGameState = gameRoom.gameState.copy(
            board = List(9) { "" },
            currentPlayer = gameRoom.gameState.player1.uid,
            winner = "",
            gameStatus = GameStatus.IN_PROGRESS
        )

        gameRef.child("gameState").setValue(updatedGameState).await()
    }

    suspend fun leaveGame(gameId: String, playerId: String) {
        val gameRef = gamesRef.child(gameId)
        val gameSnapshot = gameRef.get().await()
        val gameRoom = gameSnapshot.getValue(GameRoom::class.java) ?: return

        when (playerId) {
            gameRoom.gameState.player1.uid -> {
                // Owner leaves - delete entire room
                gameRef.removeValue().await()
            }
            gameRoom.gameState.player2?.uid -> {
                // Player 2 leaves - reset to waiting state
                val updatedGameState = gameRoom.gameState.copy(
                    player2 = null,
                    gameStatus = GameStatus.WAITING,
                    board = List(9) { "" },
                    winner = "",
                    currentPlayer = gameRoom.gameState.player1.uid
                )
                gameRef.child("gameState").setValue(updatedGameState).await()
            }
        }
    }

    private fun checkWinner(board: List<String>, symbol: String): Boolean {
        val winPatterns = listOf(
            // Rows
            listOf(0, 1, 2),
            listOf(3, 4, 5),
            listOf(6, 7, 8),
            // Columns
            listOf(0, 3, 6),
            listOf(1, 4, 7),
            listOf(2, 5, 8),
            // Diagonals
            listOf(0, 4, 8),
            listOf(2, 4, 6)
        )

        return winPatterns.any { pattern ->
            pattern.all { position -> board[position] == symbol }
        }
    }

    private suspend fun updatePlayerScore(uid: String, points: Int) {
        playersRef.child(uid).child("score").get().await().let { snapshot ->
            val currentScore = snapshot.getValue(Int::class.java) ?: 0
            playersRef.child(uid).child("score").setValue(currentScore + points)
        }
    }

    private fun generateGameId(): String {
        return List(6) { ('A'..'Z').random() }.joinToString("")
    }

    suspend fun createOrUpdatePlayer(player: Player) {
        playersRef.child(player.uid).setValue(player).await()
    }

    suspend fun deleteGame(gameId: String) {
        gamesRef.child(gameId).removeValue().await()
    }
}
