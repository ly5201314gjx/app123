package com.example.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "global_settings")

object GlobalSettings {
    val BG_IMAGE_URI = stringPreferencesKey("bg_image_uri")
    val BG_ALPHA = floatPreferencesKey("bg_alpha")
    val BG_BLUR = floatPreferencesKey("bg_blur")
    
    val BUBBLE_STYLE = stringPreferencesKey("bubble_style") // "glass", "plain", "custom_image"
    val BUBBLE_IMAGE_URI = stringPreferencesKey("bubble_image_uri")
    val BUBBLE_ALPHA = floatPreferencesKey("bubble_alpha")
    val BUBBLE_TEXT_COLOR = longPreferencesKey("bubble_text_color") // Storing as Long for Color value

    fun getSettings(context: Context): Flow<AppThemeSettings> {
        return context.dataStore.data.map { prefs ->
            AppThemeSettings(
                bgImageUri = prefs[BG_IMAGE_URI],
                bgAlpha = prefs[BG_ALPHA] ?: 0.5f,
                bgBlur = prefs[BG_BLUR] ?: 0f,
                bubbleStyle = prefs[BUBBLE_STYLE] ?: "glass",
                bubbleImageUri = prefs[BUBBLE_IMAGE_URI],
                bubbleAlpha = prefs[BUBBLE_ALPHA] ?: 1.0f,
                bubbleTextColor = prefs[BUBBLE_TEXT_COLOR] ?: 0xFFFFFFFFL // Default white
            )
        }
    }

    suspend fun updateSettings(context: Context, update: MutablePreferences.() -> Unit) {
        context.dataStore.edit(update)
    }
}

data class AppThemeSettings(
    val bgImageUri: String? = null,
    val bgAlpha: Float = 0.5f,
    val bgBlur: Float = 0f,
    val bubbleStyle: String = "glass", // "glass", "plain", "custom_image"
    val bubbleImageUri: String? = null,
    val bubbleAlpha: Float = 1.0f,
    val bubbleTextColor: Long = 0xFFFFFFFFL
)
