package com.omniclone.ui.clone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omniclone.data.CloneRepository
import com.omniclone.data.CloneStats
import com.omniclone.model.CloneConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the clone manager dashboard.
 */
@HiltViewModel
class CloneManagerViewModel @Inject constructor(
    private val repository: CloneRepository
) : ViewModel() {

    val clones: StateFlow<List<CloneConfig>> = repository.getAllClones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteClone(cloneId: String) {
        viewModelScope.launch {
            repository.deleteClone(cloneId)
        }
    }

    fun getStats(cloneId: String, onResult: (CloneStats?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.getStats(cloneId))
        }
    }
}
