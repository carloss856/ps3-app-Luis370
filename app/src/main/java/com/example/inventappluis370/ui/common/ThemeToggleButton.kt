package com.example.inventappluis370.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.inventappluis370.ui.theme.LocalThemeController
import com.example.inventappluis370.ui.theme.ThemeMode

@Composable
fun ThemeToggleButton(modifier: Modifier = Modifier) {
    val controller = LocalThemeController.current
    val isLight = controller.mode == ThemeMode.LIGHT
    val icon = if (isLight) Icons.Default.DarkMode else Icons.Default.WbSunny
    val tint = if (isLight) Color(0xFFC0C0C0) else Color(0xFFFFD54F)
    val contentDescription = if (isLight) "Cambiar a oscuro" else "Cambiar a claro"

    IconButton(onClick = controller.toggle, modifier = modifier) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint)
    }
}

