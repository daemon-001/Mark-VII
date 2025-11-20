package com.daemon.markvii.data

/**
 * Data class to hold error information for display in error dialogs
 * @author Nitesh
 */
data class ErrorInfo(
    val title: String,
    val mainMessage: String,
    val fullDetails: String,
    val isRetryable: Boolean,
    val lastPrompt: String? = null,
    val lastBitmap: android.graphics.Bitmap? = null
)
