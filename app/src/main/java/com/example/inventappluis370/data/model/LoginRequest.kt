package com.example.inventappluis370.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    @Json(name = "contrasena") val contrasena: String
)