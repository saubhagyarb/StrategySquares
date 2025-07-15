package com.saubh.strategysquares.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.saubh.strategysquares.model.GameRoom
import com.saubh.strategysquares.model.GameState
import com.saubh.strategysquares.model.GameStatus
import com.saubh.strategysquares.model.Player
import com.saubh.strategysquares.repository.AuthRepository
import com.saubh.strategysquares.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    private val _currentDestination = MutableStateFlow<String?>(null)
    val currentDestination: StateFlow<String?> = _currentDestination

    fun updateCurrentDestination(route: String?) {
        _currentDestination.value = route
    }

    init {
        authRepository.currentUser?.let { firebaseUser ->
            viewModelScope.launch {
                try {
                    val player = Player(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                        score = 0,
                        symbol = "X",
                        symbolColor = 0xFF000000
                    )
                    gameRepository.createOrUpdatePlayer(player)
                    _uiState.update { it.copy(
                        currentPlayer = player,
                        isSignedIn = true,
                        error = null
                    )}
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message) }
                }
            }
        }
    }

    fun handleGoogleSignIn(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                val firebaseUser = authRepository.signInWithGoogle(account)
                val player = Player(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    score = 0,
                    symbol = "X",
                    symbolColor = 0xFF000000
                )
                gameRepository.createOrUpdatePlayer(player)
                _uiState.update { it.copy(
                    currentPlayer = player,
                    isSignedIn = true,
                    error = null
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun createGame() {
        viewModelScope.launch {
            try {
                val currentPlayer = _uiState.value.currentPlayer ?: return@launch
                val gameId = gameRepository.createGame(currentPlayer)
                observeGame(gameId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun joinGame(gameId: String) {
        viewModelScope.launch {
            try {
                val currentPlayer = _uiState.value.currentPlayer ?: return@launch
                println("Joining game: $gameId with player: ${currentPlayer.name}")

                // Start observing the game before joining
                observeGame(gameId)

                gameRepository.joinGame(gameId, currentPlayer)
                println("Successfully joined game: $gameId")
            } catch (e: Exception) {
                println("Error joining game: ${e.message}")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun observeGame(gameId: String) {
        viewModelScope.launch {
            try {
                println("Starting to observe game: $gameId")
                gameRepository.observeGame(gameId).collect { gameRoom ->
                    println("Game update received: ${gameRoom.gameState.gameStatus}, players: ${gameRoom.gameState.player1.name}, ${gameRoom.gameState.player2?.name}")
                    _uiState.update { it.copy(
                        currentGameRoom = gameRoom,
                        error = null
                    )}
                }
            } catch (e: Exception) {
                println("Error observing game: ${e.message}")
                _uiState.update { it.copy(error = "Error monitoring game: ${e.message}") }
            }
        }
    }

    fun startObservingGame(gameId: String) {
        viewModelScope.launch {
            try {
                println("Starting to observe game: $gameId")
                gameRepository.observeGame(gameId).collect { gameRoom ->
                    println("Game update received - Status: ${gameRoom.gameState.gameStatus}")
                    println("Player 1: ${gameRoom.gameState.player1.name}")
                    println("Player 2: ${gameRoom.gameState.player2?.name}")
                    println("Current player turn: ${gameRoom.gameState.currentPlayer}")

                    _uiState.update { it.copy(
                        currentGameRoom = gameRoom,
                        error = null
                    )}
                }
            } catch (e: Exception) {
                println("Error observing game: ${e.message}")
                _uiState.update { it.copy(error = "Error monitoring game: ${e.message}") }
            }
        }
    }

    fun makeMove(position: Int) {
        viewModelScope.launch {
            try {
                val gameId = _uiState.value.currentGameRoom?.roomId
                val playerId = _uiState.value.currentPlayer?.uid

                println("Making move: position=$position, gameId=$gameId, playerId=$playerId")

                if (gameId == null || playerId == null) {
                    println("Error: gameId or playerId is null")
                    return@launch
                }

                val currentGameState = _uiState.value.currentGameRoom?.gameState
                if (currentGameState?.currentPlayer != playerId) {
                    println("Error: Not player's turn")
                    return@launch
                }

                if (currentGameState.board.getOrNull(position)?.isNotEmpty() == true) {
                    println("Error: Position already occupied")
                    return@launch
                }

                println("Move validation passed, sending to repository")
                gameRepository.makeMove(gameId, position, playerId)
            } catch (e: Exception) {
                println("Error making move: ${e.message}")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun requestRematch() {
        viewModelScope.launch {
            try {
                val gameId = _uiState.value.currentGameRoom?.roomId ?: return@launch
                gameRepository.rematchGame(gameId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun leaveGame() {
        viewModelScope.launch {
            try {
                val gameId = _uiState.value.currentGameRoom?.roomId
                val playerId = _uiState.value.currentPlayer?.uid

                if (gameId != null && playerId != null) {
                    gameRepository.leaveGame(gameId, playerId)
                }
                clearCurrentGame()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.update {
                    GameUiState()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun handleSignInError(error: String) {
        _uiState.update { it.copy(
            error = error,
            isSignedIn = false
        )}
    }

    fun clearCurrentGame() {
        _uiState.update { it.copy(currentGameRoom = null) }
    }

    fun updateUserEmoji(newEmoji: String) {
        viewModelScope.launch {
            val currentPlayer = _uiState.value.currentPlayer ?: return@launch
            val updatedPlayer = currentPlayer.copy(symbol = newEmoji)
            gameRepository.createOrUpdatePlayer(updatedPlayer)
            _uiState.update { it.copy(currentPlayer = updatedPlayer) }
        }
    }

}
