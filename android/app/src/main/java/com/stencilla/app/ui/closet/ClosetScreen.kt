package com.stencilla.app.ui.closet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stencilla.app.data.local.db.ClothingItemEntity
import com.stencilla.app.ui.theme.*

@Composable
fun ClosetScreen(
    onAddItem: () -> Unit,
    viewModel: ClosetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackMessage) {
        uiState.snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnack()
        }
    }

    Scaffold(
        containerColor = StencillaOffWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItem,
                containerColor = StencillaTerracotta,
                contentColor = StencillaCardWhite,
                shape = RoundedCornerShape(16.dp),
            ) { Icon(Icons.Outlined.Add, "Add item") }
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            // ── Header ───────────────────────────────────────────────────────
            item(span = { GridItemSpan(3) }) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("My Closet", style = MaterialTheme.typography.headlineMedium, color = StencillaDarkGray)
                            Text("${uiState.totalCount} items", style = MaterialTheme.typography.bodySmall, color = StencillaGray)
                        }
                        IconButton(onClick = { viewModel.refreshAnalytics() }) {
                            Icon(Icons.Outlined.Refresh, "Refresh", tint = StencillaGray)
                        }
                    }
                }
            }

            // ── Wardrobe Overview Card ────────────────────────────────────────
            item(span = { GridItemSpan(3) }) {
                WardrobeOverviewCard(uiState = uiState)
            }

            // ── AI Suggestion Card ────────────────────────────────────────────
            val suggestion = uiState.aiSuggestion
            if (!suggestion.isNullOrBlank()) {
                item(span = { GridItemSpan(3) }) {
                    AiSuggestionCard(suggestion = suggestion)
                }
            }

            // ── Smart Categories ─────────────────────────────────────────────
            if (uiState.smartCategories.isNotEmpty()) {
                item(span = { GridItemSpan(3) }) {
                    SmartCategoriesRow(
                        categories = uiState.smartCategories,
                        onSelect = { viewModel.selectSmartCategory(it) },
                    )
                }
            }

            // ── Search Bar ────────────────────────────────────────────────────
            item(span = { GridItemSpan(3) }) {
                SearchBarRow(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQuery(it) },
                )
            }

            // ── Filter Pills ─────────────────────────────────────────────────
            item(span = { GridItemSpan(3) }) {
                FilterPillsRow(
                    selected = uiState.activeFilter,
                    onSelect = { viewModel.selectFilter(it) },
                )
            }

            // ── Wardrobe Insights ─────────────────────────────────────────────
            if (uiState.topColors.isNotEmpty()) {
                item(span = { GridItemSpan(3) }) {
                    WardrobeInsightsCard(
                        topColors = uiState.topColors,
                        categoryBreakdown = uiState.categoryBreakdown,
                    )
                }
            }

            // ── Grid items ────────────────────────────────────────────────────
            if (uiState.filteredItems.isEmpty() && !uiState.isLoadingItems) {
                item(span = { GridItemSpan(3) }) {
                    EmptyClosetPlaceholder(onAdd = onAddItem)
                }
            } else {
                items(uiState.filteredItems, key = { it.id }) { item ->
                    ClothingGridItem(item = item, onClick = { /* detail */ })
                }
            }

            if (uiState.isLoadingItems) {
                item(span = { GridItemSpan(3) }) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = StencillaTerracotta)
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Wardrobe Overview Card (128 items, 32 outfits, 72% utilized)
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun WardrobeOverviewCard(uiState: ClosetUiState) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Wardrobe overview", style = MaterialTheme.typography.titleSmall, color = StencillaDarkGray)
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OverviewStat(label = "Items", value = "${uiState.totalCount}")
                OverviewDivider()
                OverviewStat(label = "Outfits", value = "${uiState.outfitCount}")
                OverviewDivider()
                OverviewStat(label = "Utilized", value = "${uiState.utilizationPct}%")
            }
            if (uiState.utilizationPct > 0) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { uiState.utilizationPct / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                    color = StencillaTerracotta,
                    trackColor = StencillaLightGray,
                )
            }
        }
    }
}

@Composable
private fun OverviewStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = StencillaTerracotta)
        Text(label, style = MaterialTheme.typography.labelSmall, color = StencillaGray)
    }
}

@Composable
private fun OverviewDivider() {
    Box(modifier = Modifier.width(1.dp).height(36.dp).background(StencillaLightGray))
}

