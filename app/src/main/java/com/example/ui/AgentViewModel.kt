package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.MessageEntity
import com.example.data.SettingsEntity
import com.example.service.OpenAIChatClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AgentViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java, "lgxai-db"
    ).build()

    private val chatClient = OpenAIChatClient()

    val allMessages = db.messageDao().getAllMessages().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val settings = db.settingsDao().getSettingsFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        // Initialize default settings if null
        viewModelScope.launch {
            if (db.settingsDao().getSettings() == null) {
                db.settingsDao().saveSettings(SettingsEntity())
            }
        }
    }

    fun saveSettings(newSettings: SettingsEntity) {
        viewModelScope.launch {
            db.settingsDao().saveSettings(newSettings)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            db.messageDao().clearHistory()
        }
    }

    fun sendMessage(prompt: String) {
        if (prompt.isBlank() || _isGenerating.value) return
        _isGenerating.value = true

        viewModelScope.launch {
            val userMsg = MessageEntity(role = "user", content = prompt)
            db.messageDao().insertMessage(userMsg)

            val currentSettings = settings.value ?: SettingsEntity()
            
            // Build system prompt and context if memoryLength > 0
            val recentMessages = db.messageDao().getAllMessages()
                // ... fetch recent for context (ignoring brevity here, simplify for now)

            val assistantMsg = MessageEntity(role = "assistant", content = "")
            // For simplicity with Room, we insert it first then update, but since Room auto-generates ID, we need its ID to update.
            // Actually, we can just keep state in a temporary flow, but let's insert a placeholder.
            // A better way is to collect stream in UI or update DB in chunks.
            var streamContent = ""
            val tempDisplayMsgId = System.currentTimeMillis().toInt()
            
            try {
                if (currentSettings.isStreamResponse) {
                    // Update a local state first or write to DB
                    chatClient.chatStream(
                        baseUrl = currentSettings.baseUrl,
                        apiKey = currentSettings.apiKey,
                        model = currentSettings.activeModel,
                        prompt = prompt,
                        temperature = currentSettings.temperature,
                        maxTokens = currentSettings.maxTokens.takeIf { it > 0 }
                    ).collect { chunk ->
                        streamContent += chunk
                        // Let's emit to UI via a temp state and save at the end to avoid heavy DB writes
                        _streamingMessage.value = streamContent
                    }
                    db.messageDao().insertMessage(MessageEntity(role = "assistant", content = streamContent))
                    _streamingMessage.value = null
                } else {
                    val res = chatClient.chatBlock(
                        baseUrl = currentSettings.baseUrl,
                        apiKey = currentSettings.apiKey,
                        model = currentSettings.activeModel,
                        prompt = prompt,
                        temperature = currentSettings.temperature,
                        maxTokens = currentSettings.maxTokens.takeIf { it > 0 }
                    )
                    db.messageDao().insertMessage(MessageEntity(role = "assistant", content = res))
                }
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                db.messageDao().insertMessage(MessageEntity(role = "assistant", content = errorMsg))
                _streamingMessage.value = null
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private val _streamingMessage = MutableStateFlow<String?>(null)
    val streamingMessage = _streamingMessage.asStateFlow()
}
