package com.example.inventappluis370.core.network

import com.squareup.moshi.JsonQualifier

/**
 * Marca campos String que pueden venir como Number/Boolean en datos legacy.
 *
 * Importante: evita registrar un adapter global Any->String que pueda interferir
 * con serializacion/deserializacion (y potencialmente causar recursion).
 */
@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class LenientString


