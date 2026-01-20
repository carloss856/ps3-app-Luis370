package com.example.inventappluis370.data.mapper

import com.example.inventappluis370.data.model.Usuario
import com.example.inventappluis370.domain.model.User

fun Usuario.toDomain(rolFallback: String? = null): User = User(
    idPersona = idPersona,
    mongoId = mongoIdResolved(),
    nombre = nombre,
    email = email,
    telefono = telefono,
    tipo = tipo,
    rol = rolFallback, // en LoginResponse no viene 'rol' según contrato (viene 'tipo'); usamos fallback.
    idEmpresa = idEmpresa,
    validadoPorGerente = validadoPorGerente,
    recibirNotificaciones = recibirNotificaciones,
    tiposNotificacion = tiposNotificacion,
)
