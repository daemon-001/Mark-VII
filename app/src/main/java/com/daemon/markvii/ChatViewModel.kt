package com.daemon.markvii

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daemon.markvii.data.Chat
import com.daemon.markvii.data.ChatData
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
                var context = getContext()
                if (event.prompt.isNotEmpty()) {
                    addPrompt(event.prompt, event.bitmap)

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
            val chat = ChatData.getResponse(prompt)
            _chatState.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(0, chat)
                    }
                )
            }
        }
    }

    private fun getResponseWithImage(prompt: String, bitmap: Bitmap) {
        viewModelScope.launch {
            val chat = ChatData.getResponseWithImage(prompt, bitmap)
            _chatState.update {
                it.copy(
                    chatList = it.chatList.toMutableList().apply {
                        add(0, chat)
                    }
                )
            }
        }
    }

}


















