package com.dnscontrol.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnscontrol.BuildConfig
import com.dnscontrol.data.AppSettings
import com.dnscontrol.data.SavedServer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settings: AppSettings,
    onServerUrlChange: (String) -> Unit,
    onApiTokenChange: (String) -> Unit,
    onDisableMinutesChange: (Int) -> Unit,
    onShowDebugChange: (Boolean) -> Unit,
    onSaveServer: (String, String, String) -> Unit,
    onSelectServer: (SavedServer) -> Unit,
    onDeleteServer: (String) -> Unit
) {
    var serverUrl by remember(settings.serverUrl) { mutableStateOf(settings.serverUrl) }
    var apiToken by remember(settings.apiToken) { mutableStateOf(settings.apiToken) }
    var disableMinutes by remember(settings) { mutableStateOf(settings.disableMinutes.toString()) }
    var showToken by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var serverNameInput by remember { mutableStateOf("") }
    var showServerDropdown by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<SavedServer?>(null) }
    
    // Save Server Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Server") },
            text = {
                Column {
                    Text(
                        "Enter a name for this server configuration:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = serverNameInput,
                        onValueChange = { serverNameInput = it },
                        label = { Text("Server Name") },
                        placeholder = { Text("e.g., Home DNS, Office DNS") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (serverNameInput.isNotBlank() && serverUrl.isNotBlank() && apiToken.isNotBlank()) {
                            onSaveServer(serverNameInput.trim(), serverUrl, apiToken)
                            serverNameInput = ""
                            showSaveDialog = false
                        }
                    },
                    enabled = serverNameInput.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showSaveDialog = false
                    serverNameInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Confirmation Dialog
    showDeleteConfirm?.let { server ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Server") },
            text = { Text("Are you sure you want to delete \"${server.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteServer(server.id)
                        showDeleteConfirm = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
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
                // Saved Servers Dropdown
                if (settings.savedServers.isNotEmpty()) {
                    val selectedServer = settings.savedServers.find { it.id == settings.selectedServerId }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedCard(
                                onClick = { showServerDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Saved Servers",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = selectedServer?.name ?: "Select a server...",
                                            fontSize = 16.sp,
                                            color = if (selectedServer != null) 
                                                MaterialTheme.colorScheme.onSurface 
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select server",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        
                            DropdownMenu(
                                expanded = showServerDropdown,
                                onDismissRequest = { showServerDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                settings.savedServers.forEach { server ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = server.name,
                                                    fontWeight = if (server.id == settings.selectedServerId) 
                                                        androidx.compose.ui.text.font.FontWeight.Bold 
                                                    else 
                                                        androidx.compose.ui.text.font.FontWeight.Normal
                                                )
                                                Text(
                                                    text = server.serverUrl,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            onSelectServer(server)
                                            showServerDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Delete button - visible when a server is selected
                        if (selectedServer != null) {
                            FilledTonalIconButton(
                                onClick = { showDeleteConfirm = selectedServer },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete server"
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    Text(
                        text = "Or enter new server details:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
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
                
                // Save Server Button
                if (serverUrl.isNotBlank() && apiToken.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Server Configuration")
                    }
                }
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
