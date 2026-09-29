package com.stencilla.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stencilla.app.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        containerColor = StencillaOffWhite,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = StencillaDarkGray)
                }
                Spacer(Modifier.width(8.dp))
                Text("Settings", style = MaterialTheme.typography.titleLarge, color = StencillaDarkGray)
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 40.dp),
        ) {
            // ── Notifications ────────────────────────────────────────────────
            item { SettingsSectionLabel("Notifications") }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Outlined.Checkroom,
                        title = "Daily Outfit Suggestion",
                        subtitle = "Morning reminder with your AI look",
                        checked = settings.notificationOutfit,
                        onCheckedChange = { viewModel.update(settings.copy(notificationOutfit = it)) },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Event Reminders",
                        subtitle = "Outfit alerts for upcoming calendar events",
                        checked = settings.notificationPlanner,
                        onCheckedChange = { viewModel.update(settings.copy(notificationPlanner = it)) },
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        icon = Icons.Outlined.Lightbulb,
                        title = "Style Tips",
                        subtitle = "Weekly AI fashion insights",
                        checked = settings.notificationTips,
                        onCheckedChange = { viewModel.update(settings.copy(notificationTips = it)) },
                    )
                }
            }

            // ── Privacy ──────────────────────────────────────────────────────
            item { SettingsSectionLabel("Privacy") }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Outlined.Analytics,
                        title = "Usage Analytics",
                        subtitle = "Help improve Stencilla (anonymous)",
                        checked = settings.privacyAnalytics,
                        onCheckedChange = { viewModel.update(settings.copy(privacyAnalytics = it)) },
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.PrivacyTip,
                        title = "Privacy Policy",
                        onClick = { /* open browser */ },
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.Security,
                        title = "Data & Storage",
                        subtitle = "Manage your locally stored images",
                        onClick = { /* navigate */ },
                    )
                }
            }

            // ── Account ──────────────────────────────────────────────────────
            item { SettingsSectionLabel("Account") }
            item {
                SettingsCard {
                    SettingsNavRow(
                        icon = Icons.Outlined.Person,
                        title = "Edit Profile",
                        onClick = { /* navigate to profile setup */ },
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.Lock,
                        title = "Change Password",
                        onClick = { /* navigate */ },
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.DeleteForever,
                        title = "Delete Account",
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = { /* show confirm dialog */ },
                    )
                }
            }

            // ── Help ─────────────────────────────────────────────────────────
            item { SettingsSectionLabel("Help & Info") }
            item {
                SettingsCard {
                    SettingsNavRow(
                        icon = Icons.Outlined.HelpOutline,
                        title = "FAQ & Support",
                        onClick = { /* open support */ },
                    )
                    SettingsDivider()
                    SettingsNavRow(
                        icon = Icons.Outlined.Info,
                        title = "About Stencilla",
                        subtitle = "Version 1.0.0",
                        onClick = { },
                    )
                }
            }

            // ── Logout ───────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.logout(); onLogout() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                        ),
                    ) {
                        Icon(Icons.Outlined.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Out", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Helper composables
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsSectionLabel(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = StencillaGray,
        modifier = Modifier.padding(start = 28.dp, top = 20.dp, bottom = 6.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = StencillaLightGray,
        thickness = 0.5.dp,
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(StencillaTerracottaLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = StencillaTerracotta, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = StencillaDarkGray)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StencillaGray)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = StencillaCardWhite,
                checkedTrackColor = StencillaTerracotta,
                uncheckedTrackColor = StencillaLightGray,
            ),
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = StencillaDarkGray,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(StencillaTerracottaLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = StencillaTerracotta, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = titleColor)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = StencillaGray)
            }
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = StencillaGray, modifier = Modifier.size(18.dp))
    }
}
