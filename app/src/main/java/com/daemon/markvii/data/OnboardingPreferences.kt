package com.daemon.markvii.data

import android.content.Context
import android.content.SharedPreferences

object OnboardingPreferences {
    private const val PREFS_NAME = "onboarding_prefs"
    private const val KEY_IS_FIRST_RUN = "is_first_run"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstRun(): Boolean {
        return prefs.getBoolean(KEY_IS_FIRST_RUN, true)
    }

    fun setFirstRunCompleted() {
        prefs.edit().putBoolean(KEY_IS_FIRST_RUN, false).apply()
    }
    
    // For testing/debugging purposes
    fun resetFirstRun() {
        prefs.edit().putBoolean(KEY_IS_FIRST_RUN, true).apply()
    }
}
