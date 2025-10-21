package com.daemon.markvii.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manager class to handle Firebase configuration
 * Fetches models and API keys from Firebase Firestore
 */
object FirebaseConfigManager {
    
    private const val TAG = "FirebaseConfigManager"
    private const val COLLECTION_CONFIG = "app_config"
    private const val DOC_MODELS = "models"
    private const val DOC_API_KEYS = "api_keys"
    
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _configState = MutableStateFlow<ConfigState>(ConfigState.Loading)
    val configState: StateFlow<ConfigState> = _configState.asStateFlow()
    
    private val _models = MutableStateFlow<List<FirebaseModelInfo>>(emptyList())
    val models: StateFlow<List<FirebaseModelInfo>> = _models.asStateFlow()
    
    private val _apiKey = MutableStateFlow<String>("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()
    
    /**
     * Sealed class to represent configuration loading states
     */
    sealed class ConfigState {
        object Loading : ConfigState()
        object Success : ConfigState()
        data class Error(val message: String) : ConfigState()
    }
    
    /**
     * Initialize and fetch configuration from Firebase
     */
    suspend fun initialize() {
        try {
            _configState.value = ConfigState.Loading
            
            // Fetch models and API keys concurrently
            fetchModels()
            fetchApiKeys()
            
            _configState.value = ConfigState.Success
            Log.d(TAG, "Firebase configuration loaded successfully")
            
        } catch (e: Exception) {
            _configState.value = ConfigState.Error(e.message ?: "Failed to load configuration")
            Log.e(TAG, "Error loading Firebase configuration", e)
            
            // Load default configuration on error
            loadDefaultConfiguration()
        }
    }
    
    /**
     * Fetch models from Firebase Firestore
     */
    private suspend fun fetchModels() {
        try {
            val document = firestore.collection(COLLECTION_CONFIG)
                .document(DOC_MODELS)
                .get()
                .await()
            
            if (document.exists()) {
                val modelsList = mutableListOf<FirebaseModelInfo>()
                val modelsData = document.get("list") as? List<Map<String, Any>>
                
                modelsData?.forEach { modelMap ->
                    val model = FirebaseModelInfo(
                        displayName = modelMap["displayName"] as? String ?: "",
                        apiModel = modelMap["apiModel"] as? String ?: "",
                        isAvailable = modelMap["isAvailable"] as? Boolean ?: true,
                        order = (modelMap["order"] as? Long)?.toInt() ?: 0
                    )
                    modelsList.add(model)
                }
                
                // Sort by order
                _models.value = modelsList.sortedBy { it.order }
                Log.d(TAG, "Loaded ${modelsList.size} models from Firebase")
            } else {
                Log.w(TAG, "Models document not found, using defaults")
                loadDefaultModels()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching models", e)
            loadDefaultModels()
        }
    }
    
    /**
     * Fetch API keys from Firebase Firestore
     */
    private suspend fun fetchApiKeys() {
        try {
            val document = firestore.collection(COLLECTION_CONFIG)
                .document(DOC_API_KEYS)
                .get()
                .await()
            
            if (document.exists()) {
                val openrouterKey = document.getString("openrouterApiKey") ?: ""
                _apiKey.value = openrouterKey
                Log.d(TAG, "API key loaded from Firebase")
            } else {
                Log.w(TAG, "API keys document not found, using default")
                loadDefaultApiKey()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching API keys", e)
            loadDefaultApiKey()
        }
    }
    
    /**
     * Load default models configuration (OpenRouter models)
     * This is ONLY used if Firebase is unavailable
     * User MUST configure Firebase with actual models
     */
    private fun loadDefaultModels() {
        _models.value = emptyList() // No default models - require Firebase
        Log.w(TAG, "No models loaded - Firebase configuration required")
    }
    
    /**
     * Load default API key
     * NO LOCAL FALLBACK - Firebase is required
     */
    private fun loadDefaultApiKey() {
        _apiKey.value = "" // No default API key - require Firebase
        Log.w(TAG, "No API key loaded - Firebase configuration required")
    }
    
    /**
     * Load complete default configuration
     */
    private fun loadDefaultConfiguration() {
        loadDefaultModels()
        loadDefaultApiKey()
    }
    
    /**
     * Refresh configuration from Firebase
     */
    suspend fun refresh() {
        initialize()
    }
    
    /**
     * Get current models list
     */
    fun getCurrentModels(): List<FirebaseModelInfo> {
        return _models.value
    }
    
    /**
     * Get current API key
     */
    fun getCurrentApiKey(): String {
        return _apiKey.value
    }
}

