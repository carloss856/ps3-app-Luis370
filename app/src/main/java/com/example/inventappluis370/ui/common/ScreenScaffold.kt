package com.example.inventappluis370.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Scaffold estandar para TODAS las pantallas:
 * - TopBar consistente (back, refresh, icono modulo)
 * - Evita duplicar top bars en cada modulo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    endIcon: ImageVector? = null,
    endIconContentDescription: String? = null,
    onRefresh: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            ModuleTopBar(
                title = title,
                onBack = onBack,
                endIcon = endIcon,
                endIconContentDescription = endIconContentDescription,
                onRefresh = onRefresh,
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        content(padding)
    }
}

