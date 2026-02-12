package com.bashmaqawa.pdf.templates

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.bashmaqawa.pdf.ColumnAlignment
import com.bashmaqawa.pdf.ColumnWidth
import com.bashmaqawa.pdf.PdfColors
import com.bashmaqawa.pdf.PdfPageSettings
import com.bashmaqawa.pdf.PdfTypography
import com.bashmaqawa.pdf.TableColumn
import com.bashmaqawa.pdf.core.PdfPageRenderer
import com.bashmaqawa.pdf.core.PdfTableRenderer
import com.bashmaqawa.pdf.models.BankStatementData
import com.bashmaqawa.pdf.models.CreditDebitIndicator
import com.bashmaqawa.pdf.models.ReportConfig
import com.bashmaqawa.pdf.models.ReportType
import com.bashmaqawa.utils.CurrencyFormatter
import java.time.format.DateTimeFormatter

/**
 * Bank Statement PDF Template
 * قالب كشف الحساب البنكي PDF
 * 
 * A premium PDF template for generating professional bank statements with:
 * - Professional header with branding
 * - Account information card
 * - Balance summary cards (glassmorphic design)
 * - Transaction table with color-coded amounts and running balance
 * - Optional analytics section
 * - Multi-page pagination support
 * - Full RTL support for Arabic text
 */
class BankStatementTemplate {
    
