package com.stencilla.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.stencilla.app.data.remote.ApiService
import com.stencilla.app.data.remote.dto.SettingsDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsStore by preferencesDataStore(name = "stencilla_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ApiService,
) {
    private val KEY_NOTIF_OUTFIT   = booleanPreferencesKey("notif_outfit")
    private val KEY_NOTIF_PLANNER  = booleanPreferencesKey("notif_planner")
    private val KEY_NOTIF_TIPS     = booleanPreferencesKey("notif_tips")
    private val KEY_PRIVACY_ANALYTICS = booleanPreferencesKey("privacy_analytics")

    val settingsFlow: Flow<SettingsDto> = context.settingsStore.data.map { prefs ->
        SettingsDto(
            notificationOutfit   = prefs[KEY_NOTIF_OUTFIT]       ?: true,
            notificationPlanner  = prefs[KEY_NOTIF_PLANNER]      ?: true,
            notificationTips     = prefs[KEY_NOTIF_TIPS]         ?: true,
            privacyAnalytics     = prefs[KEY_PRIVACY_ANALYTICS]  ?: true,
        )
    }

    suspend fun updateSettings(dto: SettingsDto) {
        context.settingsStore.edit { prefs ->
            prefs[KEY_NOTIF_OUTFIT]        = dto.notificationOutfit
            prefs[KEY_NOTIF_PLANNER]       = dto.notificationPlanner
            prefs[KEY_NOTIF_TIPS]          = dto.notificationTips
            prefs[KEY_PRIVACY_ANALYTICS]   = dto.privacyAnalytics
        }
        try { api.updateSettings(dto) } catch (_: Exception) { /* sync later */ }
    }

    suspend fun sync() {
        try {
            val remote = api.getSettings()
            context.settingsStore.edit { prefs ->
                prefs[KEY_NOTIF_OUTFIT]       = remote.notificationOutfit
                prefs[KEY_NOTIF_PLANNER]      = remote.notificationPlanner
                prefs[KEY_NOTIF_TIPS]         = remote.notificationTips
                prefs[KEY_PRIVACY_ANALYTICS]  = remote.privacyAnalytics
            }
        } catch (_: Exception) { /* offline */ }
    }
}
