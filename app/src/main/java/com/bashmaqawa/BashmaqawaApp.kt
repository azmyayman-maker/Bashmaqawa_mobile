package com.bashmaqawa

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.bashmaqawa.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

/**
 * Bashmaqawa Application Class
 * نقطة الدخول الرئيسية للتطبيق مع تهيئة Hilt للحقن التلقائي
 */
@HiltAndroidApp
class BashmaqawaApp : Application() {
    
    override fun attachBaseContext(base: Context) {
        // Get saved language from SharedPreferences directly (before Hilt is initialized)
        val prefs = base.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
        val language = prefs.getString(LANGUAGE_KEY, "ar") ?: "ar"
        super.attachBaseContext(LocaleHelper.wrapContext(base, language))
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Set up global exception handler for debugging crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            // Let the system handle it after logging
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        // Initialize any application-level components here
    }
    
    companion object {
        private const val TAG = "BashmaqawaApp"
        const val LANGUAGE_KEY = "app_language"
        
        /**
         * Save language preference to SharedPreferences
         * حفظ تفضيل اللغة في SharedPreferences للتطبيق عند البدء
         */
        fun saveLanguage(context: Context, language: String) {
            context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString(LANGUAGE_KEY, language)
                .apply()
        }
        
        /**
         * Get saved language from SharedPreferences
         * الحصول على اللغة المحفوظة من SharedPreferences
         */
        fun getSavedLanguage(context: Context): String {
            return context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
                .getString(LANGUAGE_KEY, "ar") ?: "ar"
        }
    }
}
