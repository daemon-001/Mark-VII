package com.daemon.markvii

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daemon.markvii.data.Chat
import com.daemon.markvii.data.ChatData
import com.daemon.markvii.data.ChatHistoryManager
import com.daemon.markvii.data.ErrorInfo
import com.daemon.markvii.data.GeminiClient
import kotlinx.coroutines.Dispatchers
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
    
    init {
        // Load chat history when ViewModel is created
        loadChatHistory()
    }

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
                    // Set generating state immediately for instant UI feedback
                    _chatState.update { it.copy(isGeneratingResponse = true) }
                    
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
            
            is ChatUiEvent.SwitchApiProvider -> {
                _chatState.update {
                    it.copy(currentApiProvider = event.provider)
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
        // Just enable prompt suggestions, no chat message needed
        _chatState.update {
            it.copy(
                showPromptSuggestions = true
            )
        }
    }
    
    fun dismissPromptSuggestions() {
        _chatState.update {
            it.copy(showPromptSuggestions = false)
        }
    }
    
    /**
     * Load chat history from persistent storage
     */
    private fun loadChatHistory() {
        val savedChats = ChatHistoryManager.loadChatHistory()
        if (savedChats.isNotEmpty()) {
            _chatState.update {
                it.copy(
                    chatList = savedChats.toMutableList(),
                    showPromptSuggestions = false // Don't show suggestions if history exists
                )
            }
        }
    }
    
    /**
     * Save chat history to persistent storage
     * Runs asynchronously on IO dispatcher to prevent UI blocking
     */
    private fun saveChatHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            ChatHistoryManager.saveChatHistory(_chatState.value.chatList)
        }
    }
    
    /**
     * Clear all chat history
     */
    fun clearChatHistory() {
        ChatHistoryManager.clearChatHistory()
        _chatState.update {
            it.copy(
                chatList = mutableListOf(),
                showPromptSuggestions = true
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
                bitmap = null,
                showPromptSuggestions = false,
                isGeneratingResponse = true // Set immediately for instant UI feedback
            )
        }
        saveChatHistory() // Save after adding prompt
    }

    private fun getResponse(prompt: String) {
        streamingJob = viewModelScope.launch {
            try {
                // Determine which API to use
                val currentProvider = _chatState.value.currentApiProvider
                
                when (currentProvider) {
                    ApiProvider.GEMINI -> {
                        // Use Gemini API with streaming
                        val streamingChat = Chat(
                            prompt = "",
                            bitmap = null,
                            isFromUser = false,
                            modelUsed = ChatData.selected_model,
                            isStreaming = true
                        )
                        
                        _chatState.update {
                            it.copy(
                                chatList = it.chatList.toMutableList().apply {
                                    add(0, streamingChat)
                                }
                            )
                        }
                        
                        GeminiClient.generateContentStream(
                            prompt = prompt,
                            modelName = ChatData.selected_model,
                            onChunk = { chunk ->
                                _chatState.update { state ->
                                    val updatedList = state.chatList.toMutableList()
                                    if (updatedList.isNotEmpty()) {
                                        val currentResponse = updatedList[0]
                                        updatedList[0] = currentResponse.copy(
                                            prompt = currentResponse.prompt + chunk,
                                            isStreaming = true
                                        )
                                    }
                                    state.copy(
                                        chatList = updatedList,
                                        hapticTrigger = System.currentTimeMillis() // Trigger haptic on chunk
                                    )
                                }
                            },
                            onFinish = { finishReason ->
                                // Mark streaming as complete
                                _chatState.update { state ->
                                    val updatedList = state.chatList.toMutableList()
                                    if (updatedList.isNotEmpty()) {
                                        val currentResponse = updatedList[0]
                                        
                                        // Check if response was truncated due to max tokens
                                        val finalPrompt = if (finishReason == "MAX_TOKENS") {
                                            currentResponse.prompt + "\n\n⚠️ Response truncated: Maximum token limit reached. Try asking for a shorter response or continue the conversation."
                                        } else {
                                            currentResponse.prompt
                                        }
                                        
                                        updatedList[0] = currentResponse.copy(
                                            prompt = finalPrompt,
                                            isStreaming = false
                                        )
                                    }
                                    state.copy(
                                        chatList = updatedList,
                                        isGeneratingResponse = false
                                    )
                                }
                                saveChatHistory()
                            }
                        )
                    }
                    
                    ApiProvider.OPENROUTER -> {
                        // Use OpenRouter API with streaming (existing code)
                        val streamingChat = Chat(
                            prompt = "",
                            bitmap = null,
                            isFromUser = false,
                            modelUsed = ChatData.selected_model,
                            isStreaming = true
                        )
                        
                        _chatState.update {
                            it.copy(
                                chatList = it.chatList.toMutableList().apply {
                                    add(0, streamingChat)
                                }
                            )
                        }
                        
                        var chunkCount = 0
                        val chat = ChatData.getStreamingResponse(prompt) { chunk ->
                            chunkCount++
                            _chatState.update { state ->
                                val updatedList = state.chatList.toMutableList()
                                if (updatedList.isNotEmpty()) {
                                    val currentResponse = updatedList[0]
                                    updatedList[0] = currentResponse.copy(
                                        prompt = currentResponse.prompt + chunk,
                                        isStreaming = true
                                    )
                                }
                                state.copy(
                                    chatList = updatedList
                                    // Haptics will be handled by typewriter animation in UI
                                )
                            }
                        }
                        
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
                        saveChatHistory()
                    }
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
                // Add streaming placeholder
                val streamingChat = Chat(
                    prompt = "",
                    bitmap = null,
                    isFromUser = false,
                    modelUsed = ChatData.selected_model,
                    isStreaming = true
                )
                
                _chatState.update { 
                    it.copy(
                        chatList = it.chatList.toMutableList().apply {
                            add(0, streamingChat)
                        }
                    ) 
                }
                
                // Determine which API to use
                val currentProvider = _chatState.value.currentApiProvider
                
                val chat = when (currentProvider) {
                    ApiProvider.GEMINI -> {
                        // Use Gemini API for image understanding
                        GeminiClient.generateContentWithImage(
                            prompt = prompt,
                            bitmap = bitmap,
                            modelName = ChatData.selected_model
                        )
                    }
                    ApiProvider.OPENROUTER -> {
                        // OpenRouter no longer supports image processing
                        throw Exception("IMAGE_NOT_SUPPORTED|Image processing is only available with Gemini API. Please switch to Gemini to use this feature.")
                    }
                }
                
                // Replace streaming placeholder with actual response
                _chatState.update { state ->
                    val updatedList = state.chatList.toMutableList()
                    if (updatedList.isNotEmpty() && updatedList[0].isStreaming) {
                        updatedList[0] = chat.copy(isStreaming = false)
                    }
                    state.copy(
                        chatList = updatedList,
                        isGeneratingResponse = false
                    )
                }
                saveChatHistory() // Save after image response
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
        
        // Format error message for display in chat
        val formattedError = buildString {
            appendLine("❌ Error: $errorCode")
            appendLine()
            appendLine(errorDetails)
        }
        
        // Add error as a chat message with red text
        val errorChat = Chat(
            prompt = formattedError,
            bitmap = null,
            isFromUser = false,
            modelUsed = ChatData.selected_model, // Show the model that was used for the query
            isStreaming = false,
            isError = true
        )
        
        _chatState.update {
            it.copy(
                chatList = it.chatList.toMutableList().apply {
                    add(0, errorChat)
                },
                isGeneratingResponse = false
            )
        }
        saveChatHistory()
    }

}


















