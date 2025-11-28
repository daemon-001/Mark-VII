package com.daemon.markvii.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Manages chat sessions in Firestore
 * @author Nitesh
 */
object FirestoreChatManager {
    
    private const val TAG = "FirestoreChatManager"
    private const val COLLECTION_SESSIONS = "chat_sessions"
    
    private val firestore = FirebaseFirestore.getInstance()
    
    /**
     * Create a new chat session for a user
     */
    suspend fun createSession(userId: String, title: String = "New Chat"): Result<ChatSession> {
        return try {
            val session = ChatSession(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = title,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                messages = emptyList()
            )
            
            firestore.collection(COLLECTION_SESSIONS)
                .document(session.id)
                .set(session.toMap())
                .await()
            
            Log.d(TAG, "Created session: ${session.id}")
            Result.success(session)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create session", e)
            Result.failure(Exception("FIRESTORE_ERROR|Failed to create chat session: ${e.message}"))
        }
    }
    
    /**
     * Load all sessions for a user, ordered by most recent
     */
    suspend fun loadUserSessions(userId: String): Result<List<ChatSession>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SESSIONS)
                .whereEqualTo("userId", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val sessions = snapshot.documents.mapNotNull { doc ->
                try {
                    ChatSession.fromMap(doc.data ?: emptyMap())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse session: ${doc.id}", e)
                    null
                }
            }
            
            Log.d(TAG, "Loaded ${sessions.size} sessions for user: $userId")
            Result.success(sessions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load sessions", e)
            Result.failure(Exception("FIRESTORE_ERROR|Failed to load chat sessions: ${e.message}"))
        }
    }
    
    /**
     * Load a specific session by ID
     */
    suspend fun loadSession(sessionId: String): Result<ChatSession> {
        return try {
            val doc = firestore.collection(COLLECTION_SESSIONS)
                .document(sessionId)
                .get()
                .await()
            
            if (!doc.exists()) {
                return Result.failure(Exception("SESSION_NOT_FOUND|Session not found: $sessionId"))
            }
            
            val session = ChatSession.fromMap(doc.data ?: emptyMap())
            Log.d(TAG, "Loaded session: $sessionId")
            Result.success(session)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load session: $sessionId", e)
            Result.failure(Exception("FIRESTORE_ERROR|Failed to load session: ${e.message}"))
        }
    }
    
    /**
     * Save/update a session with new messages
     */
    suspend fun saveSession(session: ChatSession, chatList: List<Chat>): Result<Unit> {
        return try {
            // Convert Chat objects to SerializableChat
            val serializableMessages = chatList.map { chat ->
                SerializableChat(
                    prompt = chat.prompt,
                    bitmapBase64 = chat.bitmap?.let { bitmapToBase64(it) },
                    isFromUser = chat.isFromUser,
                    modelUsed = chat.modelUsed,
                    isStreaming = false, // Don't persist streaming state
                    id = chat.id
                )
            }
            
            // Generate title from first user message if still "New Chat"
            val title = if (session.title == "New Chat" && serializableMessages.isNotEmpty()) {
                val firstUserMessage = serializableMessages.firstOrNull { it.isFromUser }
                firstUserMessage?.prompt?.take(50) ?: "New Chat"
            } else {
                session.title
            }
            
            val updatedSession = session.copy(
                title = title,
                updatedAt = Timestamp.now(),
                messages = serializableMessages
            )
            
            firestore.collection(COLLECTION_SESSIONS)
                .document(session.id)
                .set(updatedSession.toMap())
                .await()
            
            Log.d(TAG, "Saved session: ${session.id} with ${chatList.size} messages")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save session: ${session.id}", e)
            Result.failure(Exception("FIRESTORE_ERROR|Failed to save session: ${e.message}"))
        }
    }
    
    /**
     * Delete a session
     */
    suspend fun deleteSession(sessionId: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_SESSIONS)
                .document(sessionId)
                .delete()
                .await()
            
            Log.d(TAG, "Deleted session: $sessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete session: $sessionId", e)
            Result.failure(Exception("FIRESTORE_ERROR|Failed to delete session: ${e.message}"))
        }
    }

    /**
     * Rename a session
     */
    suspend fun renameSession(sessionId: String, newTitle: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_SESSIONS)
                .document(sessionId)
                .update("title", newTitle)
                .await()

            Log.d(TAG, "Renamed session: $sessionId to $newTitle")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rename session: $sessionId", e)
            Result.failure(Exception("FIRESTORE_ERROR|Failed to rename session: ${e.message}"))
        }
    }
    
    /**
     * Convert Chat list to SerializableChat list
     */
    fun chatListToSerializable(chatList: List<Chat>): List<SerializableChat> {
        return chatList.map { chat ->
            SerializableChat(
                prompt = chat.prompt,
                bitmapBase64 = chat.bitmap?.let { bitmapToBase64(it) },
                isFromUser = chat.isFromUser,
                modelUsed = chat.modelUsed,
                isStreaming = false,
                id = chat.id
            )
        }
    }
    
    /**
     * Convert SerializableChat list to Chat list
     */
    fun serializableToChatList(serializableList: List<SerializableChat>): List<Chat> {
        return serializableList.map { serializable ->
            Chat(
                prompt = serializable.prompt,
                bitmap = serializable.bitmapBase64?.let { base64ToBitmap(it) },
                isFromUser = serializable.isFromUser,
                modelUsed = serializable.modelUsed,
                isStreaming = false,
                id = serializable.id
            )
        }
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
}
