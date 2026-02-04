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
}
