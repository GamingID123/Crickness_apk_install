package com.example.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "crickness_settings")

data class AppSettings(
    val darkTheme: Boolean = true,
    val dynamicColors: Boolean = false,
    val defaultOvers: Int = 10,
    val defaultWickets: Int = 10,
    val autoSave: Boolean = true,
    val soundHaptics: Boolean = true,
    val pitchLengthFeet: Int = 22,
    val unitMeters: Boolean = false,
    val showArTrajectories: Boolean = true
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val DEFAULT_OVERS = intPreferencesKey("default_overs")
        val DEFAULT_WICKETS = intPreferencesKey("default_wickets")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val SOUND_HAPTICS = booleanPreferencesKey("sound_haptics")
        val PITCH_LENGTH_FEET = intPreferencesKey("pitch_length_feet")
        val UNIT_METERS = booleanPreferencesKey("unit_meters")
        val SHOW_AR_TRAJECTORIES = booleanPreferencesKey("show_ar_trajectories")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            darkTheme = prefs[Keys.DARK_THEME] ?: true,
            dynamicColors = prefs[Keys.DYNAMIC_COLORS] ?: false,
            defaultOvers = prefs[Keys.DEFAULT_OVERS] ?: 10,
            defaultWickets = prefs[Keys.DEFAULT_WICKETS] ?: 10,
            autoSave = prefs[Keys.AUTO_SAVE] ?: true,
            soundHaptics = prefs[Keys.SOUND_HAPTICS] ?: true,
            pitchLengthFeet = prefs[Keys.PITCH_LENGTH_FEET] ?: 22,
            unitMeters = prefs[Keys.UNIT_METERS] ?: false,
            showArTrajectories = prefs[Keys.SHOW_AR_TRAJECTORIES] ?: true
        )
    }

    suspend fun updateDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun updateDynamicColors(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLORS] = enabled }
    }

    suspend fun updateDefaultOvers(overs: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_OVERS] = overs }
    }

    suspend fun updateDefaultWickets(wickets: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_WICKETS] = wickets }
    }

    suspend fun updateAutoSave(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_SAVE] = enabled }
    }

    suspend fun updateSoundHaptics(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_HAPTICS] = enabled }
    }

    suspend fun updatePitchLength(feet: Int) {
        context.dataStore.edit { it[Keys.PITCH_LENGTH_FEET] = feet }
    }
}
