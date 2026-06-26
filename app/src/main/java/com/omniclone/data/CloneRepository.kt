package com.omniclone.data

import com.omniclone.model.CloneConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository exposing clone configurations and stats.
 */
@Singleton
class CloneRepository @Inject constructor(
    private val dao: CloneDao
) {
    fun getAllClones(): Flow<List<CloneConfig>> = dao.getAllClones()

    suspend fun getCloneById(cloneId: String): CloneConfig? = dao.getCloneById(cloneId)

    suspend fun getClonesByOriginalPackage(packageName: String): List<CloneConfig> =
        dao.getClonesByOriginalPackage(packageName)

    suspend fun getNextCloneIndex(packageName: String): Int {
        return (dao.getMaxCloneIndex(packageName) ?: 0) + 1
    }

    suspend fun saveClone(clone: CloneConfig) {
        dao.insertClone(clone)
    }

    suspend fun updateClone(clone: CloneConfig) {
        dao.updateClone(clone)
    }

    suspend fun deleteClone(cloneId: String) {
        dao.deleteClone(cloneId)
        dao.deleteStats(cloneId)
    }

    suspend fun getStats(cloneId: String): CloneStats? = dao.getCloneStats(cloneId)

    suspend fun saveStats(stats: CloneStats) {
        dao.insertStats(stats)
    }
}
