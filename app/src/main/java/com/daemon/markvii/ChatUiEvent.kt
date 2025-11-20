package com.daemon.markvii

import android.graphics.Bitmap

/**
 * @author Nitesh
 */
sealed class ChatUiEvent {

    data class UpdatePrompt(val newPrompt: String) : ChatUiEvent()
    data class SendPrompt(
        val prompt: String,
        val bitmap: Bitmap?
    ) : ChatUiEvent()
    data class RetryPrompt(
        val prompt: String,
        val bitmap: Bitmap?
    ) : ChatUiEvent()
}




