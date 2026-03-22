package com.rhinepereira.faithflow.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AuthStatus {
    object Loading : AuthStatus()
    object Unauthenticated : AuthStatus()
    data class Authenticated(val userId: String, val user: FirebaseUser) : AuthStatus()
}

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun authStatusFlow(): Flow<AuthStatus> = callbackFlow {
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
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
    }

    suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: throw Exception("No authenticated user")
        val userId = user.uid

        // Clear user data from Supabase
        try {
            SupabaseConfig.client.postgrest["notes"].delete { filter { eq("user_id", userId) } }
            SupabaseConfig.client.postgrest["verses"].delete { filter { eq("user_id", userId) } }
            SupabaseConfig.client.postgrest["personal_notes"].delete { filter { eq("user_id", userId) } }
            SupabaseConfig.client.postgrest["personal_note_categories"].delete { filter { eq("user_id", userId) } }
            SupabaseConfig.client.postgrest["daily_records"].delete { filter { eq("user_id", userId) } }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Delete the Firebase Auth User
        user.delete().await()
    }
}
