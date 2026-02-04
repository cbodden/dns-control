package com.dnscontrol

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dnscontrol.ui.screens.MainScreen
import com.dnscontrol.ui.screens.SettingsScreen
import com.dnscontrol.ui.screens.StatsScreen
import com.dnscontrol.ui.theme.DNSControlTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        
        setContent {
            DNSControlTheme {
                val uiState by viewModel.uiState.collectAsState()
                var selectedTab by remember { mutableIntStateOf(0) }
                val coroutineScope = rememberCoroutineScope()
                
                // File picker for export
                val exportLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/json")
                ) { uri ->
                    uri?.let {
                        coroutineScope.launch {
                            try {
                                val exportData = viewModel.exportServers()
                                contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    outputStream.write(exportData.toByteArray())
                                }
                                Toast.makeText(context, "Servers exported successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                
                // File picker for import
                val importLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    uri?.let {
                        try {
                            contentResolver.openInputStream(uri)?.use { inputStream ->
                                val jsonString = inputStream.bufferedReader().readText()
                                viewModel.importServers(jsonString) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
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
                            showDebug = uiState.settings.showDebug,
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
                            selectedStatsType = uiState.selectedStatsType,
                            customStartTime = uiState.customStartTime,
                            customEndTime = uiState.customEndTime,
                            onStatsTypeChanged = viewModel::setStatsType,
                            onCustomDateRangeSelected = viewModel::setCustomDateRange,
                            onRefresh = { viewModel.fetchDashboardStats() }
                        )
                        2 -> SettingsScreen(
                            modifier = Modifier.padding(padding),
                            settings = uiState.settings,
                            onServerUrlChange = viewModel::updateServerUrl,
                            onApiTokenChange = viewModel::updateApiToken,
                            onDisableMinutesChange = viewModel::updateDisableMinutes,
                            onShowDebugChange = viewModel::updateShowDebug,
                            onSaveServer = viewModel::saveServer,
                            onSelectServer = viewModel::selectServer,
                            onDeleteServer = viewModel::deleteServer,
                            onExportServers = {
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val fileName = "dns-control-servers-${dateFormat.format(Date())}.json"
                                exportLauncher.launch(fileName)
                            },
                            onImportServers = {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            }
                        )
                    }
                }
            }
        }
    }
}
