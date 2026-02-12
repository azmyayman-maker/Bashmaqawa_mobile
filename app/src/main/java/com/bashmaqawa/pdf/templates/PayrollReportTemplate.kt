package com.bashmaqawa.pdf.templates

import android.graphics.pdf.PdfDocument
import com.bashmaqawa.pdf.ColumnAlignment
import com.bashmaqawa.pdf.ColumnWidth
import com.bashmaqawa.pdf.PdfColors
import com.bashmaqawa.pdf.PdfPageSettings
import com.bashmaqawa.pdf.TableColumn
import com.bashmaqawa.pdf.core.PdfPageRenderer
import com.bashmaqawa.pdf.core.PdfTableRenderer
import com.bashmaqawa.pdf.models.PayrollReportData
import com.bashmaqawa.pdf.models.ReportConfig
import com.bashmaqawa.pdf.models.ReportType
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Payroll Report Template
 * قالب تقرير الرواتب
 * 
 * Generates payroll summary with:
 * - Period information
 * - Worker wages table
 * - Deductions and advances
 * - Net pay calculation
 */
class PayrollReportTemplate(
    private val pageRenderer: PdfPageRenderer,
    private val tableRenderer: PdfTableRenderer
) {
    
    private val columns = listOf(
        TableColumn("#", ColumnWidth.Fixed(30f), ColumnAlignment.CENTER),
        TableColumn("اسم العامل", ColumnWidth.Percentage(0.22f), ColumnAlignment.RIGHT),
        TableColumn("الفئة", ColumnWidth.Fixed(55f), ColumnAlignment.CENTER),
        TableColumn("الأيام", ColumnWidth.Fixed(40f), ColumnAlignment.CENTER),
        TableColumn("اليومي", ColumnWidth.Fixed(65f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("الإجمالي", ColumnWidth.Fixed(75f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("الخصومات", ColumnWidth.Fixed(65f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("السلف", ColumnWidth.Fixed(65f), ColumnAlignment.LEFT, isAmount = true),
        TableColumn("الصافي", ColumnWidth.Fixed(80f), ColumnAlignment.LEFT, isAmount = true)
    )
    
    /**
     * Render the payroll report
     * @return Number of pages generated
     */
    fun render(
        document: PdfDocument,
        data: PayrollReportData,
        config: ReportConfig
    ): Int {
        val columnWidths = tableRenderer.calculateColumnWidths(columns, PdfPageSettings.contentWidth)
        
        // Calculate pagination (account for summary at top)
        val summaryHeight = 100f
        val rowsFirstPage = PdfPageSettings.calculateRowsPerPage(true, summaryHeight)
        val rowsPerPage = PdfPageSettings.calculateRowsPerPage(false)
        
        val totalRows = data.entries.size
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
            
            // Header
            var currentY = pageRenderer.renderHeader(
                canvas = canvas,
                reportType = ReportType.PAYROLL_SUMMARY,
                periodStart = data.periodStart,
                periodEnd = data.periodEnd,
                pageNumber = pageNum,
                isFirstPage = pageNum == 1
            )
            
            // First page: summary cards
            if (pageNum == 1) {
                currentY = pageRenderer.renderSummaryCards(
                    canvas = canvas,
                    cards = listOf(
                        Triple("إجمالي الأجور", CurrencyFormatter.format(data.totalGrossWages), null),
                        Triple("إجمالي الخصومات", CurrencyFormatter.format(data.totalDeductions + data.totalAdvances), false),
                        Triple("صافي المستحق", CurrencyFormatter.format(data.totalNetPay), true)
                    ),
                    currentY = currentY
                )
                currentY += 10f
            }
            
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
                val entry = data.entries[currentRowIndex]
                
                val values = listOf(
                    (currentRowIndex + 1).toString(),
                    entry.workerName,
                    entry.category ?: "-",
                    entry.daysWorked.toString(),
                    CurrencyFormatter.formatNumber(entry.dailyRate),
                    CurrencyFormatter.format(entry.grossWage),
                    if (entry.deductions > 0) CurrencyFormatter.format(entry.deductions) else "-",
                    if (entry.advances > 0) CurrencyFormatter.format(entry.advances) else "-",
                    CurrencyFormatter.format(entry.netPay)
                )
                
                val amountColors = buildMap {
                    put(5, PdfColors.TextPrimary) // Gross
                    if (entry.deductions > 0) put(6, PdfColors.Warning)
                    if (entry.advances > 0) put(7, PdfColors.Warning)
                    put(8, PdfColors.Success) // Net
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
                    label = "المجموع",
                    values = listOf(
                        null, null, null, null, null,
                        CurrencyFormatter.format(data.totalGrossWages),
                        CurrencyFormatter.format(data.totalDeductions),
                        CurrencyFormatter.format(data.totalAdvances),
                        null
                    ),
                    currentY = currentY
                )
                
                currentY = tableRenderer.renderTotalsRow(
                    canvas = canvas,
                    columns = columns,
                    columnWidths = columnWidths,
                    label = "صافي المستحق",
                    values = listOf(
                        null, null, null, null, null, null, null, null,
                        CurrencyFormatter.format(data.totalNetPay)
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
