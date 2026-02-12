package com.bashmaqawa.presentation.screens.projects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bashmaqawa.R
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.ProjectStatus
import com.bashmaqawa.presentation.components.*
import com.bashmaqawa.presentation.navigation.Screen
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Projects Screen with ViewModel Integration - Professional Version
 * شاشة المشاريع مع تكامل الـ ViewModel - النسخة الاحترافية
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    navController: NavController,
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddProjectSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.projects_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddProjectSheet = true },
                containerColor = AppColors.Primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.AddBusiness, contentDescription = null) },
                text = { Text("مشروع جديد") }
            )
        }
    ) { paddingValues ->
        RefreshableContent(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(paddingValues)
        ) {
            if (uiState.isLoading && !uiState.isRefreshing) {
                // Show shimmer skeleton while loading
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatsRowSkeleton()
                    ListSkeleton(itemCount = 4)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary Stats Header
                    item {
                        FadeInAnimated(delay = 0) {
                            ProjectsStatsHeader(
                                totalProjects = uiState.projects.size,
                                activeCount = uiState.activeCount,
                                completedCount = uiState.completedCount,
                                pausedCount = uiState.pausedCount
                            )
                        }
                    }
                    
                    // Search Bar
                    item {
                        FadeInAnimated(delay = 50) {
                            SearchBar(
                                query = uiState.searchQuery,
                                onQueryChange = viewModel::onSearchQueryChange,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = stringResource(R.string.search)
                            )
                        }
                    }
                    
                    // Status Tabs
                    item {
                        FadeInAnimated(delay = 100) {
                            ProjectStatusTabs(
                                selectedTab = uiState.selectedTab,
                                onTabSelected = viewModel::onTabSelected,
                                allCount = uiState.projects.size,
                                activeCount = uiState.activeCount,
                                completedCount = uiState.completedCount,
                                pausedCount = uiState.pausedCount
                            )
                        }
                    }
                    
                    // Content
                    if (uiState.filteredProjects.isEmpty()) {
                        item {
                            DynamicEmptyState(
                                config = getProjectsEmptyConfig(
                                    tabIndex = uiState.selectedTab,
                                    icons = ProjectEmptyIcons(
                                        all = Icons.Filled.Business,
                                        ongoing = Icons.Filled.PlayCircle,
                                        completed = Icons.Filled.CheckCircle,
                                        onHold = Icons.Filled.PauseCircle
                                    )
                                ),
                                onAction = if (uiState.selectedTab == 0) {{ showAddProjectSheet = true }} else null,
                                tabIndex = uiState.selectedTab,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        }
                    } else {
                        items(
                            items = uiState.filteredProjects,
                            key = { it.id }
                        ) { project ->
                            FadeInAnimated(delay = 150) {
                                EnhancedProjectCard(
                                    project = project,
                                    onClick = { 
                                        navController.navigate(Screen.ProjectDetail.createRoute(project.id))
                                    },
                                    onStatusChange = { status -> 
                                        viewModel.updateProjectStatus(project.id, status) 
                                    }
                                )
                            }
                        }
                    }
                    
                    // Bottom spacing for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
    
    // Add Project Bottom Sheet
    if (showAddProjectSheet) {
        AddProjectBottomSheet(
            onDismiss = { showAddProjectSheet = false },
            onSave = { name, clientName, location ->
                viewModel.addProject(name, clientName, location)
                showAddProjectSheet = false
            }
        )
    }
}

/**
 * Projects Stats Header - Summary Cards
 */
@Composable
fun ProjectsStatsHeader(
    totalProjects: Int,
    activeCount: Int,
    completedCount: Int,
    pausedCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Active Projects Card
        MiniStatCard(
            value = activeCount.toString(),
            label = "نشط",
            color = AppColors.Success,
            modifier = Modifier.weight(1f)
        )
        
        // Completed Projects Card
        MiniStatCard(
            value = completedCount.toString(),
            label = "مكتمل",
            color = AppColors.Primary,
            modifier = Modifier.weight(1f)
        )
        
        // Paused Projects Card
        MiniStatCard(
            value = pausedCount.toString(),
            label = "متوقف",
            color = AppColors.Warning,
            modifier = Modifier.weight(1f)
        )
        
        // Total Projects Card
        MiniStatCard(
            value = totalProjects.toString(),
            label = "الكل",
            color = AppColors.Gray600,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Mini Stat Card Component
 */
@Composable
fun MiniStatCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Project Status Tabs
 */
@Composable
fun ProjectStatusTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    allCount: Int,
    activeCount: Int,
    completedCount: Int,
    pausedCount: Int
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 0.dp,
        containerColor = Color.Transparent,
        divider = {}
    ) {
        Tab(
            selected = selectedTab == 0, 
            onClick = { onTabSelected(0) }, 
            text = { Text("الكل") },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        Tab(
            selected = selectedTab == 1, 
            onClick = { onTabSelected(1) }, 
            text = { Text("جاري") },
            icon = { Icon(Icons.Filled.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        Tab(
            selected = selectedTab == 2, 
            onClick = { onTabSelected(2) }, 
            text = { Text("مكتمل") },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        Tab(
            selected = selectedTab == 3, 
            onClick = { onTabSelected(3) }, 
            text = { Text("متوقف") },
            icon = { Icon(Icons.Filled.PauseCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
    }
}

/**
 * Enhanced Project Card - Professional Design
 */
@Composable
fun EnhancedProjectCard(
    project: Project,
    onClick: () -> Unit,
    onStatusChange: (ProjectStatus) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val statusColor = when (project.status) {
        ProjectStatus.PENDING -> AppColors.Warning
        ProjectStatus.ACTIVE -> AppColors.Success
        ProjectStatus.COMPLETED -> AppColors.Primary
        ProjectStatus.PAUSED -> AppColors.Error
    }
    
    val statusIcon = when (project.status) {
        ProjectStatus.PENDING -> Icons.Filled.Schedule
        ProjectStatus.ACTIVE -> Icons.Filled.PlayCircle
        ProjectStatus.COMPLETED -> Icons.Filled.CheckCircle
        ProjectStatus.PAUSED -> Icons.Filled.PauseCircle
    }
    
    val statusText = when (project.status) {
        ProjectStatus.PENDING -> "قيد الانتظار"
        ProjectStatus.ACTIVE -> "جاري"
        ProjectStatus.COMPLETED -> "مكتمل"
        ProjectStatus.PAUSED -> "متوقف"
    }
    
    // Calculate budget if available
    val budget = (project.areaSqm ?: 0.0) * (project.pricePerMeter ?: 0.0)
    val hasBudget = budget > 0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Status indicator bar at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(statusColor, statusColor.copy(alpha = 0.5f))
                        )
                    )
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Project Icon with Status Color
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Project Name
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Client Name
                if (!project.clientName.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = project.clientName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Location
                if (!project.location.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = project.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Divider
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                // Bottom Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Budget or Area info
                    if (hasBudget) {
                        Column {
                            Text(
                                text = CurrencyFormatter.format(budget),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Primary
                            )
                            Text(
                                text = "الميزانية",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (project.areaSqm != null && project.areaSqm > 0) {
                        Column {
                            Text(
                                text = "${project.areaSqm} م²",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "المساحة",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Start date
                        if (!project.startDate.isNullOrBlank()) {
                            Column {
                                Text(
                                    text = project.startDate,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "تاريخ البدء",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                    }
                    
                    // Actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // View Details Button
                        FilledTonalIconButton(
                            onClick = onClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Filled.Visibility,
                                contentDescription = "عرض التفاصيل",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        // More Options
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.MoreVert, 
                                    contentDescription = "المزيد",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("بدء المشروع") },
                                    onClick = { 
                                        onStatusChange(ProjectStatus.ACTIVE)
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Filled.PlayCircle, null, tint = AppColors.Success) },
                                    enabled = project.status != ProjectStatus.ACTIVE
                                )
                                DropdownMenuItem(
                                    text = { Text("إيقاف مؤقت") },
                                    onClick = { 
                                        onStatusChange(ProjectStatus.PAUSED)
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Filled.PauseCircle, null, tint = AppColors.Warning) },
                                    enabled = project.status != ProjectStatus.PAUSED
                                )
                                DropdownMenuItem(
                                    text = { Text("اكتمال المشروع") },
                                    onClick = { 
                                        onStatusChange(ProjectStatus.COMPLETED)
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Filled.CheckCircle, null, tint = AppColors.Primary) },
                                    enabled = project.status != ProjectStatus.COMPLETED
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Status Badge
 */
@Composable
fun StatusBadge(status: ProjectStatus) {
    val (color, text) = when (status) {
        ProjectStatus.PENDING -> AppColors.Warning to "قيد الانتظار"
        ProjectStatus.ACTIVE -> AppColors.Primary to "جاري"
        ProjectStatus.COMPLETED -> AppColors.Success to "مكتمل"
        ProjectStatus.PAUSED -> AppColors.Error to "متوقف"
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Add Project Bottom Sheet with Enhanced Design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectBottomSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, clientName: String, location: String) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var projectNameError by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AddBusiness,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "إضافة مشروع جديد",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أدخل بيانات المشروع",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = projectName,
                onValueChange = { 
                    projectName = it
                    projectNameError = false
                },
                label = { Text("اسم المشروع *") },
                leadingIcon = {
                    Icon(Icons.Filled.Business, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = projectNameError,
                supportingText = if (projectNameError) {{ Text("اسم المشروع مطلوب") }} else null,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("اسم العميل") },
                leadingIcon = {
                    Icon(Icons.Filled.Person, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("الموقع") },
                leadingIcon = {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إلغاء")
                }
                Button(
                    onClick = { 
                        if (projectName.isBlank()) {
                            projectNameError = true
                        } else {
                            onSave(projectName, clientName, location) 
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ")
                }
            }
        }
    }
}
