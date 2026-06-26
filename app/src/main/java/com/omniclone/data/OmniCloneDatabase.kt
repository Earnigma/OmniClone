package com.omniclone.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.omniclone.model.CloneConfig

/**
 * Room database for OmniClone.
 */
@Database(
    entities = [CloneConfig::class, CloneStats::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(CloneConfigConverters::class)
abstract class OmniCloneDatabase : RoomDatabase() {
    abstract fun cloneDao(): CloneDao
}
