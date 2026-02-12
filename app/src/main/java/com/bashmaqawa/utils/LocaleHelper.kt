package com.bashmaqawa.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * LocaleHelper - مساعد إدارة اللغات
 * Professional locale management utility for the app
 */
object LocaleHelper {

    /**
     * Apply locale to a context and return the wrapped context
     * تطبيق اللغة على Context وإرجاع Context جديد مغلف
     */
    fun wrapContext(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        config.setLayoutDirection(locale)
        
        return context.createConfigurationContext(config)
    }

    /**
     * Update the configuration of an existing context
     * تحديث إعدادات Context موجود
     */
    fun updateConfiguration(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        config.setLayoutDirection(locale)
        
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /**
     * Restart the activity with a smooth transition
     * إعادة تشغيل Activity بانتقال سلس
     */
    fun restartActivity(activity: Activity) {
        val intent = Intent(activity, activity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.finish()
        activity.startActivity(intent)
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /**
     * Restart the entire application
     * إعادة تشغيل التطبيق بالكامل
     */
    fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    /**
     * Get the layout direction for a language
     * الحصول على اتجاه الواجهة للغة معينة
     */
    fun isRtl(language: String): Boolean {
        return language == "ar" || language == "he" || language == "fa" || language == "ur"
    }

    /**
     * Find the activity from a context
     * البحث عن Activity من Context
     */
    fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }
}
