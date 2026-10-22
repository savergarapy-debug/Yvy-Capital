package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Bathtub
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AdvisorMemberships
import com.example.data.model.Property
import com.example.ui.theme.YvyPriceCardStyle
import com.example.ui.theme.YvyPriceCompactCardStyle
import com.example.ui.theme.YvyShapes
import com.example.ui.theme.YvyTerracotta
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DeepLinkHandler

/**
 * Cabecera visual de la propiedad:
 * Muestra el contenedor neutro `#ECE7DE` (`MaterialTheme.colorScheme.surfaceVariant`)
 * con un ícono de casa discreto (`Icons.Outlined.Home`), sin ilustraciones de edificios ni degradados.
 */
@Composable
fun PropertyArtworkHeader(
    @Suppress("UNUSED_PARAMETER") property: Property,
    height: Dp = 136.dp,
    @Suppress("UNUSED_PARAMETER") variantIndex: Int = 0
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Home,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Tarjeta plana de propiedad (destacada `compact = false` y compacta `compact = true`):
 * - Radio: 12 dp (`YvyShapes.Card`), fondo blanco `#FFFFFF`, borde fino de 1 dp `#E6E1D8`, sin sombras dramáticas.
 * - Máximo dos etiquetas pequeñas y sobrias sobre la foto: operación («En venta» / «En alquiler» en tierra roja `#B5532E`)
 *   y tipo de propiedad.
 * - Precio con cifras tabulares (`tnum`) en tierra roja `#B5532E` y título/metadatos con contraste WCAG AA.
 * - Íconos de línea consistentes (`Icons.Outlined.*`) fuera de cajas de color.
 */
@Composable
fun PropertyCard(
    property: Property,
    @Suppress("UNUSED_PARAMETER") displayCurrency: String = property.currency,
    isFavorite: Boolean,
    isStaff: Boolean = false,
    membershipTier: String? = null,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val context = LocalContext.current
    val cardHeight = if (compact) 212.dp else 248.dp
    val imageHeight = if (compact) 104.dp else 132.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable(onClick = onCardClick)
            .testTag("property_card_${property.id}"),
        shape = YvyShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
            ) {
                PropertyArtworkHeader(
                    property = property,
                    height = imageHeight
                )

                // Fila superior sobre la foto: máximo 2 etiquetas sobrias a la izquierda y acciones a la derecha
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(top = 2.dp, start = 2.dp, end = 4.dp)
                    ) {
                        if (isStaff) {
                            StaffBadge()
                        } else if (!membershipTier.isNullOrBlank() && membershipTier != "FREE") {
                            StatusBadge(
                                text = AdvisorMemberships.getSpec(membershipTier)?.name ?: "Asesor Pro",
                                tone = StatusBadgeTone.PRO_GOLD
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(
                                text = if (property.operationType == "SALE") "En venta" else "En alquiler",
                                tone = StatusBadgeTone.TERRACOTTA_ACCENT
                            )

                            if (!compact && !isStaff) {
                                StatusBadge(
                                    text = CurrencyFormatter.propertyTypeLabel(property.propertyType),
                                    tone = StatusBadgeTone.NEUTRAL_OUTLINED
                                )
                            }
                        }

                        if (compact) {
                            StatusBadge(
                                text = CurrencyFormatter.propertyTypeLabel(property.propertyType),
                                tone = StatusBadgeTone.NEUTRAL_OUTLINED
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Button,
                                    onClick = {
                                        DeepLinkHandler.shareProperty(context, property, property.currency)
                                    }
                                )
                                .testTag("share_card_btn_${property.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = "Compartir propiedad",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Button,
                                    onClick = onFavoriteClick
                                )
                                .testTag("favorite_btn_${property.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                                        tint = if (isFavorite) YvyTerracotta else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cuerpo de la tarjeta sobre fondo blanco #FFFFFF
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = if (compact) 10.dp else 12.dp,
                        vertical = if (compact) 8.dp else 10.dp
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Precio destacado en tierra roja (#B5532E) con cifras tabulares
                Text(
                    text = CurrencyFormatter.formatPrice(
                        amount = property.price,
                        sourceCurrency = property.currency
                    ),
                    style = if (compact) YvyPriceCompactCardStyle else YvyPriceCardStyle,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                // Título en una línea
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = property.title,
                        style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (property.verified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = "Propiedad verificada",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Ubicación en 12 sp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (compact) {
                            "${property.neighborhood}, ${property.city}"
                        } else {
                            "${property.neighborhood}, ${property.city} (${property.department})"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Fila de métricas con íconos Outlined fuera de cajas de color
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricBadge(
                        icon = Icons.Outlined.SquareFoot,
                        text = "${property.areaTotalM2} m²"
                    )
                    if (property.bedrooms > 0) {
                        MetricBadge(
                            icon = Icons.Outlined.Bed,
                            text = if (compact) "${property.bedrooms}d" else "${property.bedrooms} dorm."
                        )
                    }
                    if (property.bathrooms > 0) {
                        MetricBadge(
                            icon = Icons.Outlined.Bathtub,
                            text = if (compact) "${property.bathrooms}b" else "${property.bathrooms} b."
                        )
                    }
                    if (!compact && property.parkingSpaces > 0) {
                        MetricBadge(
                            icon = Icons.Outlined.DirectionsCar,
                            text = "${property.parkingSpaces} coch."
                        )
                    }
                }
            }
        }
    }
}

/**
 * Metadato técnico limpio con ícono de línea consistente (sin cajas de fondo).
 */
@Composable
fun MetricBadge(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )
    }
}
