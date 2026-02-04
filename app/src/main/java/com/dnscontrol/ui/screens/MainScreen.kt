package com.dnscontrol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnscontrol.data.AppSettings
import com.dnscontrol.network.StatusResponse
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    settings: AppSettings,
    isServerReachable: Boolean,
    isCheckingReachability: Boolean,
    statusResponse: StatusResponse?,
    isLoading: Boolean,
    lastError: String?,
    lastSuccess: String?,
    showDebug: Boolean,
    onDisableBlocking: () -> Unit,
    onCheckStatus: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DNS Control") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isLoading && !isCheckingReachability
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Card
            StatusCard(
                isServerReachable = isServerReachable,
                isCheckingReachability = isCheckingReachability,
                serverUrl = settings.serverUrl,
                statusResponse = statusResponse
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main Controls - Show when server is reachable
            AnimatedVisibility(
                visible = isServerReachable && !isCheckingReachability,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Disable Blocking Button
                    Button(
                        onClick = onDisableBlocking,
                        enabled = !isLoading && !isCheckingReachability,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Disable Blocking (${settings.disableMinutes} min)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    // Check Status Button
                    OutlinedButton(
                        onClick = onCheckStatus,
                        enabled = !isLoading && !isCheckingReachability,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Check Status",
                            fontSize = 14.sp
                        )
                    }
                    
                    // Success Message
                    AnimatedVisibility(visible = lastSuccess != null) {
                        lastSuccess?.let { success ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(
                                    text = success,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    // Error Message
                    AnimatedVisibility(visible = lastError != null) {
                        lastError?.let { error ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    // Debug: Raw JSON Response (only show if enabled in settings)
                    if (showDebug) statusResponse?.let { response ->
                        val clipboardManager = LocalClipboardManager.current
                        var copied by remember { mutableStateOf(false) }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Debug: Raw Response",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(response.rawJson))
                                            copied = true
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (copied) "Copied!" else "Copy", fontSize = 12.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = response.rawJson,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.horizontalScroll(rememberScrollState())
                                )
                            }
                        }
                    }
                }
            }
            
            // Loading State
            AnimatedVisibility(
                visible = isCheckingReachability,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Connecting to server...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Not Reachable Message
            AnimatedVisibility(
                visible = !isServerReachable && !isCheckingReachability,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NotReachableCard(
                    serverUrl = settings.serverUrl,
                    lastError = lastError,
                    onNavigateToSettings = onNavigateToSettings,
                    onRetry = onRefresh
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    isServerReachable: Boolean,
    isCheckingReachability: Boolean,
    serverUrl: String,
    statusResponse: StatusResponse?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Connection Status Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(
                        if (isCheckingReachability) {
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            )
                        } else if (isServerReachable) {
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCheckingReachability) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = if (isServerReachable) Icons.Default.CheckCircle else Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isServerReachable) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when {
                    isCheckingReachability -> "Connecting..."
                    isServerReachable -> "Connected"
                    else -> "Unreachable"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (serverUrl.isNotEmpty()) serverUrl else "No server configured",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            // Status Info
            if (isServerReachable && statusResponse != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Blocking",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = if (statusResponse.isBlocking) "Enabled" else "Disabled",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (statusResponse.isBlocking) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                        )
                    }
                    
                    statusResponse.temporaryDisableBlockingTill?.let { time ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Resumes",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = formatRelativeTime(time),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotReachableCard(
    serverUrl: String,
    lastError: String?,
    onNavigateToSettings: () -> Unit,
    onRetry: () -> Unit
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (serverUrl.isEmpty()) {
                    "Please configure your server in Settings"
                } else {
                    "Cannot reach server at \"$serverUrl\""
                },
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            lastError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
                
                Button(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settings")
                }
            }
        }
    }
}

private fun formatRelativeTime(isoTimestamp: String): String {
    return try {
        // Parse the ISO 8601 timestamp
        val targetTime = ZonedDateTime.parse(isoTimestamp, DateTimeFormatter.ISO_DATE_TIME)
        val now = ZonedDateTime.now(targetTime.zone)
        val duration = Duration.between(now, targetTime)
        
        val totalMinutes = duration.toMinutes()
        val totalSeconds = duration.seconds
        
        when {
            totalSeconds <= 0 -> "now"
            totalMinutes < 1 -> "in ${totalSeconds}s"
            totalMinutes < 60 -> "in ${totalMinutes} min"
            totalMinutes < 120 -> {
                val hours = totalMinutes / 60
                val mins = totalMinutes % 60
                if (mins > 0) "in ${hours}h ${mins}m" else "in ${hours} hour"
            }
            else -> {
                val hours = totalMinutes / 60
                "in ${hours} hours"
            }
        }
    } catch (e: Exception) {
        // If parsing fails, return the original string
        isoTimestamp
    }
}
