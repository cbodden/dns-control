package com.dnscontrol.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val serverUrl: String = "dns.poa.local:538",
    val apiToken: String = "",
    val disableMinutes: Int = 5
)

class SettingsDataStore(private val context: Context) {
    
    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val API_TOKEN = stringPreferencesKey("api_token")
        private val DISABLE_MINUTES = intPreferencesKey("disable_minutes")
    }
    
    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            serverUrl = preferences[SERVER_URL] ?: "dns.poa.local:538",
            apiToken = preferences[API_TOKEN] ?: "",
            disableMinutes = preferences[DISABLE_MINUTES] ?: 5
        )
    }
    
    suspend fun updateServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_URL] = url
        }
    }
    
    suspend fun updateApiToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[API_TOKEN] = token
        }
    }
    
    suspend fun updateDisableMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[DISABLE_MINUTES] = minutes.coerceIn(1, 120)
        }
    }
}
