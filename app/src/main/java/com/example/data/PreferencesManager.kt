package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.DetailLevel

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("line_sketch_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_API_KEY = "gemini_api_key"
        private const val KEY_GUIDE_COLOR = "guide_color_argb"
        private const val KEY_GUIDE_OPACITY = "guide_opacity"
        private const val KEY_REF_OPACITY = "ref_opacity"
        private const val KEY_BRUSH_SIZE = "brush_size"
        private const val KEY_DETAIL_LEVEL = "detail_level"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value.trim()).apply()

    var guideColorArgb: Long
        get() = prefs.getLong(KEY_GUIDE_COLOR, 0xFFFF7A18)
        set(value) = prefs.edit().putLong(KEY_GUIDE_COLOR, value).apply()

    var guideOpacity: Float
        get() = prefs.getFloat(KEY_GUIDE_OPACITY, 0.95f)
        set(value) = prefs.edit().putFloat(KEY_GUIDE_OPACITY, value).apply()

    var referenceOpacity: Float
        get() = prefs.getFloat(KEY_REF_OPACITY, 0.40f)
        set(value) = prefs.edit().putFloat(KEY_REF_OPACITY, value).apply()

    var brushSize: Float
        get() = prefs.getFloat(KEY_BRUSH_SIZE, 6f)
        set(value) = prefs.edit().putFloat(KEY_BRUSH_SIZE, value).apply()

    var detailLevel: DetailLevel
        get() {
            val raw = prefs.getString(KEY_DETAIL_LEVEL, DetailLevel.NORMAL.name)
            return try {
                DetailLevel.valueOf(raw ?: DetailLevel.NORMAL.name)
            } catch (e: Exception) {
                DetailLevel.NORMAL
            }
        }
        set(value) = prefs.edit().putString(KEY_DETAIL_LEVEL, value.name).apply()

    var onboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
}
