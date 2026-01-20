package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.common.ApiErrorResponse
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response

object ApiErrorParser {
    /**
     * Extrae el mensaje de error del cuerpo de la respuesta de Retrofit.
     * Maneja el formato { "message": "...", "error": "...", "errors": { ... } }
     */
    fun parseError(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody == null) return "Error desconocido (Código: ${response.code()})"

            val jsonObject = JSONObject(errorBody)

            // Caso 1: Existe un mensaje de validación detallado
            if (jsonObject.has("errors")) {
                val errors = jsonObject.getJSONObject("errors")
                val firstKey = errors.keys().next()
                val firstErrorArray = errors.getJSONArray(firstKey)
                // Retornamos el primer error de validación encontrado
                return firstErrorArray.getString(0)
            }

            // Caso 2: Existe un campo "message" o "error"
            jsonObject.optString("message", jsonObject.optString("error", "Error del servidor"))
        } catch (_: Exception) {
            "Error al procesar la respuesta del servidor (${response.code()})"
        }
    }

    /**
     * Devuelve estructura completa de error del backend (message/errors) para 422 UI-friendly.
     */
    fun parseErrorResponse(response: Response<*>): ApiErrorResponse {
        return try {
            val errorBody = response.errorBody()?.string() ?: return ApiErrorResponse(
                message = "Error desconocido (Código: ${response.code()})"
            )

            val jsonObject = JSONObject(errorBody)
            val message = jsonObject.optString(
                "message",
                jsonObject.optString("error", "Error del servidor")
            )

            val errorsMap = if (jsonObject.has("errors")) {
                val errorsObj = jsonObject.getJSONObject("errors")
                val keys = errorsObj.keys()
                val out = linkedMapOf<String, List<String>>()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val arr = errorsObj.optJSONArray(key) ?: JSONArray()
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.optString(i))
                    }
                    out[key] = list
                }
                out
            } else {
                null
            }

            ApiErrorResponse(message = message, errors = errorsMap)
        } catch (_: Exception) {
            ApiErrorResponse(message = "Error al procesar la respuesta del servidor (${response.code()})")
        }
    }

    /**
     * Para 403 (RolePermission) el backend puede incluir: {"message":"Forbidden","detail":{...}}.
     * Retornamos el JSON crudo de `detail` si existe; si no, null.
     */
    fun parseDetailRaw(response: Response<*>): String? {
        return try {
            val errorBody = response.errorBody()?.string() ?: return null
            val jsonObject = JSONObject(errorBody)
            if (!jsonObject.has("detail")) return null
            jsonObject.get("detail").toString()
        } catch (_: Exception) {
            null
        }
    }
}
