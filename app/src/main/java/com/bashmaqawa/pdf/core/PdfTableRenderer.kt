package com.bashmaqawa.pdf.core

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.bashmaqawa.pdf.ColumnAlignment
import com.bashmaqawa.pdf.ColumnWidth
import com.bashmaqawa.pdf.PdfColors
import com.bashmaqawa.pdf.PdfPageSettings
import com.bashmaqawa.pdf.PdfTypography
import com.bashmaqawa.pdf.TableColumn

/**
 * PDF Table Renderer
 * عارض الجداول PDF
 * 
 * Handles professional table rendering with:
 * - Header row with primary color background
 * - Alternating row colors
 * - RTL/LTR text alignment
 * - Multi-page table continuation
 * - Subtotals and grand totals
 */
class PdfTableRenderer {
    
    // Pre-created paints for efficiency
    private val headerBgPaint = PdfTypography.createFillPaint(PdfColors.TableHeaderBackground)
    private val rowBgPaint = PdfTypography.createFillPaint(PdfColors.TableRowBackground)
    private val altRowBgPaint = PdfTypography.createFillPaint(PdfColors.TableRowAlternate)
    private val totalsBgPaint = PdfTypography.createFillPaint(PdfColors.TableTotalsBackground)
    private val grandTotalBgPaint = PdfTypography.createFillPaint(PdfColors.TableGrandTotalBackground)
    private val borderPaint = PdfTypography.createLinePaint().apply {
        color = PdfColors.TableBorder
        strokeWidth = PdfPageSettings.TABLE_BORDER_WIDTH
    }
    private val headerTextPaint = PdfTypography.createTableHeaderPaint()
    private val bodyTextPaint = PdfTypography.createTableBodyPaint()
    private val bodyTextLtrPaint = PdfTypography.createTableBodyLtrPaint()
    private val bodyTextCenterPaint = PdfTypography.createTableBodyCenterPaint()
    private val amountPaint = PdfTypography.createAmountPaint()
    private val totalTextPaint = PdfTypography.createTotalPaint()
    
    /**
     * Calculate actual column widths from column definitions
     * 
     * @param columns List of column definitions
     * @param tableWidth Available table width
     * @return List of calculated widths in points
     */
    fun calculateColumnWidths(columns: List<TableColumn>, tableWidth: Float): List<Float> {
        val result = MutableList(columns.size) { 0f }
        var remainingWidth = tableWidth
        var autoColumns = 0
        
        // First pass: handle fixed and percentage widths
        columns.forEachIndexed { index, column ->
            when (val width = column.width) {
                is ColumnWidth.Fixed -> {
                    result[index] = width.width
                    remainingWidth -= width.width
                }
                is ColumnWidth.Percentage -> {
                    val calculated = tableWidth * width.percent
                    result[index] = calculated
                    remainingWidth -= calculated
                }
                is ColumnWidth.Auto -> {
                    autoColumns++
                }
            }
        }
        
        // Second pass: distribute remaining width to auto columns
        if (autoColumns > 0 && remainingWidth > 0) {
            val autoWidth = remainingWidth / autoColumns
            columns.forEachIndexed { index, column ->
                if (column.width is ColumnWidth.Auto) {
                    result[index] = autoWidth
                }
            }
        }
        
        return result
    }
    
    /**
     * Render table header row
     * 
     * @param canvas Canvas to draw on
     * @param columns Column definitions
     * @param columnWidths Pre-calculated column widths
     * @param currentY Current Y position
     * @param isContinuation True if this is a continuation header (adds "تابع")
     * @return Y position after header
     */
    fun renderTableHeader(
        canvas: Canvas,
        columns: List<TableColumn>,
        columnWidths: List<Float>,
        currentY: Float,
        isContinuation: Boolean = false
    ): Float {
        val leftX = PdfPageSettings.MARGIN_LEFT
        val rightX = PdfPageSettings.contentStartX
        val headerHeight = PdfPageSettings.TABLE_HEADER_HEIGHT
        
        // Header background
        canvas.drawRect(
            leftX,
            currentY,
            rightX,
            currentY + headerHeight,
            headerBgPaint
        )
        
        // Draw vertical separators and header text
        var xPos = rightX // Start from right for RTL
        
        columns.forEachIndexed { index, column ->
            val colWidth = columnWidths[index]
            
            // Calculate text position based on alignment
            val textX = when (column.alignment) {
                ColumnAlignment.RIGHT -> xPos - PdfPageSettings.CELL_PADDING_H
                ColumnAlignment.LEFT -> xPos - colWidth + PdfPageSettings.CELL_PADDING_H
                ColumnAlignment.CENTER -> xPos - colWidth / 2
            }
            
            // Adjust paint alignment
            val paint = Paint(headerTextPaint).apply {
                textAlign = when (column.alignment) {
                    ColumnAlignment.RIGHT -> Paint.Align.RIGHT
                    ColumnAlignment.LEFT -> Paint.Align.LEFT
                    ColumnAlignment.CENTER -> Paint.Align.CENTER
                }
            }
            
            // Draw header text
            val headerText = if (isContinuation && index == 0) {
                "${column.header} (تابع)"
            } else {
                column.header
            }
            
            canvas.drawText(
                headerText,
                textX,
                currentY + headerHeight / 2 + 5f,
                paint
            )
            
            // Vertical separator (except for last column)
            if (index < columns.size - 1) {
                xPos -= colWidth
                canvas.drawLine(
                    xPos,
                    currentY,
                    xPos,
                    currentY + headerHeight,
                    Paint().apply {
                        color = PdfColors.withAlpha(PdfColors.TableHeaderText, 100)
                        strokeWidth = 0.5f
                    }
                )
            } else {
                xPos -= colWidth
            }
        }
        
        // Bottom border
        canvas.drawLine(
            leftX,
            currentY + headerHeight,
            rightX,
            currentY + headerHeight,
            borderPaint
        )
        
        return currentY + headerHeight
    }
    
