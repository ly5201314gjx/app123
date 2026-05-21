package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.MessageEntity
import com.example.data.SettingsEntity
import com.example.service.OpenAIChatClient
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    ).fallbackToDestructiveMigration().build()

    var isBottomBarVisible by mutableStateOf(false)

    private val chatClient = OpenAIChatClient()

    val allMessages = db.messageDao().getAllMessages().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allSettings = db.settingsDao().getAllSettingsFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _activeSettingsId = MutableStateFlow<Int?>(null)
    val activeSettingsId: StateFlow<Int?> = _activeSettingsId.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        viewModelScope.launch {
            if (db.settingsDao().getFirstSettings() == null) {
                val defaultSettings = SettingsEntity(modelNameDisplay = "GPT-4o")
                db.settingsDao().saveSettings(defaultSettings)
            }
            db.settingsDao().getAllSettingsFlow().collect { list ->
                if (list.isNotEmpty() && _activeSettingsId.value == null) {
                    _activeSettingsId.value = list.first().id
                }
            }
        }
    }

    fun setActiveSettingsId(id: Int) {
        _activeSettingsId.value = id
    }

    fun deleteSettings(settings: SettingsEntity) {
        viewModelScope.launch {
            db.settingsDao().deleteSettings(settings)
            val list = db.settingsDao().getFirstSettings()
            if (list != null) {
                if (_activeSettingsId.value == settings.id) {
                    _activeSettingsId.value = list.id
                }
            } else {
                _activeSettingsId.value = null
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

            val currentSettings = allSettings.value.find { it.id == _activeSettingsId.value } ?: allSettings.value.firstOrNull() ?: SettingsEntity()
            
            // Build system prompt and context if memoryLength > 0
            val allMsg = allMessages.value
            val recentMessages = if (currentSettings.memoryLength > 0) {
                // allMsg might not contain the message we just inserted, since it's a flow and might not have emitted yet. 
                // So we append the userMsg directly if it's not present.
                val contextList = allMsg.toMutableList()
                contextList.add(userMsg)
                contextList.takeLast(currentSettings.memoryLength)
            } else {
                listOf(userMsg)
            }

            var streamContent = ""
            
            try {
                val toolDefinitions = com.example.ui.screens.coreAgentTools.joinToString("\n") { "- ${it.id}: ${it.description}" }
                val systemPrompt = """
                    You are a highly capable intelligent agent executing a continuous ReAct loop architecture.
                    You have access to the following autonomous tools:
                    $toolDefinitions
                    
                    When processing user requests, adhere strictly to this structural protocol:
                    
                    1. THOUGHT PROCESS: 
                       Always construct a step-by-step evaluation inside <think> ... </think> tags before acting.
                       Break down the request, define dependencies, and evaluate tool requirements.
                       
                    2. TOOL CHAIN INVOCATION:
                       To trigger a tool, you MUST emit exactly:
                       <tool_call name="tool_id">json_arguments_here</tool_call>
                       (Stop generating and wait for the system to inject tool execution results).
                       
                    3. FINAL RESPONSE:
                       Once all tools resolve and information is gathered, synthesize your final response outside the <think> tags.
                """.trimIndent()

                if (currentSettings.isStreamResponse) {
                    chatClient.chatStream(
                        baseUrl = currentSettings.baseUrl,
                        apiKey = currentSettings.apiKey,
                        model = currentSettings.activeModel,
                        allMessages = recentMessages,
                        temperature = currentSettings.temperature,
                        maxTokens = currentSettings.maxTokens.takeIf { it > 0 },
                        systemPrompt = systemPrompt
                    ).collect { chunk ->
                        streamContent += chunk
                        _streamingMessage.value = streamContent
                    }
                    db.messageDao().insertMessage(MessageEntity(role = "assistant", content = streamContent))
                    _streamingMessage.value = null
                } else {
                    val res = chatClient.chatBlock(
                        baseUrl = currentSettings.baseUrl,
                        apiKey = currentSettings.apiKey,
                        model = currentSettings.activeModel,
                        allMessages = recentMessages,
                        temperature = currentSettings.temperature,
                        maxTokens = currentSettings.maxTokens.takeIf { it > 0 },
                        systemPrompt = systemPrompt
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
