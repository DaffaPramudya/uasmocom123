package com.example.kiblatku.helper

import android.content.SharedPreferences
import android.content.Context
import androidx.core.content.edit

class DataManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("compass_settings", Context.MODE_PRIVATE)

    var isNoGPSMode: Boolean
        get() = prefs.getBoolean("no_gps_mode", false)
        set(value) = prefs.edit { putBoolean("no_gps_mode", value) }

    var darkModeSetting: String
        get() = prefs.getString("dark_mode", "Auto") ?: "Auto"
        set(value) = prefs.edit { putString("dark_mode", value) }

    var lat: Float
        get() = prefs.getFloat("lat", 0f)
        set(value) = prefs.edit { putFloat("lat", value) }

    var lon: Float
        get() = prefs.getFloat("lon", 0f)
        set(value) = prefs.edit { putFloat("lon", value) }

    var locationName: String
        get() = prefs.getString("location_name", "") ?: ""
        set(value) = prefs.edit().putString("location_name", value).apply()

    var compassType: String
        get() = prefs.getString("compass_type", "Analog") ?: "Analog"
        set(value) = prefs.edit { putString("compass_type", value) }

    var textSize: String
        get() = prefs.getString("text_size", "Normal") ?: "Normal"
        set(value) = prefs.edit { putString("text_size", value) }

    var isSmoothAnimation: Boolean
        get() = prefs.getBoolean("is_smooth", true)
        set(value) = prefs.edit { putBoolean("is_smooth", value) }

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit { putBoolean("sound_enabled", value) }

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration_enabled", true)
        set(value) = prefs.edit { putBoolean("vibration_enabled", value) }

    var AccuracyStyle: String
        get() = prefs.getString("Numbers", "Color Code") ?: "Numbers"
        set(value) = prefs.edit { putString("is_smooth", value) }

}