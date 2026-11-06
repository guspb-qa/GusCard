package com.example.guscard

import android.content.Context
import android.content.SharedPreferences

object PrefsHelper {
    private const val PREFS_NAME = "guscard_prefs"
    private const val KEY_VIEW_MODE = "view_mode"
    private const val KEY_START_SCREEN = "start_screen"
    private const val KEY_LAST_CARD_ID = "last_card_id"
    private const val KEY_THEME = "theme_mode" // 0 = Светлая, 1 = Темная, 2 = Системная

    const val VIEW_GRID = 0
    const val VIEW_LIST = 1

    const val START_LIST = 0
    const val START_LAST_CARD = 1

    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getViewMode(context: Context): Int = getPrefs(context).getInt(KEY_VIEW_MODE, VIEW_GRID)
    fun setViewMode(context: Context, mode: Int) {
        getPrefs(context).edit().putInt(KEY_VIEW_MODE, mode).apply()
    }

    fun getStartScreen(context: Context): Int = getPrefs(context).getInt(KEY_START_SCREEN, START_LIST)
    fun setStartScreen(context: Context, screen: Int) {
        getPrefs(context).edit().putInt(KEY_START_SCREEN, screen).apply()
    }

    fun getLastCardId(context: Context): Long = getPrefs(context).getLong(KEY_LAST_CARD_ID, -1L)
    fun setLastCardId(context: Context, id: Long) {
        getPrefs(context).edit().putLong(KEY_LAST_CARD_ID, id).apply()
    }

    fun getTheme(context: Context): Int = getPrefs(context).getInt(KEY_THEME, THEME_SYSTEM)
    fun setTheme(context: Context, theme: Int) {
        getPrefs(context).edit().putInt(KEY_THEME, theme).apply()
    }
}