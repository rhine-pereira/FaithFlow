package com.rhinepereira.faithflow.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rhinepereira.faithflow.ui.components.ConfirmationDialog

private enum class MainTab(val index: Int, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Notes(0, "Notes", Icons.Default.Bookmark),
    Themes(1, "Themes", Icons.Default.Home),
    Daily(2, "Daily", Icons.Default.HistoryEdu);
}

@Composable
private fun KeepAliveTab(
    selected: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(if (selected) 1f else 0f)
            .graphicsLayer { alpha = if (selected) 1f else 0f }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    sharedText: String? = null,
    onSharedTextConsumed: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onDeleteAccount: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as ComponentActivity

    val verseViewModel: VerseViewModel = viewModel(activity)
    val notesViewModel: NotesViewModel = viewModel(activity)
    val dailyViewModel: DailyViewModel = viewModel(activity)

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(MainTab.Notes.index) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(sharedText) {
        if (sharedText != null) {
            selectedTabIndex = MainTab.Themes.index
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FaithFlow",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sign Out") },
                            onClick = {
                                showMenu = false
                                showSignOutDialog = true
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Privacy Policy") },
                            onClick = {
                                showMenu = false
                                LegalLinks.openPrivacyPolicy(context)
                            },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Terms and Conditions") },
                            onClick = {
                                showMenu = false
                                LegalLinks.openTermsAndConditions(context)
                            },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete My Data (Web)") },
                            onClick = {
                                showMenu = false
                                LegalLinks.openDataDeletion(context)
                            },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete Account", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                MainTab.entries.forEach { tab ->
                    val selected = selectedTabIndex == tab.index
                    NavigationBarItem(
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = null,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        },
                        label = {
                            Text(
                                tab.title,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = selected,
                        onClick = { selectedTabIndex = tab.index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            KeepAliveTab(selected = selectedTabIndex == MainTab.Notes.index) {
                NotesScreen(
                    viewModel = notesViewModel,
                    isVisible = selectedTabIndex == MainTab.Notes.index
                )
            }
            KeepAliveTab(selected = selectedTabIndex == MainTab.Themes.index) {
                VerseScreen(
                    viewModel = verseViewModel,
                    sharedText = sharedText,
                    onSharedTextConsumed = onSharedTextConsumed,
                    isVisible = selectedTabIndex == MainTab.Themes.index
                )
            }
            KeepAliveTab(selected = selectedTabIndex == MainTab.Daily.index) {
                DailyScreen(
                    viewModel = dailyViewModel,
                    isVisible = selectedTabIndex == MainTab.Daily.index
                )
            }
        }
    }

    if (showSignOutDialog) {
        ConfirmationDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out?",
            confirmButtonText = "Sign Out",
            isDestructive = false,
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Account",
            message = "Are you sure you want to permanently delete your account? All your verses, notes, and themes will be erased.",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                onDeleteAccount()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
