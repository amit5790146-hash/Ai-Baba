package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.db.ChatMessage
import com.example.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun sendMessage() {
        val question = _inputText.value.trim()
        if (question.isEmpty() || _isLoading.value) return

        _inputText.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            // Get current message history (snapshots) before inserting the new user message
            val currentHistory = messages.value

            // 1. Save user message to Room
            val userMessage = ChatMessage(isUser = true, text = question)
            repository.insertMessage(userMessage)

            // 2. Fetch answer from Gemini API (passing existing history for context)
            val answer = repository.getAiAnswer(question, currentHistory)

            // 3. Save AI response to Room
            val aiMessage = ChatMessage(isUser = false, text = answer)
            repository.insertMessage(aiMessage)

            _isLoading.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}

class ChatViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
