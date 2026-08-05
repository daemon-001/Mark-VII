package com.daemon.markvii.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ModelsCacheManager {
    private const val PREFS_NAME = "models_cache_prefs"
    private const val KEY_OPENROUTER_MODELS = "openrouter_models"
    private const val KEY_OPENROUTER_CACHE_KEY = "openrouter_cache_key"
    
    private const val KEY_GROQ_MODELS = "groq_models"
    private const val KEY_GROQ_CACHE_KEY = "groq_cache_key"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private val listType = object : TypeToken<List<ModelInfo>>() {}.type

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveOpenRouterModels(models: List<ModelInfo>, cacheKey: String) {
        val json = gson.toJson(models)
        prefs.edit().apply {
            putString(KEY_OPENROUTER_MODELS, json)
            putString(KEY_OPENROUTER_CACHE_KEY, cacheKey)
            apply()
        }
    }

    fun getOpenRouterModels(cacheKey: String): List<ModelInfo>? {
        val savedKey = prefs.getString(KEY_OPENROUTER_CACHE_KEY, null)
        if (savedKey != cacheKey) return null

        val json = prefs.getString(KEY_OPENROUTER_MODELS, null) ?: return null
        return try {
            gson.fromJson<List<ModelInfo>>(json, listType)
        } catch (e: Exception) {
            null
        }
    }

    fun saveGroqModels(models: List<ModelInfo>, cacheKey: String) {
        val json = gson.toJson(models)
        prefs.edit().apply {
            putString(KEY_GROQ_MODELS, json)
            putString(KEY_GROQ_CACHE_KEY, cacheKey)
            apply()
        }
    }

    fun getGroqModels(cacheKey: String): List<ModelInfo>? {
        val savedKey = prefs.getString(KEY_GROQ_CACHE_KEY, null)
        if (savedKey != cacheKey) return null

        val json = prefs.getString(KEY_GROQ_MODELS, null) ?: return null
        return try {
            gson.fromJson<List<ModelInfo>>(json, listType)
        } catch (e: Exception) {
            null
        }
    }
    
    fun clearAllModels() {
        prefs.edit().clear().apply()
    }
}
