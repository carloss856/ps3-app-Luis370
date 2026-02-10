package com.example.inventappluis370.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.inventappluis370.ui.dashboard.DashboardViewModel
import com.example.inventappluis370.ui.notificaciones.NotificacionesViewModel
import com.example.inventappluis370.ui.notificaciones.NotificationsPanel

@Composable
fun DashboardScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardItems by viewModel.dashboardItems.collectAsState()
    val userDisplayName by viewModel.userDisplayName.collectAsState()

    val notificacionesViewModel: NotificacionesViewModel = hiltViewModel()

    var showNotifications by rememberSaveable { mutableStateOf(false) }

    // Coordenadas del ancla (campana) para mostrar un popup "hacia abajo".
    var bellOffset by remember { mutableStateOf(IntOffset.Zero) }
    var bellHeightPx by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        Text(text = "Inventario Luis370", style = MaterialTheme.typography.titleLarge)
                    }

                    Box {
                        IconButton(
                            onClick = { showNotifications = !showNotifications },
                            modifier = Modifier.onGloballyPositioned { coords ->
                                val b = coords.boundsInWindow()
                                bellOffset = IntOffset(b.left.toInt(), b.top.toInt())
                                bellHeightPx = b.height.toInt()
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }

                        if (showNotifications) {
                            Popup(
                                alignment = Alignment.TopStart,
                                offset = IntOffset(
                                    x = bellOffset.x,
                                    y = bellOffset.y + bellHeightPx
                                ),
                                onDismissRequest = { showNotifications = false },
                                properties = PopupProperties(
                                    focusable = true,
                                    dismissOnBackPress = true,
                                    dismissOnClickOutside = true,
                                )
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    tonalElevation = 6.dp,
                                    shadowElevation = 8.dp,
                                    modifier = Modifier
                                        .widthIn(min = 280.dp, max = 360.dp)
                                ) {
                                    NotificationsPanel(
                                        navController = navController,
                                        onDismiss = { showNotifications = false },
                                        viewModel = notificacionesViewModel,
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Requisito: mostrar nombre y apellido (displayName) y NO mostrar rol.
            Text(
                text = userDisplayName?.takeIf { it.isNotBlank() } ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            if (!userDisplayName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(dashboardItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                            .clickable { navController.navigate(item.route) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(item.icon, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
