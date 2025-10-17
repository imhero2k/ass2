package com.example.fit5046_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fit5046_app.service.OllamaService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatBotUiState(
    val messages: List<ChatMessage> = listOf(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isConnected: Boolean = false
)

class ChatBotViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatBotUiState())
    val uiState: StateFlow<ChatBotUiState> = _uiState.asStateFlow()

    init {
        // Add welcome message
        addMessage(
            ChatMessage(
                text = "Hello! I'm Diabetica, your AI assistant for diabetes management. I can help you with medication reminders, blood sugar monitoring, diet advice, exercise recommendations, and general diabetes care. How can I assist you today?",
                isUser = false
            )
        )

        // Test connection
        testConnection()
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val currentInput = _uiState.value.inputText.trim()
        if (currentInput.isBlank() || _uiState.value.isLoading) return

        // Add user message
        addMessage(ChatMessage(text = currentInput, isUser = true))

        // Clear input and set loading
        _uiState.value = _uiState.value.copy(
            inputText = "",
            isLoading = true
        )

        // Get AI response
        viewModelScope.launch {
            try {
                val response = OllamaService.generateResponse(currentInput)
                addMessage(ChatMessage(text = response, isUser = false))
            } catch (e: Exception) {
                addMessage(
                    ChatMessage(
                        text = "I'm sorry, I'm having trouble connecting right now. Please try again later.",
                        isUser = false
                    )
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + message
        )
    }

    private fun testConnection() {
        viewModelScope.launch {
            val isConnected = OllamaService.testConnection()
            _uiState.value = _uiState.value.copy(isConnected = isConnected)
        }
    }

    fun retryConnection() {
        testConnection()
    }
}
