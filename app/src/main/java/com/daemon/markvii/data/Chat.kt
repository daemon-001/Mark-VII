package com.daemon.markvii.data

import android.graphics.Bitmap
import java.util.UUID

/**
 * @author Nitesh
 */
data class Chat (
    val prompt: String,
    val bitmap: Bitmap?,
    val isFromUser: Boolean,
    val modelUsed: String = "",
    val isStreaming: Boolean = false,
    val id: String = UUID.randomUUID().toString(),
    val isError: Boolean = false,
    val retryOfPrompt: String? = null // set when this is a retry response; contains the original user prompt
)





