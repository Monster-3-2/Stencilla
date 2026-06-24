package com.stencilla.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.stencilla.app.ui.navigation.Routes

@Composable
fun StencillaBottomBar(currentRoute: String, onNavigate: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.CLOSET,
            onClick = { onNavigate(Routes.CLOSET) },
            icon = { Icon(Icons.Filled.Checkroom, contentDescription = "Closet") },
            label = { Text("Closet") },
        )
        NavigationBarItem(
            selected = currentRoute == Routes.OUTFIT,
            onClick = { onNavigate(Routes.OUTFIT) },
            icon = { Icon(Icons.Filled.Style, contentDescription = "Outfits") },
            label = { Text("Outfits") },
        )
        NavigationBarItem(
            selected = currentRoute == Routes.AVATAR,
            onClick = { onNavigate(Routes.AVATAR) },
            icon = { Icon(Icons.Filled.AccessibilityNew, contentDescription = "Avatar") },
            label = { Text("Avatar") },
        )
        NavigationBarItem(
            selected = currentRoute == Routes.PROFILE,
            onClick = { onNavigate(Routes.PROFILE) },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
        )
    }
}
