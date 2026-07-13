package com.plataformas.horoscoapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore by preferencesDataStore(name = "notification_preferences")

class NotificationPreferencesDataSource(
    private val context: Context,
) {
    val selectedSign: Flow<String?> = context.notificationDataStore.data.map { preferences ->
        preferences[SELECTED_SIGN_KEY]
    }

    suspend fun saveSelectedSign(sign: String) {
        context.notificationDataStore.edit { preferences ->
            preferences[SELECTED_SIGN_KEY] = sign
        }
    }

    private companion object {
        val SELECTED_SIGN_KEY = stringPreferencesKey("selected_notification_sign")
    }
}
