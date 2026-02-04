package com.dnscontrol.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnscontrol.BuildConfig
import com.dnscontrol.data.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settings: AppSettings,
    onServerUrlChange: (String) -> Unit,
    onApiTokenChange: (String) -> Unit,
    onDisableMinutesChange: (Int) -> Unit,
    onShowDebugChange: (Boolean) -> Unit
) {
    var serverUrl by remember(settings) { mutableStateOf(settings.serverUrl) }
    var apiToken by remember(settings) { mutableStateOf(settings.apiToken) }
    var disableMinutes by remember(settings) { mutableStateOf(settings.disableMinutes.toString()) }
    var showToken by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Server Configuration
            SettingsSection(title = "Server Configuration") {
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { 
                        serverUrl = it
                        onServerUrlChange(it)
                    },
                    label = { Text("Server URL") },
                    placeholder = { Text("server.example.com:5380") },
                    leadingIcon = {
                        Icon(Icons.Default.Storage, contentDescription = null)
                    },
                    supportingText = {
                        Text("Without http:// prefix")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = apiToken,
                    onValueChange = { 
                        apiToken = it
                        onApiTokenChange(it)
                    },
                    label = { Text("API Token") },
                    placeholder = { Text("Your API token") },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showToken = !showToken }) {
                            Icon(
                                imageVector = if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showToken) "Hide token" else "Show token"
                            )
                        }
                    },
                    visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            
            // Disable Duration
            SettingsSection(title = "Disable Duration") {
                OutlinedTextField(
                    value = disableMinutes,
                    onValueChange = { value ->
                        disableMinutes = value.filter { it.isDigit() }
                        val minutes = disableMinutes.toIntOrNull() ?: 5
                        onDisableMinutesChange(minutes.coerceIn(1, 120))
                    },
                    label = { Text("Minutes (1-120)") },
                    leadingIcon = {
                        Icon(Icons.Default.Timer, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Slider for quick selection
                val sliderValue = (disableMinutes.toIntOrNull() ?: 5).coerceIn(1, 120)
                Column {
                    Slider(
                        value = sliderValue.toFloat(),
                        onValueChange = { value ->
                            val minutes = value.toInt()
                            disableMinutes = minutes.toString()
                            onDisableMinutesChange(minutes)
                        },
                        valueRange = 1f..120f,
                        steps = 118
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("120 min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            
            // Debug Settings
            SettingsSection(title = "Debug") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Show Debug Window",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Display raw JSON response on Control tab",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = settings.showDebug,
                        onCheckedChange = onShowDebugChange
                    )
                }
            }
            
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "How it works",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The app will automatically check if your DNS server is reachable when opened. If connected, you can disable blocking temporarily or check the current status.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Build Info
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DNS Control v${BuildConfig.VERSION_NAME}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Build ${BuildConfig.VERSION_CODE} • ${BuildConfig.BUILD_TIME}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
