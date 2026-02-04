package com.dnscontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dnscontrol.ui.screens.MainScreen
import com.dnscontrol.ui.screens.SettingsScreen
import com.dnscontrol.ui.theme.DNSControlTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            DNSControlTheme {
                val uiState by viewModel.uiState.collectAsState()
                var showSettings by remember { mutableStateOf(false) }
                
                if (showSettings) {
                    SettingsScreen(
                        settings = uiState.settings,
                        onServerUrlChange = viewModel::updateServerUrl,
                        onApiTokenChange = viewModel::updateApiToken,
                        onDisableMinutesChange = viewModel::updateDisableMinutes,
                        onNavigateBack = { 
                            showSettings = false
                            viewModel.refreshStatus()
                        }
                    )
                } else {
                    MainScreen(
                        settings = uiState.settings,
                        isServerReachable = uiState.isServerReachable,
                        isCheckingReachability = uiState.isCheckingReachability,
                        statusResponse = uiState.statusResponse,
                        isLoading = uiState.isLoading,
                        lastError = uiState.lastError,
                        lastSuccess = uiState.lastSuccess,
                        onDisableBlocking = viewModel::disableBlocking,
                        onCheckStatus = viewModel::checkStatus,
                        onRefresh = viewModel::refreshStatus,
                        onNavigateToSettings = { showSettings = true }
                    )
                }
            }
        }
    }
}
