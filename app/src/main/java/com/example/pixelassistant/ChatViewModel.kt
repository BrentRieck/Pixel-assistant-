package com.example.pixelassistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val input: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val selectedModel: ModelCard = ModelCatalog.featuredModels.first(),
    val isSending: Boolean = false,
    val isMemoryEnabled: Boolean = true,
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(
        context = application,
        memoryStore = MemoryStore(application),
        modelManager = LiteRtModelManager(),
    )

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()
    private var sendJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initialize()
            _state.update { current ->
                current.copy(
                    messages = repository.messages.value,
                    selectedModel = repository.selectedModel(),
                )
            }
            repository.messages.collect { history ->
                _state.update { current -> current.copy(messages = history) }
            }
        }
    }

    fun updateInput(text: String) {
        _state.update { it.copy(input = text) }
    }

    fun toggleMemory(enabled: Boolean) {
        _state.update { it.copy(isMemoryEnabled = enabled) }
    }

    fun changeModel(card: ModelCard, clearHistory: Boolean = false) {
        viewModelScope.launch {
            repository.changeModel(card, clearHistory)
            _state.update { it.copy(selectedModel = card) }
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            repository.resetConversation()
        }
    }

    fun sendMessage() {
        if (state.value.input.isBlank()) return
        sendJob?.cancel()
        sendJob = viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            val prompt = state.value.input
            _state.update { it.copy(input = "") }
            repository.send(prompt, state.value.isMemoryEnabled)
            _state.update { it.copy(isSending = false) }
        }
    }
}
