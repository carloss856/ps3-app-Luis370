package com.example.inventappluis370.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Helpers pequeños para UI de Paging.
 */
object PagingUi {

    /**
     * Convierte el error en un mensaje presentable y estable (no-null).
     */
    fun messageOf(t: Throwable): String = t.message ?: t.toString()

    @Composable
    fun ErrorText(text: String, modifier: Modifier = Modifier) {
        Text(text = text, color = MaterialTheme.colorScheme.error, modifier = modifier)
    }
}

