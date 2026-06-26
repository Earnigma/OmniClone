package com.omniclone.data

import androidx.room.TypeConverter
import com.omniclone.model.AutomationSequence
import com.omniclone.model.FeatureKey
import com.omniclone.model.IdentityConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room type converters for [CloneConfig] complex fields.
 */
class CloneConfigConverters {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    @TypeConverter
    fun fromFeatureMap(map: Map<FeatureKey, String>): String {
        return json.encodeToString(map)
    }

    @TypeConverter
    fun toFeatureMap(value: String): Map<FeatureKey, String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromIdentity(identity: IdentityConfig): String {
        return json.encodeToString(identity)
    }

    @TypeConverter
    fun toIdentity(value: String): IdentityConfig {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            IdentityConfig()
        }
    }

    @TypeConverter
    fun fromAutomations(list: List<AutomationSequence>): String {
        return json.encodeToString(list)
    }

    @TypeConverter
    fun toAutomations(value: String): List<AutomationSequence> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
