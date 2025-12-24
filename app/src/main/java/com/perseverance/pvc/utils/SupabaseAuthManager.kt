package com.perseverance.pvc.utils

import android.content.Context
import android.util.Log
import com.perseverance.pvc.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken

class SupabaseAuthManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SupabaseAuthManager"
    }
    
    private val supabase = SupabaseClient.client
    
    /**
     * Sign in to Supabase using Google ID token
     */
    suspend fun signInWithGoogle(idToken: String): Result<SupabaseUser> {
        return try {
            // Sign in to Supabase with Google ID token
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }
            
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                Log.d(TAG, "Supabase sign-in successful: ${user.id}")
                Result.success(
                    SupabaseUser(
                        id = user.id,
                        email = user.email ?: "",
                        displayName = user.userMetadata?.get("full_name") as? String ?: user.email ?: "User",
                        photoUrl = user.userMetadata?.get("avatar_url") as? String ?: ""
                    )
                )
            } else {
                Log.e(TAG, "Supabase sign-in failed: No user returned")
                Result.failure(Exception("Failed to sign in to Supabase"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase sign-in error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get current Supabase user
     */
    fun getCurrentUser(): SupabaseUser? {
        val user = supabase.auth.currentUserOrNull()
        return user?.let {
            SupabaseUser(
                id = it.id,
                email = it.email ?: "",
                displayName = it.userMetadata?.get("full_name") as? String ?: it.email ?: "User",
                photoUrl = it.userMetadata?.get("avatar_url") as? String ?: ""
            )
        }
    }
    
    /**
     * Sign out from Supabase
     */
    suspend fun signOut() {
        try {
            supabase.auth.signOut()
            Log.d(TAG, "Signed out from Supabase")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out from Supabase", e)
        }
    }
    
    /**
     * Check if user is signed in to Supabase
     */
    fun isSignedIn(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }
}

data class SupabaseUser(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String
)
