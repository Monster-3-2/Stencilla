package com.stencilla.app.ui.navigation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stencilla.app.RootViewModel
import com.stencilla.app.ui.auth.AuthScreen
import com.stencilla.app.ui.avatar.AvatarScreen
import com.stencilla.app.ui.closet.AddItemScreen
import com.stencilla.app.ui.closet.ClosetScreen
import com.stencilla.app.ui.onboarding.ProfileScreen
import com.stencilla.app.ui.outfit.OutfitScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun StencillaNavGraph(navController: NavHostController = rememberNavController()) {
    val rootViewModel: RootViewModel = hiltViewModel()
    val isLoggedIn by rootViewModel.isLoggedIn.collectAsState()

    if (isLoggedIn == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Whenever auth state flips to false (e.g. logout), snap back to the auth screen.
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == false && navController.currentDestination?.route != Routes.AUTH) {
            navController.navigate(Routes.AUTH) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn == true) Routes.CLOSET else Routes.AUTH,
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthenticated = {
                    navController.navigate(Routes.CLOSET) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.CLOSET) {
            ClosetScreen(
                onNavigate = { route -> navController.navigateBottomTab(route) },
                onAddItem = { navController.navigate(Routes.ADD_ITEM) },
            )
        }

        composable(Routes.ADD_ITEM) {
            AddItemScreen(
                onBack = { navController.popBackStack() },
                onUploaded = { navController.popBackStack() },
            )
        }

        composable(Routes.OUTFIT) {
            OutfitScreen(onNavigate = { route -> navController.navigateBottomTab(route) })
        }

        composable(Routes.AVATAR) {
            AvatarScreen(onNavigate = { route -> navController.navigateBottomTab(route) })
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}

private fun NavHostController.navigateBottomTab(route: String) {
    navigate(route) {
        popUpTo(Routes.CLOSET) { inclusive = false; saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
