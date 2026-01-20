package com.example.inventappluis370.data.model

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test

class LoginResponseMoshiTest {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `login response parsea aunque usuario no tenga id_persona`() {
        val json = """
            {
              "usuario": {
                "_id": "65a000000000000000000001",
                "nombre": "Test",
                "email": "test@test.com",
                "tipo": "Administrador"
              },
              "token": "abc",
              "tipo": "Administrador",
              "expires_at": "2026-01-09T00:00:00Z"
            }
        """.trimIndent()

        val adapter = moshi.adapter(LoginResponse::class.java)
        val parsed = adapter.fromJson(json)

        assertThat(parsed).isNotNull()
        val usuario = parsed!!.usuario
        assertThat(usuario).isNotNull()
        assertThat(usuario!!.mongoId).isEqualTo("65a000000000000000000001")
        assertThat(usuario.idPersona).isNull()
        assertThat(parsed.token).isEqualTo("abc")
    }
}
