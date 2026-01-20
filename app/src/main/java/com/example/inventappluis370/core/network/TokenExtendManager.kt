package com.example.inventappluis370.core.network

import android.util.Log
import com.example.inventappluis370.data.remote.AuthApiService
import com.example.inventappluis370.domain.repository.TokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementa la estrategia del front React:
 * - threshold: si faltan < 5 min para expirar, intentar extender.
 * - throttle: no intentar más de 1 vez cada 10 min.
 * - evitar bucles: el request que ejecuta extend debe setear una marca "skip".
 *
 * Nota: el contrato indica que el server expone header X-Token-Expires-At y existe POST /token/extend.
 *
 * Nota de anti-bucle:
 * El request de /token/extend se ejecuta usando el AuthApiService (cliente "auth"), y ese cliente
 * no dispara el auto-extend. Si además quieres blindaje extra, puedes enviar un header interno
 * (p.ej. X-Skip-Token-Extend) desde un interceptor del cliente auth.
 */
@Singleton
class TokenExtendManager @Inject constructor(
    private val authApiService: AuthApiService,
    private val tokenRepository: TokenRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val inFlight = AtomicBoolean(false)

    @Volatile
    private var lastAttemptAtMs: Long = 0L

    /**
     * Se llama luego de recibir una respuesta (cuando tenemos el header X-Token-Expires-At actualizado).
     *
     * Nota: es async. Para tests unitarios usa [extendIfNeeded].
     */
    fun maybeExtendToken() {
        scope.launch {
            extendIfNeeded()
        }
    }

    /**
     * Versión determinística (suspend) para poder testear sin sleeps.
     */
    suspend fun extendIfNeeded() {
        val token = tokenRepository.getToken() ?: return
        if (token.isBlank()) return

        if (!tokenRepository.needsExtension()) return

        val now = System.currentTimeMillis()
        if (now - lastAttemptAtMs < THROTTLE_MS) return

        if (!inFlight.compareAndSet(false, true)) return
        lastAttemptAtMs = now

        try {
            val response = authApiService.extendToken()

            if (response.isSuccessful) {
                val body = response.body()

                body?.expiresAt?.let { tokenRepository.updateExpiration(it) }
                body?.token?.let { newToken -> tokenRepository.updateTokenOnly(newToken) }
            } else {
                if (response.code() == 401) {
                    tokenRepository.clearSession()
                }
            }
        } catch (t: Throwable) {
            Log.w("TokenExtendManager", "No se pudo extender el token", t)
        } finally {
            inFlight.set(false)
        }
    }

    companion object {
        private const val THROTTLE_MS = 10 * 60 * 1000L
    }
}
