package com.daemon.markvii.data

import androidx.annotation.Keep

/**
 * Data class to hold Firebase model configuration
 */
@Keep
data class FirebaseModelInfo(
    val displayName: String = "",
    val apiModel: String = "",
    val isAvailable: Boolean = true,
    val order: Int = 0,
    val isPro: Boolean = false
)

/**
 * Data class to hold Firebase API keys configuration
 */
@Keep
data class FirebaseApiKeys(
    val openrouterApiKey: String = ""
)

/**
 * Data class to hold complete Firebase configuration
 */
@Keep
data class FirebaseConfig(
    val models: List<FirebaseModelInfo> = emptyList(),
    val apiKeys: FirebaseApiKeys = FirebaseApiKeys()
)

