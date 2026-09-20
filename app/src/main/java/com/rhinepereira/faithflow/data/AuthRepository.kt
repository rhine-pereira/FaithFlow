package com.rhinepereira.faithflow.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

sealed class AuthStatus {
    object Loading : AuthStatus()
    object Unauthenticated : AuthStatus()
    data class Authenticated(val userId: String, val user: FirebaseUser) : AuthStatus()
}

/** Single auth listener for the whole app — avoids duplicate Firebase callbacks and sync storms. */
object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val authStatus: StateFlow<AuthStatus> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(AuthStatus.Authenticated(user.uid, user))
            } else {
                trySend(AuthStatus.Unauthenticated)
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }.onStart {
        emit(AuthStatus.Loading)
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AuthStatus.Loading
    )

    fun authStatusFlow(): Flow<AuthStatus> = authStatus

    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun signOut() {
        CloudSyncGate.invalidate()
        auth.signOut()
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        CloudSyncGate.invalidate()
        val user = auth.currentUser ?: throw Exception("No authenticated user")
        val userId = user.uid

        try {
            SupabaseConfig.client.postgrest["notes"].delete { filter { eq("user_id", userId) } }
            SupabaseConfig.client.postgrest["verses"].delete { filter { eq("user_id", userId) } }
            SupabaseConfig.client.postgrest["personal_notes"].delete { filter { eq("user_id", userId) } }
            SupabaseConfig.client.postgrest["personal_note_categories"].delete { filter { eq("user_id", userId) } }
            SupabaseConfig.client.postgrest["daily_records"].delete { filter { eq("user_id", userId) } }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        user.delete().await()
    }
}