    /**
     * Render a data row
     * 
     * @param canvas Canvas to draw on
     * @param columns Column definitions
     * @param columnWidths Pre-calculated column widths
     * @param values Row values (same order as columns)
     * @param currentY Current Y position
     * @param rowIndex Row index (for alternating colors)
     * @param amountColors Optional map of column index to color (for colored amounts)
     * @return Y position after row
     */
    fun renderDataRow(
        canvas: Canvas,
        columns: List<TableColumn>,
        columnWidths: List<Float>,
        values: List<String>,
        currentY: Float,
        rowIndex: Int,
        amountColors: Map<Int, Int>? = null
    ): Float {
        val leftX = PdfPageSettings.MARGIN_LEFT
        val rightX = PdfPageSettings.contentStartX
        val rowHeight = PdfPageSettings.TABLE_ROW_HEIGHT
        
        // Row background (alternating)
        val bgPaint = if (rowIndex % 2 == 0) rowBgPaint else altRowBgPaint
        canvas.drawRect(leftX, currentY, rightX, currentY + rowHeight, bgPaint)
        
        // Draw values
        var xPos = rightX
        
        columns.forEachIndexed { index, column ->
            val colWidth = columnWidths[index]
            val value = values.getOrElse(index) { "" }
            
            // Calculate text position based on alignment
            val textX = when (column.alignment) {
                ColumnAlignment.RIGHT -> xPos - PdfPageSettings.CELL_PADDING_H
                ColumnAlignment.LEFT -> xPos - colWidth + PdfPageSettings.CELL_PADDING_H
                ColumnAlignment.CENTER -> xPos - colWidth / 2
            }
            
            // Select paint
            val textColor = amountColors?.get(index) ?: PdfColors.TextPrimary
            val paint = when {
                column.isAmount -> Paint(amountPaint).apply {
                    color = textColor
                    textAlign = when (column.alignment) {
                        ColumnAlignment.RIGHT -> Paint.Align.RIGHT
                        ColumnAlignment.LEFT -> Paint.Align.LEFT
                        ColumnAlignment.CENTER -> Paint.Align.CENTER
                    }
                }
                column.alignment == ColumnAlignment.RIGHT -> Paint(bodyTextPaint).apply { color = textColor }
                column.alignment == ColumnAlignment.LEFT -> Paint(bodyTextLtrPaint).apply { color = textColor }
                else -> Paint(bodyTextCenterPaint).apply { color = textColor }
            }
            
            // Draw text (truncate if too long)
            val maxWidth = colWidth - 2 * PdfPageSettings.CELL_PADDING_H
            val displayText = truncateText(value, paint, maxWidth)
            
            canvas.drawText(
                displayText,
                textX,
                currentY + rowHeight / 2 + 4f,
                paint
            )
            
            // Vertical separator
            if (index < columns.size - 1) {
                xPos -= colWidth
                canvas.drawLine(
                    xPos,
                    currentY,
                    xPos,
                    currentY + rowHeight,
                    borderPaint
                )
            } else {
                xPos -= colWidth
            }
        }
        
        // Bottom border
        canvas.drawLine(leftX, currentY + rowHeight, rightX, currentY + rowHeight, borderPaint)
        
        return currentY + rowHeight
    }
    
