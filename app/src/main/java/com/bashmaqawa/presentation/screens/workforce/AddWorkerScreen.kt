package com.bashmaqawa.presentation.screens.workforce

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bashmaqawa.data.database.entities.SkillLevel
import com.bashmaqawa.data.database.entities.WorkerCategory
import com.bashmaqawa.presentation.components.GlassmorphicCard
import com.bashmaqawa.presentation.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerScreen(
    navController: NavController,
    viewModel: AddWorkerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Handle Save Success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.popBackStack()
            viewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إضافة عامل جديد") },
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
            FloatingActionButton(
                onClick = { viewModel.saveWorker() },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.Error.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error!!,
                        color = AppColors.Error,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Personal Info Section
            Text(
                text = "البيانات الشخصية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
            
            GlassmorphicCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Name
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("الاسم بالكامل") },
                        leadingIcon = { Icon(Icons.Filled.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.nameError != null,
                        supportingText = { uiState.nameError?.let { Text(it) } },
                        singleLine = true
                    )

                    // Phone
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = viewModel::onPhoneChange,
                        label = { Text("رقم الهاتف") },
                        leadingIcon = { Icon(Icons.Filled.Phone, null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    // WhatsApp
                    OutlinedTextField(
                        value = uiState.whatsappPhone,
                        onValueChange = viewModel::onWhatsappPhoneChange,
                        label = { Text("رقم الواتساب (اختياري)") },
                        leadingIcon = { Icon(Icons.Filled.Phone, null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    // National ID
                    OutlinedTextField(
                        value = uiState.nationalId,
                        onValueChange = viewModel::onNationalIdChange,
                        label = { Text("الرقم القومي (اختياري)") },
                        leadingIcon = { Icon(Icons.Filled.Badge, null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        maxLines = 1
                    )
                }
            }

            // Job Info Section
            Text(
                text = "بيانات العمل",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )

            GlassmorphicCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Dropdown
                    CategoryDropdown(
                        categories = viewModel.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = viewModel::onCategorySelected
                    )

                    // Skill Level Chips
                    Text(
                        text = "مستوى المهارة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkillLevelChip(
                            level = SkillLevel.HELPER,
                            selected = uiState.selectedSkillLevel == SkillLevel.HELPER,
                            onClick = { viewModel.onSkillLevelSelected(SkillLevel.HELPER) },
                            label = "عامل"
                        )
                        SkillLevelChip(
                            level = SkillLevel.SKILLED,
                            selected = uiState.selectedSkillLevel == SkillLevel.SKILLED,
                            onClick = { viewModel.onSkillLevelSelected(SkillLevel.SKILLED) },
                            label = "صنايعي"
                        )
                        SkillLevelChip(
                            level = SkillLevel.MASTER,
                            selected = uiState.selectedSkillLevel == SkillLevel.MASTER,
                            onClick = { viewModel.onSkillLevelSelected(SkillLevel.MASTER) },
                            label = "أسطى"
                        )
                    }
                    
                    // Daily Rate
                    OutlinedTextField(
                        value = uiState.dailyRate,
                        onValueChange = viewModel::onDailyRateChange,
                        label = { Text("اليومية (جم)") },
                        leadingIcon = { Icon(Icons.Filled.AttachMoney, null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.dailyRateError != null,
                        supportingText = { uiState.dailyRateError?.let { Text(it) } },
                        singleLine = true
                    )
                    
                    // Notes
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::onNotesChange,
                        label = { Text("ملاحظات إضافية") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<WorkerCategory>,
    selectedCategory: WorkerCategory?,
    onCategorySelected: (WorkerCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "اختر الفئة/المهنة",
            onValueChange = {},
            readOnly = true,
            label = { Text("المهنة") },
            leadingIcon = { Icon(Icons.Filled.Work, null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = category.name) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillLevelChip(
    level: SkillLevel,
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppColors.Primary.copy(alpha = 0.2f),
            selectedLabelColor = AppColors.Primary
        )
    )
}
