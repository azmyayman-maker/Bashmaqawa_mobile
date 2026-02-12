package com.bashmaqawa.presentation.screens.attendance

import com.bashmaqawa.data.database.entities.AttendanceStatus
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.Worker

/**
 * UI State for Record Attendance Screen
 * حالة واجهة مستخدم تسجيل الحضور
 */
data class RecordAttendanceUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val date: String = "", // Current selected date (YYYY-MM-DD)
    val formattedDate: String = "", // Display date (e.g. 15 Jan 2024)
    val projects: List<Project> = emptyList(),
    val selectedProjectId: Int? = null,
    val workers: List<WorkerAttendanceItem> = emptyList(),
    val searchQuery: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
) {
    val totalPresent: Int
        get() = workers.count { it.status == AttendanceStatus.PRESENT }
        
    val totalAbsent: Int
        get() = workers.count { it.status == AttendanceStatus.ABSENT }
        
    val totalLate: Int
        get() = workers.count { it.status == AttendanceStatus.HALF_DAY } // Using HALF_DAY as Late/Half
}

/**
 * Worker Item for Attendance List
 * عنصر العامل في قائمة الحضور
 */
data class WorkerAttendanceItem(
    val worker: Worker,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val isSelected: Boolean = true, // If false, attendance won't be recorded for this worker
    val notes: String? = null
)
