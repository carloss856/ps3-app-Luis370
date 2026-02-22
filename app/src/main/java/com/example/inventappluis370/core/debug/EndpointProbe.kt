package com.example.inventappluis370.core.debug

import com.example.inventappluis370.BuildConfig
import com.example.inventappluis370.data.model.LoginRequest
import com.example.inventappluis370.data.remote.AuthApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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

    private fun classifyNetworkError(t: Throwable): String {
        val variant = "${BuildConfig.FLAVOR}${BuildConfig.BUILD_TYPE.replaceFirstChar { it.uppercase() }}".trim()
        val base = "baseUrl=${BuildConfig.BASE_URL}, variant=$variant"

        val hint = when {
            BuildConfig.BASE_URL.contains("127.0.0.1") ->
                "HINT: 127.0.0.1 dentro del EMULADOR apunta al propio emulador (NO a tu PC).\n" +
                    "- Si estás en EMULADOR: ejecuta el variant emulatorDebug (BASE_URL=10.0.2.2).\n" +
                    "- Si estás en TELÉFONO por USB: usa deviceDebug y ejecuta en tu PC: adb reverse tcp:8000 tcp:8000"

            BuildConfig.BASE_URL.contains("10.0.2.2") ->
                "HINT: 10.0.2.2 solo funciona en EMULADOR (redirige al localhost de tu PC).\n" +
                    "- Si estás en TELÉFONO: usa deviceDebug con adb reverse tcp:8000 tcp:8000 (o apunta a IP LAN del PC)."

            else -> null
        }

        val classified = when (t) {
            is SocketTimeoutException -> "TIMEOUT ($base): ${t.message ?: t}"
            is ConnectException -> "CONNECT ($base): ${t.message ?: t}"
            is UnknownHostException -> "DNS ($base): ${t.message ?: t}"
            is IOException -> "IO ($base): ${t.message ?: t}"
            else -> "ERROR ($base): ${t.message ?: t}"
        }

        return if (hint != null) "$classified\n$hint" else classified
    }

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
                message = if (loginResp.isSuccessful) {
                    "OK (baseUrl=${BuildConfig.BASE_URL})"
                } else {
                    "HTTP ${loginResp.code()} ${loginResp.message()} (baseUrl=${BuildConfig.BASE_URL})"
                },
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
                message = classifyNetworkError(t),
            )
        }

        results
    }
}
