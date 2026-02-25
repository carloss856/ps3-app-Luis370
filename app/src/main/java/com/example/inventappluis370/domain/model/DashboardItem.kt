package com.example.inventappluis370.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Representa un item en el menu del dashboard.
 *
 * @param title El texto que se mostrara para este item.
 * @param icon El icono que representara visualmente a este item.
 * @param route La ruta de navegacion a la que se dirigira el usuario al pulsar el item.
 */
data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

