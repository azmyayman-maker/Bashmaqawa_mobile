package com.bashmaqawa.presentation.navigation

/**
 * Navigation routes for Bashmaqawa app
 * مسارات التنقل في التطبيق
 */
sealed class Screen(val route: String) {
    // Auth
    data object Login : Screen("login")
    data object Setup : Screen("setup")
    
    // Main Screens
    data object Home : Screen("home")
    data object Calendar : Screen("calendar")
    data object Workforce : Screen("workforce")
    data object Projects : Screen("projects")
    data object Financial : Screen("financial")
    data object Analytics : Screen("analytics")
    data object AIChat : Screen("ai_chat")
    data object Settings : Screen("settings")
    
    // Detail Screens
    data object WorkerDetail : Screen("worker_detail/{workerId}") {
        fun createRoute(workerId: Int) = "worker_detail/$workerId"
    }
    
    data object ProjectDetail : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: Int) = "project_detail/$projectId"
    }
    
    data object AccountDetail : Screen("account_detail/{accountId}") {
        fun createRoute(accountId: Int) = "account_detail/$accountId"
    }
    
    data object AddWorker : Screen("add_worker")
    data object AddProject : Screen("add_project")
    data object AddTransaction : Screen("add_transaction")
    data object RecordAttendance : Screen("record_attendance")
    data object Backup : Screen("backup")
    data object Reports : Screen("reports")
    data object BankStatement : Screen("bank_statement")
    
    // Bottom nav items
    companion object {
        val bottomNavItems = listOf(Home, Calendar, Workforce, Projects, Financial)
    }
}
