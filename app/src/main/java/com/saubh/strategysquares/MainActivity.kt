package com.saubh.strategysquares

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.saubh.strategysquares.repository.AuthRepository
import com.saubh.strategysquares.ui.MainViewModel
import com.saubh.strategysquares.ui.navigation.NavGraph
import com.saubh.strategysquares.ui.theme.StrategySquaresTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.util.Log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val viewModel: MainViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                if (result.data == null) {
                    Log.e("MainActivity", "Sign-in intent returned null data!")
                    viewModel.handleSignInError("Sign-in failed: No data returned.")
                    return@registerForActivityResult
                }
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    Log.d("MainActivity", "Sign in intent completed, attempting to get account...")
                    val account = task.getResult(ApiException::class.java)
                    Log.d("MainActivity", "Got account successfully: ${account.email}")
                    account?.let {
                        viewModel.handleGoogleSignIn(it)
                    }
                } catch (e: ApiException) {
                    Log.e("MainActivity", "Google Sign-In failed with status code: ${e.statusCode}", e)
                    when (e.statusCode) {
                        GoogleSignInStatusCodes.SIGN_IN_FAILED ->
                            viewModel.handleSignInError("Sign in failed. Please try again.")
                        GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
                            viewModel.handleSignInError("Sign in cancelled")
                        GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS ->
                            viewModel.handleSignInError("Sign in already in progress")
                        GoogleSignInStatusCodes.SIGN_IN_REQUIRED ->
                            viewModel.handleSignInError("Sign in required")
                        else -> viewModel.handleSignInError("Error ${e.statusCode}: ${e.message}")
                    }
                }
            }
            RESULT_CANCELED -> {
                Log.d("MainActivity", "Sign in was cancelled by user")
                viewModel.handleSignInError("Sign in cancelled by user")
            }
            else -> {0
                Log.e("MainActivity", "Sign in failed with unexpected result code: ${result.resultCode}")
                viewModel.handleSignInError("Sign in failed unexpectedly. Please try again.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
        Log.d("MainActivity", "Last signed in account: ${lastSignedInAccount?.email}")

        if (lastSignedInAccount != null) {
            Log.d("MainActivity", "Found existing sign in, attempting to sign in with account")
            viewModel.handleGoogleSignIn(lastSignedInAccount)
        }

        setContent {
            StrategySquaresTheme {
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(uiState.error) {
                    uiState.error?.let { error ->
                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        modifier = Modifier.padding(innerPadding),
                        onSignInClick = {
                            Log.d("MainActivity", "Starting sign in flow...")
                            try {
                                val intent = authRepository.getSignInIntent()
                                signInLauncher.launch(intent)
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Failed to start sign in", e)
                                viewModel.handleSignInError("Failed to start sign in: ${e.message}")
                            }
                        },
                        isSignedIn = uiState.isSignedIn
                    )
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel.leaveGame()
    }
}