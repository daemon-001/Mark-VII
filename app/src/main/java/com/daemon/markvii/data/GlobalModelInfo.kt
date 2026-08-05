package com.daemon.markvii.data

import com.daemon.markvii.ApiProvider

data class GlobalModelInfo(
    val apiModel: String,
    val displayName: String,
    val provider: ApiProvider,
    val isPro: Boolean = false,
    val isPaid: Boolean = false,
    val isAvailable: Boolean = true,
    val created: Long = 0L
)
