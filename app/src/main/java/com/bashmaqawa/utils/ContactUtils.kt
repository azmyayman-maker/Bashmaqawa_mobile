package com.bashmaqawa.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Contact Utilities for Phone, WhatsApp, and SMS
 * أدوات الاتصال للهاتف وواتساب والرسائل
 */
object ContactUtils {
    
    /**
     * Make a phone call
     * إجراء مكالمة هاتفية
     */
    fun makePhoneCall(context: Context, phoneNumber: String?) {
        if (phoneNumber.isNullOrBlank()) {
            Toast.makeText(context, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "حدث خطأ في فتح تطبيق الهاتف", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Open WhatsApp chat
     * فتح محادثة واتساب
     */
    fun openWhatsApp(context: Context, phoneNumber: String?, message: String = "") {
        if (phoneNumber.isNullOrBlank()) {
            Toast.makeText(context, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Format phone number (remove spaces, dashes, add country code if needed)
        val formattedNumber = formatPhoneNumber(phoneNumber)
        
        try {
            val url = if (message.isNotEmpty()) {
                "https://wa.me/$formattedNumber?text=${Uri.encode(message)}"
            } else {
                "https://wa.me/$formattedNumber"
            }
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "واتساب غير مثبت على الجهاز", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Send SMS
     * إرسال رسالة نصية
     */
    fun sendSms(context: Context, phoneNumber: String?, message: String = "") {
        if (phoneNumber.isNullOrBlank()) {
            Toast.makeText(context, "رقم الهاتف غير متوفر", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phoneNumber")
                putExtra("sms_body", message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "حدث خطأ في فتح تطبيق الرسائل", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Share worker statement via any app
     * مشاركة كشف حساب العامل
     */
    fun shareText(context: Context, text: String, title: String = "مشاركة") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
    
    /**
     * Format phone number for international use
     * تنسيق رقم الهاتف للاستخدام الدولي
     */
    private fun formatPhoneNumber(phone: String): String {
        // Remove all non-digit characters except +
        var cleaned = phone.replace(Regex("[^+\\d]"), "")
        
        // If starts with 0, replace with Egypt country code
        if (cleaned.startsWith("0")) {
            cleaned = "20${cleaned.substring(1)}"
        }
        
        // If doesn't start with +, assume it needs country code
        if (!cleaned.startsWith("+") && !cleaned.startsWith("20")) {
            cleaned = "20$cleaned"
        }
        
        return cleaned.removePrefix("+")
    }
    
    /**
     * Generate salary statement text for sharing
     * إنشاء نص كشف الراتب للمشاركة
     */
    fun generateSalaryStatement(
        workerName: String,
        totalEarnings: Double,
        totalAdvances: Double,
        totalDeductions: Double,
        netSalary: Double,
        periodStart: String,
        periodEnd: String
    ): String {
        return buildString {
            appendLine("═══════════════════════════")
            appendLine("    كشف حساب العامل    ")
            appendLine("═══════════════════════════")
            appendLine()
            appendLine("الاسم: $workerName")
            appendLine("الفترة: من $periodStart إلى $periodEnd")
            appendLine()
            appendLine("───────────────────────────")
            appendLine("المستحقات: ${CurrencyFormatter.format(totalEarnings)}")
            appendLine("السلف: ${CurrencyFormatter.format(totalAdvances)}")
            appendLine("الخصومات: ${CurrencyFormatter.format(totalDeductions)}")
            appendLine("───────────────────────────")
            appendLine("الصافي: ${CurrencyFormatter.format(netSalary)}")
            appendLine("═══════════════════════════")
            appendLine()
            appendLine("تم الإنشاء بواسطة بشمقاول")
        }
    }
}
