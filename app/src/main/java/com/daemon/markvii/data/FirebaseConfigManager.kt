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
    
    // Exception models that require ":free" postfix - stored as map of modelId to modelName
    private val _exceptionModels = MutableStateFlow<Map<String, String>>(emptyMap())
    val exceptionModels: StateFlow<Map<String, String>> = _exceptionModels.asStateFlow()
    
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
            fetchExceptionModels()
            
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
                @Suppress("UNCHECKED_CAST")
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
     * Fetch exception models list from Firebase
     * These models require ":free" postfix to work properly
     */
    private suspend fun fetchExceptionModels() {
        try {
            val document = firestore.collection(COLLECTION_CONFIG)
                .document("exp_models")
                .get()
                .await()
            
            if (document.exists()) {
                @Suppress("UNCHECKED_CAST")
                val modelsList = document.get("list") as? List<Map<String, String>> ?: emptyList()
                val modelsMap = modelsList.associate { 
                    (it["modelId"] ?: "") to (it["modelName"] ?: "")
                }
                _exceptionModels.value = modelsMap
            } else {
                _exceptionModels.value = emptyMap()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching exception models", e)
            _exceptionModels.value = emptyMap()
        }
    }
    
    /**
     * Add a model to the exception list in Firebase
     * This model will keep its ":free" postfix
     */
    suspend fun addExceptionModel(modelId: String, modelName: String) {
        try {
            // Get current list from Firestore directly to ensure we have latest data
            val document = firestore.collection(COLLECTION_CONFIG)
                .document("exp_models")
                .get()
                .await()
            
            val currentMap = if (document.exists()) {
                @Suppress("UNCHECKED_CAST")
                val existingList = document.get("list") as? List<Map<String, String>> ?: emptyList()
                existingList.associate { 
                    (it["modelId"] ?: "") to (it["modelName"] ?: "")
                }.toMutableMap()
            } else {
                mutableMapOf<String, String>()
            }
            
            // Check if already exists
            if (currentMap.containsKey(modelId)) {
                return
            }
            
            currentMap[modelId] = modelName
            
            // Convert to list of maps for Firebase
            val listForFirebase = currentMap.map { (id, name) ->
                hashMapOf(
                    "modelId" to id,
                    "modelName" to name
                )
            }
            
            val data = hashMapOf(
                "list" to listForFirebase,
                "lastUpdated" to com.google.firebase.Timestamp.now()
            )
            
            // Use set with merge to create or update the document
            firestore.collection(COLLECTION_CONFIG)
                .document("exp_models")
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .await()
            
            // Update local state
            _exceptionModels.value = currentMap
        } catch (e: Exception) {
            Log.e(TAG, "Error adding exception model: $modelId", e)
        }
    }
    
    /**
     * Check if a model is in the exception list
     */
    fun isExceptionModel(modelId: String): Boolean {
        return _exceptionModels.value.containsKey(modelId)
    }
    
    /**
     * Test Firebase write permissions
     * Call this to verify Firebase is writable
     */
    suspend fun testFirebaseWrite(): Boolean {
        return try {
            val testData = hashMapOf(
                "test" to "write_test",
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            
            firestore.collection(COLLECTION_CONFIG)
                .document("exp_models")
                .set(testData, com.google.firebase.firestore.SetOptions.merge())
                .await()
            
            Log.d(TAG, "Firebase write test SUCCESSFUL")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Firebase write test FAILED", e)
            Log.e(TAG, "Error message: ${e.message}")
            false
        }
    }
    
    /**
     * Get current exception models list (for debugging)
     */
    fun getExceptionModelsList(): Map<String, String> {
        return _exceptionModels.value
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

