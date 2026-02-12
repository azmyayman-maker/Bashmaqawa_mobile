package com.bashmaqawa.pdf.templates

import android.graphics.pdf.PdfDocument
import com.bashmaqawa.pdf.ColumnAlignment
import com.bashmaqawa.pdf.ColumnWidth
import com.bashmaqawa.pdf.PdfColors
import com.bashmaqawa.pdf.PdfPageSettings
import com.bashmaqawa.pdf.TableColumn
import com.bashmaqawa.pdf.core.PdfPageRenderer
import com.bashmaqawa.pdf.core.PdfTableRenderer
import com.bashmaqawa.pdf.models.ProjectReportData
import com.bashmaqawa.pdf.models.ReportConfig
import com.bashmaqawa.pdf.models.ReportType
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Project Report Template
 * قالب تقرير المشروع
 * 
 * Generates project financial summary with:
 * - Project information card
 * - Income/Expense summary
 * - Category breakdowns
 * - Net profit highlight
 */
class ProjectReportTemplate(
    private val pageRenderer: PdfPageRenderer,
    private val tableRenderer: PdfTableRenderer
) {
    
    private val breakdownColumns = listOf(
        TableColumn("الفئة", ColumnWidth.Percentage(0.65f), ColumnAlignment.RIGHT),
        TableColumn("المبلغ", ColumnWidth.Percentage(0.35f), ColumnAlignment.LEFT, isAmount = true)
    )
    
    /**
     * Render the project report
     * @return Number of pages generated
     */
    fun render(
        document: PdfDocument,
        data: ProjectReportData,
        config: ReportConfig
    ): Int {
        val columnWidths = tableRenderer.calculateColumnWidths(breakdownColumns, PdfPageSettings.contentWidth)
        
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
            reportType = ReportType.PROJECT_REPORT,
            periodStart = data.startDate,
            periodEnd = data.endDate,
            pageNumber = 1,
            isFirstPage = true
        )
        
        // Project info card
        val statusLabel = when (data.status) {
            "ACTIVE" -> "جاري"
            "COMPLETED" -> "مكتمل"
            "PENDING" -> "قيد الانتظار"
            "PAUSED" -> "متوقف"
            else -> data.status
        }
        
        currentY = pageRenderer.renderInfoCard(
            canvas = canvas,
            title = "معلومات المشروع",
            items = buildList {
                add("اسم المشروع" to data.projectName)
                data.clientName?.let { add("العميل" to it) }
                data.location?.let { add("الموقع" to it) }
                add("الحالة" to statusLabel)
                data.startDate?.let { add("تاريخ البدء" to it) }
                data.endDate?.let { add("تاريخ الانتهاء" to it) }
            },
            currentY = currentY
        )
        
        // Summary cards
        currentY = pageRenderer.renderSummaryCards(
            canvas = canvas,
            cards = listOf(
                Triple("إجمالي الإيرادات", CurrencyFormatter.format(data.totalIncome), true),
                Triple("إجمالي المصروفات", CurrencyFormatter.format(data.totalExpenses), false),
                Triple("صافي الربح", CurrencyFormatter.format(data.netProfit), data.netProfit >= 0)
            ),
            currentY = currentY + 10f
        )
        
        // Income breakdown (if available)
        if (data.incomeBreakdown.isNotEmpty()) {
            currentY = pageRenderer.renderSectionHeader(canvas, "تفاصيل الإيرادات", currentY)
            
            val incomeTableStartY = currentY
            currentY = tableRenderer.renderTableHeader(
                canvas = canvas,
                columns = breakdownColumns,
                columnWidths = columnWidths,
                currentY = currentY
            )
            
            data.incomeBreakdown.forEachIndexed { index, category ->
                currentY = tableRenderer.renderDataRow(
                    canvas = canvas,
                    columns = breakdownColumns,
                    columnWidths = columnWidths,
                    values = listOf(category.category, CurrencyFormatter.format(category.amount)),
                    currentY = currentY,
                    rowIndex = index,
                    amountColors = mapOf(1 to PdfColors.Success)
                )
            }
            
            currentY = tableRenderer.renderTotalsRow(
                canvas = canvas,
                columns = breakdownColumns,
                columnWidths = columnWidths,
                label = "إجمالي الإيرادات",
                values = listOf(null, CurrencyFormatter.format(data.totalIncome)),
                currentY = currentY
            )
            
            tableRenderer.renderTableBorder(canvas, incomeTableStartY, currentY)
        }
        
        // Expense breakdown (if available)
        if (data.expenseBreakdown.isNotEmpty()) {
            currentY = pageRenderer.renderSectionHeader(canvas, "تفاصيل المصروفات", currentY + 15f)
            
            val expenseTableStartY = currentY
            currentY = tableRenderer.renderTableHeader(
                canvas = canvas,
                columns = breakdownColumns,
                columnWidths = columnWidths,
                currentY = currentY
            )
            
            data.expenseBreakdown.forEachIndexed { index, category ->
                currentY = tableRenderer.renderDataRow(
                    canvas = canvas,
                    columns = breakdownColumns,
                    columnWidths = columnWidths,
                    values = listOf(category.category, CurrencyFormatter.format(category.amount)),
                    currentY = currentY,
                    rowIndex = index,
                    amountColors = mapOf(1 to PdfColors.Error)
                )
            }
            
            currentY = tableRenderer.renderTotalsRow(
                canvas = canvas,
                columns = breakdownColumns,
                columnWidths = columnWidths,
                label = "إجمالي المصروفات",
                values = listOf(null, CurrencyFormatter.format(data.totalExpenses)),
                currentY = currentY
            )
            
            tableRenderer.renderTableBorder(canvas, expenseTableStartY, currentY)
        }
        
        // Net profit highlight
        currentY = pageRenderer.renderHighlightBox(
            canvas = canvas,
            title = "صافي ربح المشروع",
            value = CurrencyFormatter.format(data.netProfit),
            subtitle = if (data.netProfit >= 0) "ربح ✓" else "خسارة ✗",
            isPositive = data.netProfit >= 0,
            currentY = currentY + 15f
        )
        
        // Footer
        pageRenderer.renderFooter(canvas, 1, 1)
        
        document.finishPage(page)
        
        return 1
    }
}
