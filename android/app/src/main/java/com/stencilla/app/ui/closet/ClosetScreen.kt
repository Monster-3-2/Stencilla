package com.stencilla.app.ui.closet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.stencilla.app.data.local.db.ClothingItemEntity
import com.stencilla.app.data.StyleOptions
import com.stencilla.app.ui.components.ChipOption
import com.stencilla.app.ui.components.ChipSelector
import com.stencilla.app.ui.components.StencillaBottomBar
import com.stencilla.app.ui.navigation.Routes
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosetScreen(
    onNavigate: (String) -> Unit,
    onAddItem: () -> Unit,
    viewModel: ClosetViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsState()
    val state by viewModel.uiState.collectAsState()

    // Clarification dialog
    state.pendingClarificationItem?.let { item ->
        ClarificationDialog(
            item = item,
            onSubmit = { material, fit -> viewModel.submitClarification(item.id, material, fit) },
            onDismiss = viewModel::dismissClarification,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Your Closet", style = MaterialTheme.typography.titleLarge)
            })
        },
        bottomBar = { StencillaBottomBar(currentRoute = Routes.CLOSET, onNavigate = onNavigate) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddItem,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add item", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Category tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(CLOSET_TABS) { tab ->
                    FilterChip(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        label = { Text(tab, style = MaterialTheme.typography.labelLarge) },
                    )
                }
            }

            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nothing here yet", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tap + to photograph a clothing item",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items, key = { it.id }) { item ->
                        ClothingItemCard(
                            item = item,
                            onDelete = { viewModel.deleteItem(item) },
                        )
                    }
                }
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ClothingItemCard(
    item: ClothingItemEntity,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onDelete),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column {
            // Photo
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                AsyncImage(
                    model = File(item.localImagePath),
                    contentDescription = item.subcategory,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                )
                if (!item.aiTagged) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(4.dp),
                    ) {
                        Text(
                            text = "Analysing…",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                        )
                    }
                }
                if (item.needsClarification) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text("?", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.subcategory ?: item.category?.replaceFirstChar { it.uppercase() } ?: "Item",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    item.colorPrimary?.let { TagChip(it) }
                    item.material?.let { TagChip(it) }
                    item.fit?.let { TagChip(it) }
                }
                item.aiImageDescription?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun TagChip(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ClarificationDialog(
    item: ClothingItemEntity,
    onSubmit: (material: String?, fit: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedMaterial by remember { mutableStateOf<String?>(null) }
    var selectedFit by remember { mutableStateOf<String?>(null) }

    val materialOptions = listOf("cotton","polyester","wool","linen","denim","leather","silk","synthetic","knit","velvet")
        .map { ChipOption(it, it.replaceFirstChar { c -> c.uppercase() }) }
    val fitOptions = listOf("slim","regular","oversized","relaxed","skinny","tailored")
        .map { ChipOption(it, it.replaceFirstChar { c -> c.uppercase() }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("One quick question", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = item.clarificationQuestion ?: "Help us tag this item better:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (item.material == null || item.material == "unknown") {
                    ChipSelector("Material", materialOptions, selectedMaterial) { selectedMaterial = it }
                }
                if (item.fit == null || item.fit == "unknown") {
                    ChipSelector("Fit", fitOptions, selectedFit) { selectedFit = it }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(selectedMaterial, selectedFit) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Skip") }
        },
    )
}
