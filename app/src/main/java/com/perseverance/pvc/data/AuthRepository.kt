package com.perseverance.pvc.data

import com.perseverance.pvc.di.SupabaseModule
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

import android.util.Log

class AuthRepository {
    private val auth = SupabaseModule.client.auth

    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    val currentUser get() = auth.currentUserOrNull()

    suspend fun signUpWithEmail(email: String, password: String, displayName: String?) {
        Log.d("AuthDebug", "Repo: Attempting sign up for $email")
        try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                // Optional: Add metadata
                if (displayName != null) {
                    data = kotlinx.serialization.json.JsonObject(
                        mapOf("full_name" to kotlinx.serialization.json.JsonPrimitive(displayName))
                    )
                }
            }
            Log.d("AuthDebug", "Repo: Sign up successful request for $email")
        } catch (e: Exception) {
            Log.e("AuthDebug", "Repo: Sign up failed for $email. Error: ${e.message}", e)
            throw e
        }
    }

    suspend fun signInWithEmail(email: String, password: String) {
        Log.d("AuthDebug", "Repo: Attempting sign in for $email")
        try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Log.d("AuthDebug", "Repo: Sign in successful for $email")
        } catch (e: Exception) {
            Log.e("AuthDebug", "Repo: Sign in failed for $email. Error: ${e.message}", e)
            throw e
        }
    }

    suspend fun signOut() {
        Log.d("AuthDebug", "Repo: Signing out")
        try {
            auth.signOut()
            Log.d("AuthDebug", "Repo: Sign out successful")
        } catch (e: Exception) {
            Log.e("AuthDebug", "Repo: Sign out failed", e)
        }
    }
}
