package com.dnscontrol.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnscontrol.network.DashboardStats
import com.dnscontrol.network.StatsType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    dashboardStats: DashboardStats?,
    isLoading: Boolean,
    isServerReachable: Boolean,
    lastError: String?,
    selectedStatsType: StatsType,
    customStartTime: String?,
    customEndTime: String?,
    onStatsTypeChanged: (StatsType) -> Unit,
    onCustomDateRangeSelected: (String, String) -> Unit,
    onRefresh: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    // Custom date range state
    var showCustomRangePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    val calendar = remember { Calendar.getInstance() }
    var startDate by remember { mutableStateOf(calendar.timeInMillis - 86400000) } // Default: 24 hours ago
    var startHour by remember { mutableIntStateOf(0) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endDate by remember { mutableStateOf(calendar.timeInMillis) }
    var endHour by remember { mutableIntStateOf(23) }
    var endMinute by remember { mutableIntStateOf(59) }
    
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val isoFormatter = remember { 
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    
    // Show custom range picker when Custom is selected
    LaunchedEffect(selectedStatsType) {
        if (selectedStatsType == StatsType.Custom) {
            showCustomRangePicker = true
        }
    }
    
    Scaffold(
        topBar = {
            StatsTopAppBar(
                title = { Text("Dashboard Stats") },
                subtitle = {
                    Box {
                        Row(
                            modifier = Modifier.clickable(
                                enabled = !isLoading && isServerReachable
                            ) { dropdownExpanded = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedStatsType.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select time period",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            StatsType.entries.forEach { statsType ->
                                DropdownMenuItem(
                                    text = { Text(statsType.displayName) },
                                    onClick = {
                                        dropdownExpanded = false
                                        onStatsTypeChanged(statsType)
                                    },
                                    leadingIcon = if (statsType == selectedStatsType) {
                                        {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isLoading && isServerReachable
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { padding ->
        // Date Pickers
        if (showStartDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { startDate = it }
                        showStartDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        if (showEndDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { endDate = it }
                        showEndDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        // Time Pickers
        if (showStartTimePicker) {
            val timePickerState = rememberTimePickerState(initialHour = startHour, initialMinute = startMinute)
            TimePickerDialog(
                onDismiss = { showStartTimePicker = false },
                onConfirm = {
                    startHour = timePickerState.hour
                    startMinute = timePickerState.minute
                    showStartTimePicker = false
                }
            ) {
                TimePicker(state = timePickerState)
            }
        }
        
        if (showEndTimePicker) {
            val timePickerState = rememberTimePickerState(initialHour = endHour, initialMinute = endMinute)
            TimePickerDialog(
                onDismiss = { showEndTimePicker = false },
                onConfirm = {
                    endHour = timePickerState.hour
                    endMinute = timePickerState.minute
                    showEndTimePicker = false
                }
            ) {
                TimePicker(state = timePickerState)
            }
        }
        
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading stats...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                !isServerReachable -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Server not connected",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        lastError?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                selectedStatsType == StatsType.Custom && showCustomRangePicker && dashboardStats == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CustomRangePickerCard(
                            startDate = startDate,
                            startHour = startHour,
                            startMinute = startMinute,
                            endDate = endDate,
                            endHour = endHour,
                            endMinute = endMinute,
                            dateFormatter = dateFormatter,
                            timeFormatter = timeFormatter,
                            onStartDateClick = { showStartDatePicker = true },
                            onStartTimeClick = { showStartTimePicker = true },
                            onEndDateClick = { showEndDatePicker = true },
                            onEndTimeClick = { showEndTimePicker = true },
                            onApply = {
                                val startCal = Calendar.getInstance().apply {
                                    timeInMillis = startDate
                                    set(Calendar.HOUR_OF_DAY, startHour)
                                    set(Calendar.MINUTE, startMinute)
                                    set(Calendar.SECOND, 0)
                                }
                                val endCal = Calendar.getInstance().apply {
                                    timeInMillis = endDate
                                    set(Calendar.HOUR_OF_DAY, endHour)
                                    set(Calendar.MINUTE, endMinute)
                                    set(Calendar.SECOND, 59)
                                }
                                val startIso = isoFormatter.format(startCal.time)
                                val endIso = isoFormatter.format(endCal.time)
                                showCustomRangePicker = false
                                onCustomDateRangeSelected(startIso, endIso)
                            }
                        )
                    }
                }
                
                dashboardStats != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Show custom range editor if Custom type is selected
                        if (selectedStatsType == StatsType.Custom) {
                            CustomRangePickerCard(
                                startDate = startDate,
                                startHour = startHour,
                                startMinute = startMinute,
                                endDate = endDate,
                                endHour = endHour,
                                endMinute = endMinute,
                                dateFormatter = dateFormatter,
                                timeFormatter = timeFormatter,
                                onStartDateClick = { showStartDatePicker = true },
                                onStartTimeClick = { showStartTimePicker = true },
                                onEndDateClick = { showEndDatePicker = true },
                                onEndTimeClick = { showEndTimePicker = true },
                                onApply = {
                                    val startCal = Calendar.getInstance().apply {
                                        timeInMillis = startDate
                                        set(Calendar.HOUR_OF_DAY, startHour)
                                        set(Calendar.MINUTE, startMinute)
                                        set(Calendar.SECOND, 0)
                                    }
                                    val endCal = Calendar.getInstance().apply {
                                        timeInMillis = endDate
                                        set(Calendar.HOUR_OF_DAY, endHour)
                                        set(Calendar.MINUTE, endMinute)
                                        set(Calendar.SECOND, 59)
                                    }
                                    val startIso = isoFormatter.format(startCal.time)
                                    val endIso = isoFormatter.format(endCal.time)
                                    onCustomDateRangeSelected(startIso, endIso)
                                }
                            )
                        }
                        
                        // Query Summary Card
                        StatsCard(title = "Query Summary") {
                            StatRow(
                                icon = Icons.Default.Search,
                                label = "Total Queries",
                                value = numberFormat.format(dashboardStats.totalQueries),
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatRow(
                                icon = Icons.Default.CheckCircle,
                                label = "No Error",
                                value = numberFormat.format(dashboardStats.totalNoError),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            StatRow(
                                icon = Icons.Default.Error,
                                label = "Server Failure",
                                value = numberFormat.format(dashboardStats.totalServerFailure),
                                color = MaterialTheme.colorScheme.error
                            )
                            StatRow(
                                icon = Icons.Default.Dns,
                                label = "NX Domain",
                                value = numberFormat.format(dashboardStats.totalNxDomain),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            StatRow(
                                icon = Icons.Default.Block,
                                label = "Refused",
                                value = numberFormat.format(dashboardStats.totalRefused),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        // Resolution Types Card
                        StatsCard(title = "Resolution Types") {
                            StatRow(
                                icon = Icons.Default.Cloud,
                                label = "Authoritative",
                                value = numberFormat.format(dashboardStats.totalAuthoritative),
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatRow(
                                icon = Icons.Default.Search,
                                label = "Recursive",
                                value = numberFormat.format(dashboardStats.totalRecursive),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            StatRow(
                                icon = Icons.Default.Cached,
                                label = "Cached",
                                value = numberFormat.format(dashboardStats.totalCached),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        
                        // Blocking Stats Card
                        StatsCard(title = "Blocking") {
                            StatRow(
                                icon = Icons.Default.Shield,
                                label = "Blocked",
                                value = numberFormat.format(dashboardStats.totalBlocked),
                                color = MaterialTheme.colorScheme.error
                            )
                            StatRow(
                                icon = Icons.Default.Block,
                                label = "Dropped",
                                value = numberFormat.format(dashboardStats.totalDropped),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        // Server Info Card
                        StatsCard(title = "Server Info") {
                            StatRow(
                                icon = Icons.Default.Devices,
                                label = "Total Clients",
                                value = numberFormat.format(dashboardStats.totalClients),
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatRow(
                                icon = Icons.Default.Dns,
                                label = "Zones",
                                value = numberFormat.format(dashboardStats.zones),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            StatRow(
                                icon = Icons.Default.Cached,
                                label = "Cached Entries",
                                value = numberFormat.format(dashboardStats.cachedEntries),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        
                        // Zone Lists Card
                        StatsCard(title = "Zone Lists") {
                            StatRow(
                                icon = Icons.Default.CheckCircle,
                                label = "Allowed Zones",
                                value = numberFormat.format(dashboardStats.allowedZones),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            StatRow(
                                icon = Icons.Default.Block,
                                label = "Blocked Zones",
                                value = numberFormat.format(dashboardStats.blockedZones),
                                color = MaterialTheme.colorScheme.error
                            )
                            StatRow(
                                icon = Icons.Default.CheckCircle,
                                label = "Allow List Zones",
                                value = numberFormat.format(dashboardStats.allowListZones),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            StatRow(
                                icon = Icons.Default.Shield,
                                label = "Block List Zones",
                                value = numberFormat.format(dashboardStats.blockListZones),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No stats available",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Stats")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsTopAppBar(
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    colors: TopAppBarColors,
    actions: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        title = {
            Column {
                title()
                subtitle()
            }
        },
        colors = colors,
        actions = actions
    )
}

@Composable
private fun StatsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = color.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { content() }
    )
}

@Composable
private fun CustomRangePickerCard(
    startDate: Long,
    startHour: Int,
    startMinute: Int,
    endDate: Long,
    endHour: Int,
    endMinute: Int,
    dateFormatter: SimpleDateFormat,
    timeFormatter: SimpleDateFormat,
    onStartDateClick: () -> Unit,
    onStartTimeClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onEndTimeClick: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Custom Date Range",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Start Date/Time
            Text(
                text = "Start",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStartDateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateFormatter.format(Date(startDate)))
                }
                OutlinedButton(
                    onClick = onStartTimeClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // End Date/Time
            Text(
                text = "End",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEndDateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateFormatter.format(Date(endDate)))
                }
                OutlinedButton(
                    onClick = onEndTimeClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Range")
            }
        }
    }
}
