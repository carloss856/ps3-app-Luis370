package com.example.inventappluis370.core.network

import com.squareup.moshi.JsonQualifier

/**
 * Marca campos que representan un `_id` de MongoDB (ObjectId).
 *
 * En algunos backends el _id puede venir como string ("507f1f...") o como Mongo Extended JSON
 * (por ejemplo: {"$oid":"507f1f..."}).
 */
@JsonQualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MongoId

