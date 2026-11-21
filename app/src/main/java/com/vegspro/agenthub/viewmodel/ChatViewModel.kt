package com.vegspro.agenthub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vegspro.agenthub.data.AppSettings
import com.vegspro.agenthub.data.ChatMessage
import com.vegspro.agenthub.data.SettingsRepository
import com.vegspro.agenthub.network.OpenAIClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepo = SettingsRepository(application)
    private var client = OpenAIClient("https://api.openai.com/v1", "")

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepo.settings.collect { s ->
                _settings.value = s
                client.updateCredentials(s.apiBaseUrl, s.apiKey)
            }
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        _error.value = null
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val s = settingsRepo.settings.first()
                client.updateCredentials(s.apiBaseUrl, s.apiKey)

                val userMsg = ChatMessage(role = "user", content = text)
                val current = _messages.value.toMutableList()
                
                // Ensure system prompt is first
                if (current.none { it.role == "system" }) {
                    current.add(0, ChatMessage(role = "system", content = s.systemPrompt))
                }
                current.add(userMsg)
                _messages.value = current

                if (s.enableTools) {
                    val result = client.agentLoop(
                        initialMessages = current.toMutableList(),
                        model = s.defaultModel,
                        temperature = s.temperature,
                        maxTokens = s.maxTokens,
                        onUpdate = { updated ->
                            _messages.value = updated
                        }
                    )
                    _messages.value = result
                } else {
                    val response = client.chat(
                        messages = current,
                        model = s.defaultModel,
                        temperature = s.temperature,
                        maxTokens = s.maxTokens,
                        enableTools = false
                    )
                    _messages.value = current + response
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            settingsRepo.update(newSettings)
        }
    }
}
