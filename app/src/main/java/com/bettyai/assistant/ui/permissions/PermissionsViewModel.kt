package com.bettyai.assistant.ui.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bettyai.assistant.permissions.PermissionManager
import com.bettyai.assistant.permissions.PermissionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _statuses = MutableStateFlow(permissionManager.allStatuses())
    val statuses: StateFlow<List<PermissionStatus>> = _statuses.asStateFlow()

    /** Re-read the OS permission state (call after a request dialog closes). */
    fun refresh() {
        _statuses.value = permissionManager.allStatuses()
    }
}
