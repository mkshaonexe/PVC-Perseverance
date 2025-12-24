package com.perseverance.pvc.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.perseverance.pvc.BuildConfig
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(private val context: Context) {

    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.await()
            if (account.idToken != null) {
                SignInResult.Success(account.idToken!!)
            } else {
                SignInResult.Error("No ID token found")
            }
        } catch (e: ApiException) {
            SignInResult.Error(e.message ?: "SignIn failed")
        }
    }

    suspend fun signOut() {
        try {
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

sealed class SignInResult {
    data class Success(val idToken: String) : SignInResult()
    data class Error(val message: String) : SignInResult()
}
