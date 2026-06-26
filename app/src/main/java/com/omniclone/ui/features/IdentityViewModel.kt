package com.omniclone.ui.features

import androidx.lifecycle.ViewModel
import com.omniclone.model.IdentityConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import kotlin.random.Random

/**
 * ViewModel for identity management.
 */
class IdentityViewModel : ViewModel() {

    private val _identity = MutableStateFlow(IdentityConfig())
    val identity: StateFlow<IdentityConfig> = _identity

    fun updateIdentity(update: IdentityConfig.() -> IdentityConfig) {
        _identity.value = _identity.value.update()
    }

    fun randomizeAll() {
        _identity.value = IdentityConfig(
            androidId = Random.nextLong().toString(16).padStart(16, '0'),
            imei = (100000000000000L + Random.nextLong(89999999999999L)).toString(),
            imsi = (100000000000000L + Random.nextLong(89999999999999L)).toString(),
            wifiMac = randomMac(),
            bluetoothMac = randomMac(),
            googleAdvertisingId = UUID.randomUUID().toString()
        )
    }

    private fun randomMac(): String {
        return (1..6).joinToString(":") {
            Random.nextInt(256).toString(16).padStart(2, '0')
        }
    }
}