// ──────────────────────────────────────────────────────────────────────────────
// AI Suggestion Card
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun AiSuggestionCard(suggestion: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaLavender),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.AutoAwesome, null, tint = StencillaLavenderDark, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("AI Insight", style = MaterialTheme.typography.labelMedium, color = StencillaLavenderDark)
                Text(suggestion, style = MaterialTheme.typography.bodySmall, color = StencillaLavenderDark.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Smart Categories Row
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun SmartCategoriesRow(
    categories: List<com.stencilla.app.data.remote.dto.SmartCategoryDto>,
    onSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            "Smart categories",
            style = MaterialTheme.typography.titleSmall,
            color = StencillaDarkGray,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(categories) { cat ->
                Card(
                    modifier = Modifier.clickable { onSelect(cat.label) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
                    elevation = CardDefaults.cardElevation(1.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(cat.label, style = MaterialTheme.typography.labelMedium, color = StencillaDarkGray)
                        Text("${cat.count} items", style = MaterialTheme.typography.labelSmall, color = StencillaGray)
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Search bar
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun SearchBarRow(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search by color, type, brand…", color = StencillaGray) },
        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = StencillaGray) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Outlined.Close, null, tint = StencillaGray)
                }
            }
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = StencillaTerracotta,
            unfocusedBorderColor = StencillaLightGray,
            focusedContainerColor = StencillaCardWhite,
            unfocusedContainerColor = StencillaCardWhite,
        ),
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Filter pills
// ──────────────────────────────────────────────────────────────────────────────
private val FILTERS = listOf("All", "Tops", "Bottoms", "Dresses", "Outers", "Shoes", "Bags", "Accessories")

@Composable
private fun FilterPillsRow(selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        items(FILTERS) { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) StencillaTerracotta else StencillaCardWhite)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    filter,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) StencillaCardWhite else StencillaDarkGray,
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Wardrobe Insights
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun WardrobeInsightsCard(
    topColors: List<com.stencilla.app.data.remote.dto.ColorEntryDto>,
    categoryBreakdown: List<com.stencilla.app.data.remote.dto.CategoryCountDto>,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Wardrobe insights", style = MaterialTheme.typography.titleSmall, color = StencillaDarkGray)
            Spacer(Modifier.height(12.dp))

            if (topColors.isNotEmpty()) {
                Text("Most worn colors", style = MaterialTheme.typography.labelMedium, color = StencillaGray)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    topColors.take(5).forEach { colorEntry ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StencillaLightGray),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(colorEntry.color.take(1).uppercase(),
                                    style = MaterialTheme.typography.labelMedium, color = StencillaDarkGray)
                            }
                            Text(colorEntry.color.take(8), style = MaterialTheme.typography.labelSmall, color = StencillaGray)
                        }
                    }
                }
            }

            if (categoryBreakdown.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text("Category split", style = MaterialTheme.typography.labelMedium, color = StencillaGray)
                Spacer(Modifier.height(6.dp))
                val total = categoryBreakdown.sumOf { it.count }.coerceAtLeast(1)
                categoryBreakdown.take(4).forEach { cat ->
                    val pct = cat.count * 100 / total
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            cat.category, style = MaterialTheme.typography.bodySmall, color = StencillaDarkGray,
                            modifier = Modifier.width(90.dp),
                        )
                        LinearProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(4.dp)),
                            color = StencillaTerracotta, trackColor = StencillaLightGray,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("$pct%", style = MaterialTheme.typography.labelSmall, color = StencillaGray)
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Clothing Grid Item
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun ClothingGridItem(item: ClothingItemEntity, onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(0.75f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(item.localImagePath).crossfade(true).build(),
                    contentDescription = item.category,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
                Text(
                    item.category ?: "Item",
                    style = MaterialTheme.typography.labelSmall,
                    color = StencillaDarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                item.colorPrimary?.let { color ->
                    Text(color, style = MaterialTheme.typography.labelSmall, color = StencillaGray, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun EmptyClosetPlaceholder(onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.Checkroom, null, tint = StencillaLightGray, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Your closet is empty", style = MaterialTheme.typography.titleMedium, color = StencillaGray)
        Text(
            "Tap + to photograph your clothes. Stencilla will tag them automatically.",
            style = MaterialTheme.typography.bodySmall,
            color = StencillaGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onAdd, shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StencillaTerracotta),
        ) {
            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add First Item", color = StencillaCardWhite)
        }
    }
}
