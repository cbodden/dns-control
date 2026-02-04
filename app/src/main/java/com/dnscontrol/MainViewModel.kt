package com.dnscontrol

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnscontrol.data.AppSettings
import com.dnscontrol.data.SettingsDataStore
import com.dnscontrol.network.ApiService
import com.dnscontrol.network.DashboardStats
import com.dnscontrol.network.StatusResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val settings: AppSettings = AppSettings(),
    val isServerReachable: Boolean = false,
    val isCheckingReachability: Boolean = true,
    val statusResponse: StatusResponse? = null,
    val dashboardStats: DashboardStats? = null,
    val isLoadingStats: Boolean = false,
    val isLoading: Boolean = false,
    val lastError: String? = null,
    val lastSuccess: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsDataStore = SettingsDataStore(application)
    private val apiService = ApiService()
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private var hasInitialized = false
    
    init {
        // Observe settings and check reachability when they change
        viewModelScope.launch {
            settingsDataStore.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
                
                // Auto-check on first load or when settings change
                if (settings.serverUrl.isNotEmpty() && settings.apiToken.isNotEmpty()) {
                    checkServerAndStatus()
                } else {
                    _uiState.update { 
                        it.copy(
                            isCheckingReachability = false,
                            isServerReachable = false
                        ) 
                    }
                }
            }
        }
    }
    
    private fun checkServerAndStatus() {
        val settings = _uiState.value.settings
        if (settings.serverUrl.isEmpty() || settings.apiToken.isEmpty()) {
            _uiState.update { 
                it.copy(
                    isCheckingReachability = false,
                    isServerReachable = false
                ) 
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingReachability = true) }
            
            val result = apiService.checkStatus(settings.serverUrl, settings.apiToken)
            
            result.fold(
                onSuccess = { response ->
                    _uiState.update { 
                        it.copy(
                            statusResponse = response,
                            isServerReachable = true,
                            isCheckingReachability = false,
                            lastError = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isServerReachable = false,
                            isCheckingReachability = false,
                            lastError = "Server unreachable: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            settingsDataStore.updateServerUrl(url)
        }
    }
    
    fun updateApiToken(token: String) {
        viewModelScope.launch {
            settingsDataStore.updateApiToken(token)
        }
    }
    
    fun updateDisableMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsDataStore.updateDisableMinutes(minutes)
        }
    }
    
    fun checkStatus() {
        val settings = _uiState.value.settings
        if (settings.serverUrl.isEmpty() || settings.apiToken.isEmpty()) {
            _uiState.update { it.copy(lastError = "Please configure server URL and API token") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, lastError = null, lastSuccess = null) }
            
            val result = apiService.checkStatus(settings.serverUrl, settings.apiToken)
            
            result.fold(
                onSuccess = { response ->
                    val statusText = if (response.isBlocking) "Blocking is enabled" else "Blocking is disabled"
                    _uiState.update { 
                        it.copy(
                            statusResponse = response,
                            isServerReachable = true,
                            isLoading = false,
                            lastError = null,
                            lastSuccess = statusText
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            lastError = "Error: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    fun disableBlocking() {
        val settings = _uiState.value.settings
        if (settings.serverUrl.isEmpty() || settings.apiToken.isEmpty()) {
            _uiState.update { it.copy(lastError = "Please configure server URL and API token") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, lastError = null, lastSuccess = null) }
            
            val disableResult = apiService.disableBlocking(settings.serverUrl, settings.apiToken, settings.disableMinutes)
            
            disableResult.fold(
                onSuccess = {
                    // Now fetch the updated status
                    val statusResult = apiService.checkStatus(settings.serverUrl, settings.apiToken)
                    statusResult.fold(
                        onSuccess = { response ->
                            _uiState.update { 
                                it.copy(
                                    statusResponse = response,
                                    isLoading = false,
                                    lastError = null,
                                    lastSuccess = "Blocking disabled for ${settings.disableMinutes} minutes"
                                )
                            }
                        },
                        onFailure = {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    lastSuccess = "Blocking disabled for ${settings.disableMinutes} minutes",
                                    lastError = null
                                )
                            }
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            lastError = "Error: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    fun refreshStatus() {
        checkServerAndStatus()
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(lastError = null, lastSuccess = null) }
    }
    
    fun updateShowDebug(show: Boolean) {
        viewModelScope.launch {
            settingsDataStore.updateShowDebug(show)
        }
    }
    
    fun fetchDashboardStats() {
        val settings = _uiState.value.settings
        if (settings.serverUrl.isEmpty() || settings.apiToken.isEmpty()) {
            _uiState.update { it.copy(lastError = "Please configure server URL and API token") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true, lastError = null) }
            
            val result = apiService.getDashboardStats(settings.serverUrl, settings.apiToken)
            
            result.fold(
                onSuccess = { stats ->
                    _uiState.update { 
                        it.copy(
                            dashboardStats = stats,
                            isLoadingStats = false,
                            lastError = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(
                            isLoadingStats = false,
                            lastError = "Error fetching stats: ${error.message}"
                        )
                    }
                }
            )
        }
    }
}
