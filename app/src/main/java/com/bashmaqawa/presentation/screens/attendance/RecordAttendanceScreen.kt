package com.bashmaqawa.presentation.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bashmaqawa.R
import com.bashmaqawa.data.database.entities.AttendanceStatus
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.presentation.components.GlassmorphicCard
import com.bashmaqawa.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordAttendanceScreen(
    navController: NavController,
    viewModel: RecordAttendanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle Save Success Navigation
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.popBackStack()
            viewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تسجيل الحضور") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading && uiState.workers.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.saveAttendance() },
                    containerColor = AppColors.Primary,
                    contentColor = Color.White
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Date & Stats)
                AttendanceHeader(
                    date = uiState.formattedDate,
                    present = uiState.totalPresent,
                    absent = uiState.totalAbsent,
                    late = uiState.totalLate
                )

                // Project Selector
                ProjectSelector(
                    projects = uiState.projects,
                    selectedProjectId = uiState.selectedProjectId,
                    onProjectSelected = viewModel::onProjectSelected
                )

                // Search Bar
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("بحث عن عامل...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Validation Error
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = AppColors.Error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Worker List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                ) {
                    val filteredWorkers = if (uiState.searchQuery.isBlank()) {
                        uiState.workers
                    } else {
                        uiState.workers.filter { 
                            it.worker.name.contains(uiState.searchQuery, ignoreCase = true) 
                        }
                    }

                    items(filteredWorkers, key = { it.worker.id }) { item ->
                        WorkerAttendanceCard(
                            item = item,
                            onStatusChanged = { status -> 
                                viewModel.onWorkerStatusChanged(item.worker.id, status) 
                            },
                            onSelectionToggled = { isSelected ->
                                viewModel.onWorkerSelectionToggled(item.worker.id, isSelected)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceHeader(
    date: String,
    present: Int,
    absent: Int,
    late: Int
) {
    GlassmorphicCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "حاضر", count = present, color = AppColors.Success)
                StatItem(label = "غائب", count = absent, color = AppColors.Error)
                StatItem(label = "متأخر/نصف", count = late, color = AppColors.Warning)
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectSelector(
    projects: List<Project>,
    selectedProjectId: Int?,
    onProjectSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = projects.find { it.id == selectedProjectId }?.name ?: "اختر المشروع",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            projects.forEach { project ->
                ProjectDropdownItem(
                    project = project,
                    onClick = {
                        onProjectSelected(project.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProjectDropdownItem(
    project: Project,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = project.name) },
        onClick = onClick
    )
}

@Composable
fun WorkerAttendanceCard(
    item: WorkerAttendanceItem,
    onStatusChanged: (AttendanceStatus) -> Unit,
    onSelectionToggled: (Boolean) -> Unit
) {
    val containerColor = if (item.isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isSelected) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = item.isSelected,
                    onCheckedChange = onSelectionToggled
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = item.worker.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = item.worker.role ?: "عامل", // Default fallback
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (item.isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusChip(
                        status = AttendanceStatus.PRESENT,
                        isSelected = item.status == AttendanceStatus.PRESENT,
                        onClick = { onStatusChanged(AttendanceStatus.PRESENT) }
                    )
                    StatusChip(
                        status = AttendanceStatus.HALF_DAY,
                        isSelected = item.status == AttendanceStatus.HALF_DAY,
                        onClick = { onStatusChanged(AttendanceStatus.HALF_DAY) }
                    )
                    StatusChip(
                        status = AttendanceStatus.ABSENT,
                        isSelected = item.status == AttendanceStatus.ABSENT,
                        onClick = { onStatusChanged(AttendanceStatus.ABSENT) }
                    )
                    StatusChip(
                        status = AttendanceStatus.OVERTIME,
                        isSelected = item.status == AttendanceStatus.OVERTIME,
                        onClick = { onStatusChanged(AttendanceStatus.OVERTIME) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(
    status: AttendanceStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (label, color) = when (status) {
        AttendanceStatus.PRESENT -> "حاضر" to AppColors.Success
        AttendanceStatus.ABSENT -> "غائب" to AppColors.Error
        AttendanceStatus.HALF_DAY -> "نصف" to AppColors.Warning
        AttendanceStatus.OVERTIME -> "إضافي" to AppColors.Accent
    }
    
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)) }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color,
            selectedLeadingIconColor = color
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}
