package com.example.inventappluis370.domain.repository

import android.content.SharedPreferences
import com.example.inventappluis370.domain.model.AuthSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    private val _tokenFlow = MutableStateFlow(getToken())
    val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    private val _userRoleFlow = MutableStateFlow(getRole())
    val userRoleFlow: StateFlow<String?> = _userRoleFlow.asStateFlow()

    private val _userDisplayNameFlow = MutableStateFlow(getUserDisplayName())
    val userDisplayNameFlow: StateFlow<String?> = _userDisplayNameFlow.asStateFlow()

    fun getToken(): String? = sharedPreferences.getString(TOKEN_KEY, null)
    fun getRole(): String? = sharedPreferences.getString(USER_ROLE_KEY, null)
    fun getUserId(): String? = sharedPreferences.getString(USER_ID_KEY, null)
    fun getMongoUserId(): String? = sharedPreferences.getString(MONGO_USER_ID_KEY, null)
    fun getUserDisplayName(): String? = sharedPreferences.getString(USER_DISPLAY_NAME_KEY, null)

    /**
     * expiración ISO-8601 que viene del contrato (header X-Token-Expires-At o body expires_at).
     *
     * Compatibilidad:
     * - versiones anteriores guardaban en key `expires_at`.
     * - migramos en caliente a `token_expires_at`.
     */
    private fun getTokenExpiresAt(): String? {
        val current = sharedPreferences.getString(TOKEN_EXPIRES_AT_KEY, null)
        if (!current.isNullOrBlank()) return current

        // compatibilidad hacia atrás
        val legacy = sharedPreferences.getString(LEGACY_EXPIRES_AT_KEY, null)
        if (!legacy.isNullOrBlank()) {
            sharedPreferences.edit().putString(TOKEN_EXPIRES_AT_KEY, legacy).remove(LEGACY_EXPIRES_AT_KEY).apply()
            return legacy
        }
        return null
    }

    /**
     * Expuesto solo para flujos internos (ej: TokenExtendManager) cuando necesita el valor crudo.
     * Evitar usarlo en UI.
     */
    internal fun getExpiresAtUnsafe(): String? = getTokenExpiresAt()

    fun getSession(): AuthSession? {
        val token = getToken() ?: return null
        return AuthSession(
            token = token,
            expiresAt = getTokenExpiresAt(),
            role = getRole(),
            userId = getUserId(),
            mongoUserId = getMongoUserId(),
        )
    }

    fun saveSession(token: String, role: String, userId: String, expiresAt: String) {
        sharedPreferences.edit()
            .putString(TOKEN_KEY, token)
            .putString(USER_ROLE_KEY, role)
            .putString(USER_ID_KEY, userId)
            .putString(TOKEN_EXPIRES_AT_KEY, expiresAt)
            .apply()
        _tokenFlow.value = token
        _userRoleFlow.value = role
        // display name no se toca aquí para permitir mantenerlo si el backend no lo reenvía.
    }

    fun saveUserDisplayName(displayName: String?) {
        val trimmed = displayName?.trim().orEmpty()
        sharedPreferences.edit().putString(USER_DISPLAY_NAME_KEY, trimmed.ifBlank { null }).apply()
        _userDisplayNameFlow.value = trimmed.ifBlank { null }
    }

    /** Guarda el _id (Mongo) del usuario autenticado para endpoints que lo requieren. */
    fun saveMongoUserId(mongoUserId: String) {
        sharedPreferences.edit().putString(MONGO_USER_ID_KEY, mongoUserId).apply()
    }

    /**
     * En algunos flujos (p.ej. /token/extend) el backend podría devolver un token nuevo sin reenviar
     * identidad/rol. Este helper actualiza solo el token manteniendo el resto.
     */
    fun updateTokenOnly(token: String) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply()
        _tokenFlow.value = token
    }

    fun updateExpiration(newExpiresAt: String) {
        sharedPreferences.edit().putString(TOKEN_EXPIRES_AT_KEY, newExpiresAt).apply()
    }

    fun clearSession() {
        sharedPreferences.edit().clear().apply()
        _tokenFlow.value = null
        _userRoleFlow.value = null
        _userDisplayNameFlow.value = null
    }

    /**
     * Verifica si el token expirará en los próximos 5 minutos.
     */
    fun needsExtension(): Boolean {
        val expiresAt = getTokenExpiresAt() ?: return false
        return try {
            val expirationTimeMs = parseIso8601ToMillis(expiresAt) ?: return false
            val currentTimeMs = System.currentTimeMillis()
            val fiveMinutesInMillis = 5 * 60 * 1000L
            (expirationTimeMs - currentTimeMs) < fiveMinutesInMillis
        } catch (_: Exception) {
            false
        }
    }

    private fun parseIso8601ToMillis(value: String): Long? {
        val trimmed = value.trim()

        // 1) Intento con formato con 'Z' literal (ej: 2026-01-09T00:00:00Z)
        run {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            try {
                val parsed = sdf.parse(trimmed)
                if (parsed != null) return parsed.time
            } catch (_: ParseException) {
            }
        }

        // 2) Intento con offset RFC3339 (ej: 2026-01-09T00:00:00+00:00)
        run {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            try {
                val parsed = sdf.parse(trimmed)
                if (parsed != null) return parsed.time
            } catch (_: ParseException) {
            }
        }

        // 3) Intento con fracciones + offset (ej: 2026-01-09T00:00:00.123+00:00)
        run {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            try {
                val parsed = sdf.parse(trimmed)
                if (parsed != null) return parsed.time
            } catch (_: ParseException) {
            }
        }

        // 4) Intento con fracciones + Z (ej: 2026-01-09T00:00:00.123Z)
        run {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            try {
                val parsed = sdf.parse(trimmed)
                if (parsed != null) return parsed.time
            } catch (_: ParseException) {
            }
        }

        return null
    }

    companion object {
        private const val TOKEN_KEY = "auth_token"
        private const val USER_ROLE_KEY = "user_role"
        private const val USER_ID_KEY = "user_id"
        // contrato: token_expires_at (internamente lo guardamos como string ISO)
        private const val TOKEN_EXPIRES_AT_KEY = "token_expires_at"
        // compatibilidad con builds anteriores
        private const val LEGACY_EXPIRES_AT_KEY = "expires_at"
        private const val MONGO_USER_ID_KEY = "mongo_user_id"
        private const val USER_DISPLAY_NAME_KEY = "user_display_name"
    }
}
