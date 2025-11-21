package com.daemon.markvii.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream

/**
 * Manages persistence of chat history using SharedPreferences
 */
object ChatHistoryManager {
    
    private const val PREFS_NAME = "mark_vii_chat_history"
    private const val KEY_CHAT_LIST = "chat_list"
    private const val MAX_CHAT_HISTORY = 50 // Limit to prevent excessive storage
    
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    
    /**
     * Initialize with application context
     * Call this once during app startup
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Save chat history to persistent storage
     */
    fun saveChatHistory(chatList: List<Chat>) {
        try {
            // Convert to serializable format
            val serializableChats = chatList.take(MAX_CHAT_HISTORY).map { chat ->
                SerializableChat(
                    prompt = chat.prompt,
                    bitmapBase64 = chat.bitmap?.let { bitmapToBase64(it) },
                    isFromUser = chat.isFromUser,
                    modelUsed = chat.modelUsed,
                    isStreaming = false, // Don't persist streaming state
                    id = chat.id
                )
            }
            
            // Serialize to JSON
            val json = gson.toJson(serializableChats)
            
            // Save to SharedPreferences
            prefs.edit().putString(KEY_CHAT_LIST, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Load chat history from persistent storage
     */
    fun loadChatHistory(): List<Chat> {
        try {
            val json = prefs.getString(KEY_CHAT_LIST, null) ?: return emptyList()
            
            // Deserialize from JSON
            val type = object : TypeToken<List<SerializableChat>>() {}.type
            val serializableChats: List<SerializableChat> = gson.fromJson(json, type)
            
            // Convert back to Chat objects
            return serializableChats.map { serializableChat ->
                Chat(
                    prompt = serializableChat.prompt,
                    bitmap = serializableChat.bitmapBase64?.let { base64ToBitmap(it) },
                    isFromUser = serializableChat.isFromUser,
                    modelUsed = serializableChat.modelUsed,
                    isStreaming = false,
                    id = serializableChat.id
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    /**
     * Clear all chat history
     */
    fun clearChatHistory() {
        prefs.edit().remove(KEY_CHAT_LIST).apply()
    }
    
    /**
     * Convert Bitmap to Base64 string for storage
     */
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    /**
     * Convert Base64 string back to Bitmap
     */
    private fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Serializable version of Chat for JSON storage
     */
    private data class SerializableChat(
        val prompt: String,
        val bitmapBase64: String?,
        val isFromUser: Boolean,
        val modelUsed: String,
        val isStreaming: Boolean,
        val id: String
    )
}
