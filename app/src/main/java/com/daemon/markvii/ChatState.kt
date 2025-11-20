package com.daemon.markvii

import android.graphics.Bitmap
import com.daemon.markvii.data.Chat
import com.daemon.markvii.data.ErrorInfo

/**
 * @author Nitesh
 */
data class ChatState (
    val chatList: MutableList<Chat> = mutableListOf(),
    val prompt: String = "",
    val bitmap: Bitmap? = null,
    val error: ErrorInfo? = null,
    val isGeneratingResponse: Boolean = false
)





