package com.onlyone.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Minimal local snapshot of a chain.
 * - contributorsCsv: comma-separated UIDs (no author/step pairing)
 * - createdDay: ISO date (UTC) like "2025-10-14"
 * - savedAt: millis for local sorting
 */
@Entity(tableName = "saved_chains")
data class SavedChainEntity(
    @PrimaryKey val chainId: String,
    val title: String?,
    val message: String,          // Full message: Title (if present) + all lines
    val contributorsCsv: String,  // Comma-separated display names (fallback to UIDs)
    val createdDay: String,       // YYYY-MM-DD (local or UTC as you prefer)
    val savedAt: Long             // epochMillis
)