    /**
     * Render a totals row
     * 
     * @param canvas Canvas to draw on
     * @param columns Column definitions
     * @param columnWidths Pre-calculated column widths
     * @param label Label for the totals row
     * @param values Values (null for non-total columns)
     * @param currentY Current Y position
     * @param isGrandTotal True for grand total styling
     * @return Y position after row
     */
    fun renderTotalsRow(
        canvas: Canvas,
        columns: List<TableColumn>,
        columnWidths: List<Float>,
        label: String,
        values: List<String?>,
        currentY: Float,
        isGrandTotal: Boolean = false
    ): Float {
        val leftX = PdfPageSettings.MARGIN_LEFT
        val rightX = PdfPageSettings.contentStartX
        val rowHeight = PdfPageSettings.TABLE_TOTALS_HEIGHT
        
        // Background
        val bgPaint = if (isGrandTotal) grandTotalBgPaint else totalsBgPaint
        canvas.drawRect(leftX, currentY, rightX, currentY + rowHeight, bgPaint)
        
        // Top border (thicker for totals)
        canvas.drawLine(
            leftX, currentY, rightX, currentY,
            Paint().apply {
                color = PdfColors.Primary
                strokeWidth = if (isGrandTotal) 2f else 1f
            }
        )
        
        // Draw label in first column(s)
        var labelDrawn = false
        var xPos = rightX
        
        columns.forEachIndexed { index, column ->
            val colWidth = columnWidths[index]
            val value = values.getOrNull(index)
            
            if (!labelDrawn && value == null) {
                // Draw label in this column
                canvas.drawText(
                    label,
                    xPos - PdfPageSettings.CELL_PADDING_H,
                    currentY + rowHeight / 2 + 4f,
                    if (isGrandTotal) {
                        Paint(PdfTypography.createGrandTotalPaint()).apply {
                            textAlign = Paint.Align.RIGHT
                        }
                    } else {
                        Paint(totalTextPaint).apply {
                            textAlign = Paint.Align.RIGHT
                        }
                    }
                )
                labelDrawn = true
            } else if (value != null) {
                // Draw value
                val textX = when (column.alignment) {
                    ColumnAlignment.RIGHT -> xPos - PdfPageSettings.CELL_PADDING_H
                    ColumnAlignment.LEFT -> xPos - colWidth + PdfPageSettings.CELL_PADDING_H
                    ColumnAlignment.CENTER -> xPos - colWidth / 2
                }
                
                val paint = if (isGrandTotal) {
                    Paint(PdfTypography.createGrandTotalPaint())
                } else {
                    Paint(totalTextPaint)
                }.apply {
                    textAlign = when (column.alignment) {
                        ColumnAlignment.RIGHT -> Paint.Align.RIGHT
                        ColumnAlignment.LEFT -> Paint.Align.LEFT
                        ColumnAlignment.CENTER -> Paint.Align.CENTER
                    }
                }
                
                canvas.drawText(value, textX, currentY + rowHeight / 2 + 4f, paint)
            }
            
            // Vertical separator
            if (index < columns.size - 1) {
                xPos -= colWidth
                canvas.drawLine(xPos, currentY, xPos, currentY + rowHeight, borderPaint)
            } else {
                xPos -= colWidth
            }
        }
        
        // Bottom border
        canvas.drawLine(
            leftX, currentY + rowHeight, rightX, currentY + rowHeight,
            Paint().apply {
                color = PdfColors.Primary
                strokeWidth = if (isGrandTotal) 2f else 1f
            }
        )
        
        return currentY + rowHeight
    }
    
    /**
     * Render outer table border
     */
    fun renderTableBorder(
        canvas: Canvas,
        startY: Float,
        endY: Float
    ) {
        val leftX = PdfPageSettings.MARGIN_LEFT
        val rightX = PdfPageSettings.contentStartX
        
        // Left and right borders
        canvas.drawLine(leftX, startY, leftX, endY, borderPaint)
        canvas.drawLine(rightX, startY, rightX, endY, borderPaint)
    }
    
    /**
     * Truncate text to fit within max width
     */
    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return if (truncated.isEmpty()) "..." else "$truncated..."
    }
    
    /**
     * Calculate total height for a table
     * 
     * @param rowCount Number of data rows
     * @param hasTotals True if table has totals row
     * @param hasGrandTotal True if table has grand total row
     */
    fun calculateTableHeight(
        rowCount: Int,
        hasTotals: Boolean = false,
        hasGrandTotal: Boolean = false
    ): Float {
        var height = PdfPageSettings.TABLE_HEADER_HEIGHT
        height += rowCount * PdfPageSettings.TABLE_ROW_HEIGHT
        if (hasTotals) height += PdfPageSettings.TABLE_TOTALS_HEIGHT
        if (hasGrandTotal) height += PdfPageSettings.TABLE_TOTALS_HEIGHT
        return height
    }
}
