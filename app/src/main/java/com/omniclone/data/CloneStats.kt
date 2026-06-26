package com.omniclone.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for per-clone runtime statistics.
 */
@Entity(tableName = "clone_stats")
data class CloneStats(
    @PrimaryKey val cloneId: String,
    val storageBytes: Long = 0L,
    val launchCount: Int = 0,
    val lastActivity: String? = null,
    val activityLog: String? = null
)
