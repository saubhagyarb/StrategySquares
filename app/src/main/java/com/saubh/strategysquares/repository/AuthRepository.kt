package com.saubh.strategysquares.repository

import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @Named("firebaseAuth") private val auth: FirebaseAuth,
    @Named("googleSignInClient") private val googleSignInClient: GoogleSignInClient
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithGoogle(account: GoogleSignInAccount): FirebaseUser {
        try {
            Log.d("AuthRepository", "Starting Google sign in process with account: ${account.email}")
            val idToken = account.idToken
            if (idToken == null) {
                Log.e("AuthRepository", "ID Token is null for account: ${account.email}")
                throw IllegalStateException("ID Token is missing")
            }

            Log.d("AuthRepository", "Got ID token length: ${idToken.length}, creating credentials")
            val credentials = GoogleAuthProvider.getCredential(idToken, null)
            Log.d("AuthRepository", "Created credentials, attempting Firebase sign in")

            val result = auth.signInWithCredential(credentials).await()
            Log.d("AuthRepository", "Firebase sign in successful for user: ${result.user?.email}")

            return result.user ?: throw IllegalStateException("Sign in failed: No user returned")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in failed with exception type: ${e.javaClass.simpleName}", e)
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    Log.e("AuthRepository", "Invalid credentials error code: ${e.errorCode}")
                    throw Exception("Invalid credentials. Please try again.")
                }
                is FirebaseAuthInvalidUserException -> {
                    Log.e("AuthRepository", "Invalid user error code: ${e.errorCode}")
                    throw Exception("Account doesn't exist or has been disabled.")
                }
                else -> {
                    Log.e("AuthRepository", "Unexpected error during sign in", e)
                    throw Exception("Sign in failed: ${e.message}")
                }
            }
        }
    }

    fun getSignInIntent(): Intent {
        try {
            Log.d("AuthRepository", "Getting Google sign in intent with client: ${googleSignInClient.javaClass.simpleName}")
            val intent = googleSignInClient.signInIntent
            Log.d("AuthRepository", "Successfully created sign in intent")
            return intent
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to get sign in intent", e)
            throw Exception("Failed to start sign in: ${e.message}")
        }
    }

    suspend fun signOut() {
        try {
            Log.d("AuthRepository", "Starting sign out process for user: ${currentUser?.email}")
            auth.signOut()
            googleSignInClient.signOut().await()
            Log.d("AuthRepository", "Sign out successful")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign out failed", e)
            throw Exception("Failed to sign out: ${e.message}")
        }
    }
}
