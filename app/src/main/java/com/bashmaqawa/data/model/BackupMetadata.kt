package com.bashmaqawa.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupMetadata(
    val versionCode: Int,
    val versionName: String,
    val timestamp: Long,
    val checksum: String,
    val manufacturer: String,
    val model: String,
    val sdkInt: Int
)
