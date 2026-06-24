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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.stencilla.app.data.StyleOptions
import com.stencilla.app.data.local.db.ClothingItemEntity
import com.stencilla.app.ui.components.ChipSelector
import com.stencilla.app.ui.components.StencillaBottomBar
import com.stencilla.app.ui.navigation.Routes
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitScreen(
    onNavigate: (String) -> Unit,
    viewModel: OutfitViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val wardrobe by viewModel.wardrobeItems.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Build an outfit") }) },
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
            ChipSelector(
                title = "Occasion",
                options = StyleOptions.occasions,
                selected = state.occasion,
                onSelect = viewModel::onOccasionSelect,
            )

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
                    Text("Style me")
                }
            }

            state.result?.let { result ->
                val resolvedItems = viewModel.resolveResultItems()

                HorizontalDivider()
                Text(text = "Your outfit", style = MaterialTheme.typography.titleLarge)

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
