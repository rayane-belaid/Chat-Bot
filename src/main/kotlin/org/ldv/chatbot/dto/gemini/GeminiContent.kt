package org.ldv.chatbot.dto.gemini

data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart> = emptyList()
)