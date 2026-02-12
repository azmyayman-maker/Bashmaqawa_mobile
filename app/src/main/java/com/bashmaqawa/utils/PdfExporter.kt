package com.bashmaqawa.utils

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PDF Export Utility
 * أداة تصدير PDF
 */
object PdfExporter {
    
    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 24f
    
    /**
     * Export analytics report to PDF
     */
    fun exportAnalyticsReport(
        context: Context,
        totalIncome: Double,
        totalExpense: Double,
        netProfit: Double,
        activeProjects: Int,
        totalWorkers: Int,
        periodTitle: String,
        onSuccess: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Paint configurations
            val titlePaint = Paint().apply {
                color = Color.parseColor("#1976D2")
                textSize = 28f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.RIGHT
            }
            
            val headerPaint = Paint().apply {
                color = Color.parseColor("#333333")
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.RIGHT
            }
            
            val normalPaint = Paint().apply {
                color = Color.parseColor("#666666")
                textSize = 14f
                textAlign = Paint.Align.RIGHT
            }
            
            val valuePaint = Paint().apply {
                color = Color.parseColor("#333333")
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.LEFT
            }
            
            val linePaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                strokeWidth = 1f
            }
            
            var yPosition = MARGIN + 40f
            val rightMargin = PAGE_WIDTH - MARGIN
            
            // Title
            canvas.drawText("تقرير التحليلات المالية", rightMargin, yPosition, titlePaint)
            yPosition += 20f
            
            // Date
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))
            canvas.drawText("تاريخ التقرير: ${dateFormat.format(Date())}", rightMargin, yPosition, normalPaint)
            yPosition += 40f
            
            // Period
            canvas.drawText("الفترة: $periodTitle", rightMargin, yPosition, headerPaint)
            yPosition += 40f
            
            // Separator line
            canvas.drawLine(MARGIN, yPosition, rightMargin, yPosition, linePaint)
            yPosition += 30f
            
            // Financial Summary Header
            canvas.drawText("الملخص المالي", rightMargin, yPosition, headerPaint)
            yPosition += 35f
            
            // Income
            drawReportRow(canvas, "إجمالي الإيرادات", CurrencyFormatter.format(totalIncome), rightMargin, MARGIN, yPosition, normalPaint, valuePaint)
            yPosition += LINE_HEIGHT
            
            // Expenses
            drawReportRow(canvas, "إجمالي المصروفات", CurrencyFormatter.format(totalExpense), rightMargin, MARGIN, yPosition, normalPaint, valuePaint)
            yPosition += LINE_HEIGHT
            
            // Net Profit
            val profitPaint = valuePaint.apply {
                color = if (netProfit >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            }
            drawReportRow(canvas, "صافي الربح", CurrencyFormatter.format(netProfit), rightMargin, MARGIN, yPosition, normalPaint, profitPaint)
            yPosition += 40f
            
            // Separator line
            canvas.drawLine(MARGIN, yPosition, rightMargin, yPosition, linePaint)
            yPosition += 30f
            
            // Quick Stats Header
            canvas.drawText("إحصائيات سريعة", rightMargin, yPosition, headerPaint)
            yPosition += 35f
            
            valuePaint.color = Color.parseColor("#333333")
            
            // Active Projects
            drawReportRow(canvas, "المشاريع الجارية", activeProjects.toString(), rightMargin, MARGIN, yPosition, normalPaint, valuePaint)
            yPosition += LINE_HEIGHT
            
            // Total Workers
            drawReportRow(canvas, "إجمالي العمال", totalWorkers.toString(), rightMargin, MARGIN, yPosition, normalPaint, valuePaint)
            yPosition += 60f
            
            // Footer
            normalPaint.textSize = 12f
            normalPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("تم إنشاء هذا التقرير بواسطة تطبيق بشمقاول", (PAGE_WIDTH / 2).toFloat(), PAGE_HEIGHT - MARGIN, normalPaint)
            
            pdfDocument.finishPage(page)
            
            // Save to file
            val fileName = "analytics_report_${System.currentTimeMillis()}.pdf"
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val file = File(outputDir, fileName)
            
            FileOutputStream(file).use { output ->
                pdfDocument.writeTo(output)
            }
            
            pdfDocument.close()
            onSuccess(file)
            
        } catch (e: Exception) {
            onError(e)
        }
    }
    
    private fun drawReportRow(
        canvas: Canvas,
        label: String,
        value: String,
        rightX: Float,
        leftX: Float,
        y: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        canvas.drawText(label, rightX, y, labelPaint)
        canvas.drawText(value, leftX, y, valuePaint)
    }
    
    /**
     * Share PDF file using system share intent
     */
    fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة التقرير"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
