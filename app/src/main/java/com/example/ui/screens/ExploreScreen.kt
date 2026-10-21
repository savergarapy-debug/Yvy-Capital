package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.components.PropertyCard
import com.example.ui.theme.YvyForestDeep
import com.example.ui.theme.YvyForestPrimary
import com.example.ui.theme.YvyGoldAccent
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
    onUpdateFilter: ((PropertyFilter) -> PropertyFilter) -> Unit,
    onResetFilters: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onToggleViewMode: () -> Unit = {},
    onLoadMore: () -> Unit,
    onPropertyClick: (Property) -> Unit,
    onToggleFavorite: (Property) -> Unit
) {
    var showFilter1Dropdown by remember { mutableStateOf(false) }

    // Draft state inside Filter 1 panel
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

    // Count active criteria in Filter 1
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

    // Dynamic cities present in Firestore catalog for the selected draftDepartment
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
            .testTag("explore_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Compact Header: Search Bar + Single Filter Button (Filtro 1) + Price Sorting Bar + Result Count
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Compact Search Bar + Filter 1 (Second filter removed per Requirement #7)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = MaterialTheme.colorScheme.primary,
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
                                            style = MaterialTheme.typography.bodySmall,
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
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpiar texto",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // BOTÓN DE FILTRO PRINCIPAL: Ubicación, Operación y Tipo de Propiedad
                        Surface(
                            modifier = Modifier
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    draftDepartment = filter.department
                                    draftCity = filter.city
                                    draftOperation = filter.operationType
                                    draftPropertyType = filter.propertyType
                                    showFilter1Dropdown = !showFilter1Dropdown
                                }
                                .testTag("toggle_advanced_filters_btn"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (showFilter1Dropdown || activeFilter1Count > 0) {
                                YvyForestPrimary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (showFilter1Dropdown || activeFilter1Count > 0) {
                                    YvyForestPrimary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
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
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filtro: Ubicación, Operación y Tipo",
                                    tint = if (showFilter1Dropdown || activeFilter1Count > 0) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Filtrar",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showFilter1Dropdown || activeFilter1Count > 0) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (activeFilter1Count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(YvyGoldAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = activeFilter1Count.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = YvyForestDeep
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (showFilter1Dropdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = if (showFilter1Dropdown || activeFilter1Count > 0) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Row 2: Price Sorting Selector (Below search & filters, Above "Nro. Propiedades disponibles")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Ordenar:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val isAsc = filter.sortOrder == "PRICE_ASC"
                        val isDesc = filter.sortOrder == "PRICE_DESC"

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val nextOrder = if (isAsc) "NONE" else "PRICE_ASC"
                                    onUpdateFilter { it.copy(sortOrder = nextOrder) }
                                }
                                .testTag("sort_price_asc_chip"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAsc) YvyForestPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isAsc) YvyForestPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isAsc) YvyGoldAccent else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Precio: menor a mayor",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isAsc) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAsc) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    val nextOrder = if (isDesc) "NONE" else "PRICE_DESC"
                                    onUpdateFilter { it.copy(sortOrder = nextOrder) }
                                }
                                .testTag("sort_price_desc_chip"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDesc) YvyForestPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isDesc) YvyForestPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (isDesc) YvyGoldAccent else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Precio: mayor a menor",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isDesc) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isDesc) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Row 3: Result Count ("Nro. Propiedades disponibles") + Clear Filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${filteredProperties.size} propiedades disponibles",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (filter.sortOrder != "NONE" && hasMultipleCurrenciesInResults) {
                                Text(
                                    text = "Agrupadas por moneda original (₲ PYG • $ USD)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (activeFilter1Count > 0 || filter.searchQuery.isNotBlank() || filter.sortOrder != "NONE") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        draftDepartment = "Todos"
                                        draftCity = "Todas"
                                        draftOperation = "ALL"
                                        draftPropertyType = "ALL"
                                        onResetFilters()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("reset_filters_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Limpiar filtros",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Active Filter Pills Row
                    if (activeFilter1Count > 0) {
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
                        }
                    }
                }
            }

            // Results Grid (Uniform fixed-size cards)
            if (filteredProperties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "No encontramos propiedades con esos criterios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Prueba seleccionando otro departamento, ciudad, operación o tipo de propiedad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onResetFilters) {
                            Text("Restablecer filtros")
                        }
                    }
                }
            } else if (!isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibleItems, key = { "grid_${it.id}" }) { prop ->
                        PropertyCard(
                            property = prop,
                            displayCurrency = prop.currency,
                            isFavorite = favoriteIds.contains(prop.id),
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
                                OutlinedButton(
                                    onClick = onLoadMore,
                                    modifier = Modifier.testTag("load_more_btn")
                                ) {
                                    Text("Cargar más (${filteredProperties.size - visibleCount} restantes)")
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visibleItems, key = { "list_${it.id}" }) { prop ->
                        PropertyCard(
                            property = prop,
                            displayCurrency = prop.currency,
                            isFavorite = favoriteIds.contains(prop.id),
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
                                OutlinedButton(
                                    onClick = onLoadMore,
                                    modifier = Modifier.testTag("load_more_btn")
                                ) {
                                    Text("Cargar más (${filteredProperties.size - visibleCount} restantes)")
                                }
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
                        onClick = {
                            showFilter1Dropdown = false
                        }
                    )
            )
        }

        // ANCHORED DROPDOWN / EXTENSION PANEL FOR FILTER 1
        AnimatedVisibility(
            visible = showFilter1Dropdown,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp, start = 14.dp, end = 14.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 430.dp)
                    .testTag("explore_filter_1_panel"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
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
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = YvyForestPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ubicación, Operación y Tipo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { showFilter1Dropdown = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar panel de filtros",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Operación Inmobiliaria
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "1. Operación Inmobiliaria",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OPERATION_OPTIONS.forEach { (code, label) ->
                                    val selected = draftOperation == code
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            draftOperation = code
                                            onUpdateFilter { it.copy(operationType = code) }
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        leadingIcon = if (selected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = YvyForestPrimary,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = YvyGoldAccent
                                        )
                                    )
                                }
                            }
                        }

                        // 2. Ubicación en Paraguay — Departamento o Capital
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "2. Departamento o Capital (Paraguay)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Selecciona Asunción o cualquiera de los 17 departamentos:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ParaguayLocations.departmentsWithAll.forEach { dept ->
                                    val selected = draftDepartment.equals(dept, ignoreCase = true)
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            draftDepartment = dept
                                            draftCity = "Todas"
                                            onUpdateFilter {
                                                it.copy(department = dept, city = "Todas")
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = dept,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = YvyForestPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        // 3. Ciudad / Distrito
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (draftDepartment == "Todos") {
                                    "3. Ciudad o Distrito (Principales de Paraguay)"
                                } else {
                                    "3. Ciudad o Distrito en $draftDepartment"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableCities.forEach { cityOption ->
                                    val selected = draftCity.equals(cityOption, ignoreCase = true)
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            draftCity = cityOption
                                            onUpdateFilter { it.copy(city = cityOption) }
                                        },
                                        label = {
                                            Text(
                                                text = cityOption,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = YvyGoldAccent,
                                            selectedLabelColor = YvyForestDeep
                                        )
                                    )
                                }
                            }
                        }

                        // 4. Tipo de Propiedad
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "4. Tipo de Propiedad",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PROPERTY_TYPES.forEach { (code, label) ->
                                    val selected = draftPropertyType == code
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            draftPropertyType = code
                                            onUpdateFilter { it.copy(propertyType = code) }
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = YvyForestPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                draftDepartment = "Todos"
                                draftCity = "Todas"
                                draftOperation = "ALL"
                                draftPropertyType = "ALL"
                                onResetFilters()
                            }
                        ) {
                            Text("Restablecer")
                        }

                        Button(
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YvyForestPrimary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("apply_filter_1_btn")
                        ) {
                            Text("Ver ${filteredProperties.size} resultados", fontWeight = FontWeight.Bold)
                        }
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
        shape = RoundedCornerShape(10.dp),
        color = YvyForestPrimary.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, YvyForestPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClear)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Quitar filtro $label",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
