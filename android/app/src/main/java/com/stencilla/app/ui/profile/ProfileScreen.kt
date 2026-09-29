package com.stencilla.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StencillaOffWhite)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding(),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Profile", style = MaterialTheme.typography.headlineMedium, color = StencillaDarkGray)
                Text("Your style identity", style = MaterialTheme.typography.bodySmall, color = StencillaGray)
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Outlined.Settings, "Settings", tint = StencillaGray)
            }
        }

        // ── Avatar + name ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                // Avatar placeholder [4]
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(StencillaTerracottaLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        uiState.fullName?.take(1)?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = StencillaTerracotta,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(uiState.fullName ?: "Your Name", style = MaterialTheme.typography.titleMedium, color = StencillaDarkGray)
                    Text(uiState.email ?: "", style = MaterialTheme.typography.bodySmall, color = StencillaGray)
                    uiState.styleGoal?.let { goal ->
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(StencillaTerracottaLight).padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(goal.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = StencillaTerracotta)
                        }
                    }
                }
            }
        }

        // ── Style identity tags ───────────────────────────────────────────────
        if (!uiState.styleInspirations.isNullOrBlank() || !uiState.styleGoal.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StencillaLavender),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Style Identity", style = MaterialTheme.typography.labelMedium, color = StencillaLavenderDark)
                    Spacer(Modifier.height(8.dp))
                    val tags = listOfNotNull(
                        uiState.styleGoal?.replace("_", " "),
                        uiState.formalityPreference?.replace("_", " "),
                        uiState.comfortPriority?.let { if (it == "high") "Comfort first" else null },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.forEach { tag ->
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(StencillaCardWhite).padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text(tag, style = MaterialTheme.typography.labelSmall, color = StencillaLavenderDark)
                            }
                        }
                    }
                }
            }
        }

        // ── Profile sections ──────────────────────────────────────────────────
        Text("My Details", style = MaterialTheme.typography.titleSmall, color = StencillaGray,
            modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 6.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Column {
                ProfileRow(Icons.Outlined.Person,        "Body & Fit",         "${uiState.bodyType ?: "—"} · ${uiState.heightCm?.let { "${it}cm" } ?: "—"}") { viewModel.startEditing() }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = StencillaLightGray, thickness = 0.5.dp)
                ProfileRow(Icons.Outlined.Palette,       "Color Preferences",  uiState.preferredColors ?: "Not set") { viewModel.startEditing() }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = StencillaLightGray, thickness = 0.5.dp)
                ProfileRow(Icons.Outlined.Style,         "Style Preferences",  uiState.preferredFits ?: "Not set") { viewModel.startEditing() }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = StencillaLightGray, thickness = 0.5.dp)
                ProfileRow(Icons.Outlined.EventNote,     "Occasions",          uiState.lifestyle ?: "Not set") { viewModel.startEditing() }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(StencillaTerracottaLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = StencillaTerracotta, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = StencillaDarkGray)
            Text(value, style = MaterialTheme.typography.bodySmall, color = StencillaGray, maxLines = 1)
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = StencillaGray, modifier = Modifier.size(18.dp))
    }
}
