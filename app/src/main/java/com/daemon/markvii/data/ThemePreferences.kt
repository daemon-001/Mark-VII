package com.daemon.markvii.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    SYSTEM_DEFAULT,
    LIGHT,
    DARK
}

object ThemePreferences {
    private const val PREFS_NAME = "theme_preferences"
    private const val KEY_THEME = "app_theme"
    
    private lateinit var prefs: SharedPreferences
    
    private val _currentTheme = MutableStateFlow(AppTheme.SYSTEM_DEFAULT)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Load saved theme
        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM_DEFAULT.name)
        _currentTheme.value = AppTheme.valueOf(themeName ?: AppTheme.SYSTEM_DEFAULT.name)
    }
    
    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }
    
    fun getTheme(): AppTheme = _currentTheme.value
}
