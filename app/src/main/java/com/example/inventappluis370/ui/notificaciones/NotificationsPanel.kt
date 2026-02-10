package com.example.inventappluis370.ui.notificaciones

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inventappluis370.ui.navigation.Routes

/**
 * Contenido compacto para popup de notificaciones.
 * Se usa dentro de un DropdownMenu anclado a la campana.
 *
 * Importante: este composable NO debe usar DropdownMenuItem porque ese API requiere
 * estar dentro del scope de DropdownMenu { ... }.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsPanel(
    navController: NavController,
    onDismiss: () -> Unit,
    viewModel: NotificacionesViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    fun safeNavigate(route: String) {
        try {
            navController.navigate(route)
        } catch (t: Throwable) {
            // No dejamos que una ruta mal registrada o un estado inválido tumbe la app.
            Log.e("NotificationsPanel", "Navigation failed to $route", t)
        }
    }

    // Al abrir el popup, refrescamos para que siempre muestre lo último.
    // Importante: NO dispararlo en cada recomposición.
    val loadedOnce = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!loadedOnce.value) {
            loadedOnce.value = true
            try {
                viewModel.getNotificaciones()
            } catch (t: Throwable) {
                Log.e("NotificationsPanel", "getNotificaciones() crashed", t)
            }
        }
    }

    Column(
        modifier = Modifier
            .widthIn(min = 280.dp, max = 360.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "Notificaciones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        onDismiss()
                        safeNavigate(Routes.CONFIG_NOTIFICACIONES)
                    }
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Configurar notificaciones")
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        }

        Divider()

        when (val state = uiState) {
            is NotificacionesUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            is NotificacionesUiState.Error -> {
                Text(
                    "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )

                Text(
                    text = "Reintentar",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.getNotificaciones() }
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            is NotificacionesUiState.Success -> {
                val notificaciones = state.notificaciones
                if (notificaciones.isEmpty()) {
                    Text("No hay notificaciones.", modifier = Modifier.padding(12.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notificaciones.take(7)) { notificacion ->
                            val id = notificacion.idNotificacion
                            NotificacionItem(
                                notificacion = notificacion,
                                onMarkRead = {
                                    if (!id.isNullOrBlank()) viewModel.setLeida(id)
                                },
                                onDelete = {
                                    if (!id.isNullOrBlank()) viewModel.deleteNotificacion(id)
                                },
                                canDelete = viewModel.canDelete()
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(2.dp))
                            Divider()

                            Text(
                                text = "Ver todas",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDismiss()
                                        safeNavigate(Routes.NOTIFICACIONES)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
