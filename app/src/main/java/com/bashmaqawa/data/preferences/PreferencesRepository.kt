package com.bashmaqawa.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")

        val LANGUAGE = stringPreferencesKey("language")
        val COMPANY_NAME = stringPreferencesKey("company_name")
        val PASSWORD_HASH = stringPreferencesKey("password_hash")
        val LAST_BACKUP_TIMESTAMP = androidx.datastore.preferences.core.longPreferencesKey("last_backup_timestamp")

    }

    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] ?: false
        }


    
    val language: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE] ?: "ar"
        }
    
    val currency: Flow<String> = dataStore.data
        .map { "EGP" } // Always EGP

    
    val companyName: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.COMPANY_NAME] ?: "بشمقاول للمقاولات"
        }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = enabled
        }
    }


    
    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }
    
    

    suspend fun setCompanyName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPANY_NAME] = name
        }
    }
    
    suspend fun setPassword(password: String) {
        // Simple hash for demo - in production use proper hashing
        val hash = password.hashCode().toString()
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSWORD_HASH] = hash
        }
    }
    
    suspend fun verifyPassword(password: String): Boolean {
        val storedHash = dataStore.data.first()[PreferencesKeys.PASSWORD_HASH]
        // Default password is "1234" 
        if (storedHash == null) {
            return password == "1234"
        }
        return storedHash == password.hashCode().toString()
    }

    /**
     * Get language synchronously (for Application startup)
     * الحصول على اللغة بشكل متزامن (لبدء التطبيق)
     */
    suspend fun getLanguageSync(): String {
        return dataStore.data.first()[PreferencesKeys.LANGUAGE] ?: "ar"
    }

    val lastBackupTimestamp: Flow<Long> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_BACKUP_TIMESTAMP] ?: 0L
        }

    suspend fun setLastBackupTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BACKUP_TIMESTAMP] = timestamp
        }
    }
}
