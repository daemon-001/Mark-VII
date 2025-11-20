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
import kotlinx.coroutines.Job

/**
 * @author Nitesh
 */
class ChatViewModel : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()
    
    private var streamingJob: Job? = null

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
            
            is ChatUiEvent.StopStreaming -> {
                streamingJob?.cancel()
                streamingJob = null
                // Mark current streaming chat as complete
                _chatState.update {
                    val updatedList = it.chatList.toMutableList()
                    if (updatedList.isNotEmpty() && updatedList[0].isStreaming) {
                        updatedList[0] = updatedList[0].copy(isStreaming = false)
                    }
                    it.copy(
                        chatList = updatedList,
                        isGeneratingResponse = false
                    )
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
               • Choose from FREE AI models powered by OpenRouter
            
            2️⃣ START CHATTING
               • Type your message in the text box
               • Tap the send button (✈️)
               • Get instant AI responses
            
            3️⃣ IMAGE UNDERSTANDING
               • Tap the 📷 icon to attach images
               • Ask questions about the image
               • AI will analyze and respond
            
            💡 Tips:
               • All models are FREE to use
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
        streamingJob = viewModelScope.launch {
            try {
                // Set loading state
                _chatState.update { it.copy(isGeneratingResponse = true) }
                
                // Create a placeholder chat for streaming response
                val streamingChat = Chat(
                    prompt = "",
                    bitmap = null,
                    isFromUser = false,
                    modelUsed = ChatData.selected_model,
                    isStreaming = true
                )
                
                // Add placeholder to chat list
                _chatState.update {
                    it.copy(
                        chatList = it.chatList.toMutableList().apply {
                            add(0, streamingChat)
                        }
                    )
                }
                
                // Use streaming to get response with live updates
                var chunkCount = 0
                val chat = ChatData.getStreamingResponse(prompt) { chunk ->
                    chunkCount++
                    // Update the streaming chat with each chunk
                    _chatState.update { state ->
                        val updatedList = state.chatList.toMutableList()
                        if (updatedList.isNotEmpty()) {
                            val currentResponse = updatedList[0]
                            updatedList[0] = currentResponse.copy(
                                prompt = currentResponse.prompt + chunk,
                                isStreaming = true
                            )
                        }
                        state.copy(chatList = updatedList)
                    }
                }
                
                // Update with final response (stop streaming)
                _chatState.update { state ->
                    val updatedList = state.chatList.toMutableList()
                    if (updatedList.isNotEmpty()) {
                        updatedList[0] = chat.copy(isStreaming = false)
                    }
                    state.copy(
                        chatList = updatedList,
                        isGeneratingResponse = false
                    )
                }
            } catch (e: Exception) {
                // Remove the placeholder chat on error
                _chatState.update { state ->
                    state.copy(
                        chatList = state.chatList.toMutableList().apply {
                            if (isNotEmpty() && !get(0).isFromUser) {
                                removeAt(0)
                            }
                        },
                        isGeneratingResponse = false
                    )
                }
                handleError(e, prompt, null)
            }
        }
    }

    private fun getResponseWithImage(prompt: String, bitmap: Bitmap) {
        streamingJob = viewModelScope.launch {
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
        
        // Use only the API error message, not the stack trace
        val apiErrorLog = buildString {
            appendLine("Error Code: $errorCode")
            appendLine("")
            appendLine("Details:")
            appendLine(if (parts.size == 2) parts[1] else errorMessage)
        }
        
        val (title, mainMessage, isRetryable) = when (errorCode) {
            "API_KEY_MISSING" -> Triple(
                "Configuration Error",
                "API key is not configured",
                false
            )
            "BAD_REQUEST" -> Triple(
                "Error Code: 400",
                "Invalid or missing params, CORS",
                true
            )
            "UNAUTHORIZED" -> Triple(
                "Error Code: 401",
                "Invalid credentials (OAuth session expired, disabled/invalid API key)",
                false
            )
            "INSUFFICIENT_CREDITS" -> Triple(
                "Error Code: 402",
                "Your account or API key has insufficient credits. Add more credits and retry the request.",
                false
            )
            "CONTENT_FLAGGED" -> Triple(
                "Error Code: 403",
                "Your chosen model requires moderation and your input was flagged",
                false
            )
            "REQUEST_TIMEOUT" -> Triple(
                "Error Code: 408",
                "Your request timed out",
                true
            )
            "RATE_LIMITED" -> Triple(
                "Error Code: 429",
                "You are being rate limited",
                true
            )
            "MODEL_DOWN" -> Triple(
                "Error Code: 502",
                "Your chosen model is down or we received an invalid response from it",
                true
            )
            "MODEL_404_RETRY" -> Triple(
                "Model Fixed",
                "Model ID corrected. Please retry your request.",
                true
            )
            "MODEL_NOT_FOUND" -> Triple(
                "Error Code: 404",
                "Model not available on server",
                false
            )
            "NO_PROVIDER" -> Triple(
                "Error Code: 503",
                "There is no available model provider that meets your routing requirements",
                true
            )
            "TIMEOUT" -> Triple(
                "Connection Timeout",
                "Request timed out. Check your connection",
                true
            )
            "NO_INTERNET" -> Triple(
                "No Internet",
                "No internet connection available",
                true
            )
            "CONNECTION_FAILED" -> Triple(
                "Connection Failed",
                "Could not connect to server",
                true
            )
            "NETWORK_ERROR" -> Triple(
                "Network Error",
                "Unable to connect to AI service",
                true
            )
            "HTTP_401" -> Triple(
                "Error Code: 401",
                "Invalid or missing API key",
                false
            )
            "HTTP_403" -> Triple(
                "Error Code: 403",
                "Insufficient credits or permissions",
                false
            )
            "HTTP_404" -> Triple(
                "Model Not Found",
                "The selected AI model is not available",
                false
            )
            "HTTP_429" -> Triple(
                "Error Code: 429",
                "Too many requests. Please wait a moment.",
                true
            )
            "API_ERROR" -> Triple(
                "API Error",
                "An error occurred with the AI service",
                true
            )
            else -> Triple(
                "Unknown Error",
                "An unexpected error occurred",
                true
            )
        }
        
        _chatState.update {
            it.copy(
                error = ErrorInfo(
                    title = title,
                    mainMessage = mainMessage,
                    fullDetails = apiErrorLog,
                    isRetryable = isRetryable,
                    lastPrompt = prompt,
                    lastBitmap = bitmap,
                    rawException = e.stackTraceToString()
                )
            )
        }
    }

}


















