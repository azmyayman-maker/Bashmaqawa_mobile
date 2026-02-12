package com.bashmaqawa.pdf

import android.graphics.Paint
import android.graphics.Typeface

/**
 * PDF Typography System
 * نظام الخطوط للتقارير PDF
 * 
 * Provides consistent text styling across all PDF reports.
 * Uses Android Paint objects configured for PDF rendering.
 */
object PdfTypography {
    
    // ══════════════════════════════════════════════════════════════════════
    // FONT SIZES (in points - 1 point = 1/72 inch)
    // ══════════════════════════════════════════════════════════════════════
    
    /** Page title size - Company/Report name */
    const val SIZE_PAGE_TITLE = 28f
    
    /** Report subtitle size */
    const val SIZE_SUBTITLE = 16f
    
    /** Section header size */
    const val SIZE_SECTION_HEADER = 18f
    
    /** Subsection header size */
    const val SIZE_SUBSECTION_HEADER = 14f
    
    /** Table header size */
    const val SIZE_TABLE_HEADER = 12f
    
    /** Table body text size */
    const val SIZE_TABLE_BODY = 11f
    
    /** Amount/number size */
    const val SIZE_AMOUNT = 12f
    
    /** Total amount size */
    const val SIZE_TOTAL = 14f
    
    /** Grand total size */
    const val SIZE_GRAND_TOTAL = 16f
    
    /** Label text size */
    const val SIZE_LABEL = 10f
    
    /** Footer/page number size */
    const val SIZE_FOOTER = 9f
    
    /** Caption/notes size */
    const val SIZE_CAPTION = 8f
    
    // ══════════════════════════════════════════════════════════════════════
    // PAINT FACTORY METHODS
    // ══════════════════════════════════════════════════════════════════════
    
    /**
     * Create Paint for page title (Company/Report name)
     * RTL aligned for Arabic text
     */
    fun createPageTitlePaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.Primary
        textSize = SIZE_PAGE_TITLE
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.RIGHT
    }
    
    /**
     * Create Paint for report subtitle
     */
    fun createSubtitlePaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TextSecondary
        textSize = SIZE_SUBTITLE
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.RIGHT
    }
    
    /**
     * Create Paint for section headers
     */
    fun createSectionHeaderPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.PrimaryDark
        textSize = SIZE_SECTION_HEADER
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.RIGHT
    }
    
    /**
     * Create Paint for subsection headers
     */
    fun createSubsectionHeaderPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TextPrimary
        textSize = SIZE_SUBSECTION_HEADER
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.RIGHT
    }
    
    /**
     * Create Paint for table headers
     */
    fun createTableHeaderPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TableHeaderText
        textSize = SIZE_TABLE_HEADER
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    
    /**
     * Create Paint for table body text (RTL - Arabic content)
     */
    fun createTableBodyPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TextPrimary
        textSize = SIZE_TABLE_BODY
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.RIGHT
    }
    
    /**
     * Create Paint for table body text (LTR - numbers, English)
     */
    fun createTableBodyLtrPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TextPrimary
        textSize = SIZE_TABLE_BODY
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.LEFT
    }
    
    /**
     * Create Paint for centered table body text
     */
    fun createTableBodyCenterPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TextPrimary
        textSize = SIZE_TABLE_BODY
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.CENTER
    }
    
    /**
     * Create Paint for amount/currency values
     * Uses monospace font for alignment
     */
    fun createAmountPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TextPrimary
        textSize = SIZE_AMOUNT
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.LEFT
    }
    
    /**
     * Create Paint for positive amounts (income, profit)
     */
    fun createPositiveAmountPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.Success
        textSize = SIZE_AMOUNT
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.LEFT
    }
    
    /**
     * Create Paint for negative amounts (expense, loss)
     */
    fun createNegativeAmountPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.Error
        textSize = SIZE_AMOUNT
        typeface = Typeface.MONOSPACE
        textAlign = Paint.Align.LEFT
    }
    
    /**
     * Create Paint for totals row
     */
    fun createTotalPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.Primary
        textSize = SIZE_TOTAL
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.LEFT
    }
    
    /**
     * Create Paint for grand total/final amount
     */
    fun createGrandTotalPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.PrimaryDark
        textSize = SIZE_GRAND_TOTAL
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.LEFT
    }
    
    /**
     * Create Paint for labels
     */
    fun createLabelPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TextSecondary
        textSize = SIZE_LABEL
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.RIGHT
    }
    
    /**
     * Create Paint for footer/page numbers
     */
    fun createFooterPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.FooterText
        textSize = SIZE_FOOTER
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.CENTER
    }
    
    /**
     * Create Paint for captions/notes
     */
    fun createCaptionPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TextMuted
        textSize = SIZE_CAPTION
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.RIGHT
    }
    
    /**
     * Create Paint for drawing lines/borders
     */
    fun createLinePaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.Separator
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    
    /**
     * Create Paint for thick separator lines
     */
    fun createThickLinePaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.Primary
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    
    /**
     * Create Paint for rectangle fill
     */
    fun createFillPaint(color: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    
    /**
     * Create Paint for card info label (bold, secondary color)
     */
    fun createInfoLabelPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TextSecondary
        textSize = SIZE_TABLE_BODY
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.RIGHT
    }
    
    /**
     * Create Paint for card info value
     */
    fun createInfoValuePaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = PdfColors.TextPrimary
        textSize = SIZE_TABLE_BODY
        typeface = Typeface.DEFAULT
        textAlign = Paint.Align.LEFT
    }
}
