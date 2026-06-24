package com.stencilla.app.ui.avatar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stencilla.app.ui.components.StencillaBottomBar
import com.stencilla.app.ui.navigation.Routes
import com.stencilla.app.ui.outfit.OutfitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarScreen(
    onNavigate: (String) -> Unit,
    outfitViewModel: OutfitViewModel = hiltViewModel(),
) {
    val outfitState by outfitViewModel.uiState.collectAsState()
    val resolvedItems = outfitViewModel.resolveResultItems()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Outfit Preview", style = MaterialTheme.typography.titleLarge) }) },
        bottomBar = { StencillaBottomBar(currentRoute = Routes.AVATAR, onNavigate = onNavigate) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (resolvedItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No outfit generated yet", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Go to Outfits and tap \"Style me\" first",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                // Extract colors for the avatar layers
                val topItem = resolvedItems.firstOrNull { it.category in listOf("shirt", "tshirt", "jacket", "coat", "sweater") }
                val bottomItem = resolvedItems.firstOrNull { it.category in listOf("jeans", "trousers", "shorts", "skirt") }
                val shoeItem = resolvedItems.firstOrNull { it.category == "shoes" }

                val topColor = colorFromName(topItem?.colorPrimary)
                val bottomColor = colorFromName(bottomItem?.colorPrimary)
                val shoeColor = colorFromName(shoeItem?.colorPrimary)

                // SVG-style mannequin canvas
                Canvas(
                    modifier = Modifier
                        .size(220.dp, 400.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFAF8F5)),
                ) {
                    drawMannequin(topColor, bottomColor, shoeColor)
                }

                // Item labels beneath the figure
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    resolvedItems.forEach { item ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colorFromName(item.colorPrimary).copy(alpha = 0.15f),
                            ),
                            elevation = CardDefaults.cardElevation(0.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(colorFromName(item.colorPrimary)),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.subcategory ?: item.category ?: "Item",
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                )
                            }
                        }
                    }
                }

                // AI reasoning
                outfitState.result?.reasoning?.let { reasoning ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(0.dp),
                    ) {
                        Text(
                            text = reasoning,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }

                // Avatar description (text-based 3D description from AI)
                outfitState.result?.avatarDescription?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Draws a simple stylized mannequin figure with outfit color zones layered on top. */
private fun DrawScope.drawMannequin(
    topColor: Color,
    bottomColor: Color,
    shoeColor: Color,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f

    val skinColor = Color(0xFFD4A574)
    val outlineColor = Color(0xFF2B2B2B)

    // Head
    drawCircle(color = skinColor, radius = w * 0.12f, center = Offset(cx, h * 0.08f))
    drawCircle(color = outlineColor, radius = w * 0.12f + 1f, center = Offset(cx, h * 0.08f), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))

    // Neck
    drawRect(color = skinColor, topLeft = Offset(cx - w * 0.03f, h * 0.18f), size = Size(w * 0.06f, h * 0.05f))

    // Torso (top color)
    drawRoundRect(
        color = topColor,
        topLeft = Offset(cx - w * 0.22f, h * 0.22f),
        size = Size(w * 0.44f, h * 0.28f),
        cornerRadius = CornerRadius(12f),
    )
    drawRoundRect(
        color = outlineColor,
        topLeft = Offset(cx - w * 0.22f, h * 0.22f),
        size = Size(w * 0.44f, h * 0.28f),
        cornerRadius = CornerRadius(12f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f),
    )

    // Arms
    val armTop = h * 0.24f
    val armH = h * 0.24f
    val armW = w * 0.1f
    // Left arm
    drawRoundRect(color = topColor, topLeft = Offset(cx - w * 0.33f, armTop), size = Size(armW, armH), cornerRadius = CornerRadius(8f))
    drawRoundRect(color = outlineColor, topLeft = Offset(cx - w * 0.33f, armTop), size = Size(armW, armH), cornerRadius = CornerRadius(8f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
    // Right arm
    drawRoundRect(color = topColor, topLeft = Offset(cx + w * 0.23f, armTop), size = Size(armW, armH), cornerRadius = CornerRadius(8f))
    drawRoundRect(color = outlineColor, topLeft = Offset(cx + w * 0.23f, armTop), size = Size(armW, armH), cornerRadius = CornerRadius(8f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))

    // Legs (bottom color)
    val legTop = h * 0.50f
    val legH = h * 0.38f
    val legW = w * 0.18f
    // Left leg
    drawRoundRect(color = bottomColor, topLeft = Offset(cx - w * 0.22f, legTop), size = Size(legW, legH), cornerRadius = CornerRadius(8f))
    drawRoundRect(color = outlineColor, topLeft = Offset(cx - w * 0.22f, legTop), size = Size(legW, legH), cornerRadius = CornerRadius(8f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
    // Right leg
    drawRoundRect(color = bottomColor, topLeft = Offset(cx + w * 0.04f, legTop), size = Size(legW, legH), cornerRadius = CornerRadius(8f))
    drawRoundRect(color = outlineColor, topLeft = Offset(cx + w * 0.04f, legTop), size = Size(legW, legH), cornerRadius = CornerRadius(8f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.5f))

    // Shoes
    val shoeTop = h * 0.87f
    val shoeH = h * 0.07f
    // Left shoe
    drawRoundRect(color = shoeColor, topLeft = Offset(cx - w * 0.24f, shoeTop), size = Size(w * 0.22f, shoeH), cornerRadius = CornerRadius(10f))
    // Right shoe
    drawRoundRect(color = shoeColor, topLeft = Offset(cx + w * 0.02f, shoeTop), size = Size(w * 0.22f, shoeH), cornerRadius = CornerRadius(10f))
}

/** Maps a color name string to a Compose Color for the avatar layers. */
fun colorFromName(name: String?): Color = when (name?.lowercase()) {
    "white" -> Color(0xFFF5F5F5)
    "black" -> Color(0xFF1A1A1A)
    "navy", "navy blue" -> Color(0xFF1A237E)
    "blue" -> Color(0xFF1565C0)
    "light blue", "sky blue" -> Color(0xFF64B5F6)
    "grey", "gray" -> Color(0xFF9E9E9E)
    "charcoal" -> Color(0xFF37474F)
    "beige", "cream", "ivory" -> Color(0xFFF5F0E8)
    "brown" -> Color(0xFF6D4C41)
    "tan", "khaki" -> Color(0xFFD2B48C)
    "red" -> Color(0xFFC62828)
    "burgundy", "maroon", "wine" -> Color(0xFF6A1428)
    "pink" -> Color(0xFFE91E8C)
    "green" -> Color(0xFF2E7D32)
    "olive" -> Color(0xFF827717)
    "yellow", "mustard" -> Color(0xFFF9A825)
    "orange" -> Color(0xFFE65100)
    "purple" -> Color(0xFF6A1B9A)
    "gold" -> Color(0xFFC9A668)
    "silver" -> Color(0xFFB0BEC5)
    "denim" -> Color(0xFF4A6FA5)
    else -> Color(0xFF9E9E9E)
}
