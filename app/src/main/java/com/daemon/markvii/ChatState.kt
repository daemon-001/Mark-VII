package com.daemon.markvii

import android.graphics.Bitmap
import com.daemon.markvii.data.Chat

/**
 * @author Nitesh
 */
data class ChatState (
    val chatList: MutableList<Chat> = mutableListOf(),
    val prompt: String = "",
    val bitmap: Bitmap? = null
)