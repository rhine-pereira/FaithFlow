package com.rhinepereira.faithflow.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rhinepereira.faithflow.data.AppDatabase
import com.rhinepereira.faithflow.data.AuthRepository
import com.rhinepereira.faithflow.data.AuthStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object SignedOut : AuthState()
    data class SignedIn(val userId: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authStatusFlow().collect { status ->
                _authState.value = when (status) {
                    is AuthStatus.Authenticated -> AuthState.SignedIn(status.userId)
                    is AuthStatus.Unauthenticated -> AuthState.SignedOut
                    is AuthStatus.Loading -> AuthState.Loading
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun signInWithGoogle(idToken: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signInWithGoogle(idToken)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteAccount(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Clear local database
                withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(context).clearAllTables()
                }
                
                // Erase remote data and Firebase Auth
                authRepository.deleteAccount()
                
                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }
}
