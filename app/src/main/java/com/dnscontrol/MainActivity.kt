package com.dnscontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dnscontrol.ui.screens.MainScreen
import com.dnscontrol.ui.screens.SettingsScreen
import com.dnscontrol.ui.screens.StatsScreen
import com.dnscontrol.ui.theme.DNSControlTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            DNSControlTheme {
                val uiState by viewModel.uiState.collectAsState()
                var selectedTab by remember { mutableIntStateOf(0) }
                
                // Fetch stats when switching to stats tab and server is reachable
                LaunchedEffect(selectedTab, uiState.isServerReachable) {
                    if (selectedTab == 1 && uiState.isServerReachable && uiState.dashboardStats == null) {
                        viewModel.fetchDashboardStats()
                    }
                }
                
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Control") },
                                label = { Text("Control") },
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Analytics, contentDescription = "Stats") },
                                label = { Text("Stats") },
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") },
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 }
                            )
                        }
                    }
                ) { padding ->
                    when (selectedTab) {
                        0 -> MainScreen(
                            modifier = Modifier.padding(padding),
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
                            onNavigateToSettings = { selectedTab = 2 }
                        )
                        1 -> StatsScreen(
                            modifier = Modifier.padding(padding),
                            dashboardStats = uiState.dashboardStats,
                            isLoading = uiState.isLoadingStats,
                            isServerReachable = uiState.isServerReachable,
                            lastError = uiState.lastError,
                            onRefresh = viewModel::fetchDashboardStats
                        )
                        2 -> SettingsScreen(
                            modifier = Modifier.padding(padding),
                            settings = uiState.settings,
                            onServerUrlChange = viewModel::updateServerUrl,
                            onApiTokenChange = viewModel::updateApiToken,
                            onDisableMinutesChange = viewModel::updateDisableMinutes
                        )
                    }
                }
            }
        }
    }
}
