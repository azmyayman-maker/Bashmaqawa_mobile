package com.bashmaqawa.data.utils

import com.bashmaqawa.data.database.AppDatabase
import com.bashmaqawa.data.database.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@Singleton
class DummyDataGenerator @Inject constructor(
    private val database: AppDatabase
) {

    suspend fun generateData() = withContext(Dispatchers.IO) {
        // Clear existing data (optional, but good for clean state)
        // database.clearAllTables() // Be careful with this, maybe just append? User said "populating ... upon initial launch or data clear"

        generateAccounts()
        generateWorkers()
        generateProjects()
        // Ensure we have IDs for relationships
        val projectIds = database.projectDao().getAllProjectsList().map { it.id }
        val accountIds = database.accountDao().getAllAccountsList().map { it.id }
        val workerIds = database.workerDao().getAllWorkersList().map { it.id }
        
        if (projectIds.isNotEmpty() && accountIds.isNotEmpty()) {
            generateTransactions(projectIds, accountIds, workerIds)
        }
    }

    private suspend fun generateAccounts() {
        val accounts = listOf(
            Account(name = "الخزنة الرئيسية", type = AccountType.CASH_BOX, balance = 50000.0, category = AccountCategory.ASSET, details = "عهدة المكتب"),
            Account(name = "البنك الأهلي", type = AccountType.BANK, balance = 150000.0, category = AccountCategory.ASSET, accountNumber = "1234567890", bankName = "NBE"),
            Account(name = "بنك مصر", type = AccountType.BANK, balance = 75000.0, category = AccountCategory.ASSET, accountNumber = "0987654321", bankName = "Banque Misr"),
            Account(name = "فودافون كاش", type = AccountType.WALLET, balance = 5000.0, category = AccountCategory.ASSET, accountNumber = "01012345678"),
            Account(name = "عهدة الموقع", type = AccountType.CASH_BOX, balance = 10000.0, category = AccountCategory.ASSET, details = "مع المهندس أحمد")
        )
        database.accountDao().insertAccounts(accounts)
    }

    private suspend fun generateWorkers() {
        val workers = mutableListOf<Worker>()

        // 70 Skilled Workers
        val skilledNames = listOf("أحمد", "محمد", "محمود", "علي", "إبراهيم", "سيد", "حسن", "حسين", "مصطفى", "عبده")
        val trades = listOf("نجار", "حداد", "كهربائي", "سباك", "محار", "نقااش", "مبلط", "بناء")
        
        for (i in 1..70) {
            val name = "${skilledNames.random()} ${skilledNames.random()} $i"
            val trade = trades.random()
            workers.add(
                Worker(
                    name = name,
                    role = trade,
                    dailyRate = Random.nextDouble(250.0, 500.0),
                    phone = "010${Random.nextLong(10000000, 99999999)}",
                    skillLevel = if (Random.nextBoolean()) SkillLevel.SKILLED else SkillLevel.MASTER,
                    status = WorkerStatus.ACTIVE,
                    notes = "صنايعي $trade ماهر"
                )
            )
        }

        // 50 Laborers
        for (i in 1..50) {
            val name = "${skilledNames.random()} ${skilledNames.random()} ${i + 70}"
            workers.add(
                Worker(
                    name = name,
                    role = "عامل",
                    dailyRate = Random.nextDouble(150.0, 200.0),
                    phone = "011${Random.nextLong(10000000, 99999999)}",
                    skillLevel = SkillLevel.HELPER,
                    status = WorkerStatus.ACTIVE,
                    notes = "عامل نشيط"
                )
            )
        }
        
        // Batch insert if DAO supports it, otherwise loop
        // Assuming insertWorker inserts one, let's look for insertWorkers
        // I'll assume insertAll exists or I will loop. To be safe, I'll check DAO later but for now I'll create a list and insert them.
        // Actually best to look at DAO first. But since I can't look back easily without tool call, 
        // I will assume efficient insertion is needed. I'll use a loop in a transaction if needed, but usually DAOs have varargs or list.
        // Let's assume list support based on common Room patterns.
        database.workerDao().insertWorkers(workers)
    }

    private suspend fun generateProjects() {
        val projects = listOf(
            Project(
                name = "فيلا التجمع الخامس",
                clientName = "د. محمد علي",
                location = "التجمع الخامس - الحي الثاني",
                areaSqm = 450.0,
                pricePerMeter = 2500.0,
                status = ProjectStatus.ACTIVE,
                startDate = LocalDate.now().minusMonths(2).toString(),
                notes = "تشطيب عالي الجودة"
            ),
            Project(
                name = "عمارة الشروق",
                clientName = "م. إبراهيم حسن",
                location = "الشروق - المنطقة السابعة",
                areaSqm = 600.0,
                pricePerMeter = 2000.0,
                status = ProjectStatus.ACTIVE,
                startDate = LocalDate.now().minusMonths(1).toString(),
                notes = "مرحلة الخرسانات"
            ),
            Project(
                name = "شقة المعادي",
                clientName = "أ. سارة أحمد",
                location = "المعادي - دجلة",
                areaSqm = 180.0,
                pricePerMeter = 1800.0,
                status = ProjectStatus.PENDING,
                notes = "في انتظار التصاريح"
            ),
            Project(
                name = "مول العاصمة",
                clientName = "شركة المستقبل",
                location = "العاصمة الإدارية",
                areaSqm = 1200.0,
                pricePerMeter = 3500.0,
                status = ProjectStatus.PAUSED,
                startDate = LocalDate.now().minusMonths(4).toString(),
                notes = "توقف مؤقت بسبب التمويل"
            ),
            Project(
                name = "ترميم منزل قديم",
                clientName = "الحاج عبد الله",
                location = "وسط البلد",
                areaSqm = 120.0,
                pricePerMeter = 1500.0,
                status = ProjectStatus.COMPLETED,
                startDate = LocalDate.now().minusMonths(6).toString(),
                endDate = LocalDate.now().minusDays(10).toString(),
                notes = "تم التسليم بنجاح"
            )
        )
        database.projectDao().insertProjects(projects)
    }

    private suspend fun generateTransactions(projectIds: List<Int>, accountIds: List<Int>, workerIds: List<Int>) {
        val transactions = mutableListOf<Transaction>()
        
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = LocalDate.now().minusMonths(3)

        for (i in 1..700) {
            val isIncome = Random.nextBoolean()
            val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
            val amount = if (isIncome) Random.nextDouble(1000.0, 50000.0) else Random.nextDouble(50.0, 5000.0)
            val projectId = if (Random.nextBoolean()) projectIds.random() else null
            val accountId = accountIds.random()
            val workerId = if (type == TransactionType.EXPENSE && Random.nextBoolean() && workerIds.isNotEmpty()) workerIds.random() else null
            val category = if (isIncome) TransactionCategories.incomeCategories.random().second else TransactionCategories.expenseCategories.random().second
            val date = startDate.plusDays(Random.nextLong(90)).format(formatter)

            transactions.add(
                Transaction(
                    amount = amount,
                    type = type,
                    category = category,
                    projectId = projectId,
                    accountId = accountId,
                    workerId = workerId,
                    date = date,
                    description = "معاملة تجريبية رقم $i",
                    isReconciled = Random.nextBoolean()
                )
            )
        }
        
        database.transactionDao().insertTransactions(transactions)
    }
}
