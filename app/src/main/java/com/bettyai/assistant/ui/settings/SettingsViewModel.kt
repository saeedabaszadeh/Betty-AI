package com.bettyai.assistant.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bettyai.assistant.BuildConfig
import com.bettyai.assistant.domain.model.AppSettings
import com.bettyai.assistant.domain.model.SkillInfo
import com.bettyai.assistant.domain.model.VoiceInfo
import com.bettyai.assistant.settings.SettingsManager
import com.bettyai.assistant.skills.SkillManager
import com.bettyai.assistant.voice.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val skills: List<SkillInfo> = emptyList(),
    val ttsVoices: List<VoiceInfo> = emptyList(),
    val appVersion: String = BuildConfig.VERSION_NAME,
    val wakeWordSupported: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val skillManager: SkillManager,
    private val ttsManager: TtsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.settings.collect { s ->
                _uiState.update { it.copy(settings = s) }
            }
        }
        viewModelScope.launch {
            skillManager.ensureInitialized()
            _uiState.update { it.copy(skills = skillManager.infos()) }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(ttsVoices = ttsManager.availableVoices()) }
        }
    }

    fun setBettyEnabled(enabled: Boolean) = viewModelScope.launch { settingsManager.setBettyEnabled(enabled) }
    fun setVoiceEnabled(enabled: Boolean) = viewModelScope.launch { settingsManager.setVoiceEnabled(enabled) }
    fun setWakeWordEnabled(enabled: Boolean) = viewModelScope.launch { settingsManager.setWakeWordEnabled(enabled) }
    fun setLanguage(tag: String) = viewModelScope.launch { settingsManager.setLanguage(tag) }
    fun setTtsVoice(name: String?) = viewModelScope.launch { settingsManager.setTtsVoice(name) }
    fun setAiProvider(id: String) = viewModelScope.launch { settingsManager.setAiProvider(id) }
    fun setAiBaseUrl(url: String) = viewModelScope.launch { settingsManager.setAiBaseUrl(url) }
    fun setAiModel(model: String) = viewModelScope.launch { settingsManager.setAiModel(model) }
    fun setUserDisplayName(name: String) = viewModelScope.launch { settingsManager.setUserDisplayName(name) }

    fun setApiKey(key: String) = viewModelScope.launch {
        settingsManager.setApiKey(key)
    }

    fun clearApiKey() = viewModelScope.launch { settingsManager.setApiKey(null) }

    fun setSkillEnabled(id: String, enabled: Boolean) = viewModelScope.launch {
        skillManager.setEnabled(id, enabled)
        _uiState.update { it.copy(skills = skillManager.infos()) }
    }
}
