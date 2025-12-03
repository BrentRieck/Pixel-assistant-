package com.example.pixelassistant

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

class ChatRepository(
    private val context: Context,
    private val memoryStore: MemoryStore,
    private val modelManager: LiteRtModelManager,
) {
    private val internalMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private var currentModel: ModelCard = ModelCatalog.featuredModels.first()

    val messages = internalMessages.asStateFlow()

    suspend fun initialize() {
        val snapshot = memoryStore.stream().first()
        snapshot?.let { restore ->
            currentModel = ModelCatalog.featuredModels.find { it.id == restore.modelId } ?: currentModel
            internalMessages.value = restore.messages
        }
    }

    fun selectedModel(): ModelCard = currentModel

    suspend fun changeModel(card: ModelCard, clearHistory: Boolean) {
        currentModel = card
        if (clearHistory) {
            resetConversation()
        }
    }

    suspend fun send(prompt: String, persistMemory: Boolean): ChatMessage.Assistant {
        val updated = internalMessages.value + ChatMessage.User(prompt)
        internalMessages.value = updated
        val replyText = modelManager.generateReply(
            context = context,
            model = currentModel,
            messages = updated,
            prompt = prompt,
        )
        val assistantMessage = ChatMessage.Assistant(replyText)
        internalMessages.update { it + assistantMessage }
        if (persistMemory) {
            memoryStore.save(
                ConversationSnapshot(
                    modelId = currentModel.id,
                    messages = internalMessages.value,
                ),
            )
        }
        return assistantMessage
    }

    suspend fun resetConversation() {
        internalMessages.value = emptyList()
        memoryStore.save(null)
    }
}
