package com.stencilla.app.ui.outfit

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.stencilla.app.data.StyleOptions
import com.stencilla.app.data.local.db.ClothingItemEntity
import androidx.compose.material3.Slider
import com.stencilla.app.ui.components.ChipOption
import com.stencilla.app.ui.components.ChipSelector
import com.stencilla.app.ui.components.StencillaBottomBar
import com.stencilla.app.ui.navigation.Routes
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitScreen(
    onNavigate: (String) -> Unit,
    onViewHistory: () -> Unit,
    onViewPlanner: () -> Unit,
    onOpenVerifier: () -> Unit,
    viewModel: OutfitViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val wardrobe by viewModel.wardrobeItems.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.refreshDeviceContext() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today") },
                actions = { TextButton(onClick = onViewPlanner) { Text("Planner") }; TextButton(onClick = onViewHistory) { Text("History") } },
            )
        },
        bottomBar = { StencillaBottomBar(currentRoute = Routes.OUTFIT, onNavigate = onNavigate) },
        ) { paddingValues ->
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Good morning",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "Let’s make getting dressed feel easy.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Your styling context", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = state.contextMessage ?: "Weather and your next calendar event will shape today's look.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${state.weatherTemp.toInt()}°C · ${state.weatherCondition}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Text(
                    text = "What are you dressing for?",
                    style = MaterialTheme.typography.titleLarge,
                )

            ChipSelector(
                title = "Occasion",
                options = StyleOptions.occasions,
                selected = state.occasion,
                onSelect = viewModel::onOccasionSelect,
            )

            ChipSelector(
                title = "Dress code (optional)",
                options = StyleOptions.dressCodes,
                selected = state.dressCode,
                onSelect = viewModel::onDressCodeSelect,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Fine-tune today's suggestion", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = state.contextMessage ?: "Weather and your next calendar event are used automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Temperature: ${state.weatherTemp.toInt()}°C",
                    style = MaterialTheme.typography.labelLarge,
                )
                Slider(
                    value = state.weatherTemp.toFloat(),
                    onValueChange = { viewModel.onWeatherTempChange(it.toDouble()) },
                    valueRange = 0f..45f,
                )
                ChipSelector(
                    title = "Conditions",
                    options = StyleOptions.weatherConditions,
                    selected = state.weatherCondition,
                    onSelect = viewModel::onWeatherConditionSelect,
                )
                OutlinedTextField(
                    value = if (state.calendarEvent == "None") "" else state.calendarEvent,
                    onValueChange = viewModel::onCalendarEventChange,
                    label = { Text("Calendar event (optional)") },
                    placeholder = { Text("e.g. client presentation or dinner") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                TextButton(onClick = {
                    permissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_CALENDAR,
                    ))
                }) { Text("Refresh weather & calendar") }
            }

            Column {
                Text(text = "Anchor item (optional)", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = "Pick one piece you want to build the look around.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 10.dp),
                ) {
                    items(wardrobe, key = { it.id }) { item ->
                        AnchorThumbnail(
                            item = item,
                            isSelected = state.anchorItemId == item.id,
                            onClick = {
                                viewModel.onAnchorSelect(if (state.anchorItemId == item.id) null else item.id)
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Anything else? e.g. \"it's raining\", \"want to look slim\"") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            state.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = viewModel::generateOutfit,
                enabled = !state.isGenerating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create my look")
                }
            }

            OutlinedButton(onClick = onOpenVerifier, modifier = Modifier.fillMaxWidth()) {
                Text("Optional AI outfit verifier")
            }

            state.result?.let { result ->
                val resolvedItems = viewModel.resolveResultItems()

                HorizontalDivider()
                Text(text = "Today’s look", style = MaterialTheme.typography.headlineSmall)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(resolvedItems, key = { it.id }) { item ->
                        AsyncImage(
                            model = File(item.localImagePath),
                            contentDescription = item.subcategory,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    }
                }

                result.reasoning?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyLarge)
                }

                if (result.factors.isNotEmpty()) {
                    Text("Why this works", style = MaterialTheme.typography.titleMedium)
                    result.factors.forEach { factor ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${factor.label} · ${factor.score}%", style = MaterialTheme.typography.titleSmall)
                                Text(factor.explanation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                if (result.alternatives.isNotEmpty()) {
                    Text("Try another direction", style = MaterialTheme.typography.titleMedium)
                    result.alternatives.forEach { alternative ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(alternative.label, style = MaterialTheme.typography.titleSmall)
                                Text(alternative.reason, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("How did this suggestion work?", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Your private feedback helps future suggestions improve.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("Outcome", style = MaterialTheme.typography.labelLarge)
                        TextButton(onClick = { viewModel.onFeedbackOutcome("worn") }) {
                            Text(if (state.feedbackOutcome == "worn") "✓ Wore it" else "Wore it")
                        }
                        TextButton(onClick = { viewModel.onFeedbackOutcome("skipped") }) {
                            Text(if (state.feedbackOutcome == "skipped") "✓ Skipped it" else "Skipped it")
                        }
                        Text("Rating (optional)", style = MaterialTheme.typography.labelLarge)
                        (1..5).forEach { rating ->
                            TextButton(onClick = { viewModel.onFeedbackRating(rating) }) {
                                Text(if (state.feedbackRating == rating) "✓ $rating / 5" else "$rating / 5")
                            }
                        }
                        OutlinedTextField(
                            value = state.feedbackReason,
                            onValueChange = viewModel::onFeedbackReason,
                            label = { Text("Why? (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Button(
                            onClick = viewModel::saveFeedback,
                            enabled = state.feedbackOutcome != null && !state.feedbackSaved,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(if (state.feedbackSaved) "Feedback saved" else "Save feedback") }
                    }
                }

                if (result.shoppingSuggestions.isNotEmpty()) {
                    Text(text = "Worth adding to your wardrobe", style = MaterialTheme.typography.titleMedium)
                    result.shoppingSuggestions.forEach { suggestion ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = suggestion.item, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = suggestion.reason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnchorThumbnail(
    item: ClothingItemEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(10.dp))
    } else {
        Modifier
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .then(borderModifier)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = File(item.localImagePath),
            contentDescription = item.subcategory,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
