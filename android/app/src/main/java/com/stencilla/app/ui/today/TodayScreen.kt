package com.stencilla.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun TodayScreen(
    onNavigateToStyleNotes: () -> Unit,
    onNavigateToVerifier: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
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
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(uiState.greeting, style = MaterialTheme.typography.bodySmall, color = StencillaGray)
            Text("Your look for today", style = MaterialTheme.typography.headlineMedium, color = StencillaDarkGray)
        }

        // ── Weather Card ──────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.WbSunny, null, tint = StencillaGold, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "${uiState.weatherTemp?.toInt() ?: "--"}°C · ${uiState.weatherCondition ?: "Loading…"}",
                        style = MaterialTheme.typography.titleMedium, color = StencillaDarkGray,
                    )
                    uiState.calendarEvent?.let { event ->
                        Text("Next: $event", style = MaterialTheme.typography.bodySmall, color = StencillaGray)
                    }
                }
            }
        }

        // ── Upcoming calendar event from planner ──────────────────────────────
        uiState.upcomingEventContext?.let { ctx ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StencillaLavender),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, null, tint = StencillaLavenderDark, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(ctx, style = MaterialTheme.typography.bodySmall, color = StencillaLavenderDark)
                }
            }
        }

        // ── Today's AI Outfit Placeholder ─────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Today's Outfit", style = MaterialTheme.typography.titleMedium, color = StencillaDarkGray, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.refreshOutfit() }) {
                        Text("Refresh", style = MaterialTheme.typography.labelSmall, color = StencillaTerracotta)
                    }
                }
                Spacer(Modifier.height(10.dp))
                // Placeholder [3] - AI outfit look image / item carousel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(StencillaLightGray),
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(color = StencillaTerracotta)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Checkroom, null, tint = StencillaGray, modifier = Modifier.size(48.dp))
                            Text("[3] Outfit preview", style = MaterialTheme.typography.labelSmall, color = StencillaGray)
                        }
                    }
                }
                uiState.outfitReasoning?.let { reason ->
                    Spacer(Modifier.height(10.dp))
                    Text(reason, style = MaterialTheme.typography.bodySmall, color = StencillaGray)
                }
            }
        }

        // ── "Why this works" factors ──────────────────────────────────────────
        if (uiState.outfitFactors.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                Text("Why this works", style = MaterialTheme.typography.titleSmall, color = StencillaDarkGray, modifier = Modifier.padding(bottom = 8.dp))
                uiState.outfitFactors.forEach { factor ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                        LinearProgressIndicator(
                            progress = { factor.score / 100f },
                            modifier = Modifier.width(80.dp).height(6.dp).clip(RoundedCornerShape(4.dp)),
                            color = StencillaTerracotta, trackColor = StencillaLightGray,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("${factor.label} · ${factor.score}%", style = MaterialTheme.typography.labelSmall, color = StencillaDarkGray)
                    }
                }
            }
        }

        // ── Quick Actions ─────────────────────────────────────────────────────
        Text(
            "Quick actions",
            style = MaterialTheme.typography.titleSmall,
            color = StencillaDarkGray,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickActionCard("Create Outfit",    Icons.Outlined.AutoFixHigh,   modifier = Modifier.weight(1f)) { viewModel.refreshOutfit() }
            QuickActionCard("Style Notes",      Icons.Outlined.StickyNote2,   modifier = Modifier.weight(1f)) { onNavigateToStyleNotes() }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickActionCard("Outfit Verifier",  Icons.Outlined.CameraAlt,     modifier = Modifier.weight(1f)) { onNavigateToVerifier() }
            QuickActionCard("Weather Aware",    Icons.Outlined.Thermostat,    modifier = Modifier.weight(1f)) { viewModel.refreshDeviceContext() }
        }

        // ── Ask AI stylist (style notes input) ───────────────────────────────
        var aiQuery by remember { mutableStateOf("") }
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
            elevation = CardDefaults.cardElevation(1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ask AI Stylist", style = MaterialTheme.typography.titleSmall, color = StencillaDarkGray)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = aiQuery,
                        onValueChange = { aiQuery = it },
                        placeholder = { Text("e.g. something edgy but work-appropriate…", color = StencillaGray, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StencillaTerracotta,
                            unfocusedBorderColor = StencillaLightGray,
                        ),
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.askStylist(aiQuery); aiQuery = "" },
                        enabled = aiQuery.isNotBlank() && !uiState.isGenerating,
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(StencillaTerracotta),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Send, null, tint = StencillaCardWhite, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                uiState.stylistReply?.let { reply ->
                    Spacer(Modifier.height(10.dp))
                    Text(reply, style = MaterialTheme.typography.bodySmall, color = StencillaGray)
                }
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun QuickActionCard(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(StencillaTerracottaLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = StencillaTerracotta, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = StencillaDarkGray)
        }
    }
}
