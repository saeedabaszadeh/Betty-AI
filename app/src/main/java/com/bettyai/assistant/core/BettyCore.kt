package com.bettyai.assistant.core

import com.bettyai.assistant.ai.AIEngine
import com.bettyai.assistant.core.event.BettyEvent
import com.bettyai.assistant.core.event.EventBus
import com.bettyai.assistant.core.logger.BettyLogger
import com.bettyai.assistant.core.result.BettyError
import com.bettyai.assistant.core.state.BettyState
import com.bettyai.assistant.core.state.BettyStatus
import com.bettyai.assistant.domain.model.MessageRole
import com.bettyai.assistant.domain.model.SpeechResult
import com.bettyai.assistant.domain.model.TtsResult
import com.bettyai.assistant.domain.provider.NetworkMonitor
import com.bettyai.assistant.domain.repository.ConversationRepository
import com.bettyai.assistant.domain.repository.UserRepository
import com.bettyai.assistant.memory.MemoryManager
import com.bettyai.assistant.permissions.PermissionManager
import com.bettyai.assistant.router.CommandRouter
import com.bettyai.assistant.router.RouteDecision
import com.bettyai.assistant.router.TextNormalizer
import com.bettyai.assistant.settings.SettingsManager
import com.bettyai.assistant.skills.SkillContext
import com.bettyai.assistant.skills.SkillInput
import com.bettyai.assistant.skills.SkillManager
import com.bettyai.assistant.voice.TtsManager
import com.bettyai.assistant.voice.VoiceEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The heart of Betty.
 *
 * [BettyCore] wires the engines together and owns the state machine. It is the
 * single entry point the UI talks to; everything else (voice, AI, TTS, skills,
 * memory, settings, permissions, notifications) hangs off it and can be extended
 * without modifying this class.
 */
@Singleton
class BettyCore @Inject constructor(
    private val voiceEngine: VoiceEngine,
    private val ttsManager: TtsManager,
    private val aiEngine: AIEngine,
    private val commandRouter: CommandRouter,
    private val skillManager: SkillManager,
    private val memoryManager: MemoryManager,
    private val settingsManager: SettingsManager,
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository,
    private val permissionManager: PermissionManager,
    private val networkMonitor: NetworkMonitor,
    private val eventBus: EventBus,
    private val logger: BettyLogger
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _status = MutableStateFlow(BettyStatus())
    val status: StateFlow<BettyStatus> = _status.asStateFlow()

    val state: StateFlow<BettyState> = _status
        .map { it.state }
        .stateIn(scope, SharingStarted.Eagerly, BettyState.IDLE)

    private var turnJob: Job? = null

    @Volatile
    private var bootstrapped = false

    /** Prepare the engine: register skills and start observing connectivity/settings. */
    suspend fun initialize() {
        if (bootstrapped) return
        bootstrapped = true

        skillManager.ensureInitialized()
        val settings = settingsManager.current()
        setState(if (settings.bettyEnabled) BettyState.IDLE else BettyState.DISABLED)

        scope.launch {
            networkMonitor.online.collect { online ->
                _status.value = _status.value.copy(isOnline = online)
                eventBus.publish(BettyEvent.NetworkChanged(online))
            }
        }

        scope.launch {
            settingsManager.settings.collect { s ->
                if (!s.bettyEnabled && _status.value.state != BettyState.DISABLED) {
                    setState(BettyState.DISABLED)
                } else if (s.bettyEnabled && _status.value.state == BettyState.DISABLED) {
                    setState(BettyState.IDLE)
                }
            }
        }
    }

    fun isEnabled(): Boolean = _status.value.state != BettyState.DISABLED

    /** Start a full voice turn (listen → understand → respond → speak). */
    fun startVoiceTurn() {
        if (turnJob?.isActive == true) return
        turnJob = scope.launch { safeTurn { runVoiceTurn() } }
    }

    /** Process a typed utterance (from the conversation screen). */
    fun submitText(text: String) {
        if (text.isBlank() || turnJob?.isActive == true) return
        turnJob = scope.launch { safeTurn { runTextTurn(text) } }
    }

    /** Cancel any in-flight turn and stop speaking. */
    fun stop() {
        turnJob?.cancel()
        voiceEngine.cancel()
        ttsManager.stop()
        setState(BettyState.IDLE)
    }

    fun shutdown() {
        scope.cancel()
        ttsManager.shutdown()
    }

    // ---- internals ----

    private suspend fun safeTurn(block: suspend () -> Unit) {
        try {
            val settings = settingsManager.current()
            if (!settings.bettyEnabled) {
                setState(BettyState.DISABLED)
                return
            }
            block()
        } catch (t: Throwable) {
            logger.error(TAG, "Turn failed", t)
            handleError(BettyError.Unknown(t.message ?: "", t))
        }
    }

    private suspend fun runVoiceTurn() {
        setState(BettyState.LISTENING)
        when (val result = voiceEngine.listen()) {
            is SpeechResult.Success -> runTextTurn(result.text)
            SpeechResult.NoMatch -> setState(BettyState.IDLE, "چیزی نشنیدم")
            SpeechResult.Cancelled -> setState(BettyState.IDLE)
            is SpeechResult.Failure -> handleError(result.error)
        }
    }

    private suspend fun runTextTurn(text: String) {
        setState(BettyState.PROCESSING)

        val settings = settingsManager.current()
        val conversation = conversationRepository.getActiveConversation()
        val priorHistory = conversationRepository.latestMessages(HISTORY_LIMIT)

        conversationRepository.addMessage(conversation.id, MessageRole.USER, text)
        eventBus.publish(BettyEvent.UserSpoke(text))

        var response = ""
        var skillId: String? = null

        when (val decision = commandRouter.route(text)) {
            is RouteDecision.SkillMatch -> {
                val context = SkillContext(
                    userDisplayName = userRepository.displayName(),
                    languageTag = settings.languageTag,
                    memoryManager = memoryManager
                )
                val input = SkillInput(
                    rawText = text,
                    normalizedText = TextNormalizer.normalize(text),
                    matchedIntentId = decision.intentId,
                    languageTag = settings.languageTag
                )
                val result = skillManager.execute(decision.skillId, input, context)
                response = result.spokenText
                skillId = decision.skillId
                eventBus.publish(BettyEvent.SkillExecuted(decision.skillId, result.handled))
            }

            RouteDecision.AiFallback -> {
                val ai = aiEngine.respond(text, priorHistory)
                response = ai.text
            }
        }

        conversationRepository.addMessage(conversation.id, MessageRole.BETTY, response, skillId)
        eventBus.publish(BettyEvent.BettyReplied(response, skillId))

        setState(BettyState.SPEAKING)
        val tts = ttsManager.speak(response)
        if (tts is TtsResult.Failure) {
            logger.warning(TAG, "TTS failed: ${tts.error.code}")
        }
        setState(BettyState.IDLE)
    }

    private suspend fun handleError(error: BettyError) {
        logger.warning(TAG, "Error ${error.code}: ${error.message}")
        _status.value = _status.value.copy(state = BettyState.ERROR, lastError = error.userMessage)
        eventBus.publish(BettyEvent.ErrorOccurred(error.code, error.userMessage))
        runCatching { ttsManager.speak(error.userMessage) }
        _status.value = _status.value.copy(state = BettyState.IDLE)
    }

    private fun setState(state: BettyState, detail: String? = null) {
        _status.value = _status.value.copy(state = state, detail = detail)
        eventBus.publish(BettyEvent.StateChanged(state))
    }

    private companion object {
        const val TAG = "BettyCore"
        const val HISTORY_LIMIT = 12
    }
}
