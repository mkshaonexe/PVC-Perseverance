package com.perseverance.pvc.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.perseverance.pvc.BuildConfig
import com.perseverance.pvc.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    
    private val supabase = SupabaseClient.client
    
    // Get current user
    fun getCurrentUser(): UserInfo? {
        return supabase.auth.currentUserOrNull()
    }
    
    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }
    
    // Sign up with email and password
    suspend fun signUpWithEmail(email: String, password: String): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                // Using .await() is not needed for suspend functions in newer versions, 
                // but signUpWith behaves differently depending on version. 
                // In 2.x/3.x it suspends.
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Sign up failed"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Sign up error", e)
                Result.failure(e)
            }
        }
    }
    
    // Sign in with email and password
    suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Sign in failed"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Sign in error", e)
                Result.failure(e)
            }
        }
    }
    
    // Get Google Sign-In Intent
    fun getGoogleSignInIntent(): android.content.Intent {
        val clientId = if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotEmpty() && BuildConfig.GOOGLE_WEB_CLIENT_ID != "null") {
            BuildConfig.GOOGLE_WEB_CLIENT_ID
        } else {
            "MISSING_WEB_CLIENT_ID" // Preventing crash, but login will fail
        }
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
        
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }
    
    // Sign in with Google (process the result)
    suspend fun signInWithGoogle(data: android.content.Intent?): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                
                if (idToken != null) {
                    supabase.auth.signInWith(IDToken) {
                        this.idToken = idToken
                        this.provider = Google
                    }
                    val user = supabase.auth.currentUserOrNull()
                    if (user != null) {
                        Result.success(user)
                    } else {
                        Result.failure(Exception("Google sign in failed"))
                    }
                } else {
                    Result.failure(Exception("No ID token received"))
                }
            } catch (e: ApiException) {
                Log.e("AuthRepository", "Google sign in error", e)
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Google sign in error", e)
                Result.failure(e)
            }
        }
    }
    
    // Sign out
    suspend fun signOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.signOut()
                
                // Also sign out from Google
                val clientId = if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotEmpty() && BuildConfig.GOOGLE_WEB_CLIENT_ID != "null") {
                    BuildConfig.GOOGLE_WEB_CLIENT_ID
                } else {
                     "MISSING_WEB_CLIENT_ID"
                }
                
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut()
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Sign out error", e)
                Result.failure(e)
            }
        }
    }
    
    // Reset password
    suspend fun resetPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.resetPasswordForEmail(email)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Reset password error", e)
                Result.failure(e)
            }
        }
    }
}
