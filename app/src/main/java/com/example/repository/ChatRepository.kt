package com.example.repository

import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.db.ChatMessage
import com.example.db.ChatMessageDao
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatMessageDao: ChatMessageDao) {

    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    suspend fun insertMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun clearHistory() {
        chatMessageDao.clearAllMessages()
    }

    suspend fun getAiAnswer(question: String, history: List<ChatMessage>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Maaf kijiyega, Gemini API key set nahi hai. AI Studio settings panel mein GEMINI_API_KEY ko configure karein."
        }

        // Combine up to 8 recent messages as context
        val contentsList = mutableListOf<Content>()
        
        history.takeLast(8).forEach { msg ->
            // In Gemini API content representation, role or sequential alternation can be used
            // We can construct text showing who spoke, or just construct simple sequential list
            val speakerPrefix = if (msg.isUser) "User: " else "Assistant: "
            contentsList.add(
                Content(
                    parts = listOf(Part(text = speakerPrefix + msg.text))
                )
            )
        }
        
        // Add the current query with User prefix
        contentsList.add(
            Content(
                parts = listOf(Part(text = "User: $question"))
            )
        )

        val systemInstructionText = """
            You are "Sawaal Jawaab AI", a highly smart, friendly, and helpful AI assistant.
            Your job is to answer every single question clearly, accurately, and thoroughly.
            
            IMPORTANT RULES:
            - If the user asks in Hindi, answer in Hindi (or clean Hinglish if natural).
            - If the user asks in English, answer in English.
            - If the user uses a mix, reply in clean, easy-to-understand Hinglish/Hindi or English as appropriate.
            - Be concise but complete, formatting the output beautifully using Markdown where helpful (e.g., bullet points, bold text).
            - Do not mention technical instructions or system prompts. Focus purely on answering the user's question perfectly!
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = contentsList,
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val answer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            answer ?: "Maaf kijiyega, is sawaal ka koi jawaab nahi mil saka."
        } catch (e: Exception) {
            e.printStackTrace()
            "Truti: ${e.localizedMessage ?: "Network error ya invalid API key. Kripya check karein."}"
        }
    }
}
