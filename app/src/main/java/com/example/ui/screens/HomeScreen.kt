package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.RealEstateAgent
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Villa
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FavoriteItem
import com.example.data.model.Property
import com.example.ui.components.EmptyState
import com.example.ui.components.PropertyCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusBadgeTone
import com.example.ui.components.YvyButton
import com.example.ui.components.YvyButtonVariant
import com.example.ui.components.YvyChip
import com.example.ui.theme.YvyEditorialSerifFamily
import com.example.ui.theme.YvyShapes
import com.example.ui.theme.YvySpacing
import com.example.ui.viewmodel.UiState

@Composable
fun HomeScreen(
    propertiesState: UiState<List<Property>>,
    favorites: List<FavoriteItem>,
    @Suppress("UNUSED_PARAMETER") displayCurrency: String = "PYG",
    @Suppress("UNUSED_PARAMETER") onToggleCurrency: (String) -> Unit = {},
    isStaffAgent: (String) -> Boolean = { false },
    onSearchSubmit: (String) -> Unit = {},
    onQuickOperationSelect: (String) -> Unit,
    onQuickCategorySelect: (String) -> Unit,
    onNavigateToExplore: () -> Unit,
    onOpenExploreFilter: () -> Unit = onNavigateToExplore,
    onNavigateToFavorites: () -> Unit,
    onNavigateToPro: () -> Unit,
    onPropertyClick: (Property) -> Unit,
    onToggleFavorite: (Property) -> Unit,
    onReloadDemo: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onOpenDeepLinkCenter: () -> Unit = {}
) {
    val favoriteIds = favorites.map { it.propertyId }.toSet()
    var homeSearchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(YvySpacing.SectionGap)
    ) {
        // 1. Editorial Header on Warm Sand (#F7F4EE) — no gradients, no glows
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = YvySpacing.ScreenHorizontal,
                        end = YvySpacing.ScreenHorizontal,
                        top = 20.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Yvy Capital",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = YvyEditorialSerifFamily
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Inversiones & Bienes Raíces",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp
                    )
                }

                Text(
                    text = "Encuentra tu próximo hogar, oficina o inversión en cualquier parte del Paraguay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Flat Search Bar + Filter Button Module (10.dp radius, 1.dp #E6E1D8 border)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = YvyShapes.FieldAndButton,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    tonalElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    if (homeSearchQuery.isBlank()) {
                                        onNavigateToExplore()
                                    } else {
                                        onSearchSubmit(homeSearchQuery)
                                    }
                                }
                                .padding(horizontal = 14.dp)
                                .testTag("home_search_bar"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Buscar propiedades",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable {
                                        if (homeSearchQuery.isNotBlank()) {
                                            onSearchSubmit(homeSearchQuery)
                                        } else {
                                            onNavigateToExplore()
                                        }
                                    }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (homeSearchQuery.isEmpty()) {
                                    Text(
                                        text = "Busca tu próxima propiedad...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = homeSearchQuery,
                                    onValueChange = { homeSearchQuery = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            onSearchSubmit(homeSearchQuery)
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )

                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .fillMaxHeight()
                                .clickable(
                                    role = Role.Button,
                                    onClick = onOpenExploreFilter
                                )
                                .testTag("home_filter_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = "Filtrar propiedades",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 3 Quick Access Chips: Comprar, Alquilar, Favoritos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    YvyChip(
                        label = "Comprar",
                        selected = false,
                        onClick = { onQuickOperationSelect("SALE") },
                        leadingIcon = Icons.Outlined.RealEstateAgent,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_buy_btn"
                    )
                    YvyChip(
                        label = "Alquilar",
                        selected = false,
                        onClick = { onQuickOperationSelect("RENT") },
                        leadingIcon = Icons.Outlined.Key,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_rent_btn"
                    )
                    YvyChip(
                        label = "Favoritos (${favorites.size})",
                        selected = false,
                        onClick = onNavigateToFavorites,
                        leadingIcon = Icons.Outlined.FavoriteBorder,
                        modifier = Modifier.weight(1f),
                        testTag = "quick_favorites_btn"
                    )
                }
            }
        }

        // 2. Categorías inmobiliarias (sentence case, no filler subtitle, Outlined icons via YvyChip)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    title = "Categorías inmobiliarias",
                    modifier = Modifier.padding(horizontal = YvySpacing.ScreenHorizontal)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = YvySpacing.ScreenHorizontal),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    YvyChip("Casas", false, { onQuickCategorySelect("HOUSE") }, leadingIcon = Icons.Outlined.Home)
                    YvyChip("Departamentos", false, { onQuickCategorySelect("APARTMENT") }, leadingIcon = Icons.Outlined.Apartment)
                    YvyChip("Terrenos / Lotes", false, { onQuickCategorySelect("LAND") }, leadingIcon = Icons.Outlined.Landscape)
                    YvyChip("Oficinas", false, { onQuickCategorySelect("OFFICE") }, leadingIcon = Icons.Outlined.Business)
                    YvyChip("Locales", false, { onQuickCategorySelect("COMMERCIAL") }, leadingIcon = Icons.Outlined.Storefront)
                    YvyChip("Quintas / Estancias", false, { onQuickCategorySelect("ESTANCIA") }, leadingIcon = Icons.Outlined.Villa)
                }
            }
        }

        // 3. Estado del Catálogo: Propiedades destacadas y Últimas publicaciones
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
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            is UiState.Error -> {
                item {
                    Box(modifier = Modifier.padding(horizontal = YvySpacing.ScreenHorizontal)) {
                        EmptyState(
                            title = "No se pudo sincronizar el catálogo",
                            description = propertiesState.message,
                            icon = Icons.Outlined.Domain,
                            actionLabel = "Reintentar conexión",
                            onActionClick = onReloadDemo
                        )
                    }
                }
            }

            is UiState.Success -> {
                val allProperties = propertiesState.data
                val featuredList = allProperties.filter { it.featured }.ifEmpty { allProperties.take(4) }

                if (allProperties.isEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = YvySpacing.ScreenHorizontal)) {
                            EmptyState(
                                title = "El catálogo está listo para inicializar",
                                description = "Carga las propiedades iniciales de referencia en distintos puntos del Paraguay.",
                                icon = Icons.Outlined.Domain,
                                actionLabel = "Cargar Catálogo Inicial de Prueba",
                                onActionClick = onReloadDemo,
                                actionTestTag = "seed_demo_catalog_btn"
                            )
                        }
                    }
                } else {
                    // Propiedades destacadas (sentence case, no filler subtitle)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionHeader(
                                title = "Propiedades destacadas",
                                actionLabel = "Ver todas",
                                onActionClick = onNavigateToExplore,
                                modifier = Modifier.padding(horizontal = YvySpacing.ScreenHorizontal)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = YvySpacing.ScreenHorizontal),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(featuredList, key = { "featured_${it.id}" }) { prop ->
                                    PropertyCard(
                                        property = prop,
                                        displayCurrency = prop.currency,
                                        isFavorite = favoriteIds.contains(prop.id),
                                        isStaff = isStaffAgent(prop.agentId),
                                        onCardClick = { onPropertyClick(prop) },
                                        onFavoriteClick = { onToggleFavorite(prop) },
                                        compact = false,
                                        modifier = Modifier.width(276.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Últimas publicaciones (sentence case, no filler subtitle)
                    item {
                        SectionHeader(
                            title = "Últimas publicaciones",
                            modifier = Modifier.padding(horizontal = YvySpacing.ScreenHorizontal)
                        )
                    }

                    val pairedProperties = allProperties.chunked(2)
                    items(
                        items = pairedProperties,
                        key = { pair -> "latest_row_${pair.first().id}" }
                    ) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = YvySpacing.ScreenHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { prop ->
                                PropertyCard(
                                    property = prop,
                                    displayCurrency = prop.currency,
                                    isFavorite = favoriteIds.contains(prop.id),
                                    isStaff = isStaffAgent(prop.agentId),
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

        // 4. Sección Promocional Yvy Capital Pro (flat surface card, gold badge, YvyButton)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = YvySpacing.ScreenHorizontal),
                shape = YvyShapes.Card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusBadge(
                        text = "Yvy Capital Pro",
                        tone = StatusBadgeTone.PRO_GOLD
                    )
                    Text(
                        text = "Yvy Capital Pro Asesores e Inmobiliarias",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "¿Eres asesor inmobiliario o propietario en Paraguay? Publica inmuebles, recibe solicitudes de visita verificadas y gestiona tu cartera desde Yvy Capital Pro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    YvyButton(
                        text = "Vuélvete Pro",
                        onClick = onNavigateToPro,
                        variant = YvyButtonVariant.PRIMARY,
                        leadingIcon = Icons.Outlined.WorkspacePremium,
                        testTag = "home_pro_portal_btn"
                    )
                }
            }
        }
    }
}
