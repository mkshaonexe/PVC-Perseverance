package com.perseverance.pvc.data

import com.perseverance.pvc.data.SupabaseClient.client
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.user.UserInfo

class AuthRepository {

    suspend fun signInWithGoogle(idToken: String) {
        client.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
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
