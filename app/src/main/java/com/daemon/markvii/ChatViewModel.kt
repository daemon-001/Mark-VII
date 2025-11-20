package com.daemon.markvii

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daemon.markvii.data.Chat
import com.daemon.markvii.data.ChatData
import com.daemon.markvii.data.ErrorInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.AccessController.getContext

/**
 * @author Nitesh
 */
class ChatViewModel : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()

    fun onEvent(event: ChatUiEvent) {

        when (event) {
            is ChatUiEvent.SendPrompt -> {
                if (event.prompt.isNotEmpty()) {
                    addPrompt(event.prompt, event.bitmap)

                    if (event.bitmap != null) {
                        getResponseWithImage(event.prompt, event.bitmap)
                    } else {

                        getResponse(event.prompt)
                    }
                }
            }
            
            is ChatUiEvent.RetryPrompt -> {
                // Retry without adding prompt to chat again
                if (event.prompt.isNotEmpty()) {
                    if (event.bitmap != null) {
                        getResponseWithImage(event.prompt, event.bitmap)
                    } else {
                        getResponse(event.prompt)
                    }
                }
            }

            is ChatUiEvent.UpdatePrompt -> {
                _chatState.update {
                    it.copy(prompt = event.newPrompt)
                }
            }
        }
    }
    
    /**
     * Clear the current error from state
     */
    fun clearError() {
        _chatState.update {
            it.copy(error = null)
        }
    }

//    Show welcome guide without making API call
    fun showWelcomeGuide() {
        val welcomeMessage = """
            👋 Welcome to Mark VII!
            
            🚀 Quick Start Guide:
            
            1️⃣ SELECT MODEL
               • Tap the model dropdown at the top
               • Choose from multiple available models.
            
            3️⃣ START CHATTING
               • Type your message in the text box
               • Tap the send button (✈️)
               • Get instant AI responses
            
            4️⃣ IMAGE UNDERSTANDING
               • Tap the 📷 icon to attach images
               • Ask questions about the image
               • AI will analyze and respond
            
            💡 Tips:
               • Different models have different strengths
            
            ✨ Ready to start? Just type your first message!
        """.trimIndent()
        
        val welcomeChat = Chat(
            prompt = welcomeMessage,
            bitmap = null,
            isFromUser = false,
            modelUsed = "" // Welcome guide has no model
        )
        
        _chatState.update {
            it.copy(
                chatList = it.chatList.toMutableList().apply {
                    add(0, welcomeChat)
                }
            )
        }
    }

    private fun addPrompt(prompt: String, bitmap: Bitmap?) {
        _chatState.update {
            it.copy(
                chatList = it.chatList.toMutableList().apply {
                    add(0, Chat(prompt, bitmap, true))
                },
                prompt = "",
                bitmap = null
            )
        }
    }

    private fun getResponse(prompt: String) {
        viewModelScope.launch {
            try {
                // Set loading state
                _chatState.update { it.copy(isGeneratingResponse = true) }
                
                val chat = ChatData.getResponse(prompt)
                _chatState.update {
                    it.copy(
                        chatList = it.chatList.toMutableList().apply {
                            add(0, chat)
                        },
                        isGeneratingResponse = false
                    )
                }
            } catch (e: Exception) {
                _chatState.update { it.copy(isGeneratingResponse = false) }
                handleError(e, prompt, null)
            }
        }
    }

    private fun getResponseWithImage(prompt: String, bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                // Set loading state
                _chatState.update { it.copy(isGeneratingResponse = true) }
                
                val chat = ChatData.getResponseWithImage(prompt, bitmap)
                _chatState.update {
                    it.copy(
                        chatList = it.chatList.toMutableList().apply {
                            add(0, chat)
                        },
                        isGeneratingResponse = false
                    )
                }
            } catch (e: Exception) {
                _chatState.update { it.copy(isGeneratingResponse = false) }
                handleError(e, prompt, bitmap)
            }
        }
    }
    
    private fun handleError(e: Exception, prompt: String, bitmap: Bitmap?) {
        val errorMessage = e.message ?: "Unknown error occurred"
        val parts = errorMessage.split("|", limit = 2)
        
        val errorCode = if (parts.size == 2) parts[0] else "UNKNOWN_ERROR"
        val errorDetails = if (parts.size == 2) parts[1] else errorMessage
        
        val (title, mainMessage, isRetryable) = when (errorCode) {
            "API_KEY_MISSING" -> Triple(
                "Configuration Error",
                "API key is not configured",
                false
            )
            "HTTP_401" -> Triple(
                "Authentication Error",
                "Invalid or missing API key",
                false
            )
            "HTTP_403" -> Triple(
                "Access Denied",
                "Insufficient credits or permissions",
                false
            )
            "HTTP_404" -> Triple(
                "Model Not Found",
                "The selected AI model is not available",
                false
            )
            "HTTP_429" -> Triple(
                "Rate Limited",
                "Too many requests. Please wait a moment.",
                true
            )
            "NETWORK_ERROR" -> Triple(
                "Connection Error",
                "Unable to connect to AI service",
                true
            )
            "API_ERROR" -> Triple(
                "API Error",
                "An error occurred with the AI service",
                true
            )
            else -> Triple(
                "Error",
                "An unexpected error occurred",
                true
            )
        }
        
        _chatState.update {
            it.copy(
                error = ErrorInfo(
                    title = title,
                    mainMessage = mainMessage,
                    fullDetails = errorDetails,
                    isRetryable = isRetryable,
                    lastPrompt = prompt,
                    lastBitmap = bitmap
                )
            )
        }
    }

}


















