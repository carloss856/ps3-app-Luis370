package com.example.inventappluis370.data.remote

import com.example.inventappluis370.data.model.PagedResponseDto
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.ResponseBody
import okio.Buffer
import java.io.IOException

/**
 * Helper para endpoints index con paginación dual-mode:
 * - Si el body es un array -> se interpreta como List<T> (legacy)
 * - Si el body es un objeto -> se interpreta como PagedResponseDto<T>
 *
 * Si el backend devuelve un objeto con un formato inesperado (por ejemplo {repuestos:[...]})
 * NO debe tumbar la app: hacemos fallback y/o devolvemos un error legible.
 */
object DualModePageParser {

    sealed class ParsedPage<T> {
        data class LegacyList<T>(val items: List<T>) : ParsedPage<T>()
        data class Paged<T>(val page: PagedResponseDto<T>) : ParsedPage<T>()
    }

    fun <T> parse(body: ResponseBody, moshi: Moshi, itemClass: Class<T>): ParsedPage<T> {
        val raw = try {
            // OJO: ResponseBody.string() solo se puede leer una vez.
            body.string().trim()
        } catch (e: Throwable) {
            // En vez de tumbar la app, devolvemos vacío con un mensaje en exception si el caller lo quiere.
            throw IOException("No se pudo leer el body de la respuesta", e)
        }

        if (raw.isEmpty()) return ParsedPage.LegacyList(emptyList())

        if (raw.startsWith("[")) {
            val listType = Types.newParameterizedType(List::class.java, itemClass)
            val adapter: JsonAdapter<List<T>> = moshi.adapter(listType)
            val items = try {
                adapter.fromJson(raw).orEmpty()
            } catch (_: Throwable) {
                emptyList()
            }
            return ParsedPage.LegacyList(items)
        }

        if (!raw.startsWith("{")) {
            // Formato rarísimo; no rompemos la app.
            return ParsedPage.LegacyList(emptyList())
        }

        // 1) Intento principal: wrapper estándar { data: [...], meta: {...} }
        runCatching {
            val pageType = Types.newParameterizedType(PagedResponseDto::class.java, itemClass)
            val adapter: JsonAdapter<PagedResponseDto<T>> = moshi.adapter(pageType)
            val page = adapter.fromJson(raw) ?: PagedResponseDto(emptyList(), null)

            if (page.data.isNotEmpty() || page.meta != null) {
                return ParsedPage.Paged(page)
            }
        }

        // 2) Fallback seguro: buscar un array en el objeto, incluso si está anidado.
        val listType = Types.newParameterizedType(List::class.java, itemClass)
        val listAdapter: JsonAdapter<List<T>> = moshi.adapter(listType)

        val preferredKeys = setOf("data", "repuestos", "items", "results")

        val reader = JsonReader.of(Buffer().writeUtf8(raw))
        reader.isLenient = true

        fun readFirstArrayInObject(): List<T>? {
            reader.beginObject()
            while (reader.hasNext()) {
                val name = reader.nextName()
                when (reader.peek()) {
                    JsonReader.Token.BEGIN_ARRAY -> {
                        return runCatching { listAdapter.fromJson(reader).orEmpty() }.getOrNull()
                    }
                    JsonReader.Token.BEGIN_OBJECT -> {
                        // Si la key es una de las preferidas, buscamos dentro del objeto.
                        if (name in preferredKeys) {
                            val nested = runCatching {
                                reader.beginObject()
                                @Suppress("UNUSED_VARIABLE")
                                while (reader.hasNext()) {
                                    @Suppress("UNUSED_VARIABLE")
                                    run { reader.nextName() }
                                    if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
                                        return@runCatching listAdapter.fromJson(reader).orEmpty()
                                    } else {
                                        reader.skipValue()
                                    }
                                }
                                reader.endObject()
                                null
                            }.getOrNull()
                            if (nested != null) return nested
                        } else {
                            reader.skipValue()
                        }
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            return null
        }

        val items = runCatching { readFirstArrayInObject() }.getOrNull()
        if (items != null) return ParsedPage.LegacyList(items)

        // Último fallback: no rompemos la app. Queda visible como lista vacía.
        return ParsedPage.LegacyList(emptyList())
    }
}
