package com.bashmaqawa.presentation.screens.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bashmaqawa.presentation.components.InfoCard
import com.bashmaqawa.presentation.components.PrimaryButton
import com.bashmaqawa.presentation.components.SecondaryButton
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.presentation.viewmodel.BackupUiState
import com.bashmaqawa.presentation.viewmodel.BackupViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lastBackupTimestamp by viewModel.lastBackupTimestamp.collectAsState()
    
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // SAF Launchers
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { viewModel.createBackup(it) }
    }
    
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreBackup(it) }
    }
    
    // Notifications and Restart Logic
    var showRestartDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is BackupUiState.Success -> {
                if (state.message.contains("استعادة")) {
                     // Trigger restart dialog
                     successMessage = state.message
                     showRestartDialog = true
                } else {
                    snackbarHostState.showSnackbar(state.message)
                }
                viewModel.resetState()
            }
            is BackupUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = {}, // Non-dismissible
            title = { Text("تمت العملية بنجاح") },
            text = { Text("تم استعادة النسخة الاحتياطية بنجاح. يجب إعادة تشغيل التطبيق لتطبيق التغييرات.") },
            confirmButton = {
                Button(
                    onClick = {
                        com.bashmaqawa.utils.LocaleHelper.restartApp(context)
                    }
                ) {
                    Text("إعادة التشغيل الآن")
                }
            }
        )
    }
    
    // Restore Warning Dialog
    var showRestoreWarning by remember { mutableStateOf(false) }
    
    if (showRestoreWarning) {
        AlertDialog(
            onDismissRequest = { showRestoreWarning = false },
            title = { Text("تحذير: استعادة النسخة الاحتياطية") },
            text = { Text("ستقوم هذه العملية بحذف جميع البيانات الحالية واستبدالها بالبيانات من النسخة الاحتياطية. هل أنت متأكد؟") },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreWarning = false
                        restoreBackupLauncher.launch(arrayOf("application/zip"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("استعادة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreWarning = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("النسخ الاحتياطي والاستعادة") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // Last Backup Info
                val dateString = if (lastBackupTimestamp > 0) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale("ar"))
                    sdf.format(Date(lastBackupTimestamp))
                } else {
                    "لا توجد نسخ احتياطية سابقة"
                }

                InfoCard(
                    title = "آخر نسخة احتياطية",
                    value = dateString,
                    icon = Icons.Default.CheckCircle,
                    gradient = AppColors.PrimaryGradient
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Actions
                PrimaryButton(
                    text = "إنشاء نسخة احتياطية",
                    onClick = {
                         val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                         val fileName = "Bashmaqawa_Backup_${sdf.format(Date())}.zip"
                         createBackupLauncher.launch(fileName)
                    },
                    icon = Icons.Default.Backup,
                    isLoading = uiState is BackupUiState.Loading && (uiState as? BackupUiState.Loading) != null // Logic check
                )

                SecondaryButton(
                    text = "استعادة نسخة احتياطية",
                    onClick = { showRestoreWarning = true },
                    icon = Icons.Default.Restore
                )
                
                if (uiState is BackupUiState.Loading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "جاري العمل...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
