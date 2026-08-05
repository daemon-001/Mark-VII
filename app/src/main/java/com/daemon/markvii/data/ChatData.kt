package com.daemon.markvii.data

import android.graphics.Bitmap
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * @author Nitesh
 */

/**
 * Data class to hold model information
 */
data class ModelInfo(
    val displayName: String,
    val apiModel: String,
    val isAvailable: Boolean = true,
    val isPro: Boolean = false,
    val isPaid: Boolean = false,
    val created: Long = 0
)

object ChatData {

    // API key loaded ONLY from Firebase - no local fallback
    var openrouter_api_key: String = ""

    // Groq API key from user preferences
    var groq_api_key: String = ""

    var selected_model = ""
    
    // Cache for free models so we do not re-fetch on view recreation
    var cachedFreeModels: List<ModelInfo> = emptyList()
    var cachedFreeModelsKey: String = ""

    // Cache for Groq models
    var cachedGroqModels: List<ModelInfo> = emptyList()
    var cachedGroqModelsKey: String = ""

    /**
     * Update API key from Firebase
     * This is the ONLY way to set the API key
     */
    fun updateApiKey(newKey: String) {
        if (newKey.isNotEmpty()) {
            openrouter_api_key = newKey
            OpenRouterClient.updateApiKey(openrouter_api_key)
        }
    }

    fun updateGroqApiKey(newKey: String) {
        if (newKey.isNotEmpty()) {
            groq_api_key = newKey
            GroqClient.updateApiKey(groq_api_key)
        }
    }
    
