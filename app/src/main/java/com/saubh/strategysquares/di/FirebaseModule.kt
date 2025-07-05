package com.saubh.strategysquares.di

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
internal object FirebaseModule {

    @Provides
    @Singleton
    @Named("firebaseDatabase")
    internal fun provideFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database("https://strategy-squares-default-rtdb.asia-southeast1.firebasedatabase.app")
    }

    @Provides
    @Singleton
    @Named("firebaseAuth")
    internal fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Suppress("DEPRECATION")
    @Provides
    @Singleton
    @Named("googleSignInClient")
    internal fun provideGoogleSignInClient(
        @ApplicationContext context: Context
    ): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("381492099418-ao16tn2q80mgf84h7dcjhbligrhlasho.apps.googleusercontent.com") // Using web client ID
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
}
