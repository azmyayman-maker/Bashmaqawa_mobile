package com.bashmaqawa.pdf

import android.graphics.Color

/**
 * PDF Color Palette
 * نظام ألوان التقارير PDF
 * 
 * Aligned with AppColors from the presentation theme for consistent branding.
 * Uses Android Graphics Color (Int) instead of Compose Color for PDF rendering.
 */
object PdfColors {
    
    // ══════════════════════════════════════════════════════════════════════
    // PRIMARY BRAND COLORS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Primary blue - Used for headers, titles, and emphasis */
    val Primary = Color.parseColor("#1E88E5")
    
    /** Dark variant of primary - Used for emphasized elements */
    val PrimaryDark = Color.parseColor("#1565C0")
    
    /** Light variant of primary - Used for backgrounds */
    val PrimaryLight = Color.parseColor("#64B5F6")
    
    /** Accent gold - Used for highlights and totals */
    val Accent = Color.parseColor("#FFB300")
    
    /** Dark accent - Used for emphasized accents */
    val AccentDark = Color.parseColor("#FF8F00")
    
    // ══════════════════════════════════════════════════════════════════════
    // STATUS COLORS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Success/positive values - Income, profits */
    val Success = Color.parseColor("#43A047")
    
    /** Success background - Light green for positive highlights */
    val SuccessLight = Color.parseColor("#E8F5E9")
    
    /** Error/negative values - Expenses, losses */
    val Error = Color.parseColor("#E53935")
    
    /** Error background - Light red for negative highlights */
    val ErrorLight = Color.parseColor("#FFEBEE")
    
    /** Warning - Pending items, caution */
    val Warning = Color.parseColor("#FF9800")
    
    /** Warning background - Light orange */
    val WarningLight = Color.parseColor("#FFF3E0")
    
    /** Info - Informational elements */
    val Info = Color.parseColor("#2196F3")
    
    /** Info background - Light blue */
    val InfoLight = Color.parseColor("#E3F2FD")
    
    // ══════════════════════════════════════════════════════════════════════
    // TEXT COLORS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Primary text - Main content text */
    val TextPrimary = Color.parseColor("#212121")
    
    /** Secondary text - Labels, descriptions */
    val TextSecondary = Color.parseColor("#757575")
    
    /** Muted text - Hints, footnotes, captions */
    val TextMuted = Color.parseColor("#9E9E9E")
    
    /** Inverted text - White text on dark backgrounds */
    val TextInverted = Color.parseColor("#FFFFFF")
    
    // ══════════════════════════════════════════════════════════════════════
    // TABLE COLORS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Table header background - Primary color */
    val TableHeaderBackground = Primary
    
    /** Table header text - White */
    val TableHeaderText = Color.parseColor("#FFFFFF")
    
    /** Table row background - White */
    val TableRowBackground = Color.parseColor("#FFFFFF")
    
    /** Table alternate row background - Light gray */
    val TableRowAlternate = Color.parseColor("#F5F5F5")
    
    /** Table border color - Light gray */
    val TableBorder = Color.parseColor("#E0E0E0")
    
    /** Table totals row background - Light primary */
    val TableTotalsBackground = Color.parseColor("#E3F2FD")
    
    /** Table grand total row background - Primary with opacity */
    val TableGrandTotalBackground = Color.parseColor("#BBDEFB")
    
    // ══════════════════════════════════════════════════════════════════════
    // CARD & SECTION COLORS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Card background - White */
    val CardBackground = Color.parseColor("#FFFFFF")
    
    /** Card border - Light gray */
    val CardBorder = Color.parseColor("#E0E0E0")
    
    /** Section background - Very light gray */
    val SectionBackground = Color.parseColor("#FAFAFA")
    
    /** Separator line color */
    val Separator = Color.parseColor("#E0E0E0")
    
    /** Highlight box background - Very light blue */
    val HighlightBackground = Color.parseColor("#E3F2FD")
    
    // ══════════════════════════════════════════════════════════════════════
    // PAGE COLORS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Page background - White */
    val PageBackground = Color.parseColor("#FFFFFF")
    
    /** Header background gradient start */
    val HeaderGradientStart = Primary
    
    /** Header background gradient end */
    val HeaderGradientEnd = PrimaryDark
    
    /** Footer text color */
    val FooterText = TextMuted
    
    // ══════════════════════════════════════════════════════════════════════
    // HELPER FUNCTIONS
    // ══════════════════════════════════════════════════════════════════════
    
    /**
     * Get color for amount based on positive/negative value
     * @param amount The numeric amount
     * @return Success color for positive, Error color for negative
     */
    fun getAmountColor(amount: Double): Int {
        return when {
            amount > 0 -> Success
            amount < 0 -> Error
            else -> TextPrimary
        }
    }
    
    /**
     * Get color for transaction type
     * @param isIncome True for income, false for expense
     * @return Success color for income, Error color for expense
     */
    fun getTransactionTypeColor(isIncome: Boolean): Int {
        return if (isIncome) Success else Error
    }
    
    /**
     * Get color with alpha (transparency)
     * @param color Base color
     * @param alpha Alpha value (0-255)
     * @return Color with applied alpha
     */
    fun withAlpha(color: Int, alpha: Int): Int {
        return Color.argb(
            alpha,
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }
}
