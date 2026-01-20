package com.example.inventappluis370.core.debug

import com.example.inventappluis370.data.model.LoginRequest
import com.example.inventappluis370.data.remote.AuthApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Utilidad interna para probar endpoints desde la app (sin consola) y capturar:
 * - status code
 * - headers (incluyendo X-Token-Expires-At)
 * - body (si aplica)
 * - errorBody cuando el backend responde error
 * - excepción (incluyendo parse errors de Moshi)
 *
 * NOTA: Esto NO debe ir a producción. Úsalo solo en debug.
 */
object EndpointProbe {

    data class ProbeResult(
        val name: String,
        val ok: Boolean,
        val status: Int?,
        val message: String,
        val tokenExpiresAtHeader: String? = null,
        val rawBody: String? = null,
        val rawErrorBody: String? = null,
    )

    private fun safeReadErrorBody(resp: retrofit2.Response<*>): String? =
        runCatching { resp.errorBody()?.string() }.getOrNull()

    suspend fun probeAuth(
        authApi: AuthApiService,
        email: String,
        password: String
    ): List<ProbeResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ProbeResult>()

        // 1) Login
        try {
            val loginResp = authApi.login(LoginRequest(email = email, contrasena = password))
            val header = loginResp.headers()["X-Token-Expires-At"]

            val ok = loginResp.isSuccessful && !loginResp.body()?.token.isNullOrBlank()
            results += ProbeResult(
                name = "POST /login",
                ok = ok,
                status = loginResp.code(),
                message = if (loginResp.isSuccessful) "OK" else "HTTP ${loginResp.code()} ${loginResp.message()}",
                tokenExpiresAtHeader = header,
                rawBody = runCatching {
                    val body = loginResp.body()
                    "token=${body?.token?.take(8)}..., tipo=${body?.tipo}, usuario.idPersona=${body?.usuario?.idPersona}, usuario._id=${body?.usuario?.mongoIdResolved()}"
                }.getOrNull(),
                rawErrorBody = if (loginResp.isSuccessful) null else safeReadErrorBody(loginResp)
            )
        } catch (t: Throwable) {
            results += ProbeResult(
                name = "POST /login",
                ok = false,
                status = (t as? HttpException)?.code(),
                message = t.message ?: t.toString(),
            )
        }

        results
    }
}
