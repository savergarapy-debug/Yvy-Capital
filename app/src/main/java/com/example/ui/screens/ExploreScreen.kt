package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FavoriteItem
import com.example.data.model.Property
import com.example.data.model.PropertyFilter
import com.example.ui.components.EmptyState
import com.example.ui.components.PropertyCard
import com.example.ui.components.YvyButton
import com.example.ui.components.YvyButtonVariant
import com.example.ui.components.YvyChip
import com.example.ui.theme.YvyMotion
import com.example.ui.theme.YvyShapes
import com.example.ui.theme.YvySpacing
import com.example.ui.util.ParaguayLocations

private val OPERATION_OPTIONS = listOf(
    "ALL" to "Todas",
    "SALE" to "Comprar",
    "RENT" to "Alquilar"
)

private val PROPERTY_TYPES = listOf(
    "ALL" to "Todos",
    "HOUSE" to "Casas",
    "APARTMENT" to "Departamentos",
    "LAND" to "Terrenos / Lotes",
    "OFFICE" to "Oficinas",
    "COMMERCIAL" to "Locales",
    "ESTANCIA" to "Quintas / Estancias"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    filteredProperties: List<Property>,
    allCatalogProperties: List<Property> = filteredProperties,
    favorites: List<FavoriteItem>,
    filter: PropertyFilter,
    @Suppress("UNUSED_PARAMETER") displayCurrency: String = "PYG",
    isGridView: Boolean,
    visibleCount: Int,
    autoOpenFilter1: Boolean = false,
    onConsumeAutoOpenFilter1: () -> Unit = {},
    isStaffAgent: (String) -> Boolean = { false },
    onUpdateFilter: ((PropertyFilter) -> PropertyFilter) -> Unit,
    onResetFilters: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onToggleViewMode: () -> Unit = {},
    onLoadMore: () -> Unit,
    onPropertyClick: (Property) -> Unit,
    onToggleFavorite: (Property) -> Unit
) {
    var showFilter1Dropdown by remember { mutableStateOf(false) }

    var draftDepartment by remember(filter.department) { mutableStateOf(filter.department) }
    var draftCity by remember(filter.city) { mutableStateOf(filter.city) }
    var draftOperation by remember(filter.operationType) { mutableStateOf(filter.operationType) }
    var draftPropertyType by remember(filter.propertyType) { mutableStateOf(filter.propertyType) }

    LaunchedEffect(autoOpenFilter1) {
        if (autoOpenFilter1) {
            showFilter1Dropdown = true
            onConsumeAutoOpenFilter1()
        }
    }

    val favoriteIds = favorites.map { it.propertyId }.toSet()
    val visibleItems = filteredProperties.take(visibleCount)

    val activeFilter1Count = remember(filter) {
        var count = 0
        if (filter.department != "Todos") count++
        if (filter.city != "Todas") count++
        if (filter.operationType != "ALL") count++
        if (filter.propertyType != "ALL") count++
        count
    }

    val hasMultipleCurrenciesInResults = remember(filteredProperties) {
        filteredProperties.map { it.currency.uppercase().trim() }.distinct().size > 1
    }

    val dynamicCitiesForDraftDept = remember(allCatalogProperties, draftDepartment) {
        allCatalogProperties
            .filter {
                draftDepartment == "Todos" ||
                    it.department.equals(draftDepartment, ignoreCase = true)
            }
            .map { it.city }
            .distinct()
    }

    val availableCities = remember(draftDepartment, dynamicCitiesForDraftDept) {
        ParaguayLocations.citiesForDepartment(draftDepartment, dynamicCitiesForDraftDept)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("explore_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Flat Header: Search Bar + Filter Button + Compact Sort Dropdown + Result Count
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = YvySpacing.ScreenHorizontal, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Search Bar (10.dp radius) + Filter Button (10.dp radius)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = YvyShapes.FieldAndButton,
                            color = MaterialTheme.colorScheme.background,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Buscar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (filter.searchQuery.isEmpty()) {
                                        Text(
                                            text = "Busca tu próxima propiedad...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    BasicTextField(
                                        value = filter.searchQuery,
                                        onValueChange = { q ->
                                            onUpdateFilter { it.copy(searchQuery = q) }
                                        },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("explore_search_input")
                                    )
                                }
                                if (filter.searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onUpdateFilter { it.copy(searchQuery = "") } },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Clear,
                                            contentDescription = "Limpiar texto",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Filter trigger button
                        val isFilterActive = showFilter1Dropdown || activeFilter1Count > 0
                        Surface(
                            modifier = Modifier
                                .height(46.dp)
                                .clip(YvyShapes.FieldAndButton)
                                .clickable {
                                    draftDepartment = filter.department
                                    draftCity = filter.city
                                    draftOperation = filter.operationType
                                    draftPropertyType = filter.propertyType
                                    showFilter1Dropdown = !showFilter1Dropdown
                                }
                                .testTag("toggle_advanced_filters_btn"),
                            shape = YvyShapes.FieldAndButton,
                            color = if (isFilterActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isFilterActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = "Filtro: Ubicación, Operación y Tipo",
                                    tint = if (isFilterActive) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Filtrar",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isFilterActive) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (activeFilter1Count > 0) {
                                    Surface(
                                        modifier = Modifier.size(18.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondary
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = activeFilter1Count.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondary
                                            )
                                        }
                                    }
                                }
                                Icon(
                                    imageVector = if (showFilter1Dropdown) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                    contentDescription = null,
                                    tint = if (isFilterActive) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Row 2: Compact Price Sorting Dropdown + Result Count
                    var showSortMenu by remember { mutableStateOf(false) }
                    val sortLabel = when (filter.sortOrder) {
                        "PRICE_ASC" -> "Ordenar: Precio: menor a mayor ▾"
                        "PRICE_DESC" -> "Ordenar: Precio: mayor a menor ▾"
                        else -> "Ordenar: Relevancia ▾"
                    }
                    val hasCustomSort = filter.sortOrder == "PRICE_ASC" || filter.sortOrder == "PRICE_DESC"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "${filteredProperties.size} propiedades disponibles",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (filter.sortOrder != "NONE" && hasMultipleCurrenciesInResults) {
                                Text(
                                    text = "Agrupadas por moneda original (₲ PYG · $ USD)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            Surface(
                                modifier = Modifier
                                    .clip(YvyShapes.Chip)
                                    .clickable { showSortMenu = true }
                                    .testTag("sort_dropdown_trigger"),
                                shape = YvyShapes.Chip,
                                color = if (hasCustomSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (hasCustomSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.SwapVert,
                                        contentDescription = null,
                                        tint = if (hasCustomSort) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = sortLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (hasCustomSort) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (hasCustomSort) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Precio: menor a mayor",
                                            fontWeight = if (filter.sortOrder == "PRICE_ASC") FontWeight.SemiBold else FontWeight.Normal,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.ArrowUpward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    trailingIcon = if (filter.sortOrder == "PRICE_ASC") {
                                        {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        onUpdateFilter { it.copy(sortOrder = "PRICE_ASC") }
                                        showSortMenu = false
                                    },
                                    modifier = Modifier.testTag("sort_price_asc_chip")
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Precio: mayor a menor",
                                            fontWeight = if (filter.sortOrder == "PRICE_DESC") FontWeight.SemiBold else FontWeight.Normal,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.ArrowDownward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    trailingIcon = if (filter.sortOrder == "PRICE_DESC") {
                                        {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        onUpdateFilter { it.copy(sortOrder = "PRICE_DESC") }
                                        showSortMenu = false
                                    },
                                    modifier = Modifier.testTag("sort_price_desc_chip")
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Restablecer",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.RestartAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = {
                                        onUpdateFilter { it.copy(sortOrder = "NONE") }
                                        showSortMenu = false
                                    },
                                    modifier = Modifier.testTag("sort_reset_option")
                                )
                            }
                        }
                    }

                    // Active Filter Pills + Clear Filters link
                    if (activeFilter1Count > 0 || filter.searchQuery.isNotBlank() || filter.sortOrder != "NONE") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (filter.department != "Todos") {
                                ActiveCriteriaPill(
                                    label = "Dpto: ${filter.department}",
                                    onClear = {
                                        onUpdateFilter { it.copy(department = "Todos", city = "Todas") }
                                    }
                                )
                            }
                            if (filter.city != "Todas") {
                                ActiveCriteriaPill(
                                    label = "Ciudad: ${filter.city}",
                                    onClear = {
                                        onUpdateFilter { it.copy(city = "Todas") }
                                    }
                                )
                            }
                            if (filter.operationType != "ALL") {
                                val opName = OPERATION_OPTIONS.firstOrNull { it.first == filter.operationType }?.second
                                    ?: filter.operationType
                                ActiveCriteriaPill(
                                    label = opName,
                                    onClear = {
                                        onUpdateFilter { it.copy(operationType = "ALL") }
                                    }
                                )
                            }
                            if (filter.propertyType != "ALL") {
                                val typeName = PROPERTY_TYPES.firstOrNull { it.first == filter.propertyType }?.second
                                    ?: filter.propertyType
                                ActiveCriteriaPill(
                                    label = typeName,
                                    onClear = {
                                        onUpdateFilter { it.copy(propertyType = "ALL") }
                                    }
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(YvyShapes.Chip)
                                    .clickable {
                                        draftDepartment = "Todos"
                                        draftCity = "Todas"
                                        draftOperation = "ALL"
                                        draftPropertyType = "ALL"
                                        onResetFilters()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("reset_filters_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.RestartAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Limpiar filtros",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

            // Results Grid / List
            if (filteredProperties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(YvySpacing.ScreenHorizontal),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        title = "No encontramos propiedades con esos criterios",
                        description = "Prueba seleccionando otro departamento, ciudad, operación o tipo de propiedad.",
                        icon = Icons.Outlined.SearchOff,
                        actionLabel = "Restablecer filtros",
                        onActionClick = onResetFilters
                    )
                }
            } else if (!isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    contentPadding = PaddingValues(
                        horizontal = YvySpacing.ScreenHorizontal,
                        vertical = 14.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibleItems, key = { "grid_${it.id}" }) { prop ->
                        PropertyCard(
                            property = prop,
                            displayCurrency = prop.currency,
                            isFavorite = favoriteIds.contains(prop.id),
                            isStaff = isStaffAgent(prop.agentId),
                            onCardClick = { onPropertyClick(prop) },
                            onFavoriteClick = { onToggleFavorite(prop) },
                            compact = true
                        )
                    }

                    if (visibleCount < filteredProperties.size) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                YvyButton(
                                    text = "Cargar más (${filteredProperties.size - visibleCount} restantes)",
                                    onClick = onLoadMore,
                                    variant = YvyButtonVariant.SECONDARY_OUTLINED,
                                    testTag = "load_more_btn"
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = YvySpacing.ScreenHorizontal,
                        vertical = 14.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibleItems, key = { "list_${it.id}" }) { prop ->
                        PropertyCard(
                            property = prop,
                            displayCurrency = prop.currency,
                            isFavorite = favoriteIds.contains(prop.id),
                            isStaff = isStaffAgent(prop.agentId),
                            onCardClick = { onPropertyClick(prop) },
                            onFavoriteClick = { onToggleFavorite(prop) },
                            compact = false
                        )
                    }

                    if (visibleCount < filteredProperties.size) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                YvyButton(
                                    text = "Cargar más (${filteredProperties.size - visibleCount} restantes)",
                                    onClick = onLoadMore,
                                    variant = YvyButtonVariant.SECONDARY_OUTLINED,
                                    testTag = "load_more_btn"
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scrim when anchored dropdown is open
        if (showFilter1Dropdown) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 74.dp)
                    .background(Color.Black.copy(alpha = 0.22f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showFilter1Dropdown = false }
                    )
            )
        }

        // ANCHORED DROPDOWN / EXTENSION PANEL FOR FILTER 1 (subtle 200ms motion, 12.dp card, 1.dp border)
        AnimatedVisibility(
            visible = showFilter1Dropdown,
            enter = fadeIn(tween(YvyMotion.DurationMedium)) + expandVertically(tween(YvyMotion.DurationMedium)),
            exit = fadeOut(tween(YvyMotion.DurationFast)) + shrinkVertically(tween(YvyMotion.DurationFast)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp, start = YvySpacing.ScreenHorizontal, end = YvySpacing.ScreenHorizontal)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .testTag("explore_filter_1_panel"),
                shape = YvyShapes.Card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ubicación, operación y tipo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { showFilter1Dropdown = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Cerrar panel de filtros",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Operación inmobiliaria
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "1. Operación inmobiliaria",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OPERATION_OPTIONS.forEach { (code, label) ->
                                    val selected = draftOperation == code
                                    YvyChip(
                                        label = label,
                                        selected = selected,
                                        onClick = {
                                            draftOperation = code
                                            onUpdateFilter { it.copy(operationType = code) }
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Departamento o Capital (Paraguay)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "2. Departamento o Capital",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ParaguayLocations.departmentsWithAll.forEach { dept ->
                                    val selected = draftDepartment.equals(dept, ignoreCase = true)
                                    YvyChip(
                                        label = dept,
                                        selected = selected,
                                        onClick = {
                                            draftDepartment = dept
                                            draftCity = "Todas"
                                            onUpdateFilter {
                                                it.copy(department = dept, city = "Todas")
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // 3. Ciudad o Distrito
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (draftDepartment == "Todos") {
                                    "3. Ciudad o distrito"
                                } else {
                                    "3. Ciudad o distrito en $draftDepartment"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableCities.forEach { cityOption ->
                                    val selected = draftCity.equals(cityOption, ignoreCase = true)
                                    YvyChip(
                                        label = cityOption,
                                        selected = selected,
                                        onClick = {
                                            draftCity = cityOption
                                            onUpdateFilter { it.copy(city = cityOption) }
                                        }
                                    )
                                }
                            }
                        }

                        // 4. Tipo de propiedad
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "4. Tipo de propiedad",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PROPERTY_TYPES.forEach { (code, label) ->
                                    val selected = draftPropertyType == code
                                    YvyChip(
                                        label = label,
                                        selected = selected,
                                        onClick = {
                                            draftPropertyType = code
                                            onUpdateFilter { it.copy(propertyType = code) }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        YvyButton(
                            text = "Restablecer",
                            onClick = {
                                draftDepartment = "Todos"
                                draftCity = "Todas"
                                draftOperation = "ALL"
                                draftPropertyType = "ALL"
                                onResetFilters()
                            },
                            variant = YvyButtonVariant.SECONDARY_OUTLINED
                        )

                        YvyButton(
                            text = "Ver ${filteredProperties.size} resultados",
                            onClick = {
                                onUpdateFilter {
                                    it.copy(
                                        department = draftDepartment,
                                        city = draftCity,
                                        operationType = draftOperation,
                                        propertyType = draftPropertyType
                                    )
                                }
                                showFilter1Dropdown = false
                            },
                            variant = YvyButtonVariant.PRIMARY,
                            testTag = "apply_filter_1_btn"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveCriteriaPill(
    label: String,
    onClear: () -> Unit
) {
    Surface(
        shape = YvyShapes.Chip,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClear)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Quitar filtro $label",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
