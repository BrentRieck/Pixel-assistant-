package com.example.pixelassistant

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "chat_memory")

class MemoryStore(private val context: Context) {
    private val snapshotKey = stringPreferencesKey("snapshot")
    private val json = Json { ignoreUnknownKeys = true }

    fun stream(): Flow<ConversationSnapshot?> = context.dataStore.data.map { prefs ->
        prefs[snapshotKey]?.let { stored ->
            runCatching { json.decodeFromString(ConversationSnapshot.serializer(), stored) }.getOrNull()
        }
    }

    suspend fun save(snapshot: ConversationSnapshot?) {
        context.dataStore.edit { prefs ->
            if (snapshot == null) {
                prefs.remove(snapshotKey)
            } else {
                prefs[snapshotKey] = json.encodeToString(ConversationSnapshot.serializer(), snapshot)
            }
        }
    }
}
