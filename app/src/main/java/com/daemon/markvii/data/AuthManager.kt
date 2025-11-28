package com.daemon.markvii.data

import android.app.Activity
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

/**
 * Manages Firebase Authentication and Google Sign-In
 * @author Nitesh
 */
object AuthManager {
    
    private const val TAG = "AuthManager"
    const val RC_SIGN_IN = 9001
    
    // Your Web Client ID from Firebase Console
    // TODO: Replace with actual Web Client ID from google-services.json
    private const val WEB_CLIENT_ID = "81954501703-5lh5f9eah4tql8acd51vc2a86nhcl0sp.apps.googleusercontent.com"
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
    init {
        // Listen to auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }
    
    /**
     * Sign in with Google using traditional GoogleSignInClient
     * More reliable than Credential Manager on some devices
     */
    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "Starting Google Sign-In process...")
            
            // Configure Google Sign-In
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
            )
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build()
            
            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(activity, gso)
            
            Log.d(TAG, "Launching Google Sign-In intent...")
            
            // Sign out first to ensure account picker shows
            googleSignInClient.signOut().await()
            
            // Get sign-in intent
            val signInIntent = googleSignInClient.signInIntent
            
            // Launch sign-in
            activity.startActivityForResult(signInIntent, RC_SIGN_IN)
            
            // Return pending result - actual result will be handled in onActivityResult
            Result.success(auth.currentUser ?: throw Exception("Sign-in in progress"))
            
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed with exception: ${e.javaClass.simpleName}", e)
            Log.e(TAG, "Error message: ${e.message}")
            val errorMessage = when {
                e.message?.contains("canceled", ignoreCase = true) == true ->
                    "SIGN_IN_CANCELED|Sign in was canceled"
                e.message?.contains("network", ignoreCase = true) == true ->
                    "NETWORK_ERROR|Network error during sign in"
                e.message?.contains("12500", ignoreCase = true) == true ->
                    "API_NOT_CONNECTED|Google Play Services not available. Please check SHA-1 fingerprint in Firebase Console."
                else -> "SIGN_IN_ERROR|${e.message ?: "Unknown error during sign in"}"
            }
            Result.failure(Exception(errorMessage))
        }
    }
    
    /**
     * Handle the result from Google Sign-In activity
     * Call this from MainActivity's onActivityResult
     */
    suspend fun handleSignInResult(data: android.content.Intent?): Result<FirebaseUser> {
        return try {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.await()
            
            Log.d(TAG, "Got Google account: ${account.email}")
            
            // Get ID token
            val idToken = account.idToken
            if (idToken == null) {
                Log.e(TAG, "ID token is null")
                return Result.failure(Exception("INVALID_TOKEN|Failed to get ID token"))
            }
            
            Log.d(TAG, "Authenticating with Firebase...")
            
            // Authenticate with Firebase
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            
            val user = authResult.user
            if (user != null) {
                Log.d(TAG, "Sign in successful: ${user.email}")
                Result.success(user)
            } else {
                Log.e(TAG, "Authentication succeeded but user is null")
                Result.failure(Exception("SIGN_IN_FAILED|Authentication succeeded but user is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle sign-in result", e)
            Result.failure(Exception("SIGN_IN_ERROR|${e.message ?: "Failed to complete sign-in"}"))
        }
    }
    
    /**
     * Sign out the current user
     */
    fun signOut() {
        auth.signOut()
        Log.d(TAG, "User signed out")
    }
    
    /**
     * Get current user ID (null if not signed in)
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Generate a random nonce for security
     */
    private fun generateNonce(): String {
        return UUID.randomUUID().toString()
    }
    
    /**
     * Hash the nonce using SHA-256
     */
    private fun hashNonce(nonce: String): String {
        val bytes = nonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}
