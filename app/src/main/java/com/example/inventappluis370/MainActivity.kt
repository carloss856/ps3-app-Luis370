package com.example.inventappluis370

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.inventappluis370.domain.repository.TokenRepository
import com.example.inventappluis370.ui.navigation.AppNavGraph
import com.example.inventappluis370.ui.navigation.Routes
import com.example.inventappluis370.ui.theme.InventAppLuis370Theme
import com.example.inventappluis370.ui.theme.LocalThemeController
import com.example.inventappluis370.ui.theme.ThemeController
import com.example.inventappluis370.ui.theme.ThemeMode
import com.example.inventappluis370.ui.theme.ThemePreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenRepository: TokenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current.applicationContext
            val scope = rememberCoroutineScope()
            val themeMode by ThemePreferences.themeModeFlow(context).collectAsState(initial = ThemeMode.LIGHT)
            val isDarkTheme = themeMode == ThemeMode.DARK
            val themeController = ThemeController(
                mode = themeMode,
                toggle = {
                    scope.launch {
                        val nextMode = if (themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
                        ThemePreferences.setThemeMode(context, nextMode)
                    }
                }
            )

            CompositionLocalProvider(LocalThemeController provides themeController) {
                InventAppLuis370Theme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        val token by tokenRepository.tokenFlow.collectAsState()

                        val startDestination = if (token != null) Routes.DASHBOARD else Routes.LOGIN

                        LaunchedEffect(token) {
                            if (token == null && navController.currentBackStackEntry?.destination?.route != Routes.LOGIN) {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            } else if (token != null && navController.currentBackStackEntry?.destination?.route == Routes.LOGIN) {
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        }

                        AppNavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}

