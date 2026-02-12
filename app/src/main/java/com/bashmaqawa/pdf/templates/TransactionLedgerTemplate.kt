package com.bashmaqawa.pdf.templates

import android.graphics.pdf.PdfDocument
import com.bashmaqawa.pdf.ColumnAlignment
import com.bashmaqawa.pdf.ColumnWidth
import com.bashmaqawa.pdf.PdfColors
import com.bashmaqawa.pdf.PdfPageSettings
import com.bashmaqawa.pdf.TableColumn
import com.bashmaqawa.pdf.core.PdfPageRenderer
import com.bashmaqawa.pdf.core.PdfTableRenderer
import com.bashmaqawa.pdf.models.ReportConfig
import com.bashmaqawa.pdf.models.ReportType
import com.bashmaqawa.pdf.models.TransactionRow
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Transaction Ledger Template
 * قالب سجل المعاملات
 * 
 * Generates transaction ledger with:
 * - Filterable transaction list
 * - Multi-page pagination
 * - Summary totals
 */
class TransactionLedgerTemplate(
    private val pageRenderer: PdfPageRenderer,
    private val tableRenderer: PdfTableRenderer
) {
    
    private val columns = listOf(
        TableColumn("التاريخ", ColumnWidth.Fixed(60f), ColumnAlignment.CENTER),
        TableColumn("الرقم المرجعي", ColumnWidth.Fixed(65f), ColumnAlignment.CENTER),
        TableColumn("الوصف", ColumnWidth.Percentage(0.28f), ColumnAlignment.RIGHT),
        TableColumn("النوع", ColumnWidth.Fixed(55f), ColumnAlignment.CENTER),
        TableColumn("مدين", ColumnWidth.Fixed(80f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("دائن", ColumnWidth.Fixed(80f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("الحالة", ColumnWidth.Fixed(55f), ColumnAlignment.CENTER)
    )
    
    /**
     * Render the transaction ledger report
     * @return Number of pages generated
     */
    fun render(
        document: PdfDocument,
        transactions: List<TransactionRow>,
        periodStart: String,
        periodEnd: String,
        config: ReportConfig
    ): Int {
        val columnWidths = tableRenderer.calculateColumnWidths(columns, PdfPageSettings.contentWidth)
        
        // Calculate pagination
        val rowsFirstPage = PdfPageSettings.calculateRowsPerPage(true, 0f)
        val rowsPerPage = PdfPageSettings.calculateRowsPerPage(false)
        
        val totalRows = transactions.size
        val totalPages = if (totalRows <= rowsFirstPage) 1
        else 1 + ((totalRows - rowsFirstPage + rowsPerPage - 1) / rowsPerPage)
        
        // Calculate totals
        var totalDebits = 0.0
        var totalCredits = 0.0
        transactions.forEach { 
            totalDebits += it.debit
            totalCredits += it.credit
        }
        
        var currentRowIndex = 0
        
        for (pageNum in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(
                PdfPageSettings.A4_WIDTH,
                PdfPageSettings.A4_HEIGHT,
                pageNum
            ).create()
            
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            
            // Header
            var currentY = pageRenderer.renderHeader(
                canvas = canvas,
                reportType = ReportType.TRANSACTION_LEDGER,
                periodStart = periodStart,
                periodEnd = periodEnd,
                pageNumber = pageNum,
                isFirstPage = pageNum == 1
            )
            
            // Calculate rows for this page
            val rowsThisPage = if (pageNum == 1) rowsFirstPage else rowsPerPage
            val endRowIndex = minOf(currentRowIndex + rowsThisPage, totalRows)
            
            // Table header
            val tableStartY = currentY
            currentY = tableRenderer.renderTableHeader(
                canvas = canvas,
                columns = columns,
                columnWidths = columnWidths,
                currentY = currentY,
                isContinuation = pageNum > 1
            )
            
            // Rows
            var rowInPage = 0
            while (currentRowIndex < endRowIndex) {
                val tx = transactions[currentRowIndex]
                
                val typeLabel = when (tx.type) {
                    "INCOME" -> "إيراد"
                    "EXPENSE" -> "مصروف"
                    "TRANSFER" -> "تحويل"
                    else -> tx.type
                }
                
                val stateLabel = when (tx.state) {
                    "CLEARED" -> "مقاصة"
                    "PENDING" -> "معلقة"
                    "VOID" -> "ملغاة"
                    else -> tx.state
                }
                
                val values = listOf(
                    tx.date,
                    tx.referenceNumber ?: "-",
                    tx.description,
                    typeLabel,
                    if (tx.debit > 0) CurrencyFormatter.format(tx.debit) else "-",
                    if (tx.credit > 0) CurrencyFormatter.format(tx.credit) else "-",
                    stateLabel
                )
                
                val amountColors = buildMap {
                    if (tx.debit > 0) put(4, PdfColors.Error)
                    if (tx.credit > 0) put(5, PdfColors.Success)
                    
                    // State color
                    when (tx.state) {
                        "VOID" -> put(6, PdfColors.TextMuted)
                        "PENDING" -> put(6, PdfColors.Warning)
                    }
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
            
            // Last page: totals
            if (pageNum == totalPages) {
                currentY = tableRenderer.renderTotalsRow(
                    canvas = canvas,
                    columns = columns,
                    columnWidths = columnWidths,
                    label = "المجموع الكلي",
                    values = listOf(
                        null, null, null, null,
                        CurrencyFormatter.format(totalDebits),
                        CurrencyFormatter.format(totalCredits),
                        null
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
