package com.rhinepereira.faithflow.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.rhinepereira.faithflow.BuildConfig
import com.rhinepereira.faithflow.R
import kotlinx.coroutines.launch
import android.util.Log

private const val TAG = "FaithFlowAuth"

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "FaithFlow",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Track your Bible reading journey",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                val result = handleGoogleSignIn(context)
                                if (result != null) {
                                    Log.d(TAG, "Got ID Token, signing into Firebase...")
                                    viewModel.signInWithGoogle(result) { success ->
                                        isLoading = false
                                        if (!success) {
                                            errorMessage = "Firebase Sign-In failed. Check logs."
                                            Log.e(TAG, "Firebase signInWithGoogle returned false")
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = "Google ID Token was null. Check SHA-1 and Client ID."
                                    Log.e(TAG, "handleGoogleSignIn returned null")
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Error: ${e.localizedMessage ?: "Unknown error"}"
                                Log.e(TAG, "Exception during sign-in flow", e)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private suspend fun handleGoogleSignIn(context: Context): String? {
    val credentialManager = CredentialManager.create(context)
    
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        Log.d(TAG, "Starting getCredential...")
        val result = credentialManager.getCredential(context, request)
        val credential = result.credential
        Log.d(TAG, "Credential received: ${credential.type}")
        val googleIdToken = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
        googleIdToken.idToken
    } catch (e: Exception) {
        Log.e(TAG, "getCredential failed", e)
        throw e // Rethrow to show message in UI
    }
}
