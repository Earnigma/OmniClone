package com.omniclone.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.omniclone.model.CloneConfig
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for clone configurations and statistics.
 */
@Dao
interface CloneDao {

    @Query("SELECT * FROM clones ORDER BY installedAt DESC")
    fun getAllClones(): Flow<List<CloneConfig>>

    @Query("SELECT * FROM clones WHERE cloneId = :cloneId LIMIT 1")
    suspend fun getCloneById(cloneId: String): CloneConfig?

    @Query("SELECT * FROM clones WHERE originalPackage = :packageName ORDER BY cloneIndex DESC")
    suspend fun getClonesByOriginalPackage(packageName: String): List<CloneConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClone(clone: CloneConfig)

    @Update
    suspend fun updateClone(clone: CloneConfig)

    @Query("DELETE FROM clones WHERE cloneId = :cloneId")
    suspend fun deleteClone(cloneId: String)

    @Query("SELECT MAX(cloneIndex) FROM clones WHERE originalPackage = :packageName")
    suspend fun getMaxCloneIndex(packageName: String): Int?

    @Query("SELECT * FROM clone_stats WHERE cloneId = :cloneId LIMIT 1")
    suspend fun getCloneStats(cloneId: String): CloneStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: CloneStats)

    @Update
    suspend fun updateStats(stats: CloneStats)

    @Query("DELETE FROM clone_stats WHERE cloneId = :cloneId")
    suspend fun deleteStats(cloneId: String)
}
