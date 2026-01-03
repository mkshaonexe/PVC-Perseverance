package com.perseverance.pvc.di

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

object SupabaseModule {
    // Ideally these should come from BuildConfig found in app/build/generated/source/buildConfig/debug/com/perseverance/pvc/BuildConfig.java
    // But since I can't easily rely on BuildConfig being generated yet for .env values without a proper plugin setup in this environment,
    // I will hardcode the values from the .env file the user provided to ensure it works immediately.
    // In a production app, use BuildConfig.
    private const val SUPABASE_URL = "https://srv1238030.hstgr.cloud"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImlzcyI6InN1cGFiYXNlIiwiaWF0IjoxNzY3MjY3ODI1LCJleHAiOjIwODI2Mjc4MjV9.VBtdFLLDRAUR5RKDPPfqDwZeb_sFCvhOH_Tho5WNxeQ"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth) {
            scheme = "pvcperseverance"
            host = "login"
        }
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}
