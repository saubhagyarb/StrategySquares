package com.saubh.strategysquares.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import com.saubh.strategysquares.R
import com.saubh.strategysquares.ui.MainViewModel

@Composable
fun SignInScreen(
    onSignInClick: () -> Unit,
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var hasNavigated by remember { mutableStateOf(false) }

    // Navigate when signed in
    LaunchedEffect(uiState.isSignedIn, uiState.currentPlayer) {
        if (uiState.isSignedIn && uiState.currentPlayer != null && !hasNavigated) {
            hasNavigated = true
            Log.d("SignInScreen", "Signed In as ${uiState.currentPlayer?.name}")
            onSignInSuccess()
        }
    }

    // Lottie animation setup
//    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.xo_animation))
//    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.SportsEsports,
            contentDescription = "App Icon",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Strategy Squares",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Multiplayer XO Game",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Lottie Animation
//        LottieAnimation(
//            composition = composition,
//            progress = { progress },
//            modifier = Modifier
//                .height(220.dp)
//                .fillMaxWidth()
//        )

        Spacer(modifier = Modifier.height(32.dp))

        ElevatedButton(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(com.google.android.gms.base.R.drawable.googleg_disabled_color_18), // Replace with your asset
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
