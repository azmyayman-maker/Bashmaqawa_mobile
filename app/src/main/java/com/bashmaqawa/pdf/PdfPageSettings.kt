package com.bashmaqawa.pdf

/**
 * PDF Page Settings and Layout Constants
 * إعدادات صفحة PDF والتخطيط
 * 
 * Defines all page dimensions, margins, and layout constants for PDF generation.
 * All measurements are in points (1 point = 1/72 inch).
 */
object PdfPageSettings {
    
    // ══════════════════════════════════════════════════════════════════════
    // PAGE DIMENSIONS (A4)
    // ══════════════════════════════════════════════════════════════════════
    
    /** A4 page width in points */
    const val A4_WIDTH = 595
    
    /** A4 page height in points */
    const val A4_HEIGHT = 842
    
    /** A4 landscape width */
    const val A4_LANDSCAPE_WIDTH = 842
    
    /** A4 landscape height */
    const val A4_LANDSCAPE_HEIGHT = 595
    
    // ══════════════════════════════════════════════════════════════════════
    // MARGINS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Top margin */
    const val MARGIN_TOP = 60f
    
    /** Bottom margin */
    const val MARGIN_BOTTOM = 50f
    
    /** Left margin */
    const val MARGIN_LEFT = 40f
    
    /** Right margin */
    const val MARGIN_RIGHT = 40f
    
    // ══════════════════════════════════════════════════════════════════════
    // HEADER & FOOTER
    // ══════════════════════════════════════════════════════════════════════
    
    /** Header section height */
    const val HEADER_HEIGHT = 90f
    
    /** Footer section height */
    const val FOOTER_HEIGHT = 40f
    
    /** Space between header and content */
    const val HEADER_CONTENT_GAP = 20f
    
    /** Space between content and footer */
    const val CONTENT_FOOTER_GAP = 20f
    
    // ══════════════════════════════════════════════════════════════════════
    // CONTENT AREA CALCULATIONS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Usable content width (page width minus margins) */
    val contentWidth: Float
        get() = A4_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
    
    /** Usable content width for landscape */
    val contentWidthLandscape: Float
        get() = A4_LANDSCAPE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT
    
    /** Usable content height (page height minus header, footer, margins, gaps) */
    val contentHeight: Float
        get() = A4_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM - HEADER_HEIGHT - FOOTER_HEIGHT - HEADER_CONTENT_GAP - CONTENT_FOOTER_GAP
    
    /** Usable content height for landscape */
    val contentHeightLandscape: Float
        get() = A4_LANDSCAPE_HEIGHT - MARGIN_TOP - MARGIN_BOTTOM - HEADER_HEIGHT - FOOTER_HEIGHT - HEADER_CONTENT_GAP - CONTENT_FOOTER_GAP
    
    /** Content start X position (right side for RTL) */
    val contentStartX: Float
        get() = A4_WIDTH - MARGIN_RIGHT
    
    /** Content end X position (left side for RTL) */
    val contentEndX: Float
        get() = MARGIN_LEFT
    
    /** Content start Y position (after header) */
    val contentStartY: Float
        get() = MARGIN_TOP + HEADER_HEIGHT + HEADER_CONTENT_GAP
    
    /** Content end Y position (before footer) */
    val contentEndY: Float
        get() = A4_HEIGHT - MARGIN_BOTTOM - FOOTER_HEIGHT - CONTENT_FOOTER_GAP
    
    // ══════════════════════════════════════════════════════════════════════
    // TABLE SETTINGS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Standard table row height */
    const val TABLE_ROW_HEIGHT = 28f
    
    /** Table header row height (slightly taller) */
    const val TABLE_HEADER_HEIGHT = 35f
    
    /** Table totals row height */
    const val TABLE_TOTALS_HEIGHT = 32f
    
    /** Cell horizontal padding */
    const val CELL_PADDING_H = 8f
    
    /** Cell vertical padding */
    const val CELL_PADDING_V = 6f
    
    /** Minimum column width */
    const val MIN_COLUMN_WIDTH = 40f
    
    /** Table border width */
    const val TABLE_BORDER_WIDTH = 0.5f
    
    // ══════════════════════════════════════════════════════════════════════
    // CARD & SECTION SETTINGS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Card corner radius */
    const val CARD_CORNER_RADIUS = 8f
    
    /** Card padding */
    const val CARD_PADDING = 16f
    
    /** Card border width */
    const val CARD_BORDER_WIDTH = 1f
    
    /** Section vertical spacing */
    const val SECTION_SPACING = 24f
    
    /** Element vertical spacing (within sections) */
    const val ELEMENT_SPACING = 12f
    
    /** Line spacing for multi-line text */
    const val LINE_SPACING = 6f
    
    // ══════════════════════════════════════════════════════════════════════
    // SUMMARY CARDS
    // ══════════════════════════════════════════════════════════════════════
    
    /** Summary card width (for 3 cards in a row) */
    val summaryCardWidth: Float
        get() = (contentWidth - 2 * ELEMENT_SPACING) / 3
    
    /** Summary card height */
    const val SUMMARY_CARD_HEIGHT = 80f
    
    // ══════════════════════════════════════════════════════════════════════
    // HELPER FUNCTIONS
    // ══════════════════════════════════════════════════════════════════════
    
    /**
     * Calculate number of table rows that fit on a page
     * @param isFirstPage True if calculating for first page (less space due to summary)
     * @param extraHeaderHeight Additional height consumed at top of content area
     */
    fun calculateRowsPerPage(isFirstPage: Boolean, extraHeaderHeight: Float = 0f): Int {
        val availableHeight = if (isFirstPage) {
            contentHeight - extraHeaderHeight
        } else {
            contentHeight
        }
        // Reserve space for table header on each page
        val rowsHeight = availableHeight - TABLE_HEADER_HEIGHT
        return (rowsHeight / TABLE_ROW_HEIGHT).toInt()
    }
    
    /**
     * Check if there's enough space for a new section
     * @param currentY Current Y position
     * @param requiredHeight Height needed for the section
     * @return True if section fits on current page
     */
    fun canFitOnPage(currentY: Float, requiredHeight: Float): Boolean {
        return currentY + requiredHeight <= contentEndY
    }
    
    /**
     * Get the starting Y position for content on a page
     * @param pageNumber Page number (1-indexed)
     * @return Starting Y coordinate
     */
    fun getContentStartY(pageNumber: Int): Float {
        // First page has header, subsequent pages start closer to top
        return if (pageNumber == 1) contentStartY else MARGIN_TOP + 20f
    }
}

/**
 * Page orientation enum
 */
enum class PageOrientation {
    PORTRAIT,
    LANDSCAPE
}

/**
 * Text alignment enum for columns
 */
enum class ColumnAlignment {
    LEFT,
    CENTER,
    RIGHT
}

/**
 * Column width specification
 */
sealed class ColumnWidth {
    /** Fixed width in points */
    data class Fixed(val width: Float) : ColumnWidth()
    
    /** Percentage of available width (0.0 to 1.0) */
    data class Percentage(val percent: Float) : ColumnWidth()
    
    /** Auto-calculated based on remaining space */
    data object Auto : ColumnWidth()
}

/**
 * Table column definition
 */
data class TableColumn(
    val header: String,
    val width: ColumnWidth,
    val alignment: ColumnAlignment = ColumnAlignment.RIGHT,
    val isAmount: Boolean = false
)
