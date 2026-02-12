package com.bashmaqawa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bashmaqawa.presentation.navigation.BashmaqawaNavHost
import com.bashmaqawa.presentation.theme.BashmaqawaTheme
import com.bashmaqawa.utils.LocaleHelper
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - نشاط رئيسي للتطبيق
 * Main entry point for Bashmaqawa ERP Application
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Global ViewModel for app-wide state (Theme)
    private val mainViewModel: MainViewModel by viewModels()
    
    override fun attachBaseContext(newBase: Context) {
        // Apply saved language context wrapper
        val language = BashmaqawaApp.getSavedLanguage(newBase)
        super.attachBaseContext(LocaleHelper.wrapContext(newBase, language))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // CRITICAL: Install splash screen BEFORE super.onCreate()
        // This is required for proper splash screen lifecycle on Android 12+
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Set up splash screen condition - check if content is ready
        splashScreen.setKeepOnScreenCondition {
            mainViewModel.startDestination.value == null
        }
        
        setContent {
            val isDarkTheme by mainViewModel.isDarkMode.collectAsState(initial = false)
            val startDestination by mainViewModel.startDestination.collectAsState()
            
            // Get current language from our helper to determine direction
            val currentLang = BashmaqawaApp.getSavedLanguage(this)
            val layoutDirection = if (LocaleHelper.isRtl(currentLang)) LayoutDirection.Rtl else LayoutDirection.Ltr
            
            // Only render when we have a destination
            startDestination?.let { destination ->
                BashmaqawaTheme(darkTheme = isDarkTheme) {
                    // Dynamic Layout Direction based on Language
                    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            BashmaqawaNavHost(startDestination = destination)
                        }
                    }
                }
            }
        }
    }
}
