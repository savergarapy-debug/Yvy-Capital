package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.Property
import com.example.ui.theme.YvyBeigeWarm
import com.example.ui.theme.YvyForestDeep
import com.example.ui.theme.YvyForestMedium
import com.example.ui.theme.YvyForestPrimary
import com.example.ui.theme.YvyGoldAccent
import com.example.ui.util.CurrencyFormatter

@Composable
fun PropertyArtworkHeader(
    property: Property,
    height: Dp = 195.dp,
    variantIndex: Int = 0
) {
    val palette = when ((property.id.hashCode() + variantIndex) % 4) {
        0 -> listOf(Color(0xFF103123), Color(0xFF21523D), Color(0xFFC59B5F))
        1 -> listOf(Color(0xFF142A38), Color(0xFF234B61), Color(0xFFD5B17A))
        2 -> listOf(Color(0xFF2A261E), Color(0xFF473E2F), Color(0xFFC8A97E))
        else -> listOf(Color(0xFF153828), Color(0xFF2D6A4F), Color(0xFFE5D3B3))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(Brush.linearGradient(colors = palette))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sun / Golden ambient glow
            drawCircle(
                color = YvyGoldAccent.copy(alpha = 0.25f),
                radius = h * 0.42f,
                center = Offset(w * 0.8f, h * 0.28f)
            )

            // Architectural skyline / villa silhouette depending on propertyType
            when (property.propertyType.uppercase()) {
                "APARTMENT", "OFFICE" -> {
                    // Modern high-rise towers in Villa Morra / Santa Teresa
                    drawRect(
                        color = Color.White.copy(alpha = 0.12f),
                        topLeft = Offset(w * 0.12f, h * 0.28f),
                        size = Size(w * 0.22f, h * 0.72f)
                    )
                    drawRect(
                        color = YvyBeigeWarm.copy(alpha = 0.18f),
                        topLeft = Offset(w * 0.36f, h * 0.16f),
                        size = Size(w * 0.28f, h * 0.84f)
                    )
                    drawRect(
                        color = Color.White.copy(alpha = 0.10f),
                        topLeft = Offset(w * 0.66f, h * 0.38f),
                        size = Size(w * 0.20f, h * 0.62f)
                    )
                    // Warm lit windows
                    for (r in 0..4) {
                        for (c in 0..2) {
                            drawRect(
                                color = YvyGoldAccent.copy(alpha = 0.45f),
                                topLeft = Offset(w * (0.40f + c * 0.07f), h * (0.24f + r * 0.12f)),
                                size = Size(w * 0.045f, h * 0.06f)
                            )
                        }
                    }
                }
                "LAND", "ESTANCIA" -> {
                    // Rolling hills & waterfront reflection (San Bernardino / Costanera)
                    val hillPath = Path().apply {
                        moveTo(0f, h * 0.72f)
                        quadraticTo(w * 0.3f, h * 0.48f, w * 0.62f, h * 0.68f)
                        quadraticTo(w * 0.85f, h * 0.55f, w, h * 0.66f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(hillPath, color = YvyForestDeep.copy(alpha = 0.45f))
                    // Water reflection lines
                    for (i in 1..4) {
                        drawLine(
                            color = YvyGoldAccent.copy(alpha = 0.35f),
                            start = Offset(w * (0.2f + i * 0.05f), h * (0.76f + i * 0.04f)),
                            end = Offset(w * (0.75f - i * 0.04f), h * (0.76f + i * 0.04f)),
                            strokeWidth = 3f
                        )
                    }
                }
                else -> {
                    // Contemporary tropical residence with pergola & pool
                    val roof = Path().apply {
                        moveTo(w * 0.14f, h * 0.52f)
                        lineTo(w * 0.48f, h * 0.26f)
                        lineTo(w * 0.86f, h * 0.52f)
                        close()
                    }
                    drawPath(roof, color = YvyGoldAccent.copy(alpha = 0.32f))
                    drawRect(
                        color = YvyBeigeWarm.copy(alpha = 0.20f),
                        topLeft = Offset(w * 0.20f, h * 0.52f),
                        size = Size(w * 0.60f, h * 0.36f)
                    )
                    drawRect(
                        color = YvyGoldAccent.copy(alpha = 0.42f),
                        topLeft = Offset(w * 0.28f, h * 0.58f),
                        size = Size(w * 0.22f, h * 0.24f)
                    )
                    // Pool foreground
                    drawRect(
                        color = Color(0xFF4EA8DE).copy(alpha = 0.26f),
                        topLeft = Offset(w * 0.15f, h * 0.86f),
                        size = Size(w * 0.70f, h * 0.14f)
                    )
                }
            }
        }

        // Bottom dark scrim for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyCard(
    property: Property,
    displayCurrency: String,
    isFavorite: Boolean,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("property_card_${property.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box {
                PropertyArtworkHeader(
                    property = property,
                    height = if (compact) 140.dp else 190.dp
                )

                // Top Badges Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (property.operationType == "SALE") YvyForestPrimary else YvyGoldAccent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = CurrencyFormatter.operationLabel(property.operationType),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (property.operationType == "SALE") Color.White else YvyForestDeep,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = CurrencyFormatter.propertyTypeLabel(property.propertyType),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .testTag("favorite_btn_${property.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                            tint = if (isFavorite) Color(0xFFFF6B6B) else Color.White
                        )
                    }
                }

                // Bottom Overlay Price inside Image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = CurrencyFormatter.formatPrice(
                            amount = property.price,
                            sourceCurrency = property.currency,
                            displayCurrency = displayCurrency
                        ),
                        style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (!compact) {
                        Text(
                            text = CurrencyFormatter.formatSecondaryPrice(
                                amount = property.price,
                                sourceCurrency = property.currency
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = YvyGoldAccent
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = property.title,
                        style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        maxLines = if (compact) 1 else 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (property.verified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Propiedad Verificada",
                            tint = YvyForestMedium,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${property.neighborhood}, ${property.city} (${property.department})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Key Metrics Row
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetricBadge(
                        icon = Icons.Default.SquareFoot,
                        text = "${property.areaTotalM2} m²"
                    )
                    if (property.bedrooms > 0) {
                        MetricBadge(
                            icon = Icons.Default.Bed,
                            text = "${property.bedrooms} dorm."
                        )
                    }
                    if (property.bathrooms > 0) {
                        MetricBadge(
                            icon = Icons.Default.Bathtub,
                            text = "${property.bathrooms} baños"
                        )
                    }
                    if (!compact && property.parkingSpaces > 0) {
                        MetricBadge(
                            icon = Icons.Default.DirectionsCar,
                            text = "${property.parkingSpaces} coch."
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBadge(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
