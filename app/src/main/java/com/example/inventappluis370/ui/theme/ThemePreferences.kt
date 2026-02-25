package com.example.inventappluis370.ui.theme

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "ui_theme")

enum class ThemeMode {
    LIGHT,
    DARK,
}

data class ThemeController(
    val mode: ThemeMode,
    val toggle: () -> Unit,
)

val LocalThemeController = staticCompositionLocalOf {
    ThemeController(ThemeMode.LIGHT) {}
}

object ThemePreferences {
    private val KEY_THEME_MODE = stringPreferencesKey("ui_theme_mode")

    fun themeModeFlow(context: Context): Flow<ThemeMode> {
        return context.themeDataStore.data.map { prefs ->
            when (prefs[KEY_THEME_MODE]) {
                "dark" -> ThemeMode.DARK
                "light" -> ThemeMode.LIGHT
                else -> ThemeMode.LIGHT
            }
        }
    }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = if (mode == ThemeMode.DARK) "dark" else "light"
        }
    }
}

