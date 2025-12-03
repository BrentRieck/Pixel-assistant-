package com.example.pixelassistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ChatMessage {
    abstract val content: String

    @Serializable
    @SerialName("user")
    data class User(override val content: String) : ChatMessage()

    @Serializable
    @SerialName("assistant")
    data class Assistant(override val content: String) : ChatMessage()
}

@Serializable
data class ConversationSnapshot(
    val modelId: String,
    val messages: List<ChatMessage>,
)