    /**
     * Fetch all available models from OpenRouter
     * Returns list of models with pricing info
     */
    suspend fun fetchAvailableModels(): List<ModelData> {
        return try {
            val response = withContext(Dispatchers.IO) {
                OpenRouterClient.api.getModels()
            }
            response.data ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Fetch ALL models from OpenRouter (Free and Paid)
     */
    suspend fun fetchFreeModels(): List<ModelInfo> {
        return try {
            // First, ensure exception models are loaded from Firebase
            val exceptionModelsMap = FirebaseConfigManager.exceptionModels.value
            
            val allModels = fetchAvailableModels()
            
            // Use a map to deduplicate models by base ID (without :free suffix)
            val uniqueModels = mutableMapOf<String, ModelInfo>()
            
            allModels.forEach { model ->
                val pricing = model.pricing
                val promptPrice = pricing?.prompt?.toDoubleOrNull() ?: 0.0
                val completionPrice = pricing?.completion?.toDoubleOrNull() ?: 0.0
                
                // Paid models have cost > 0
                val isPaid = promptPrice > 0.0 || completionPrice > 0.0
                
                // Clean up display name
                val cleanDisplayName = (model.name ?: model.id)
                    .replace("(free)", "", ignoreCase = true)
                    .replace("  ", " ")
                    .trim()
                
                // Get base model ID without :free suffix for deduplication
                val modelIdWithoutFree = model.id.replace(":free", "", ignoreCase = true)
                
                // Check if this model (without :free) is in exception list
                val isInExceptionList = exceptionModelsMap.keys.any { exceptionId ->
                    val exceptionIdWithoutFree = exceptionId.replace(":free", "", ignoreCase = true)
                    exceptionIdWithoutFree.equals(modelIdWithoutFree, ignoreCase = true)
                }
                
                // Determine final API model ID
                val cleanApiModel = if (isInExceptionList) {
                    // Keep or add :free postfix for exception models
                    if (model.id.endsWith(":free", ignoreCase = true)) {
                        model.id
                    } else {
                        "$modelIdWithoutFree:free"
                    }
                } else {
                    // Remove :free for non-exception models
                    modelIdWithoutFree
                }
                
                // Only add if not already present (deduplication by base ID)
                if (!uniqueModels.containsKey(modelIdWithoutFree)) {
                    uniqueModels[modelIdWithoutFree] = ModelInfo(
                        displayName = cleanDisplayName,
                        apiModel = cleanApiModel,
                        isAvailable = true,
                        isPaid = isPaid,
                        created = model.created ?: 0
                    )
                }
            }
            
            uniqueModels.values.toList().sortedWith(
                compareBy<ModelInfo> { it.isPaid }.thenByDescending { it.created }
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get cached free models (memory or disk), or fetch them if not already loaded
     */
    suspend fun getOrFetchFreeModels(cacheKey: String? = null): List<ModelInfo> {
        // 1. If we have memory cached models and key matches, return them immediately
        if (cachedFreeModels.isNotEmpty() && cacheKey != null && cachedFreeModelsKey == cacheKey) {
            return cachedFreeModels
        }

        // 2. Check disk cache
        if (cacheKey != null) {
            val diskModels = ModelsCacheManager.getOpenRouterModels(cacheKey)
            if (diskModels != null && diskModels.isNotEmpty()) {
                cachedFreeModels = diskModels
                cachedFreeModelsKey = cacheKey
                return diskModels
            }
        }

        // 3. Otherwise fetch from network, update caches, and return
        val models = fetchFreeModels()
        if (models.isNotEmpty() && cacheKey != null) {
            cachedFreeModels = models
            cachedFreeModelsKey = cacheKey
            ModelsCacheManager.saveOpenRouterModels(models, cacheKey)
        }
        return models
    }

    /**
     * Fetch all available models from Groq
     * Returns all active models as ModelInfo list
     */
    suspend fun fetchGroqModels(): List<ModelInfo> {
        return try {
            val response = withContext(Dispatchers.IO) {
                GroqClient.api.getModels()
            }
            (response.data ?: emptyList())
                .filter { it.active != false } // Include only active models
                .map { model ->
                    val cleanName = model.id
                        .substringAfterLast("/")
                        .split("-")
                        .joinToString(" ") { part ->
                            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        }
                    ModelInfo(
                        displayName = cleanName,
                        apiModel = model.id,
                        isAvailable = true,
                        created = model.created ?: 0
                    )
                }
                .sortedByDescending { it.created }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get cached Groq models (memory or disk), or fetch them if not already loaded
     */
    suspend fun getOrFetchGroqModels(cacheKey: String? = null): List<ModelInfo> {
        // 1. If we have memory cached models and key matches, return them immediately
        if (cachedGroqModels.isNotEmpty() && cacheKey != null && cachedGroqModelsKey == cacheKey) {
            return cachedGroqModels
        }
        
        // 2. Check disk cache
        if (cacheKey != null) {
            val diskModels = ModelsCacheManager.getGroqModels(cacheKey)
            if (diskModels != null && diskModels.isNotEmpty()) {
                cachedGroqModels = diskModels
                cachedGroqModelsKey = cacheKey
                return diskModels
            }
        }

        // 3. Otherwise fetch from network, update caches, and return
        val models = fetchGroqModels()
        if (models.isNotEmpty() && cacheKey != null) {
            cachedGroqModels = models
            cachedGroqModelsKey = cacheKey
            ModelsCacheManager.saveGroqModels(models, cacheKey)
        }
        return models
    }
    
    /**
     * Fetch models in the background (used at app startup) to pre-warm the cache.
     * This avoids lag when ChatScreen is opened.
     */
    suspend fun prefetchModelsInBackground() {
        try {
            // OpenRouter prefetch
            val openRouterKey = FirebaseConfigManager.apiKey.value
            if (openRouterKey.isNotEmpty()) {
                val exceptionModels = FirebaseConfigManager.exceptionModels.value
                val cacheKey = "$openRouterKey|${exceptionModels.hashCode()}"
                
                val openRouterModels = fetchFreeModels()
                if (openRouterModels.isNotEmpty()) {
                    cachedFreeModels = openRouterModels
                    cachedFreeModelsKey = cacheKey
                    ModelsCacheManager.saveOpenRouterModels(openRouterModels, cacheKey)
                }
            }
            
            // Groq prefetch
            val firebaseGroqApiKey = FirebaseConfigManager.groqApiKey.value
            val userGroqKey = com.daemon.markvii.data.UserApiPreferences.groqApiKey.value
            val isUserGroqEnabled = com.daemon.markvii.data.UserApiPreferences.isGroqKeyEnabled.value
            val keyToUse = if (isUserGroqEnabled && userGroqKey.isNotBlank()) userGroqKey else firebaseGroqApiKey
            
            if (keyToUse.isNotBlank()) {
                val groqModelsList = fetchGroqModels()
                if (groqModelsList.isNotEmpty()) {
                    cachedGroqModels = groqModelsList
                    cachedGroqModelsKey = keyToUse
                    ModelsCacheManager.saveGroqModels(groqModelsList, keyToUse)
                }
            }
        } catch (e: Exception) {
            // Silently fail prefetch if network issues occur
        }
    }
    
    /**
     * Get a unified list of all models across all providers
     */
    suspend fun getAllGlobalModels(
        openRouterCacheKey: String?,
        groqCacheKey: String?
    ): List<GlobalModelInfo> {
        val geminiModels = FirebaseConfigManager.geminiModels.value.map {
            GlobalModelInfo(
                apiModel = it.apiModel,
                displayName = it.displayName,
                provider = com.daemon.markvii.ApiProvider.GEMINI,
                isPro = it.isPro,
                isPaid = false,
                isAvailable = true
            )
        }
        
        val openRouterModels = getOrFetchFreeModels(openRouterCacheKey).map {
            GlobalModelInfo(
                apiModel = it.apiModel,
                displayName = it.displayName,
                provider = com.daemon.markvii.ApiProvider.OPENROUTER,
                isPro = it.isPro,
                isPaid = it.isPaid,
                isAvailable = it.isAvailable,
                created = it.created
            )
        }
        
        val groqModels = getOrFetchGroqModels(groqCacheKey).map {
            GlobalModelInfo(
                apiModel = it.apiModel,
                displayName = it.displayName,
                provider = com.daemon.markvii.ApiProvider.GROQ,
                isPro = it.isPro,
                isPaid = it.isPaid,
                isAvailable = it.isAvailable,
                created = it.created
            )
        }
        
        return geminiModels + openRouterModels + groqModels
    }

    /**
     * Get streaming response from Groq with conversation history
     */
    suspend fun getGroqStreamingResponse(
        prompt: String,
        conversationHistory: List<Chat> = emptyList(),
        onChunk: (String) -> Unit
    ): Chat = withContext(Dispatchers.IO) {
        try {
            if (groq_api_key.isEmpty()) {
                throw Exception("API_KEY_MISSING|Groq API key is not configured. Please add your key in Settings.")
            }

            val modelToUse = when {
                selected_model.isNotEmpty() -> selected_model
                else -> "llama3-8b-8192" // Default Groq model
            }

            // Build messages array from conversation history
            val messages = mutableListOf<Message>()
            conversationHistory.takeLast(6).forEach { chat ->
                messages.add(
                    Message(
                        role = if (chat.isFromUser) "user" else "assistant",
                        content = listOf(Content(type = "text", text = chat.prompt))
                    )
                )
            }
            messages.add(
                Message(
                    role = "user",
                    content = listOf(Content(type = "text", text = prompt))
                )
            )

            val request = OpenRouterRequest(
                model = modelToUse,
                messages = messages,
                max_tokens = 3000,
                temperature = 0.7,
                stream = true
            )

            val responseBody = GroqClient.api.chatCompletionStream(request)
            val fullResponse = StringBuilder()

            try {
                responseBody.byteStream().bufferedReader().use { reader ->
                    reader.lineSequence().forEach { line ->
                        if (line.startsWith("data: ")) {
                            val data = line.substring(6)
                            if (data == "[DONE]") return@forEach
                            try {
                                val json = com.google.gson.Gson().fromJson(data, com.google.gson.JsonObject::class.java)
                                val delta = json.getAsJsonArray("choices")
                                    ?.get(0)?.asJsonObject
                                    ?.getAsJsonObject("delta")
                                    ?.get("content")?.asString
                                if (delta != null) {
                                    fullResponse.append(delta)
                                    withContext(Dispatchers.Main) {
                                        onChunk(delta)
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip malformed chunks
                            }
                        }
                    }
                }
            } catch (e: java.io.IOException) {
                if (fullResponse.isNotEmpty()) {
                    return@withContext Chat(
                        prompt = fullResponse.toString(),
                        bitmap = null,
                        isFromUser = false,
                        modelUsed = modelToUse
                    )
                }
                throw Exception("NETWORK_ERROR|Connection interrupted: ${e.message ?: "Network error"}")
            }

            return@withContext Chat(
                prompt = fullResponse.toString(),
                bitmap = null,
                isFromUser = false,
                modelUsed = modelToUse
            )

        } catch (e: Exception) {
            if (e.message?.contains("|") == true) throw e
            val errorMessage = when {
                e is retrofit2.HttpException -> when (e.code()) {
                    401 -> "UNAUTHORIZED|Invalid Groq API key. Check your key in Settings."
                    429 -> "RATE_LIMITED|Too many requests. Please wait and retry."
                    503 -> "NO_PROVIDER|Groq service is currently unavailable."
                    else -> "HTTP_ERROR|Error ${e.code()}: ${e.message()}"
                }
                e is java.net.SocketTimeoutException -> "TIMEOUT|Request timed out. Check your connection."
                e is java.net.UnknownHostException -> "NO_INTERNET|No internet connection available."
                e is java.net.ConnectException -> "CONNECTION_FAILED|Could not connect to Groq."
                e is java.io.IOException -> "NETWORK_ERROR|Network error: ${e.message}"
                else -> "UNKNOWN_ERROR|${e.message ?: "An unexpected error occurred"}"
            }
            throw Exception(errorMessage)
        }
    }
    
    /**
     * Convert Bitmap to Base64 string for API
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    suspend fun getResponse(prompt: String): Chat {
        try {
            // Check if API key is loaded
            if (openrouter_api_key.isEmpty()) {
                throw Exception("API_KEY_MISSING|API key is not configured")
            }
            
            // Use a valid default model if none selected
            val modelToUse = when {
                selected_model.isNotEmpty() -> selected_model
                else -> "anthropic/claude-3-5-sonnet-20241022" // Updated model name
            }
            
            val request = OpenRouterRequest(
                model = modelToUse,
                messages = listOf(
                    Message(
                        role = "user",
                        content = listOf(
                            Content(
                                type = "text",
                                text = prompt
                            )
                        )
                    )
                ),
                max_tokens = 3000,
                temperature = 0.7
            )

            val response = withContext(Dispatchers.IO) {
                OpenRouterClient.api.chatCompletion(request)
            }

            // Check for errors
            if (response.error != null) {
                val errorMsg = when {
                    response.error.message.contains("401") || response.error.message.contains("Unauthorized") -> 
                        "HTTP_401|HTTP 401: Unauthorized - API key is invalid or missing"
                    response.error.message.contains("404") || response.error.message.contains("Not Found") ->
                        "HTTP_404|HTTP 404: Model Not Found - The model '$modelToUse' doesn't exist"
                    else -> "API_ERROR|${response.error.message}"
                }
                throw Exception(errorMsg)
            }

            // Get response text
            val responseText = response.choices?.firstOrNull()?.message?.content 
                ?: "No response received"

            return Chat(
                prompt = responseText,
                bitmap = null,
                isFromUser = false,
                modelUsed = modelToUse
            )

        } catch (e: Exception) {
            // Re-throw if already formatted
            if (e.message?.contains("|") == true) {
                throw e
            }
            
            // Handle HTTP errors with specific codes
            val errorMessage = when {
                e is retrofit2.HttpException -> {
                    when (e.code()) {
                        400 -> "BAD_REQUEST|Invalid request parameters or CORS issue"
                        401 -> "UNAUTHORIZED|Invalid API key or expired session"
                        402 -> "INSUFFICIENT_CREDITS|Your account has insufficient credits"
                        403 -> "CONTENT_FLAGGED|Your input was flagged by moderation"
                        404 -> {
                            // Model not found - try adding :free postfix
                            val modelToUse = when {
                                selected_model.isNotEmpty() -> selected_model
                                else -> "anthropic/claude-3-5-sonnet-20241022"
                            }
                            
                            // If model doesn't have :free, add it to exception list and retry
                            if (!modelToUse.endsWith(":free", ignoreCase = true)) {
                                val fixedModel = handle404Error(modelToUse)
                                "MODEL_404_RETRY|Model not found. Retrying with corrected ID: $fixedModel"
                            } else {
                                "MODEL_NOT_FOUND|Model not available: $modelToUse"
                            }
                        }
                        408 -> "REQUEST_TIMEOUT|Your request timed out. Try again"
                        429 -> "RATE_LIMITED|Too many requests. Please wait and retry"
                        502 -> "MODEL_DOWN|Model is currently unavailable or returned invalid response"
                        503 -> "NO_PROVIDER|No available model provider meets your requirements"
                        else -> "HTTP_ERROR|Error ${e.code()}: ${e.message()}"
                    }
                }
                e is java.net.SocketTimeoutException -> "TIMEOUT|Request timed out. Check your connection"
                e is java.net.UnknownHostException -> "NO_INTERNET|No internet connection available"
                e is java.net.ConnectException -> "CONNECTION_FAILED|Could not connect to server"
                e is java.io.IOException -> "NETWORK_ERROR|Network error: ${e.message}"
                else -> "UNKNOWN_ERROR|${e.message ?: "An unexpected error occurred"}"
            }
            
            throw Exception(errorMessage)
        }
    }

    suspend fun getResponseWithImage(prompt: String, bitmap: Bitmap): Chat {
        try {
            // Convert bitmap to base64
            val base64Image = bitmapToBase64(bitmap)
            val dataUrl = "data:image/jpeg;base64,$base64Image"
            
            // Use a valid default model if none selected
            val modelToUse = when {
                selected_model.isNotEmpty() -> selected_model
                else -> "anthropic/claude-3-5-sonnet-20241022" // Updated model name
            }

            val request = OpenRouterRequest(
                model = modelToUse,
                messages = listOf(
                    Message(
                        role = "user",
                        content = listOf(
                            Content(
                                type = "image_url",
                                image_url = ImageUrl(url = dataUrl)
                            ),
                            Content(
                                type = "text",
                                text = prompt
                            )
                        )
                    )
                ),
                max_tokens = 3000,
                temperature = 0.7
            )

            val response = withContext(Dispatchers.IO) {
                OpenRouterClient.api.chatCompletion(request)
            }

            // Check for errors
            if (response.error != null) {
                throw Exception("API_ERROR|Error: ${response.error.message}")
            }

            // Get response text
            val responseText = response.choices?.firstOrNull()?.message?.content 
                ?: "No response received"

            return Chat(
                prompt = responseText,
                bitmap = null,
                isFromUser = false,
                modelUsed = modelToUse
            )

        } catch (e: Exception) {
            // Re-throw if already formatted
            if (e.message?.contains("|") == true) {
                throw e
            }
            
            // Handle HTTP errors with specific codes
            val errorMessage = when {
                e is retrofit2.HttpException -> {
                    when (e.code()) {
                        400 -> "BAD_REQUEST|Invalid request parameters or CORS issue"
                        401 -> "UNAUTHORIZED|Invalid API key or expired session"
                        402 -> "INSUFFICIENT_CREDITS|Your account has insufficient credits"
                        403 -> "CONTENT_FLAGGED|Your input was flagged by moderation"
                        404 -> {
                            // Model not found - try adding :free postfix
                            val modelToUse = when {
                                selected_model.isNotEmpty() -> selected_model
                                else -> "anthropic/claude-3-5-sonnet-20241022"
                            }
                            
                            // If model doesn't have :free, add it to exception list and retry
                            if (!modelToUse.endsWith(":free", ignoreCase = true)) {
                                val fixedModel = handle404Error(modelToUse)
                                "MODEL_404_RETRY|Model not found. Retrying with corrected ID: $fixedModel"
                            } else {
                                "MODEL_NOT_FOUND|Model not available: $modelToUse"
                            }
                        }
                        408 -> "REQUEST_TIMEOUT|Your request timed out. Try again"
                        429 -> "RATE_LIMITED|Too many requests. Please wait and retry"
                        502 -> "MODEL_DOWN|Model is currently unavailable or returned invalid response"
                        503 -> "NO_PROVIDER|No available model provider meets your requirements"
                        else -> "HTTP_ERROR|Error ${e.code()}: ${e.message()}"
                    }
                }
                e is java.net.SocketTimeoutException -> "TIMEOUT|Request timed out. Check your connection"
                e is java.net.UnknownHostException -> "NO_INTERNET|No internet connection available"
                e is java.net.ConnectException -> "CONNECTION_FAILED|Could not connect to server"
                e is java.io.IOException -> "NETWORK_ERROR|Network error: ${e.message}"
                else -> "UNKNOWN_ERROR|${e.message ?: "An unexpected error occurred"}"
            }
            
            throw Exception(errorMessage)
        }
    }
    
    /**
     * Get streaming response from AI model with conversation history
     * Yields partial responses as they are generated
     */
    suspend fun getStreamingResponse(
        prompt: String,
        conversationHistory: List<Chat> = emptyList(),
        onChunk: (String) -> Unit
    ): Chat = withContext(Dispatchers.IO) {
        try {
            // Check if API key is loaded
            if (openrouter_api_key.isEmpty()) {
                throw Exception("API_KEY_MISSING|API key is not configured")
            }
            
            // Use a valid default model if none selected
            val modelToUse = when {
                selected_model.isNotEmpty() -> selected_model
                else -> "anthropic/claude-3-5-sonnet-20241022"
            }
            
            // Build messages array from conversation history
            val messages = mutableListOf<Message>()
            
            // Add conversation history (limit to last 6 messages for faster response)
            conversationHistory.takeLast(6).forEach { chat ->
                messages.add(
                    Message(
                        role = if (chat.isFromUser) "user" else "assistant",
                        content = listOf(
                            Content(
                                type = "text",
                                text = chat.prompt
                            )
                        )
                    )
                )
            }
            
            // Add current prompt as the latest user message
//            messages.add(
//                Message(
//                    role = "user",
//                    content = listOf(
//                        Content(
//                            type = "text",
//                            text = prompt
//                        )
//                    )
//                )
//            )
            
            val request = OpenRouterRequest(
                model = modelToUse,
                messages = messages,
                max_tokens = 3000,
                temperature = 0.7,
                stream = true
            )

            val responseBody = OpenRouterClient.api.chatCompletionStream(request)
            val fullResponse = StringBuilder()
            
            // Read SSE stream with better error handling
            try {
                responseBody.byteStream().bufferedReader().use { reader ->
                    reader.lineSequence().forEach { line ->
                        if (line.startsWith("data: ")) {
                            val data = line.substring(6)
                            if (data == "[DONE]") return@forEach
                            
                            try {
                                val json = com.google.gson.Gson().fromJson(data, com.google.gson.JsonObject::class.java)
                                val delta = json.getAsJsonArray("choices")
                                    ?.get(0)?.asJsonObject
                                    ?.getAsJsonObject("delta")
                                    ?.get("content")?.asString
                                
                                if (delta != null) {
                                    fullResponse.append(delta)
                                    withContext(Dispatchers.Main) {
                                        onChunk(delta)
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip malformed chunks
                            }
                        }
                    }
                }
            } catch (e: java.io.IOException) {
                // If we got some response before connection error, return what we have
                if (fullResponse.isNotEmpty()) {
                    return@withContext Chat(
                        prompt = fullResponse.toString(),
                        bitmap = null,
                        isFromUser = false,
                        modelUsed = modelToUse
                    )
                }
                throw Exception("NETWORK_ERROR|Connection interrupted: ${e.message ?: "Network error"}")
            }

            return@withContext Chat(
                prompt = fullResponse.toString(),
                bitmap = null,
                isFromUser = false,
                modelUsed = modelToUse
            )

        } catch (e: Exception) {
            // Re-throw if already formatted
            if (e.message?.contains("|") == true) {
                throw e
            }
            
            // Handle HTTP errors with specific codes
            val errorMessage = when {
                e is retrofit2.HttpException -> {
                    when (e.code()) {
                        400 -> "BAD_REQUEST|Invalid request parameters or CORS issue"
                        401 -> "UNAUTHORIZED|Invalid API key or expired session"
                        402 -> "INSUFFICIENT_CREDITS|Your account has insufficient credits"
                        403 -> "CONTENT_FLAGGED|Your input was flagged by moderation"
                        404 -> {
                            // Model not found - try adding :free postfix
                            val modelToUse = when {
                                selected_model.isNotEmpty() -> selected_model
                                else -> "anthropic/claude-3-5-sonnet-20241022"
                            }
                            
                            // If model doesn't have :free, add it to exception list and retry
                            if (!modelToUse.endsWith(":free", ignoreCase = true)) {
                                val fixedModel = handle404Error(modelToUse)
                                "MODEL_404_RETRY|Model not found. Retrying with corrected ID: $fixedModel"
                            } else {
                                "MODEL_NOT_FOUND|Model not available: $modelToUse"
                            }
                        }
                        408 -> "REQUEST_TIMEOUT|Your request timed out. Try again"
                        429 -> "RATE_LIMITED|Too many requests. Please wait and retry"
                        502 -> "MODEL_DOWN|Model is currently unavailable or returned invalid response"
                        503 -> "NO_PROVIDER|No available model provider meets your requirements"
                        else -> "HTTP_ERROR|Error ${e.code()}: ${e.message()}"
                    }
                }
                e is java.net.SocketTimeoutException -> "TIMEOUT|Request timed out. Check your connection"
                e is java.net.UnknownHostException -> "NO_INTERNET|No internet connection available"
                e is java.net.ConnectException -> "CONNECTION_FAILED|Could not connect to server"
                e is java.io.IOException -> "NETWORK_ERROR|Network error: ${e.message}"
                else -> "UNKNOWN_ERROR|${e.message ?: "An unexpected error occurred"}"
            }
            
            throw Exception(errorMessage)
        }
    }
    
    /**
     * Handle 404 model not found error
     * Automatically adds model to exception list with ":free" postfix
     */
    private suspend fun handle404Error(modelId: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Check if model already has :free postfix
                if (modelId.endsWith(":free", ignoreCase = true)) {
                    return@withContext modelId
                }
                
                // Add :free postfix to model ID
                val modelWithFree = "$modelId:free"
                
                // Get model name from available models list
                val modelName = try {
                    val allModels = fetchAvailableModels()
                    val foundModel = allModels.find { it.id == modelWithFree || it.id == modelId }
                    (foundModel?.name ?: modelId.substringAfterLast("/").replace("-", " "))
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                } catch (e: Exception) {
                    modelId.substringAfterLast("/").replace("-", " ")
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
                
                // Add to Firebase exception list with model name (this will await and save to Firestore)
                FirebaseConfigManager.addExceptionModel(modelWithFree, modelName)
                
                // Update selected model
                selected_model = modelWithFree
                
                return@withContext modelWithFree
            } catch (e: Exception) {
                return@withContext modelId
            }
        }
    }
}

