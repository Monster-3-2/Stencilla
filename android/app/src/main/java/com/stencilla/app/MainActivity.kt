package com.stencilla.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stencilla.app.ui.navigation.Routes
import com.stencilla.app.ui.navigation.StencillaNavGraph
import com.stencilla.app.ui.theme.StencillaTheme
import com.stencilla.app.ui.theme.StencillaTerracotta
import com.stencilla.app.ui.theme.StencillaCardWhite
import com.stencilla.app.ui.theme.StencillaGray
import dagger.hilt.android.AndroidEntryPoint

private val BOTTOM_NAV_ROUTES = setOf(Routes.TODAY, Routes.CLOSET, Routes.PLANNER, Routes.PROFILE)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StencillaTheme {
                val navController = rememberNavController()
                val currentEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (currentRoute in BOTTOM_NAV_ROUTES) {
                            NavigationBar(
                                containerColor = StencillaCardWhite,
                                tonalElevation = androidx.compose.ui.unit.Dp.Unspecified,
                            ) {
                                listOf(
                                    Triple(Routes.TODAY,   Icons.Outlined.WbSunny,       "Today"),
                                    Triple(Routes.CLOSET,  Icons.Outlined.Checkroom,     "Closet"),
                                    Triple(Routes.PLANNER, Icons.Outlined.CalendarMonth,  "Planner"),
                                    Triple(Routes.PROFILE, Icons.Outlined.Person,         "Profile"),
                                ).forEach { (route, icon, label) ->
                                    NavigationBarItem(
                                        selected = currentRoute == route,
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(icon, contentDescription = label) },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor   = StencillaTerracotta,
                                            selectedTextColor   = StencillaTerracotta,
                                            indicatorColor      = StencillaTerracotta.copy(alpha = 0.12f),
                                            unselectedIconColor = StencillaGray,
                                            unselectedTextColor = StencillaGray,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    StencillaNavGraph(
                        navController = navController,
                    )
                }
            }
        }
    }
}
