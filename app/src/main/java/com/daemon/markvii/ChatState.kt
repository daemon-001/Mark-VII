package com.daemon.markvii

import android.graphics.Bitmap
import com.daemon.markvii.data.Chat
import com.daemon.markvii.data.ErrorInfo

/**
 * @author Nitesh
 */

enum class ApiProvider {
    OPENROUTER,
    GEMINI
}

data class ChatState (
    val chatList: MutableList<Chat> = mutableListOf(),
    val prompt: String = "",
    val bitmap: Bitmap? = null,
    val error: ErrorInfo? = null,
    val isGeneratingResponse: Boolean = false,
    val showPromptSuggestions: Boolean = true,
    val currentApiProvider: ApiProvider = ApiProvider.GEMINI, // Gemini as default
    val hapticTrigger: Long = 0L // Timestamp to trigger haptic feedback on chunk arrival
)





