package com.example.inventappluis370.core.network

import android.content.SharedPreferences
import com.example.inventappluis370.data.model.TokenExtendResponse
import com.example.inventappluis370.data.remote.AuthApiService
import com.example.inventappluis370.domain.repository.TokenRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import retrofit2.Response
import java.time.OffsetDateTime
import java.time.ZoneOffset

class TokenExtendManagerTest {

    private class FakeAuthApiService(
        private val response: Response<TokenExtendResponse>
    ) : AuthApiService {
        var extendCalls: Int = 0

        override suspend fun extendToken(): Response<TokenExtendResponse> {
            extendCalls++
            return response
        }

        // --- no usados en este test ---
        override suspend fun login(request: com.example.inventappluis370.data.model.LoginRequest) =
            throw UnsupportedOperationException("Not used")

        override suspend fun logout() = throw UnsupportedOperationException("Not used")

        override suspend fun forgotPassword(request: com.example.inventappluis370.data.model.ForgotPasswordRequest) =
            throw UnsupportedOperationException("Not used")

        override suspend fun verifyToken(request: com.example.inventappluis370.data.model.VerifyTokenRequest) =
            throw UnsupportedOperationException("Not used")

        override suspend fun resetPassword(request: com.example.inventappluis370.data.model.ResetPasswordRequest) =
            throw UnsupportedOperationException("Not used")
    }

    private class MemoryPrefs : SharedPreferences {
        private val data = linkedMapOf<String, Any?>()

        override fun getString(key: String?, defValue: String?): String? =
            (data[key] as? String) ?: defValue

        override fun edit(): SharedPreferences.Editor = EditorImpl()

        inner class EditorImpl : SharedPreferences.Editor {
            private val pending = linkedMapOf<String, Any?>()
            private var clear = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) pending[key] = value
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                clear = true
                return this
            }

            override fun apply() {
                if (clear) data.clear()
                data.putAll(pending)
            }

            // --- no usados ---
            override fun commit(): Boolean { apply(); return true }
            override fun putStringSet(key: String?, values: MutableSet<String>?) = this
            override fun putInt(key: String?, value: Int) = this
            override fun putLong(key: String?, value: Long) = this
            override fun putFloat(key: String?, value: Float) = this
            override fun putBoolean(key: String?, value: Boolean) = this
            override fun remove(key: String?) = this
        }

        // --- no usados ---
        override fun getAll(): MutableMap<String, *> = data
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
        override fun getInt(key: String?, defValue: Int): Int = defValue
        override fun getLong(key: String?, defValue: Long): Long = defValue
        override fun getFloat(key: String?, defValue: Float): Float = defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
        override fun contains(key: String?): Boolean = key != null && data.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    }

    @Test
    fun `extendToken actualiza expiresAt cuando el server lo retorna`() = runTest {
        // Usamos formato estable con 'Z' porque TokenRepository parsea varios formatos, pero este evita flakiness.
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val expiresSoon = now.plusMinutes(1).withNano(0).toString().replace("+00:00", "Z")
        val newExpires = now.plusHours(1).withNano(0).toString().replace("+00:00", "Z")

        val prefs = MemoryPrefs()
        val tokenRepository = TokenRepository(prefs)
        tokenRepository.saveSession(
            token = "t",
            role = "Administrador",
            userId = "USR-1",
            expiresAt = expiresSoon
        )

        val fakeApi = FakeAuthApiService(Response.success(TokenExtendResponse(expiresAt = newExpires)))
        val manager = TokenExtendManager(fakeApi, tokenRepository)

        manager.extendIfNeeded()

        // llamada disparada
        assertThat(fakeApi.extendCalls).isEqualTo(1)

        // con nueva expiración ya no debería necesitar extensión
        assertThat(tokenRepository.needsExtension()).isFalse()
        // sanity: se haya guardado la expiración nueva
        assertThat(tokenRepository.getSession()?.expiresAt).isEqualTo(newExpires)
    }

    @Test
    fun `no llama extendToken si no hay token o no necesita extensión`() = runTest {
        val prefs = MemoryPrefs()
        val tokenRepository = TokenRepository(prefs)

        // token nulo => no llama
        val fakeApi = FakeAuthApiService(Response.success(TokenExtendResponse(expiresAt = "x")))
        val manager = TokenExtendManager(fakeApi, tokenRepository)
        manager.extendIfNeeded()
        assertThat(fakeApi.extendCalls).isEqualTo(0)

        // token con expiración lejana => no llama
        val expiresLater = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toString()
        tokenRepository.saveSession(token = "t", role = "Administrador", userId = "USR-1", expiresAt = expiresLater)
        manager.extendIfNeeded()
        assertThat(fakeApi.extendCalls).isEqualTo(0)
    }
}
