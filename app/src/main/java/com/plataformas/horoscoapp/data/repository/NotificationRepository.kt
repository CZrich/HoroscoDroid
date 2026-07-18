package com.plataformas.horoscoapp.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.plataformas.horoscoapp.core.model.ZodiacSign
import com.plataformas.horoscoapp.core.model.normalizeIdentifier
import com.plataformas.horoscoapp.data.preferences.NotificationPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class NotificationRepository(
    private val preferences: NotificationPreferencesDataSource,
    private val firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
) {
    val selectedSign: Flow<String?> = preferences.selectedSign
    val favoriteSign: Flow<String?> = preferences.favoriteSign

    suspend fun saveFavoriteSign(sign: String) {
        preferences.saveFavoriteSign(sign)
    }

    suspend fun selectSign(sign: String): Result<Unit> {
        val previousSign = selectedSign.first()
        if (previousSign.equals(sign, ignoreCase = true)) return Result.success(Unit)

        return runCatching {
            previousSign?.takeIf { it.isNotBlank() }?.let { previous ->
                firebaseMessaging.unsubscribeFromTopic(topicForSign(previous)).await()
            }

            firebaseMessaging.subscribeToTopic(topicForSign(sign)).await()
            preferences.saveSelectedSign(sign)
        }
    }

    companion object {
        fun topicForSign(sign: String): String {
            return ZodiacSign.from(sign)?.topicName ?: "horoscope_${sign.normalizeIdentifier()}"
        }
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { error -> continuation.resumeWithException(error) }
}
