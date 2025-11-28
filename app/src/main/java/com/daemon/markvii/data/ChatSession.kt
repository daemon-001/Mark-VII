package com.daemon.markvii.data

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Represents a chat session stored in Firestore
 * @author Nitesh
 */
data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "", // Owner of this session
    val title: String = "New Chat", // Auto-generated from first message
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val messages: List<SerializableChat> = emptyList()
) {
    /**
     * Convert to Firestore-compatible map
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "messages" to messages.map { it.toMap() }
        )
    }
    
    companion object {
        /**
         * Create ChatSession from Firestore document
         */
        fun fromMap(map: Map<String, Any>): ChatSession {
            @Suppress("UNCHECKED_CAST")
            val messagesData = map["messages"] as? List<Map<String, Any>> ?: emptyList()
            val messages = messagesData.map { SerializableChat.fromMap(it) }
            
            return ChatSession(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                userId = map["userId"] as? String ?: "",
                title = map["title"] as? String ?: "New Chat",
                createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
                updatedAt = map["updatedAt"] as? Timestamp ?: Timestamp.now(),
                messages = messages
            )
        }
    }
}

/**
 * Serializable version of Chat for Firestore storage
 * (Bitmaps are stored as Base64 strings)
 */
data class SerializableChat(
    val prompt: String,
    val bitmapBase64: String?,
    val isFromUser: Boolean,
    val modelUsed: String,
    val isStreaming: Boolean,
    val id: String
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "prompt" to prompt,
            "bitmapBase64" to bitmapBase64,
            "isFromUser" to isFromUser,
            "modelUsed" to modelUsed,
            "isStreaming" to isStreaming,
            "id" to id
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>): SerializableChat {
            return SerializableChat(
                prompt = map["prompt"] as? String ?: "",
                bitmapBase64 = map["bitmapBase64"] as? String,
                isFromUser = map["isFromUser"] as? Boolean ?: false,
                modelUsed = map["modelUsed"] as? String ?: "",
                isStreaming = map["isStreaming"] as? Boolean ?: false,
                id = map["id"] as? String ?: UUID.randomUUID().toString()
            )
        }
    }
}
