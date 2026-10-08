package com.bettyai.assistant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bettyai.assistant.core.BettyCore
import com.bettyai.assistant.core.state.BettyState
import com.bettyai.assistant.core.state.BettyStatus
import com.bettyai.assistant.domain.model.AppSettings
import com.bettyai.assistant.domain.model.Conversation
import com.bettyai.assistant.domain.repository.ConversationRepository
import com.bettyai.assistant.domain.repository.UserRepository
import com.bettyai.assistant.permissions.PermissionManager
import com.bettyai.assistant.settings.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Aggregated state rendered by [HomeScreen]. */
data class HomeUiState(
    val bettyState: BettyState = BettyState.IDLE,
    val status: BettyStatus = BettyStatus(),
    val displayName: String = "",
    val recentConversations: List<Conversation> = emptyList(),
    val hasMicrophonePermission: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val isInitialized: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bettyCore: BettyCore,
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository,
    private val settingsManager: SettingsManager,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val initialized = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        bettyCore.status,
        conversationRepository.observeConversations(limit = 5),
        userRepository.observeProfile(),
        settingsManager.settings,
        initialized
    ) { status, conversations, profile, settings, isInit ->
        HomeUiState(
            bettyState = status.state,
            status = status,
            displayName = profile?.displayName.orEmpty(),
            recentConversations = conversations,
            hasMicrophonePermission = permissionManager.hasMicrophone(),
            settings = settings,
            isInitialized = isInit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            bettyCore.initialize()
            initialized.value = true
        }
    }

    /** Called when the user taps the central button. */
    fun onTalkPressed() {
        if (bettyCore.state.value.isBusy) {
            bettyCore.stop()
        } else {
            bettyCore.startVoiceTurn()
        }
    }

    fun onStop() = bettyCore.stop()

    /** Re-check permissions (e.g. after the user returns from a system dialog). */
    fun refreshPermissions() {
        viewModelScope.launch { /* state recomputes on next emission */ }
    }

    fun setBettyEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsManager.setBettyEnabled(enabled) }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch { conversationRepository.deleteConversation(id) }
    }

    fun saveDisplayName(name: String) {
        viewModelScope.launch {
            userRepository.setDisplayName(name.trim())
            settingsManager.setUserDisplayName(name.trim())
        }
    }
}