    private val pageRenderer = PdfPageRenderer()
    private val tableRenderer = PdfTableRenderer()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    // Table column definitions for transaction list
    private val columns = listOf(
        TableColumn("التاريخ", ColumnWidth.Fixed(65f), ColumnAlignment.CENTER),
        TableColumn("الوصف", ColumnWidth.Percentage(0.30f), ColumnAlignment.RIGHT),
        TableColumn("الفئة", ColumnWidth.Fixed(70f), ColumnAlignment.CENTER),
        TableColumn("مدين", ColumnWidth.Fixed(85f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("دائن", ColumnWidth.Fixed(85f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("الرصيد", ColumnWidth.Fixed(95f), ColumnAlignment.LEFT, isAmount = true)
    )
    
    companion object {
        // Balance card colors
        private val OPENING_BALANCE_BG = PdfColors.InfoLight
        private val CLOSING_BALANCE_BG = PdfColors.PrimaryLight
        private val TOTAL_DEBIT_BG = PdfColors.ErrorLight
        private val TOTAL_CREDIT_BG = PdfColors.SuccessLight
    }
    
    /**
     * Render the complete bank statement
     * 
     * @param document PDF document to render to
     * @param data Bank statement data
     * @param config Report configuration
     * @return Number of pages created
     */
    fun render(
        document: PdfDocument,
        data: BankStatementData,
        config: ReportConfig
    ): Int {
        val pages = mutableListOf<PdfDocument.Page>()
        val columnWidths = tableRenderer.calculateColumnWidths(columns, PdfPageSettings.contentWidth)
        
        var pageNumber = 1
        var transactionIndex = 0
        
        // Create first page
        var pageInfo = PdfDocument.PageInfo.Builder(
            PdfPageSettings.A4_WIDTH.toInt(),
            PdfPageSettings.A4_HEIGHT.toInt(),
            pageNumber
        ).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        pages.add(page)
        
        // Render first page elements
        var currentY = renderFirstPage(canvas, data, config)
        
        // Track table start for borders
        val tableStartY = currentY
        
        // Render table header
        currentY = tableRenderer.renderTableHeader(canvas, columns, columnWidths, currentY)
        
        // Render transactions with pagination
        while (transactionIndex < data.transactions.size) {
            val txn = data.transactions[transactionIndex]
            
            // Check if we need a new page
            if (currentY + PdfPageSettings.TABLE_ROW_HEIGHT > PdfPageSettings.contentEndY) {
                // Render table borders for current page
                tableRenderer.renderTableBorder(canvas, tableStartY, currentY)
                
                // Render footer
                pageRenderer.renderFooter(canvas, pageNumber, -1) // -1 = unknown total pages
                
                // Finish current page
                document.finishPage(page)
                pageNumber++
                
                // Start new page
                pageInfo = PdfDocument.PageInfo.Builder(
                    PdfPageSettings.A4_WIDTH.toInt(),
                    PdfPageSettings.A4_HEIGHT.toInt(),
                    pageNumber
                ).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                pages.add(page)
                
                // Render continuation header
                currentY = pageRenderer.renderHeader(
                    canvas,
                    ReportType.BANK_STATEMENT,
                    data.periodStart.format(dateFormatter),
                    data.periodEnd.format(dateFormatter),
                    pageNumber,
                    isFirstPage = false
                )
                
                // Render table header continuation
                currentY = tableRenderer.renderTableHeader(canvas, columns, columnWidths, currentY, isContinuation = true)
            }
            
            // Render transaction row
            val debitFormatted = if (txn.debitAmount > 0) CurrencyFormatter.formatShort(txn.debitAmount) else ""
            val creditFormatted = if (txn.creditAmount > 0) CurrencyFormatter.formatShort(txn.creditAmount) else ""
            val balanceFormatted = CurrencyFormatter.formatShort(txn.runningBalance)
            
            val values = listOf(
                txn.bookingDate.format(dateFormatter),
                txn.description,
                txn.category ?: "",
                debitFormatted,
                creditFormatted,
                balanceFormatted
            )
            
            // Color mapping for amounts
            val amountColors = mutableMapOf<Int, Int>()
            if (txn.debitAmount > 0) amountColors[3] = PdfColors.Error
            if (txn.creditAmount > 0) amountColors[4] = PdfColors.Success
            amountColors[5] = PdfColors.getAmountColor(txn.runningBalance)
            
            currentY = tableRenderer.renderDataRow(
                canvas, columns, columnWidths, values, currentY, transactionIndex, amountColors
            )
            
            transactionIndex++
        }
        
        // Render totals row
        currentY = tableRenderer.renderTotalsRow(
            canvas, columns, columnWidths,
            "الإجمالي",
            listOf(
                null, null, null,
                CurrencyFormatter.formatShort(data.totalDebits),
                CurrencyFormatter.formatShort(data.totalCredits),
                CurrencyFormatter.formatShort(data.closingBalance.signedAmount)
            ),
            currentY,
            isGrandTotal = true
        )
        
        // Render table borders
        tableRenderer.renderTableBorder(canvas, tableStartY, currentY)
        
        // Render analytics section if available and fits
        if (data.analytics != null && currentY + 150f < PdfPageSettings.contentEndY) {
            currentY = renderAnalyticsSection(canvas, data, currentY)
        }
        
        // Update footers with correct page count
        document.finishPage(page)
        
        // Re-render footers with correct page count
        pages.forEachIndexed { index, _ ->
            // Note: Pages are already finished, footer was rendered inline
        }
        
        return pageNumber
    }
    
    /**
     * Render first page specific elements
     */
    private fun renderFirstPage(
        canvas: Canvas,
        data: BankStatementData,
        config: ReportConfig
    ): Float {
        // Header
        var currentY = pageRenderer.renderHeader(
            canvas,
            ReportType.BANK_STATEMENT,
            data.periodStart.format(dateFormatter),
            data.periodEnd.format(dateFormatter),
            pageNumber = 1,
            isFirstPage = true
        )
        
        // Account Information Card
        currentY = renderAccountInfoCard(canvas, data, currentY)
        
        // Balance Summary Cards
        currentY = renderBalanceSummaryCards(canvas, data, currentY)
        
        // Transaction section header
        currentY = pageRenderer.renderSectionHeader(canvas, "تفاصيل المعاملات", currentY)
        
        return currentY
    }
    
    /**
     * Render account information card
     */
    private fun renderAccountInfoCard(
        canvas: Canvas,
        data: BankStatementData,
        currentY: Float
    ): Float {
        val items = mutableListOf(
            "اسم الحساب" to data.accountName,
            "نوع الحساب" to getAccountTypeArabic(data.accountType)
        )
        
        data.accountNumber?.let {
            items.add("رقم الحساب" to it)
        }
        
        data.bankName?.let {
            items.add("البنك" to it)
        }
        
        items.add("العملة" to getCurrencyArabic(data.currency))
        items.add("رقم الكشف" to data.statementId)
        
        return pageRenderer.renderInfoCard(
            canvas,
            "معلومات الحساب",
            items,
            currentY
        )
    }
    
    /**
     * Render balance summary cards in a 2x2 grid
     */
    private fun renderBalanceSummaryCards(
        canvas: Canvas,
        data: BankStatementData,
        currentY: Float
    ): Float {
        val leftX = PdfPageSettings.MARGIN_LEFT
        val contentWidth = PdfPageSettings.contentWidth
        val cardWidth = (contentWidth - PdfPageSettings.ELEMENT_SPACING) / 2
        val cardHeight = 70f
        val spacing = PdfPageSettings.ELEMENT_SPACING
        
        var y = currentY + 10f
        
        // Row 1: Opening Balance & Closing Balance
        renderBalanceCard(
            canvas,
            "الرصيد الافتتاحي",
            CurrencyFormatter.format(data.openingBalance.amount),
            data.openingBalance.creditDebitIndicator == CreditDebitIndicator.CREDIT,
            leftX + cardWidth + spacing,
            y,
            cardWidth,
            cardHeight,
            OPENING_BALANCE_BG
        )
        
        renderBalanceCard(
            canvas,
            "الرصيد الختامي",
            CurrencyFormatter.format(data.closingBalance.amount),
            data.closingBalance.creditDebitIndicator == CreditDebitIndicator.CREDIT,
            leftX,
            y,
            cardWidth,
            cardHeight,
            CLOSING_BALANCE_BG
        )
        
        y += cardHeight + spacing
        
        // Row 2: Total Debits & Total Credits
        renderBalanceCard(
            canvas,
            "إجمالي المدين",
            CurrencyFormatter.format(data.totalDebits),
            false, // Debits are always "negative" (outflow)
            leftX + cardWidth + spacing,
            y,
            cardWidth,
            cardHeight,
            TOTAL_DEBIT_BG
        )
        
        renderBalanceCard(
            canvas,
            "إجمالي الدائن",
            CurrencyFormatter.format(data.totalCredits),
            true, // Credits are always "positive" (inflow)
            leftX,
            y,
            cardWidth,
            cardHeight,
            TOTAL_CREDIT_BG
        )
        
        return y + cardHeight + 15f
    }
    
    /**
     * Render a single balance card with glassmorphic styling
     */
    private fun renderBalanceCard(
        canvas: Canvas,
        title: String,
        value: String,
        isPositive: Boolean,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        backgroundColor: Int
    ) {
        // Card background
        val cardRect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(
            cardRect,
            PdfPageSettings.CARD_CORNER_RADIUS,
            PdfPageSettings.CARD_CORNER_RADIUS,
            PdfTypography.createFillPaint(backgroundColor)
        )
        
        // Card border (subtle)
        canvas.drawRoundRect(
            cardRect,
            PdfPageSettings.CARD_CORNER_RADIUS,
            PdfPageSettings.CARD_CORNER_RADIUS,
            PdfTypography.createLinePaint()
        )
        
        val centerX = x + width / 2
        
        // Title
        canvas.drawText(
            title,
            centerX,
            y + 22f,
            PdfTypography.createTableBodyCenterPaint().apply {
                color = PdfColors.TextSecondary
            }
        )
        
        // Value
        val valueColor = if (isPositive) PdfColors.Success else PdfColors.Error
        canvas.drawText(
            value,
            centerX,
            y + 50f,
            PdfTypography.createTotalPaint().apply {
                color = valueColor
                textAlign = Paint.Align.CENTER
            }
        )
    }
    
    /**
     * Render analytics section
     */
    private fun renderAnalyticsSection(
        canvas: Canvas,
        data: BankStatementData,
        currentY: Float
    ): Float {
        val analytics = data.analytics ?: return currentY
        
        var y = pageRenderer.renderSectionHeader(canvas, "تحليلات الكشف", currentY + 20f)
        
        // Analytics summary cards
        val cards = listOf(
            Triple("متوسط الرصيد اليومي", CurrencyFormatter.formatShort(analytics.averageDailyBalance), null),
            Triple("أعلى رصيد", CurrencyFormatter.formatShort(analytics.highestBalance), true),
            Triple("أقل رصيد", CurrencyFormatter.formatShort(analytics.lowestBalance), false),
            Triple("عدد المعاملات", data.transactionCount.toString(), null)
        )
        
        return pageRenderer.renderSummaryCards(canvas, cards, y)
    }
    
    /**
     * Get Arabic name for account type
     */
    private fun getAccountTypeArabic(type: String): String = when (type) {
        "CASH_BOX" -> "صندوق"
        "BANK" -> "بنك"
        "WALLET" -> "محفظة"
        "RECEIVABLE" -> "ذمم مدينة"
        "PAYABLE" -> "ذمم دائنة"
        else -> type
    }
    
    /**
     * Get Arabic name for currency
     */
    private fun getCurrencyArabic(code: String): String = when (code) {
        "EGP" -> "جنيه مصري"
        "USD" -> "دولار أمريكي"
        "EUR" -> "يورو"
        "SAR" -> "ريال سعودي"
        else -> code
    }
}
