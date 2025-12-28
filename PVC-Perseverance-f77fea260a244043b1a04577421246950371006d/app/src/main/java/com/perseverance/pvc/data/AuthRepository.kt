package com.perseverance.pvc.data

import com.perseverance.pvc.di.SupabaseModule
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

class AuthRepository {
    private val auth = SupabaseModule.client.auth

    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus

    val currentUser get() = auth.currentUserOrNull()

    suspend fun signInWithGoogle() {
        auth.signInWith(Google)
    }

    suspend fun signOut() {
        auth.signOut()
    }
}
