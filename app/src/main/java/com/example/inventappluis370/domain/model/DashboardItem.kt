package com.example.inventappluis370.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Representa un item en el menú del dashboard.
 *
 * @param title El texto que se mostrará para este item.
 * @param icon El icono que representará visualmente a este item.
 * @param route La ruta de navegación a la que se dirigirá el usuario al pulsar el item.
 */
data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)
