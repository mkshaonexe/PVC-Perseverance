package com.perseverance.pvc.data

import com.perseverance.pvc.data.SupabaseClient.client
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo

class AuthRepository {

    suspend fun signInWithGoogle(idToken: String) {
        client.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
        }
    }

    suspend fun signInWithEmail(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUpWithEmail(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    fun getCurrentUser(): UserInfo? {
        return client.auth.currentUserOrNull()
    }

    val sessionStatus = client.auth.sessionStatus
}
