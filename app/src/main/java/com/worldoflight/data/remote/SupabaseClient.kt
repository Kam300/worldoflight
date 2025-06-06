package com.worldoflight.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://knnlbwmestgnivqcskli.supabase.co", // Замените на ваш URL
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imtubmxid21lc3Rnbml2cWNza2xpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDkyMjQxMTUsImV4cCI6MjA2NDgwMDExNX0.cDTZ5JAvc4MKTBWIJfW1ccvJY5pdObEPz4yBtkB1J78" // Замените на ваш ключ
    ) {
        install(Auth) {
            flowType = FlowType.PKCE
            scheme = "worldoflight"
            host = "auth"
        }
        install(Postgrest)
    }
}
