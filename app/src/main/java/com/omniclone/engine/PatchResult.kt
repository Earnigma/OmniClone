package com.omniclone.engine

import kotlinx.serialization.Serializable

/**
 * Result type returned by manifest and smali patchers.
 */
@Serializable
sealed class PatchResult {
    @Serializable
    data class Success(val message: String = "Patch applied successfully") : PatchResult()

    @Serializable
    data class Error(val message: String, val cause: String? = null) : PatchResult()
}
