package com.omniclone.engine

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Sealed result of the clone pipeline. Serialized to JSON when returned as WorkManager output Data.
 */
@Serializable
sealed class CloneResult {
    abstract val message: String

    @Serializable
    data class Success(
        val signedApkPath: String,
        val installedPackage: String,
        override val message: String = "Clone installed successfully"
    ) : CloneResult()

    @Serializable
    data class ExtractError(
        override val message: String,
        val cause: String? = null
    ) : CloneResult()

    @Serializable
    data class DecodeError(
        override val message: String,
        val cause: String? = null
    ) : CloneResult()

    @Serializable
    data class ManifestPatchError(
        override val message: String,
        val cause: String? = null
    ) : CloneResult()

    @Serializable
    data class SmaliPatchError(
        override val message: String,
        val cause: String? = null
    ) : CloneResult()

    @Serializable
    data class RebuildError(
        override val message: String,
        val cause: String? = null
    ) : CloneResult()

    @Serializable
    data class ZipAlignError(
        override val message: String,
        val cause: String? = null
    ) : CloneResult()

    @Serializable
    data class SignError(
        override val message: String,
        val cause: String? = null
    ) : CloneResult()

    @Serializable
    data class InstallError(
        override val message: String,
        val cause: String? = null
    ) : CloneResult()

    @Serializable
    data class Cancelled(
        override val message: String = "Clone operation was cancelled"
    ) : CloneResult()

    fun toJson(): String = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String): CloneResult = Json.decodeFromString(json)
    }
}
