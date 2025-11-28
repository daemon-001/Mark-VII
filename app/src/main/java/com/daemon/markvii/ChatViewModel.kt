package com.daemon.markvii

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daemon.markvii.data.AuthManager
import com.daemon.markvii.data.Chat
import com.daemon.markvii.data.ChatData
import com.daemon.markvii.data.ChatHistoryManager
import com.daemon.markvii.data.ChatSession
import com.daemon.markvii.data.ErrorInfo
import com.daemon.markvii.data.FirestoreChatManager
import com.daemon.markvii.data.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.util.UUID

/**
 * @author Nitesh
 */
class ChatViewModel : ViewModel() {

    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()
    
    private var streamingJob: Job? = null
    
    init {
        // Observe authentication state
        viewModelScope.launch {
            AuthManager.currentUser.collect { user ->
                _chatState.update { it.copy(currentUser = user) }
                
                if (user != null) {
                    // User signed in - load sessions list from Firestore (but don't open any)
                    loadUserSessions(user.uid)
                    
                    // Migrate local history to Firestore if needed
                    migrateLocalHistoryToFirestore(user.uid)
                } else {
                    // User signed out - start with empty chat
                    _chatState.update { it.copy(
                        chatList = mutableListOf(),
                        chatSessions = emptyList(), 
                        currentSessionId = null,
                        showPromptSuggestions = true
                    ) }
                }
            }
        }
        
        // Always start with empty chat on app startup
        _chatState.update { it.copy(
            chatList = mutableListOf(),
            showPromptSuggestions = true
        ) }
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
                        getResponse(event.prompt, isRetry = true)
                    }
                }
            }

            is ChatUiEvent.UpdatePrompt -> {
                _chatState.update {
                    it.copy(prompt = event.newPrompt)
                }
            }

            is ChatUiEvent.RenameSession -> {
                renameSession(event.sessionId, event.newTitle)
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
            
            is ChatUiEvent.SignInWithGoogle -> {
                // Sign-in is handled in MainActivity
            }
            
            is ChatUiEvent.SignOut -> {
                AuthManager.signOut()
            }
            
            is ChatUiEvent.CreateNewSession -> {
                createNewSession()
            }
            
            is ChatUiEvent.SwitchSession -> {
                switchToSession(event.sessionId)
            }
            
            is ChatUiEvent.DeleteSession -> {
                deleteSession(event.sessionId)
            }
            
            is ChatUiEvent.ToggleDrawer -> {
                _chatState.update {
                    it.copy(isDrawerOpen = !it.isDrawerOpen)
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
            val currentState = _chatState.value
            
            // Save to local storage
            ChatHistoryManager.saveChatHistory(currentState.chatList)
            
            // If user is signed in, save to Firestore
            if (currentState.currentUser != null) {
                // If no current session exists, create one automatically
                if (currentState.currentSessionId == null && currentState.chatList.isNotEmpty()) {
                    // Create a new session with the first user message as title
                    val firstUserMessage = currentState.chatList.lastOrNull { it.isFromUser }
                    val title = firstUserMessage?.prompt?.take(50) ?: "New Chat"
                    
                    val result = FirestoreChatManager.createSession(currentState.currentUser.uid, title)
                    result.onSuccess { newSession ->
                        // Update state with new session
                        _chatState.update {
                            it.copy(
                                currentSessionId = newSession.id,
                                chatSessions = listOf(newSession) + it.chatSessions
                            )
                        }
                        
                        // Save the chat to the new session
                        FirestoreChatManager.saveSession(newSession, currentState.chatList)
                        Log.d("ChatViewModel", "Auto-created session: ${newSession.id}")
                    }.onFailure { error ->
                        Log.e("ChatViewModel", "Failed to auto-create session: ${error.message}")
                    }
                } else if (currentState.currentSessionId != null) {
                    // Session exists, just save to it
                    val session = currentState.chatSessions.find { it.id == currentState.currentSessionId }
                    if (session != null) {
                        FirestoreChatManager.saveSession(session, currentState.chatList)
                    }
                }
            }
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

    private fun getResponse(prompt: String, isRetry: Boolean = false) {
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
                        
                        // Get conversation history (exclude the current prompt and streaming placeholder)
                        val historySource = _chatState.value.chatList.drop(1)
                        
                        // If retrying, exclude the most recent assistant message from context
                        // This prevents the model from seeing its previous (failed/rejected) response
                        val historyFiltered = if (isRetry && historySource.isNotEmpty() && !historySource[0].isFromUser) {
                            historySource.drop(1)
                        } else {
                            historySource
                        }

                        val conversationHistory = historyFiltered
                            .filter { !it.isStreaming } // Skip any other streaming messages
                            .reversed() // Reverse to chronological order
                        
                        GeminiClient.generateContentStream(
                            prompt = prompt,
                            modelName = ChatData.selected_model,
                            conversationHistory = conversationHistory,
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
                        // Use OpenRouter API with streaming and conversation history
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
                        
                        // Get conversation history (exclude the current prompt and streaming placeholder)
                        val historySource = _chatState.value.chatList.drop(1)
                        
                        // If retrying, exclude the most recent assistant message from context
                        val historyFiltered = if (isRetry && historySource.isNotEmpty() && !historySource[0].isFromUser) {
                            historySource.drop(1)
                        } else {
                            historySource
                        }

                        val conversationHistory = historyFiltered
                            .filter { !it.isStreaming } // Skip any other streaming messages
                            .reversed() // Reverse to chronological order
                        
                        var chunkCount = 0
                        val chat = ChatData.getStreamingResponse(
                            prompt = prompt,
                            conversationHistory = conversationHistory
                        ) { chunk ->
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

    /**
     * Load all sessions for the current user from Firestore
     */
    private fun loadUserSessions(userId: String) {
        viewModelScope.launch {
            val result = FirestoreChatManager.loadUserSessions(userId)
            result.onSuccess { sessions ->
                // Just load the sessions list for the drawer, don't open any
                _chatState.update { it.copy(chatSessions = sessions) }
            }.onFailure { error ->
                Log.e("ChatViewModel", "Failed to load sessions: ${error.message}")
            }
        }
    }
    
    /**
     * Create a new chat session
     */
    private fun createNewSession() {
        viewModelScope.launch {
            val currentUser = _chatState.value.currentUser
            
            if (currentUser != null) {
                // Create session in Firestore
                val result = FirestoreChatManager.createSession(currentUser.uid)
                result.onSuccess { newSession ->
                    _chatState.update {
                        it.copy(
                            chatList = mutableListOf(),
                            currentSessionId = newSession.id,
                            chatSessions = listOf(newSession) + it.chatSessions,
                            showPromptSuggestions = true,
                            isDrawerOpen = false
                        )
                    }
                }.onFailure { error ->
                    Log.e("ChatViewModel", "Failed to create session: ${error.message}")
                }
            } else {
                // Not signed in - just clear local chat
                _chatState.update {
                    it.copy(
                        chatList = mutableListOf(),
                        currentSessionId = null,
                        showPromptSuggestions = true,
                        isDrawerOpen = false
                    )
                }
                ChatHistoryManager.clearChatHistory()
            }
        }
    }
    
    /**
     * Switch to a different session
     */
    private fun switchToSession(sessionId: String) {
        viewModelScope.launch {
            val result = FirestoreChatManager.loadSession(sessionId)
            result.onSuccess { session ->
                val chatList = FirestoreChatManager.serializableToChatList(session.messages)
                
                _chatState.update {
                    it.copy(
                        chatList = chatList.toMutableList(),
                        currentSessionId = sessionId,
                        showPromptSuggestions = chatList.isEmpty(),
                        isDrawerOpen = false
                    )
                }
                
                // Save to local storage as well
                ChatHistoryManager.saveChatHistory(chatList)
            }.onFailure { error ->
                Log.e("ChatViewModel", "Failed to switch session: ${error.message}")
            }
        }
    }
    
    /**
     * Rename a session
     */
    private fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            val result = FirestoreChatManager.renameSession(sessionId, newTitle)
            result.onSuccess {
                _chatState.update { state ->
                    val updatedSessions = state.chatSessions.map { session ->
                        if (session.id == sessionId) {
                            session.copy(title = newTitle)
                        } else {
                            session
                        }
                    }
                    state.copy(chatSessions = updatedSessions)
                }
            }.onFailure { error ->
                Log.e("ChatViewModel", "Failed to rename session: ${error.message}")
            }
        }
    }
    
    /**
     * Delete a session
     */
    private fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            val result = FirestoreChatManager.deleteSession(sessionId)
            result.onSuccess {
                _chatState.update { state ->
                    val updatedSessions = state.chatSessions.filter { it.id != sessionId }
                    
                    // If we deleted the current session, switch to another or create new
                    if (state.currentSessionId == sessionId) {
                        val nextSession = updatedSessions.firstOrNull()
                        if (nextSession != null) {
                            switchToSession(nextSession.id)
                        } else {
                            createNewSession()
                        }
                    }
                    
                    state.copy(chatSessions = updatedSessions)
                }
            }.onFailure { error ->
                Log.e("ChatViewModel", "Failed to delete session: ${error.message}")
            }
        }
    }
    
    /**
     * Migrate local chat history to Firestore when user first signs in
     */
    private fun migrateLocalHistoryToFirestore(userId: String) {
        viewModelScope.launch {
            val localHistory = ChatHistoryManager.loadChatHistory()
            
            // Only migrate if there's local history and no Firestore sessions yet
            if (localHistory.isNotEmpty()) {
                val result = FirestoreChatManager.loadUserSessions(userId)
                result.onSuccess { sessions ->
                    if (sessions.isEmpty()) {
                        // Create a session with the local history
                        val createResult = FirestoreChatManager.createSession(userId, "Migrated Chat")
                        createResult.onSuccess { newSession ->
                            FirestoreChatManager.saveSession(newSession, localHistory)
                            Log.d("ChatViewModel", "Migrated local history to Firestore")
                        }
                    }
                }
            }
        }
    }
}


















