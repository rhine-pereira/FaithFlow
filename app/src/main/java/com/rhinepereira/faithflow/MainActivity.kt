package com.rhinepereira.faithflow

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rhinepereira.faithflow.ui.AuthState
import com.rhinepereira.faithflow.ui.AuthViewModel
import com.rhinepereira.faithflow.ui.LoginScreen
import com.rhinepereira.faithflow.ui.MainContainer
import com.rhinepereira.faithflow.ui.theme.FaithFlowTheme

class MainActivity : ComponentActivity() {
    private var sharedText by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        
        enableEdgeToEdge()
        setContent {
            FaithFlowTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.authState.collectAsState()

                when (authState) {
                    is AuthState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is AuthState.SignedOut -> {
                        LoginScreen(authViewModel)
                    }
                    is AuthState.SignedIn -> {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val prefs = remember { context.getSharedPreferences("faithflow_prefs", android.content.Context.MODE_PRIVATE) }
                        
                        // If onboarding is not complete, we start at ADD_THEME_FAB. Otherwise DONE.
                        val isComplete = prefs.getBoolean("onboarding_complete", false)
                        val tutorialState = remember { 
                            com.rhinepereira.faithflow.ui.TutorialState(if (isComplete) com.rhinepereira.faithflow.ui.TutorialStep.DONE else com.rhinepereira.faithflow.ui.TutorialStep.ADD_THEME_FAB) 
                        }

                        androidx.compose.runtime.CompositionLocalProvider(
                            com.rhinepereira.faithflow.ui.LocalTutorialState provides tutorialState
                        ) {
                            androidx.compose.runtime.LaunchedEffect(tutorialState.currentStep) {
                                if (tutorialState.currentStep == com.rhinepereira.faithflow.ui.TutorialStep.DONE) {
                                    prefs.edit().putBoolean("onboarding_complete", true).apply()
                                }
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                MainContainer(
                                    sharedText = sharedText,
                                    onSharedTextConsumed = { sharedText = null },
                                    onSignOut = { authViewModel.signOut() },
                                    onDeleteAccount = {
                                        authViewModel.deleteAccount(this@MainActivity) { success ->
                                            if (success) {
                                                // auth status will automatically switch to SignedOut via listener
                                            }
                                        }
                                    }
                                )

                                if (tutorialState.isActive && tutorialState.currentStep != com.rhinepereira.faithflow.ui.TutorialStep.DONE) {
                                    com.rhinepereira.faithflow.ui.TutorialOverlay(
                                        state = tutorialState,
                                        onFinished = {
                                            // Handled by LaunchedEffect
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                sharedText = it
            }
        }
    }
}
