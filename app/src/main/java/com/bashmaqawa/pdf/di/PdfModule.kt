package com.bashmaqawa.pdf.di

import android.content.Context
import com.bashmaqawa.pdf.PdfReportEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing PDF report dependencies
 * وحدة Hilt لتوفير تبعيات تقارير PDF
 */
@Module
@InstallIn(SingletonComponent::class)
object PdfModule {
    
    @Provides
    @Singleton
    fun providePdfReportEngine(
        @ApplicationContext context: Context
    ): PdfReportEngine {
        return PdfReportEngine(context)
    }
}
