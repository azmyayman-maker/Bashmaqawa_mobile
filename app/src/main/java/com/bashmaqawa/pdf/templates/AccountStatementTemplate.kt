package com.bashmaqawa.pdf.templates

import android.graphics.pdf.PdfDocument
import com.bashmaqawa.pdf.ColumnAlignment
import com.bashmaqawa.pdf.ColumnWidth
import com.bashmaqawa.pdf.PdfPageSettings
import com.bashmaqawa.pdf.TableColumn
import com.bashmaqawa.pdf.core.PdfPageRenderer
import com.bashmaqawa.pdf.core.PdfTableRenderer
import com.bashmaqawa.pdf.models.AccountStatementData
import com.bashmaqawa.pdf.models.ReportConfig
import com.bashmaqawa.pdf.models.ReportType
import com.bashmaqawa.pdf.PdfColors
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Account Statement Template
 * قالب كشف الحساب
 * 
 * Generates account statement PDF with:
 * - Account information card
 * - Transaction history table
 * - Opening/closing balance
 */
class AccountStatementTemplate(
    private val pageRenderer: PdfPageRenderer,
    private val tableRenderer: PdfTableRenderer
) {
    
    private val columns = listOf(
        TableColumn("التاريخ", ColumnWidth.Fixed(65f), ColumnAlignment.CENTER),
        TableColumn("الوصف", ColumnWidth.Percentage(0.30f), ColumnAlignment.RIGHT),
        TableColumn("الفئة", ColumnWidth.Fixed(70f), ColumnAlignment.CENTER),
        TableColumn("مدين", ColumnWidth.Fixed(85f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("دائن", ColumnWidth.Fixed(85f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("الرصيد", ColumnWidth.Fixed(95f), ColumnAlignment.LEFT, isAmount = true)
    )
    
    /**
     * Render the account statement report
     * @return Number of pages generated
     */
    fun render(
        document: PdfDocument,
        data: AccountStatementData,
        config: ReportConfig
    ): Int {
        val columnWidths = tableRenderer.calculateColumnWidths(columns, PdfPageSettings.contentWidth)
        
        // Calculate rows per page
        val firstPageInfoCardHeight = 120f // Height for account info card
        val rowsFirstPage = PdfPageSettings.calculateRowsPerPage(true, firstPageInfoCardHeight)
        val rowsPerPage = PdfPageSettings.calculateRowsPerPage(false)
        
        // Calculate total pages needed
        val totalRows = data.transactions.size
        val totalPages = if (totalRows <= rowsFirstPage) 1
        else 1 + ((totalRows - rowsFirstPage + rowsPerPage - 1) / rowsPerPage)
        
        var currentRowIndex = 0
        
        for (pageNum in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(
                PdfPageSettings.A4_WIDTH,
                PdfPageSettings.A4_HEIGHT,
                pageNum
            ).create()
            
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            // Render header
            var currentY = pageRenderer.renderHeader(
                canvas = canvas,
                reportType = ReportType.ACCOUNT_STATEMENT,
                periodStart = config.dateRange?.startDateString,
                periodEnd = config.dateRange?.endDateString,
                pageNumber = pageNum,
                isFirstPage = pageNum == 1
            )
            
            // First page: render account info card
            if (pageNum == 1) {
                currentY = pageRenderer.renderInfoCard(
                    canvas = canvas,
                    title = "معلومات الحساب",
                    items = buildList {
                        add("اسم الحساب" to data.accountName)
                        data.accountCode?.let { add("رمز الحساب" to it) }
                        data.accountType?.let { add("نوع الحساب" to it) }
                        data.bankName?.let { add("البنك" to it) }
                        data.accountNumber?.let { add("رقم الحساب" to it) }
                        add("الرصيد الافتتاحي" to CurrencyFormatter.format(data.openingBalance))
                        add("الرصيد الختامي" to CurrencyFormatter.format(data.closingBalance))
                    },
                    currentY = currentY
                )
                currentY += 10f
                
                // Section header
                currentY = pageRenderer.renderSectionHeader(canvas, "حركات الحساب", currentY)
            }
            
            // Calculate rows for this page
            val rowsThisPage = if (pageNum == 1) rowsFirstPage else rowsPerPage
            val endRowIndex = minOf(currentRowIndex + rowsThisPage, totalRows)
            
            // Render table header
            val tableStartY = currentY
            currentY = tableRenderer.renderTableHeader(
                canvas = canvas,
                columns = columns,
                columnWidths = columnWidths,
                currentY = currentY,
                isContinuation = pageNum > 1
            )
            
            // Render rows
            var rowInPage = 0
            while (currentRowIndex < endRowIndex) {
                val transaction = data.transactions[currentRowIndex]
                
                val values = listOf(
                    transaction.date,
                    transaction.description,
                    transaction.category ?: "-",
                    if (transaction.debit > 0) CurrencyFormatter.format(transaction.debit) else "-",
                    if (transaction.credit > 0) CurrencyFormatter.format(transaction.credit) else "-",
                    CurrencyFormatter.format(transaction.balance)
                )
                
                val amountColors = buildMap {
                    if (transaction.debit > 0) put(3, PdfColors.Error)
                    if (transaction.credit > 0) put(4, PdfColors.Success)
                    put(5, PdfColors.getAmountColor(transaction.balance))
                }
                
                currentY = tableRenderer.renderDataRow(
                    canvas = canvas,
                    columns = columns,
                    columnWidths = columnWidths,
                    values = values,
                    currentY = currentY,
                    rowIndex = rowInPage,
                    amountColors = amountColors
                )
                
                currentRowIndex++
                rowInPage++
            }
            
            // Last page: render totals
            if (pageNum == totalPages) {
                currentY = tableRenderer.renderTotalsRow(
                    canvas = canvas,
                    columns = columns,
                    columnWidths = columnWidths,
                    label = "المجموع",
                    values = listOf(
                        null, null, null,
                        CurrencyFormatter.format(data.totalDebits),
                        CurrencyFormatter.format(data.totalCredits),
                        null
                    ),
                    currentY = currentY
                )
                
                currentY = tableRenderer.renderTotalsRow(
                    canvas = canvas,
                    columns = columns,
                    columnWidths = columnWidths,
                    label = "الرصيد الختامي",
                    values = listOf(
                        null, null, null, null, null,
                        CurrencyFormatter.format(data.closingBalance)
                    ),
                    currentY = currentY,
                    isGrandTotal = true
                )
            }
            
            // Table borders
            tableRenderer.renderTableBorder(canvas, tableStartY, currentY)
            
            // Footer
            pageRenderer.renderFooter(canvas, pageNum, totalPages)
            
            document.finishPage(page)
        }
        
        return totalPages
    }
}
