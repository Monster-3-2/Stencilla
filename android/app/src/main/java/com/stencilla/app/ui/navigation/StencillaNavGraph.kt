package com.stencilla.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.stencilla.app.RootViewModel
import com.stencilla.app.ui.auth.AuthScreen
import com.stencilla.app.ui.closet.AddItemScreen
import com.stencilla.app.ui.closet.ClosetScreen
import com.stencilla.app.ui.onboarding.ProfileSetupScreen
import com.stencilla.app.ui.planner.PlannerScreen
import com.stencilla.app.ui.profile.ProfileScreen
import com.stencilla.app.ui.settings.SettingsScreen
import com.stencilla.app.ui.stylenotes.StyleNotesScreen
import com.stencilla.app.ui.today.TodayScreen
import com.stencilla.app.ui.verifier.OutfitVerifierScreen

@Composable
fun StencillaNavGraph(navController: NavHostController) {
    val rootVm: RootViewModel = hiltViewModel()
    val isLoggedIn by rootVm.isLoggedIn.collectAsState(initial = null)

    if (isLoggedIn == null) return  // Splash — wait for DataStore to emit

    val startDest = if (isLoggedIn == true) Routes.TODAY else Routes.AUTH

    NavHost(navController = navController, startDestination = startDest) {

        composable(Routes.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            ProfileSetupScreen(
                onComplete = {
                    navController.navigate(Routes.TODAY) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.TODAY) {
            TodayScreen(
                onNavigateToStyleNotes = { navController.navigate(Routes.STYLE_NOTES) },
                onNavigateToVerifier  = { navController.navigate(Routes.VERIFIER) },
            )
        }

        composable(Routes.CLOSET) {
            ClosetScreen(
                onAddItem = { navController.navigate(Routes.ADD_ITEM) },
            )
        }

        composable(Routes.PLANNER) {
            PlannerScreen()
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.ADD_ITEM) {
            AddItemScreen(
                onBack    = { navController.popBackStack() },
                onUploaded = { navController.popBackStack() },
            )
        }

        composable(Routes.VERIFIER) {
            OutfitVerifierScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.STYLE_NOTES) {
            StyleNotesScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
