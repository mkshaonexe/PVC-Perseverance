package com.perseverance.pvc.data.remote

import com.perseverance.pvc.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client by lazy {
        try {
            // Check for valid config or use placeholders to prevent crash
            val url = if (BuildConfig.SUPABASE_URL.isNotBlank()) BuildConfig.SUPABASE_URL else "https://placeholder.supabase.co"
            val key = if (BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) BuildConfig.SUPABASE_ANON_KEY else "placeholder"
            
            createSupabaseClient(
                supabaseUrl = url,
                supabaseKey = key
            ) {
                install(Auth) {
                    flowType = FlowType.PKCE
                    scheme = "app"
                    host = "supabase.com"
                }
                install(Postgrest)
                install(Realtime)
                install(Storage)
            }
        } catch (e: Exception) {
            // Fallback to prevent crash
            createSupabaseClient(
                supabaseUrl = "https://placeholder.supabase.co",
                supabaseKey = "placeholder"
            ) {
                install(Auth)
            }
        }
    }
}
