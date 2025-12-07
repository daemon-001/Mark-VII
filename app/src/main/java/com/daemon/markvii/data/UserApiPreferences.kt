package com.daemon.markvii.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages user-provided API keys and preferences
 */
object UserApiPreferences {
    private const val PREFS_NAME = "user_api_prefs"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    private const val KEY_GEMINI_ENABLED = "gemini_enabled"
    private const val KEY_OPENROUTER_API_KEY = "openrouter_api_key"
    private const val KEY_OPENROUTER_ENABLED = "openrouter_enabled"
    
    private lateinit var prefs: SharedPreferences
    
    // Gemini Config
    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()
    
    private val _isGeminiKeyEnabled = MutableStateFlow(false)
    val isGeminiKeyEnabled: StateFlow<Boolean> = _isGeminiKeyEnabled.asStateFlow()
    
    // OpenRouter Config
    private val _openRouterApiKey = MutableStateFlow("")
    val openRouterApiKey: StateFlow<String> = _openRouterApiKey.asStateFlow()
    
    private val _isOpenRouterKeyEnabled = MutableStateFlow(false)
    val isOpenRouterKeyEnabled: StateFlow<Boolean> = _isOpenRouterKeyEnabled.asStateFlow()
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Load saved values
        _geminiApiKey.value = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        _isGeminiKeyEnabled.value = prefs.getBoolean(KEY_GEMINI_ENABLED, false)
        
        _openRouterApiKey.value = prefs.getString(KEY_OPENROUTER_API_KEY, "") ?: ""
        _isOpenRouterKeyEnabled.value = prefs.getBoolean(KEY_OPENROUTER_ENABLED, false)
    }
    
    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        prefs.edit().putString(KEY_GEMINI_API_KEY, key).apply()
    }
    
    fun setGeminiKeyEnabled(enabled: Boolean) {
        _isGeminiKeyEnabled.value = enabled
        prefs.edit().putBoolean(KEY_GEMINI_ENABLED, enabled).apply()
    }
    
    fun setOpenRouterApiKey(key: String) {
        _openRouterApiKey.value = key
        prefs.edit().putString(KEY_OPENROUTER_API_KEY, key).apply()
    }
    
    fun setOpenRouterKeyEnabled(enabled: Boolean) {
        _isOpenRouterKeyEnabled.value = enabled
        prefs.edit().putBoolean(KEY_OPENROUTER_ENABLED, enabled).apply()
    }
}
