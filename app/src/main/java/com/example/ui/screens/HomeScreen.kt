package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.RealEstateAgent
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Villa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FavoriteItem
import com.example.data.model.Property
import com.example.ui.components.PropertyCard
import com.example.ui.theme.YvyBeigeWarm
import com.example.ui.theme.YvyForestDeep
import com.example.ui.theme.YvyForestMedium
import com.example.ui.theme.YvyForestPrimary
import com.example.ui.theme.YvyGoldAccent
import com.example.ui.viewmodel.UiState

@Composable
fun HomeScreen(
    propertiesState: UiState<List<Property>>,
    favorites: List<FavoriteItem>,
    displayCurrency: String,
    onToggleCurrency: (String) -> Unit,
    onQuickOperationSelect: (String) -> Unit,
    onQuickCategorySelect: (String) -> Unit,
    onNavigateToExplore: () -> Unit,
    onOpenExploreFilter: () -> Unit = onNavigateToExplore,
    onNavigateToFavorites: () -> Unit,
    onNavigateToPro: () -> Unit,
    onPropertyClick: (Property) -> Unit,
    onToggleFavorite: (Property) -> Unit,
    onReloadDemo: () -> Unit,
    onOpenDeepLinkCenter: () -> Unit = {}
) {
    val favoriteIds = favorites.map { it.propertyId }.toSet()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Header con interruptor deslizante ₲ / $, buscador integrado con filtro y 3 accesos rápidos
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(YvyForestDeep, YvyForestPrimary, YvyForestMedium)
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Yvy Capital",
                                style = MaterialTheme.typography.headlineMedium,
                                color = YvyBeigeWarm,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Inversiones & Bienes Raíces",
                                style = MaterialTheme.typography.labelLarge,
                                color = YvyGoldAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Compact Sliding Currency Switch (₲ / $)
                        CurrencySlidingSwitch(
                            selectedCurrency = displayCurrency,
                            onSelectCurrency = onToggleCurrency
                        )
                    }

                    Text(
                        text = "Encuentra tu próximo hogar, oficina o inversión en cualquier parte del Paraguay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = YvyBeigeWarm.copy(alpha = 0.92f)
                    )

                    // Unified Search Bar + Filter Button Module
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = YvyBeigeWarm,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Search Trigger Area
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(onClick = onNavigateToExplore)
                                    .padding(horizontal = 16.dp)
                                    .testTag("home_search_bar"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar tu próxima propiedad",
                                    tint = YvyForestPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Buscar tu próxima propiedad",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = YvyForestDeep,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Subtle Vertical Divider Line
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(YvyForestDeep.copy(alpha = 0.16f))
                            )

                            // Integrated Filter Button (min 48.dp touch target)
                            Box(
                                modifier = Modifier
                                    .width(54.dp)
                                    .fillMaxHeight()
                                    .clickable(
                                        role = Role.Button,
                                        onClick = onOpenExploreFilter
                                    )
                                    .testTag("home_filter_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(YvyForestPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filtrar propiedades",
                                        tint = YvyForestPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3 Quick Access Buttons: Comprar, Alquilar, Favoritos (Removed "Enlace")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            label = "Comprar",
                            subtitle = "En venta",
                            icon = Icons.Default.RealEstateAgent,
                            onClick = { onQuickOperationSelect("SALE") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_buy_btn")
                        )
                        QuickActionButton(
                            label = "Alquilar",
                            subtitle = "En renta",
                            icon = Icons.Default.Key,
                            onClick = { onQuickOperationSelect("RENT") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_rent_btn")
                        )
                        QuickActionButton(
                            label = "Favoritos",
                            subtitle = "${favorites.size} guardados",
                            icon = Icons.Default.Favorite,
                            onClick = onNavigateToFavorites,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_favorites_btn")
                        )
                    }
                }
            }
        }

        // 2. Categorías Inmobiliarias (Rediseñadas con estética moderna y limpia)
        item {
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categorías Inmobiliarias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Explorar catálogo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onNavigateToExplore)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryChip("Casas", Icons.Default.Home) { onQuickCategorySelect("HOUSE") }
                    CategoryChip("Departamentos", Icons.Default.Apartment) { onQuickCategorySelect("APARTMENT") }
                    CategoryChip("Terrenos / Lotes", Icons.Default.Landscape) { onQuickCategorySelect("LAND") }
                    CategoryChip("Oficinas", Icons.Default.Business) { onQuickCategorySelect("OFFICE") }
                    CategoryChip("Locales", Icons.Default.Storefront) { onQuickCategorySelect("COMMERCIAL") }
                    CategoryChip("Quintas / Estancias", Icons.Default.Villa) { onQuickCategorySelect("ESTANCIA") }
                }
            }
        }

        // 3. Estado del Catálogo: Propiedades Destacadas y Últimas Publicaciones
        when (propertiesState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Cargando propiedades verificadas en Paraguay...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            is UiState.Error -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "No se pudo sincronizar el catálogo",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = propertiesState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onReloadDemo) {
                                Text("Reintentar conexión")
                            }
                        }
                    }
                }
            }

            is UiState.Success -> {
                val allProperties = propertiesState.data
                val featuredList = allProperties.filter { it.featured }.ifEmpty { allProperties.take(4) }

                if (allProperties.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "El catálogo está listo para inicializar",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Carga las propiedades iniciales de referencia en distintos puntos del Paraguay.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onReloadDemo,
                                    modifier = Modifier.testTag("seed_demo_catalog_btn")
                                ) {
                                    Text("Cargar Catálogo Inicial de Prueba")
                                }
                            }
                        }
                    }
                } else {
                    // Propiedades Destacadas (Carrusel Horizontal Compacto)
                    item {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Propiedades Destacadas",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Selección Premium de Propiedades",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "Ver todas",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(onClick = onNavigateToExplore)
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 18.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(featuredList, key = { "featured_${it.id}" }) { prop ->
                                    PropertyCard(
                                        property = prop,
                                        displayCurrency = displayCurrency,
                                        isFavorite = favoriteIds.contains(prop.id),
                                        onCardClick = { onPropertyClick(prop) },
                                        onFavoriteClick = { onToggleFavorite(prop) },
                                        compact = true,
                                        modifier = Modifier.width(236.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Últimas Publicaciones (Cuadrícula compacta de 2 columnas para visualizar >= 2 propiedades simultáneamente)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Últimas Publicaciones",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Catálogo actualizado en todo el Paraguay",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    val pairedProperties = allProperties.chunked(2)
                    items(
                        items = pairedProperties,
                        key = { pair -> "latest_row_${pair.first().id}" }
                    ) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { prop ->
                                PropertyCard(
                                    property = prop,
                                    displayCurrency = displayCurrency,
                                    isFavorite = favoriteIds.contains(prop.id),
                                    onCardClick = { onPropertyClick(prop) },
                                    onFavoriteClick = { onToggleFavorite(prop) },
                                    compact = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 4. Banner Yvy Capital Pro & Contacto Corporativo
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = YvyForestPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Yvy Capital Pro",
                            tint = YvyGoldAccent,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Yvy Capital Pro • Asesores & Desarrolladoras",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "¿Eres asesor inmobiliario o propietario en Paraguay? Publica inmuebles, recibe solicitudes de visita verificadas y gestiona tu cartera desde Yvy Capital Pro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = YvyBeigeWarm.copy(alpha = 0.88f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Button(
                            onClick = onNavigateToPro,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YvyGoldAccent,
                                contentColor = YvyForestDeep
                            ),
                            modifier = Modifier.testTag("home_pro_portal_btn")
                        ) {
                            Text("Abrir Espacio Asesor Pro", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onOpenDeepLinkCenter,
                            modifier = Modifier.testTag("home_deeplink_center_btn")
                        ) {
                            Text("Abrir Enlace Directo", color = YvyBeigeWarm)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interruptor deslizante moderno y minimalista para alternar entre Guaraníes (₲) y Dólares ($).
 */
@Composable
private fun CurrencySlidingSwitch(
    selectedCurrency: String,
    onSelectCurrency: (String) -> Unit
) {
    val isUsd = selectedCurrency.uppercase() == "USD"
    val indicatorOffset by animateDpAsState(
        targetValue = if (isUsd) 42.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "currency_slide_offset"
    )

    val pygTextColor by animateColorAsState(
        targetValue = if (!isUsd) YvyForestDeep else Color.White.copy(alpha = 0.78f),
        label = "pyg_text_color"
    )
    val usdTextColor by animateColorAsState(
        targetValue = if (isUsd) YvyForestDeep else Color.White.copy(alpha = 0.78f),
        label = "usd_text_color"
    )

    Box(
        modifier = Modifier
            .width(88.dp)
            .height(38.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
                onClick = {
                    val next = if (isUsd) "PYG" else "USD"
                    onSelectCurrency(next)
                }
            )
            .padding(3.dp)
            .testTag("currency_toggle_chip")
    ) {
        // Animated Sliding Pill
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(40.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(YvyGoldAccent)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { if (isUsd) onSelectCurrency("PYG") }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₲",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = pygTextColor
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { if (!isUsd) onSelectCurrency("USD") }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = usdTextColor
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.13f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(YvyGoldAccent.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = YvyGoldAccent,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(7.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = YvyBeigeWarm.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
