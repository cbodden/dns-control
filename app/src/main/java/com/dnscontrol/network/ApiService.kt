package com.dnscontrol.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class StatusResponse(
    val isBlocking: Boolean,
    val temporaryDisableBlockingTill: String?,
    val rawJson: String
)

data class DisableResponse(
    val success: Boolean,
    val message: String
)

enum class StatsType(val apiValue: String, val displayName: String) {
    LastHour("LastHour", "Last Hour"),
    LastDay("LastDay", "Last Day"),
    LastWeek("LastWeek", "Last Week"),
    LastMonth("LastMonth", "Last Month"),
    LastYear("LastYear", "Last Year"),
    Custom("Custom", "Custom")
}

data class DashboardStats(
    val totalQueries: Long,
    val totalNoError: Long,
    val totalServerFailure: Long,
    val totalNxDomain: Long,
    val totalRefused: Long,
    val totalAuthoritative: Long,
    val totalRecursive: Long,
    val totalCached: Long,
    val totalBlocked: Long,
    val totalDropped: Long,
    val totalClients: Int,
    val zones: Int,
    val cachedEntries: Int,
    val allowedZones: Int,
    val blockedZones: Int,
    val allowListZones: Int,
    val blockListZones: Long
)

class ApiService {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    suspend fun checkStatus(serverUrl: String, token: String): Result<StatusResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "http://$serverUrl/api/settings/get?token=$token"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                val json = JSONObject(body)
                
                // Try multiple possible response formats
                var blocking = true // default to blocking enabled
                var temporaryDisableBlockingTill: String? = null
                
                // Technitium DNS format: nested in "response" object
                val responseObj = json.optJSONObject("response")
                if (responseObj != null) {
                    // enableBlocking field (Technitium uses this)
                    if (responseObj.has("enableBlocking")) {
                        blocking = responseObj.optBoolean("enableBlocking", true)
                    }
                    // Check for temporary disable time
                    if (responseObj.has("temporaryDisableBlockingTill")) {
                        temporaryDisableBlockingTill = responseObj.optString("temporaryDisableBlockingTill")
                            ?.takeIf { it.isNotEmpty() && it != "null" }
                    }
                }
                
                // Format 1: Direct "enableBlocking" field
                if (json.has("enableBlocking")) {
                    blocking = json.optBoolean("enableBlocking", true)
                }
                
                // Format 2: Direct "blocking" field
                if (json.has("blocking")) {
                    blocking = json.optBoolean("blocking", true)
                }
                
                // Format 3: Direct "enabled" field
                if (json.has("enabled")) {
                    blocking = json.optBoolean("enabled", true)
                }
                
                // Format 4: Nested in "settings" object
                val settings = json.optJSONObject("settings")
                if (settings != null) {
                    if (settings.has("enableBlocking")) {
                        blocking = settings.optBoolean("enableBlocking", true)
                    }
                    if (settings.has("blocking")) {
                        blocking = settings.optBoolean("blocking", true)
                    }
                    temporaryDisableBlockingTill = settings.optString("temporaryDisableBlockingTill")
                        ?.takeIf { it.isNotEmpty() && it != "null" }
                }
                
                // Top-level temporaryDisableBlockingTill
                if (json.has("temporaryDisableBlockingTill")) {
                    temporaryDisableBlockingTill = json.optString("temporaryDisableBlockingTill")
                        ?.takeIf { it.isNotEmpty() && it != "null" }
                }
                
                Result.success(StatusResponse(
                    isBlocking = blocking,
                    temporaryDisableBlockingTill = temporaryDisableBlockingTill,
                    rawJson = body
                ))
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun disableBlocking(serverUrl: String, token: String, minutes: Int): Result<DisableResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "http://$serverUrl/api/settings/temporaryDisableBlocking?token=$token&minutes=$minutes"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                Result.success(DisableResponse(
                    success = true,
                    message = "Blocking disabled for $minutes minutes"
                ))
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDashboardStats(
        serverUrl: String,
        token: String,
        statsType: StatsType = StatsType.LastHour,
        customStart: String? = null,
        customEnd: String? = null
    ): Result<DashboardStats> = withContext(Dispatchers.IO) {
        try {
            val url = buildString {
                append("http://$serverUrl/api/dashboard/stats/get?token=$token&type=${statsType.apiValue}&utc=true")
                if (statsType == StatsType.Custom && customStart != null && customEnd != null) {
                    append("&start=$customStart&end=$customEnd")
                }
            }
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                val json = JSONObject(body)
                val responseObj = json.optJSONObject("response")
                
                // Try to get stats from response.stats first, then fall back to response directly
                val stats = responseObj?.optJSONObject("stats") ?: responseObj
                
                if (stats != null && stats.has("totalQueries")) {
                    Result.success(DashboardStats(
                        totalQueries = stats.optLong("totalQueries", 0),
                        totalNoError = stats.optLong("totalNoError", 0),
                        totalServerFailure = stats.optLong("totalServerFailure", 0),
                        totalNxDomain = stats.optLong("totalNxDomain", 0),
                        totalRefused = stats.optLong("totalRefused", 0),
                        totalAuthoritative = stats.optLong("totalAuthoritative", 0),
                        totalRecursive = stats.optLong("totalRecursive", 0),
                        totalCached = stats.optLong("totalCached", 0),
                        totalBlocked = stats.optLong("totalBlocked", 0),
                        totalDropped = stats.optLong("totalDropped", 0),
                        totalClients = stats.optInt("totalClients", 0),
                        zones = stats.optInt("zones", 0),
                        cachedEntries = stats.optInt("cachedEntries", 0),
                        allowedZones = stats.optInt("allowedZones", 0),
                        blockedZones = stats.optInt("blockedZones", 0),
                        allowListZones = stats.optInt("allowListZones", 0),
                        blockListZones = stats.optLong("blockListZones", 0)
                    ))
                } else {
                    // Provide more context about what we received
                    val keys = if (responseObj != null) responseObj.keys().asSequence().toList() else emptyList()
                    Result.failure(Exception("Invalid response format. Keys found: $keys"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
