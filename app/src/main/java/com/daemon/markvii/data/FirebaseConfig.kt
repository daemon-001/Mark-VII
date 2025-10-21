package com.daemon.markvii.data

/**
 * Data class to hold Firebase model configuration
 */
data class FirebaseModelInfo(
    val displayName: String = "",
    val apiModel: String = "",
    val isAvailable: Boolean = true,
    val order: Int = 0
)

/**
 * Data class to hold Firebase API keys configuration
 */
data class FirebaseApiKeys(
    val openrouterApiKey: String = ""
)

/**
 * Data class to hold complete Firebase configuration
 */
data class FirebaseConfig(
    val models: List<FirebaseModelInfo> = emptyList(),
    val apiKeys: FirebaseApiKeys = FirebaseApiKeys()
)

