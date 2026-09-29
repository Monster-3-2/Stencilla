package com.stencilla.app.ui.onboarding

import androidx.compose.runtime.Composable

/**
 * Entry point called by the NavGraph as "ProfileSetupScreen".
 * Delegates to the ProfileScreen composable in this package.
 */
@Composable
fun ProfileSetupScreen(onComplete: () -> Unit) {
    ProfileScreen(
        onBack = onComplete,   // on first-run "back" = done
        onSaved = onComplete,
    )
}
