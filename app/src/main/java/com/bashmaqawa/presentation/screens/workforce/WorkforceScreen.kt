package com.bashmaqawa.presentation.screens.workforce

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bashmaqawa.R
import com.bashmaqawa.data.database.dao.WorkerWithCategory
import com.bashmaqawa.data.database.entities.SkillLevel
import com.bashmaqawa.presentation.components.*
import com.bashmaqawa.presentation.navigation.Screen
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.utils.CurrencyFormatter
import kotlinx.coroutines.launch

/**
 * Workforce Screen with proper state updates and user feedback
 * شاشة القوى العاملة مع تحديثات الحالة وتغذية راجعة للمستخدم
 * 
 * FIX: Snackbar shows success/error messages
 * FIX: Form is wired to ViewModel with proper callbacks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkforceScreen(
    navController: NavController,
    viewModel: WorkforceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddWorkerSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // FIX: Show snackbar for success messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }
    
    // Show snackbar for error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.workforce_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
        floatingActionButton = {
            // Only show FAB on Active tab
            if (uiState.selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddWorkerSheet = true },
                    containerColor = AppColors.Primary
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = "Add Worker")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = stringResource(R.string.search)
            )
            
            // Tabs
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text(stringResource(R.string.worker_active)) },
                    icon = { Icon(Icons.Filled.Group, contentDescription = null) }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text(stringResource(R.string.worker_archived)) },
                    icon = { Icon(Icons.Filled.Archive, contentDescription = null) }
                )
            }
            
            // Worker List
            if (uiState.isLoading) {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            } else if (uiState.workers.isEmpty()) {
                DynamicEmptyState(
                    config = getWorkforceEmptyConfig(
                        tabIndex = uiState.selectedTab,
                        icons = WorkforceEmptyIcons(
                            active = Icons.Filled.Group,
                            archived = Icons.Filled.Archive
                        )
                    ),
                    onAction = if (uiState.selectedTab == 0) {{ showAddWorkerSheet = true }} else null,
                    tabIndex = uiState.selectedTab,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Use rememberLazyListState for scroll position preservation
                val listState = rememberLazyListState()
                
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Use key for efficient recomposition during add/remove
                    items(
                        items = uiState.workers,
                        key = { it.id }
                    ) { worker ->
                        WorkerCard(
                            worker = worker,
                            onClick = {
                                navController.navigate(Screen.WorkerDetail.createRoute(worker.id))
                            },
                            onArchive = if (uiState.selectedTab == 0) {
                                { viewModel.archiveWorker(worker.id) }
                            } else null,
                            onActivate = if (uiState.selectedTab == 1) {
                                { viewModel.activateWorker(worker.id) }
                            } else null
                        )
                    }
                }
            }
        }
    }
    
    // Add Worker Bottom Sheet - FIX: Now wired to ViewModel
    if (showAddWorkerSheet) {
        AddWorkerBottomSheet(
            isSaving = uiState.isSaving,
            onDismiss = { showAddWorkerSheet = false },
            onSave = { name, phone, dailyRate, skillLevel ->
                viewModel.addWorker(
                    name = name,
                    phone = phone,
                    dailyRate = dailyRate,
                    skillLevel = skillLevel,
                    onSuccess = { showAddWorkerSheet = false },
                    onError = { /* Error shown via snackbar */ }
                )
            }
        )
    }
}

/**
 * Worker Card with actions
 * بطاقة العامل مع الإجراءات
 * 
 * Uses standardized AppCardDefaults for consistent design across screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerCard(
    worker: WorkerWithCategory,
    onClick: () -> Unit,
    onArchive: (() -> Unit)? = null,
    onActivate: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppCardDefaults.Shape,
        colors = AppCardDefaults.colors(),
        elevation = AppCardDefaults.elevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = AppColors.Primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Work,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = worker.categoryName ?: "غير محدد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(worker.dailyRate ?: 0.0),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Success
                )
                Text(
                    text = "يومياً",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Actions menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (onArchive != null) {
                        DropdownMenuItem(
                            text = { Text("أرشفة") },
                            onClick = {
                                onArchive()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Filled.Archive, null) }
                        )
                    }
                    if (onActivate != null) {
                        DropdownMenuItem(
                            text = { Text("تفعيل") },
                            onClick = {
                                onActivate()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Filled.CheckCircle, null, tint = AppColors.Success) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Add Worker Bottom Sheet with complete form
 * 
 * FIX: Phone field uses KeyboardType.Phone
 * FIX: Daily rate uses KeyboardType.Decimal with ج.م suffix
 * FIX: Loading indicator in save button
 * FIX: Form validation before save
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerBottomSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String?, dailyRate: Double?, skillLevel: SkillLevel) -> Unit
) {
    var workerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dailyRate by remember { mutableStateOf("") }
    var selectedSkillLevel by remember { mutableStateOf(SkillLevel.HELPER) }
    var nameError by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "إضافة عامل جديد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Name field (required)
            OutlinedTextField(
                value = workerName,
                onValueChange = { 
                    workerName = it
                    nameError = false
                },
                label = { Text("اسم العامل *") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                isError = nameError,
                supportingText = if (nameError) {{ Text("اسم العامل مطلوب") }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // FIX: Phone field with correct keyboard type
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // FIX: Daily rate with decimal keyboard and EGP currency
            OutlinedTextField(
                value = dailyRate,
                onValueChange = { dailyRate = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("الأجر اليومي") },
                leadingIcon = { Icon(Icons.Filled.Payments, contentDescription = null) },
                suffix = { Text("ج.م") }, // FIX: Egyptian Pound instead of $
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skill Level Selection
            Text(
                text = "مستوى المهارة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillLevel.entries.forEach { level ->
                    FilterChip(
                        selected = selectedSkillLevel == level,
                        onClick = { selectedSkillLevel = level },
                        label = { 
                            Text(
                                text = when (level) {
                                    SkillLevel.HELPER -> "عامل"
                                    SkillLevel.SKILLED -> "صنايعي"
                                    SkillLevel.MASTER -> "أسطى"
                                }
                            )
                        },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    Text("إلغاء")
                }
                Button(
                    onClick = {
                        if (workerName.isBlank()) {
                            nameError = true
                        } else {
                            onSave(
                                workerName,
                                phone.takeIf { it.isNotBlank() },
                                dailyRate.toDoubleOrNull(),
                                selectedSkillLevel
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    ),
                    enabled = !isSaving && workerName.isNotBlank()
                ) {
                    if (isSaving) {
                        // FIX: Loading indicator while saving
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
