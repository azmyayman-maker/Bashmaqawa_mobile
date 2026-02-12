package com.bashmaqawa.pdf.templates

import android.graphics.pdf.PdfDocument
import com.bashmaqawa.pdf.ColumnAlignment
import com.bashmaqawa.pdf.ColumnWidth
import com.bashmaqawa.pdf.PdfColors
import com.bashmaqawa.pdf.PdfPageSettings
import com.bashmaqawa.pdf.TableColumn
import com.bashmaqawa.pdf.core.PdfPageRenderer
import com.bashmaqawa.pdf.core.PdfTableRenderer
import com.bashmaqawa.pdf.models.ProfitLossData
import com.bashmaqawa.pdf.models.ReportConfig
import com.bashmaqawa.pdf.models.ReportType
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Profit & Loss Report Template
 * قالب تقرير الربح والخسارة
 * 
 * Generates P&L report with:
 * - Summary cards (Income, Expenses, Net Profit)
 * - Income breakdown by category
 * - Expense breakdown by category
 * - Net profit highlight box
 */
class ProfitLossReportTemplate(
    private val pageRenderer: PdfPageRenderer,
    private val tableRenderer: PdfTableRenderer
) {
    
    private val categoryColumns = listOf(
        TableColumn("الفئة", ColumnWidth.Percentage(0.60f), ColumnAlignment.RIGHT),
        TableColumn("عدد المعاملات", ColumnWidth.Fixed(80f), ColumnAlignment.CENTER),
        TableColumn("المبلغ", ColumnWidth.Percentage(0.30f), ColumnAlignment.LEFT, isAmount = true)
    )
    
    /**
     * Render the profit & loss report
     * @return Number of pages generated
     */
    fun render(
        document: PdfDocument,
        data: ProfitLossData,
        config: ReportConfig
    ): Int {
        val columnWidths = tableRenderer.calculateColumnWidths(categoryColumns, PdfPageSettings.contentWidth)
        
        // For now, create single page (can be extended for multi-page)
        val pageInfo = PdfDocument.PageInfo.Builder(
            PdfPageSettings.A4_WIDTH,
            PdfPageSettings.A4_HEIGHT,
            1
        ).create()
        
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        // Header
        var currentY = pageRenderer.renderHeader(
            canvas = canvas,
            reportType = ReportType.PROFIT_LOSS,
            periodStart = data.periodStart,
            periodEnd = data.periodEnd,
            pageNumber = 1,
            isFirstPage = true
        )
        
        // Summary cards
        currentY = pageRenderer.renderSummaryCards(
            canvas = canvas,
            cards = listOf(
                Triple("إجمالي الإيرادات", CurrencyFormatter.format(data.totalIncome), true),
                Triple("إجمالي المصروفات", CurrencyFormatter.format(data.totalExpenses), false),
                Triple("صافي الربح", CurrencyFormatter.format(data.netProfit), data.netProfit >= 0)
            ),
            currentY = currentY
        )
        
        // Income section
        currentY = pageRenderer.renderSectionHeader(canvas, "تفاصيل الإيرادات", currentY)
        
        if (data.incomeCategories.isNotEmpty()) {
            // Income table header
            val incomeTableStartY = currentY
            currentY = tableRenderer.renderTableHeader(
                canvas = canvas,
                columns = categoryColumns,
                columnWidths = columnWidths,
                currentY = currentY
            )
            
            // Income rows
            data.incomeCategories.forEachIndexed { index, category ->
                currentY = tableRenderer.renderDataRow(
                    canvas = canvas,
                    columns = categoryColumns,
                    columnWidths = columnWidths,
                    values = listOf(
                        category.category,
                        category.transactionCount.toString(),
                        CurrencyFormatter.format(category.amount)
                    ),
                    currentY = currentY,
                    rowIndex = index,
                    amountColors = mapOf(2 to PdfColors.Success)
                )
            }
            
            // Income total
            currentY = tableRenderer.renderTotalsRow(
                canvas = canvas,
                columns = categoryColumns,
                columnWidths = columnWidths,
                label = "إجمالي الإيرادات",
                values = listOf(null, null, CurrencyFormatter.format(data.totalIncome)),
                currentY = currentY
            )
            
            tableRenderer.renderTableBorder(canvas, incomeTableStartY, currentY)
        }
        
        currentY += 15f
        
        // Expense section
        currentY = pageRenderer.renderSectionHeader(canvas, "تفاصيل المصروفات", currentY)
        
        if (data.expenseCategories.isNotEmpty()) {
            // Expense table header
            val expenseTableStartY = currentY
            currentY = tableRenderer.renderTableHeader(
                canvas = canvas,
                columns = categoryColumns,
                columnWidths = columnWidths,
                currentY = currentY
            )
            
            // Expense rows
            data.expenseCategories.forEachIndexed { index, category ->
                currentY = tableRenderer.renderDataRow(
                    canvas = canvas,
                    columns = categoryColumns,
                    columnWidths = columnWidths,
                    values = listOf(
                        category.category,
                        category.transactionCount.toString(),
                        CurrencyFormatter.format(category.amount)
                    ),
                    currentY = currentY,
                    rowIndex = index,
                    amountColors = mapOf(2 to PdfColors.Error)
                )
            }
            
            // Expense total
            currentY = tableRenderer.renderTotalsRow(
                canvas = canvas,
                columns = categoryColumns,
                columnWidths = columnWidths,
                label = "إجمالي المصروفات",
                values = listOf(null, null, CurrencyFormatter.format(data.totalExpenses)),
                currentY = currentY
            )
            
            tableRenderer.renderTableBorder(canvas, expenseTableStartY, currentY)
        }
        
        currentY += 20f
        
        // Net profit highlight box
        currentY = pageRenderer.renderHighlightBox(
            canvas = canvas,
            title = "صافي الربح / الخسارة",
            value = CurrencyFormatter.format(data.netProfit),
            subtitle = if (data.netProfit >= 0) "ربح ✓" else "خسارة ✗",
            isPositive = data.netProfit >= 0,
            currentY = currentY
        )
        
        // Footer
        pageRenderer.renderFooter(canvas, 1, 1)
        
        document.finishPage(page)
        
        return 1
    }
}
