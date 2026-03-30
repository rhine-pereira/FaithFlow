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
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class MainActivity : ComponentActivity() {
    private var sharedText by mutableStateOf<String?>(null)
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateRequestCode = 123
    private lateinit var remoteConfig: FirebaseRemoteConfig

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            showUpdateSnackbar()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        setupRemoteConfig()
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

    private fun setupRemoteConfig() {
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600 // 1 hour for release
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        val defaultValues = mapOf(
            "latest_version_code" to BuildConfig.VERSION_CODE.toLong(),
            "min_supported_version_code" to BuildConfig.VERSION_CODE.toLong(),
            "force_update" to false
        )
        remoteConfig.setDefaultsAsync(defaultValues)

        remoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                checkForUpdate()
            }
        }
    }

    private fun checkForUpdate() {
        val latestVersion = remoteConfig.getLong("latest_version_code")
        val minSupportedVersion = remoteConfig.getLong("min_supported_version_code")
        val forceUpdate = remoteConfig.getBoolean("force_update")
        val currentVersion = BuildConfig.VERSION_CODE.toLong()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                when {
                    currentVersion < minSupportedVersion || forceUpdate -> {
                        if (info.isImmediateUpdateAllowed) {
                            startUpdateFlow(info, AppUpdateType.IMMEDIATE)
                        }
                    }
                    currentVersion < latestVersion -> {
                        if (info.isFlexibleUpdateAllowed) {
                            appUpdateManager.registerListener(installStateUpdatedListener)
                            startUpdateFlow(info, AppUpdateType.FLEXIBLE)
                        }
                    }
                }
            }
        }
    }

    private fun startUpdateFlow(info: com.google.android.play.core.appupdate.AppUpdateInfo, type: Int) {
        appUpdateManager.startUpdateFlowForResult(
            info,
            this,
            AppUpdateOptions.newBuilder(type).build(),
            updateRequestCode
        )
    }

    private fun showUpdateSnackbar() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            show()
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(info, AppUpdateType.IMMEDIATE)
            } else if (info.installStatus() == InstallStatus.DOWNLOADED) {
                showUpdateSnackbar()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == updateRequestCode) {
            if (resultCode != RESULT_OK) {
                // If update fails or is cancelled, re-evaluate if it's mandatory
                checkForUpdate()
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                sharedText = it
            }
        }
    }
}
