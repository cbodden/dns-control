package com.dnscontrol.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class SavedServer(
    val id: String,
    val name: String,
    val serverUrl: String,
    val apiToken: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("serverUrl", serverUrl)
        put("apiToken", apiToken)
    }
    
    companion object {
        fun fromJson(json: JSONObject): SavedServer = SavedServer(
            id = json.optString("id", ""),
            name = json.optString("name", ""),
            serverUrl = json.optString("serverUrl", ""),
            apiToken = json.optString("apiToken", "")
        )
    }
}

data class AppSettings(
    val serverUrl: String = "",
    val apiToken: String = "",
    val disableMinutes: Int = 5,
    val showDebug: Boolean = false,
    val savedServers: List<SavedServer> = emptyList(),
    val selectedServerId: String? = null
)

class SettingsDataStore(private val context: Context) {
    
    companion object {
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val API_TOKEN = stringPreferencesKey("api_token")
        private val DISABLE_MINUTES = intPreferencesKey("disable_minutes")
        private val SHOW_DEBUG = booleanPreferencesKey("show_debug")
        private val SAVED_SERVERS = stringPreferencesKey("saved_servers")
        private val SELECTED_SERVER_ID = stringPreferencesKey("selected_server_id")
    }
    
    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val savedServersJson = preferences[SAVED_SERVERS] ?: "[]"
        val savedServers = parseSavedServers(savedServersJson)
        
        AppSettings(
            serverUrl = preferences[SERVER_URL] ?: "",
            apiToken = preferences[API_TOKEN] ?: "",
            disableMinutes = preferences[DISABLE_MINUTES] ?: 5,
            showDebug = preferences[SHOW_DEBUG] ?: false,
            savedServers = savedServers,
            selectedServerId = preferences[SELECTED_SERVER_ID]
        )
    }
    
    private fun parseSavedServers(json: String): List<SavedServer> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                SavedServer.fromJson(array.getJSONObject(i))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun savedServersToJson(servers: List<SavedServer>): String {
        val array = JSONArray()
        servers.forEach { array.put(it.toJson()) }
        return array.toString()
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
    
    suspend fun updateShowDebug(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_DEBUG] = show
        }
    }
    
    suspend fun saveServer(server: SavedServer) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[SAVED_SERVERS] ?: "[]"
            val servers = parseSavedServers(currentJson).toMutableList()
            
            // Replace if exists, otherwise add
            val existingIndex = servers.indexOfFirst { it.id == server.id }
            if (existingIndex >= 0) {
                servers[existingIndex] = server
            } else {
                servers.add(server)
            }
            
            preferences[SAVED_SERVERS] = savedServersToJson(servers)
        }
    }
    
    suspend fun deleteServer(serverId: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[SAVED_SERVERS] ?: "[]"
            val servers = parseSavedServers(currentJson).filter { it.id != serverId }
            preferences[SAVED_SERVERS] = savedServersToJson(servers)
            
            // Clear selection if deleted server was selected
            if (preferences[SELECTED_SERVER_ID] == serverId) {
                preferences[SELECTED_SERVER_ID] = ""
            }
        }
    }
    
    suspend fun selectServer(serverId: String?) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_SERVER_ID] = serverId ?: ""
        }
    }
    
    suspend fun selectServerAndApply(server: SavedServer) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_SERVER_ID] = server.id
            preferences[SERVER_URL] = server.serverUrl
            preferences[API_TOKEN] = server.apiToken
        }
    }
    
    suspend fun exportServers(): String {
        val currentSettings = settings.first()
        val exportData = JSONObject().apply {
            put("version", 1)
            put("servers", JSONArray().apply {
                currentSettings.savedServers.forEach { put(it.toJson()) }
            })
        }
        return exportData.toString(2)
    }
    
    suspend fun importServers(jsonString: String): Result<Int> {
        return try {
            val importData = JSONObject(jsonString)
            val serversArray = importData.getJSONArray("servers")
            val importedServers = (0 until serversArray.length()).map { i ->
                SavedServer.fromJson(serversArray.getJSONObject(i))
            }
            
            context.dataStore.edit { preferences ->
                val currentJson = preferences[SAVED_SERVERS] ?: "[]"
                val currentServers = parseSavedServers(currentJson).toMutableList()
                
                var addedCount = 0
                importedServers.forEach { imported ->
                    // Check if server with same URL already exists
                    val existingIndex = currentServers.indexOfFirst { 
                        it.serverUrl == imported.serverUrl 
                    }
                    if (existingIndex < 0) {
                        // Add with new ID to avoid conflicts
                        currentServers.add(imported.copy(id = System.currentTimeMillis().toString() + addedCount))
                        addedCount++
                    }
                }
                
                preferences[SAVED_SERVERS] = savedServersToJson(currentServers)
            }
            
            Result.success(importedServers.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
