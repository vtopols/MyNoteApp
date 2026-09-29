package com.example.note.utils

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.example.note.ui.theme.AppAccentColor

object ThemeManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_IS_DARK = "is_dark"
    private const val KEY_ACCENT_COLOR = "accent_color"

    private var _isDark = mutableStateOf(false)
    val isDark get() = _isDark.value

    private var _accentColor = mutableStateOf(AppAccentColor.BLUE)
    val accentColor get() = _accentColor.value

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        _isDark.value = prefs.getBoolean(KEY_IS_DARK, false)
        val accentName = prefs.getString(KEY_ACCENT_COLOR, AppAccentColor.BLUE.name)
        _accentColor.value = try {
            AppAccentColor.valueOf(accentName ?: AppAccentColor.BLUE.name)
        } catch (e: Exception) {
            AppAccentColor.BLUE
        }
    }

    fun setDarkTheme(context: Context, dark: Boolean) {
        _isDark.value = dark
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DARK, dark).apply()
    }

    fun setAccentColor(context: Context, accent: AppAccentColor) {
        _accentColor.value = accent
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACCENT_COLOR, accent.name).apply()
    }
}