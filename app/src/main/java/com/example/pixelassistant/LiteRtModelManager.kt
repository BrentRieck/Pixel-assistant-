package com.example.pixelassistant

import android.content.Context
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class LiteRtModelManager(
    private val httpClient: OkHttpClient = OkHttpClient.Builder().build(),
) {
    private val warmModels = ConcurrentHashMap<String, File>()

    suspend fun ensureModelIsReady(context: Context, model: ModelCard): File = withContext(Dispatchers.IO) {
        warmModels[model.id]?.let { cached ->
            return@withContext cached
        }

        val modelDir = File(context.filesDir, "models/${model.id.replace('/', '_')}")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }

        val modelFile = File(modelDir, "model.litert")
        if (!modelFile.exists()) {
            downloadModel(model, modelFile)
        }

        warmModels[model.id] = modelFile
        return@withContext modelFile
    }

    suspend fun warmUp(model: ModelCard) {
        delay(1500)
    }

    suspend fun generateReply(
        context: Context,
        model: ModelCard,
        messages: List<ChatMessage>,
        prompt: String,
    ): String {
        ensureModelIsReady(context, model)
        warmUp(model)
        val lastAssistantResponse = messages.filterIsInstance<ChatMessage.Assistant>().lastOrNull()?.content
        val contextHint = lastAssistantResponse?.let { " (continuing from previous context)" } ?: ""
        return "${model.displayName} says: ${prompt.trim()}$contextHint"
    }

    private fun downloadModel(model: ModelCard, destination: File) {
        val url = "https://huggingface.co/${model.id}/resolve/main/model.litert?download=1"
        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("Failed to fetch model: ${response.code}")
            }
            val body = response.body ?: error("Empty response body for ${model.id}")
            destination.outputStream().use { output ->
                body.byteStream().copyTo(output)
            }
        }
    }
}
