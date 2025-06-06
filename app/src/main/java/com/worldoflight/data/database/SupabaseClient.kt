package com.worldoflight.data.database

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "YOUR_SUPABASE_URL", // Замените на ваш URL
        supabaseKey = "YOUR_SUPABASE_ANON_KEY" // Замените на ваш ключ
    ) {
        install(Postgrest)
        install(Auth)
    }
}
