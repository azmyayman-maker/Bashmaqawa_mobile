package com.bashmaqawa.pdf.templates

import android.graphics.pdf.PdfDocument
import com.bashmaqawa.pdf.ColumnAlignment
import com.bashmaqawa.pdf.ColumnWidth
import com.bashmaqawa.pdf.PdfColors
import com.bashmaqawa.pdf.PdfPageSettings
import com.bashmaqawa.pdf.TableColumn
import com.bashmaqawa.pdf.core.PdfPageRenderer
import com.bashmaqawa.pdf.core.PdfTableRenderer
import com.bashmaqawa.pdf.models.JournalEntryRow
import com.bashmaqawa.pdf.models.ReportConfig
import com.bashmaqawa.pdf.models.ReportType
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Journal Entries Template
 * قالب قيود اليومية
 * 
 * Generates audit trail journal entries with:
 * - Debit/Credit double-entry display
 * - Reference tracking
 * - Multi-page pagination
 */
class JournalEntriesTemplate(
    private val pageRenderer: PdfPageRenderer,
    private val tableRenderer: PdfTableRenderer
) {
    
    private val columns = listOf(
        TableColumn("التاريخ", ColumnWidth.Fixed(60f), ColumnAlignment.CENTER),
        TableColumn("الوصف", ColumnWidth.Percentage(0.28f), ColumnAlignment.RIGHT),
        TableColumn("حساب المدين", ColumnWidth.Percentage(0.18f), ColumnAlignment.RIGHT),
        TableColumn("حساب الدائن", ColumnWidth.Percentage(0.18f), ColumnAlignment.RIGHT),
        TableColumn("المبلغ", ColumnWidth.Fixed(90f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("المرجع", ColumnWidth.Fixed(60f), ColumnAlignment.CENTER)
    )
    
    /**
     * Render the journal entries report
     * @return Number of pages generated
     */
    fun render(
        document: PdfDocument,
        entries: List<JournalEntryRow>,
        periodStart: String,
        periodEnd: String,
        config: ReportConfig
    ): Int {
        val columnWidths = tableRenderer.calculateColumnWidths(columns, PdfPageSettings.contentWidth)
        
        // Calculate pagination
        val rowsFirstPage = PdfPageSettings.calculateRowsPerPage(true, 0f)
        val rowsPerPage = PdfPageSettings.calculateRowsPerPage(false)
        
        val totalRows = entries.size
        val totalPages = if (totalRows <= rowsFirstPage) 1
        else 1 + ((totalRows - rowsFirstPage + rowsPerPage - 1) / rowsPerPage)
        
        // Calculate total
        val totalAmount = entries.sumOf { it.amount }
        
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
                reportType = ReportType.JOURNAL_ENTRIES,
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
                val entry = entries[currentRowIndex]
                
                val refLabel = when (entry.referenceType) {
                    "TRANSACTION" -> "معاملة"
                    "PAYROLL" -> "رواتب"
                    "ADVANCE" -> "سلفة"
                    "TRANSFER" -> "تحويل"
                    "ADJUSTMENT" -> "تسوية"
                    "OPENING_BALANCE" -> "افتتاحي"
                    else -> entry.referenceType ?: "-"
                }
                
                val values = listOf(
                    entry.date,
                    entry.description,
                    entry.debitAccountName,
                    entry.creditAccountName,
                    CurrencyFormatter.format(entry.amount),
                    refLabel
                )
                
                val amountColors = buildMap {
                    put(4, PdfColors.Primary)
                    // Mark reversing entries differently
                    if (entry.isReversing) {
                        put(4, PdfColors.Warning)
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
                    label = "إجمالي القيود",
                    values = listOf(
                        null, null, null, null,
                        CurrencyFormatter.format(totalAmount),
                        entries.size.toString()
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
