package com.omniclone.ui.config

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.omniclone.data.CloneRepository
import com.omniclone.engine.CloneEngine
import com.omniclone.model.CloneConfig
import com.omniclone.model.FeatureKey
import com.omniclone.model.IdentityConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the clone creation wizard and configuration screen.
 */
@HiltViewModel
class CloneConfigViewModel @Inject constructor(
    private val repository: CloneRepository,
    private val workManager: WorkManager,
    private val packageManager: PackageManager
) : ViewModel() {

    private val _apps = MutableStateFlow<List<ApplicationInfo>>(emptyList())
    val apps: StateFlow<List<ApplicationInfo>> = _apps

    private val _selectedApp = MutableStateFlow<ApplicationInfo?>(null)
    val selectedApp: StateFlow<ApplicationInfo?> = _selectedApp

    private val _features = MutableStateFlow<Map<FeatureKey, String>>(emptyMap())
    val features: StateFlow<Map<FeatureKey, String>> = _features

    private val _identity = MutableStateFlow(IdentityConfig())
    val identity: StateFlow<IdentityConfig> = _identity

    private val _cloneName = MutableStateFlow("")
    val cloneName: StateFlow<String> = _cloneName

    fun loadInstalledApps(includeSystem: Boolean) {
        viewModelScope.launch {
            val flags = PackageManager.GET_META_DATA
            val installed = packageManager.getInstalledApplications(flags)
                .filter { includeSystem || (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .sortedBy { it.loadLabel(packageManager).toString() }
            _apps.value = installed
        }
    }

    fun selectApp(app: ApplicationInfo) {
        _selectedApp.value = app
        if (_cloneName.value.isBlank()) {
            _cloneName.value = app.loadLabel(packageManager).toString()
        }
    }

    fun setCloneName(name: String) {
        _cloneName.value = name
    }

    fun toggleFeature(key: FeatureKey, enabled: Boolean, value: String? = null) {
        _features.value = _features.value.toMutableMap().apply {
            if (enabled) {
                put(key, value ?: "true")
            } else {
                remove(key)
            }
        }
    }

    fun setFeatureValue(key: FeatureKey, value: String) {
        _features.value = _features.value.toMutableMap().apply {
            put(key, value)
        }
    }

    fun setIdentity(identity: IdentityConfig) {
        _identity.value = identity
    }

    fun startClone() {
        val app = _selectedApp.value ?: return
        val name = _cloneName.value.ifBlank { app.loadLabel(packageManager).toString() }

        viewModelScope.launch {
            val index = repository.getNextCloneIndex(app.packageName)
            val cloneId = UUID.randomUUID().toString()
            val clonePackage = "com.omniclone.${app.packageName.replace(".", "_")}.$index"

            val config = CloneConfig(
                cloneId = cloneId,
                originalPackage = app.packageName,
                cloneName = name,
                clonePackage = clonePackage,
                cloneIndex = index,
                features = _features.value,
                identity = _identity.value
            )

            repository.saveClone(config)

            workManager.enqueue(
                androidx.work.OneTimeWorkRequestBuilder<CloneEngine>()
                    .setInputData(CloneEngine.inputData(config))
                    .addTag("clone_$cloneId")
                    .build()
            )
        }
    }
}
