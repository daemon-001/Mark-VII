package com.daemon.markvii.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.listOf

/**
 * @author Nitesh
 */

/**
 * Data class to hold model information
 */
data class ModelInfo(
    val displayName: String,
    val apiModel: String,
    val isAvailable: Boolean = true
)

/**
 * Configuration class for all AI models
 * Centralized location for managing model definitions
 */
object ModelConfiguration {

    val models = listOf(
        ModelInfo("Gemini 1.5 8b ⚡", "gemini-1.5-flash-8b"),
        ModelInfo("Gemini 1.5 ⚡⚡", "gemini-1.5-flash"),
        ModelInfo("Gemini 2.0 lite 🤖", "gemini-2.0-flash-lite"),
        ModelInfo("Gemini 2.0 🤖🤖", "gemini-2.0-flash"),
        ModelInfo("Gemini 2.5 lite 🚀", "gemini-2.5-flash-lite"),
        ModelInfo("Gemini 2.5 🚀🚀", "gemini-2.5-flash"),






//        ModelInfo("Gemini 2.5 pro", "gemini-2.5-pro"),//
//        ModelInfo("gemini-2.5-flash-preview-tts", "gemini-2.5-flash-preview-tts"), //audio op
//        ModelInfo("gemini-2.0-flash-preview-image-generation", "gemini-2.0-flash-preview-image-generation"),//image op
//        ModelInfo("gemini-1.5-pro", "gemini-1.5-pro"),//
//        ModelInfo("gemini-embedding-exp", "gemini-embedding-exp"),//not avail
//        ModelInfo("imagen-3.0-generate-002", "imagen-3.0-generate-002"),//
//        ModelInfo("veo-2.0-generate-001", "veo-2.0-generate-001"),//
//        ModelInfo("gemini-2.0-flash-live-001", "gemini-2.0-flash-live-001"),//


//        ModelInfo("Gemini Flash 8B ⚡⚡", "gemini-1.5-flash-8b"),
//        ModelInfo("Gemini Flash ⚡", "gemini-1.5-flash-001"),
//        ModelInfo("Gemini Pro ✨", "gemini-2.5-pro"),
//        ModelInfo("Gemini Ultra 🚀", "gemini-ultra"),
//        ModelInfo("Claude Sonnet 🎭", "claude-sonnet"),
//        ModelInfo("GPT-4 Turbo 🔥", "gpt-4-turbo"),
//        ModelInfo("LLaMA 2 🦙", "llama-2-70b"),
//        ModelInfo("PaLM 2 🌴", "palm-2"),
//        ModelInfo("Mistral 7B 🌪️", "mistral-7b"),
//        ModelInfo("CodeLlama 💻", "codellama-34b")
    )
}

object ChatData {

//    val gpt_api_key = "Enter your API Key"
    val gemini_api_key = Keys.gemini_api_key ?: "your-api-key"  // Set here your api key
//    val gpt_api_key = ""

    var gemini_api_model = ""
//    var gpt_api_model = ""


    suspend fun getResponse(prompt: String): Chat {
        val generativeModel = GenerativeModel(
            modelName = gemini_api_model, apiKey = gemini_api_key
        )

        try {
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }

            return Chat(
                prompt = response.text ?: "error",
                bitmap = null,
                isFromUser = false
            )

        } catch (e: Exception) {
            return Chat(
//                prompt = "Model Offline",
                prompt = e.message ?: "error",
                bitmap = null,
                isFromUser = false
            )
        }

    }

    suspend fun getResponseWithImage(prompt: String, bitmap: Bitmap): Chat {
        val generativeModel = GenerativeModel(

            modelName = "gemini-2.5-flash", apiKey = gemini_api_key
        )


        try {

            val inputContent = content {
                image(bitmap)
                text(prompt)
            }

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(inputContent)
            }

            return Chat(
                prompt = response.text ?: "error",
                bitmap = null,
                isFromUser = false
            )

        } catch (e: Exception) {
            return Chat(
                prompt = "Model Offline",
//                prompt = e.message ?: "error",
                bitmap = null,
                isFromUser = false
            )
        }
    }
}




















