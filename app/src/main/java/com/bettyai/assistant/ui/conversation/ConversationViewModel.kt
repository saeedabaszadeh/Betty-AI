package com.bettyai.assistant.ui.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bettyai.assistant.core.BettyCore
import com.bettyai.assistant.core.state.BettyState
import com.bettyai.assistant.domain.model.Message
import com.bettyai.assistant.domain.repository.ConversationRepository
import com.bettyai.assistant.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationUiState(
    val conversationId: Long = 0L,
    val title: String = "",
    val messages: List<Message> = emptyList(),
    val bettyState: BettyState = BettyState.IDLE,
    val displayName: String = "",
    val inputText: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bettyCore: BettyCore,
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val conversationId: Long = savedStateHandle.get<String>("conversationId")?.toLongOrNull() ?: 0L
    private val input = MutableStateFlow("")

    private val messages = flowOf(conversationId)
        .flatMapLatest { id ->
            if (id > 0) conversationRepository.observeMessages(id) else flowOf(emptyList())
        }

    val uiState: StateFlow<ConversationUiState> = combine(
        messages,
        bettyCore.state,
        userRepository.observeProfile(),
        input
    ) { msgs, state, profile, text ->
        ConversationUiState(
            conversationId = conversationId,
            title = msgs.firstOrNull()?.let { "" } ?: "",
            messages = msgs,
            bettyState = state,
            displayName = profile?.displayName.orEmpty(),
            inputText = text
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ConversationUiState(conversationId = conversationId)
    )

    fun onInputChange(value: String) {
        input.value = value
    }

    fun sendText() {
        val text = input.value.trim()
        if (text.isEmpty()) return
        input.value = ""
        bettyCore.submitText(text)
    }

    fun startVoiceTurn() = bettyCore.startVoiceTurn()

    fun stop() = bettyCore.stop()

    fun clearHistory() {
        viewModelScope.launch {
            if (conversationId > 0) conversationRepository.deleteConversation(conversationId)
        }
    }
}
