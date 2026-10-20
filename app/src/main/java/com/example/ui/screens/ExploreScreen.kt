package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FavoriteItem
import com.example.data.model.Property
import com.example.data.model.PropertyFilter
import com.example.ui.components.PropertyCard

private val PARAGUAY_DEPARTMENTS = listOf(
    "Todos", "Asunción", "Central", "Cordillera", "Alto Paraná", "Itapúa"
)

private val OPERATION_OPTIONS = listOf(
    "ALL" to "Todos",
    "SALE" to "Comprar",
    "RENT" to "Alquilar"
)

private val PROPERTY_TYPES = listOf(
    "ALL" to "Todos los tipos",
    "HOUSE" to "Casas",
    "APARTMENT" to "Departamentos",
    "LAND" to "Terrenos",
    "OFFICE" to "Oficinas",
    "COMMERCIAL" to "Locales",
    "ESTANCIA" to "Estancias"
)

@Composable
fun ExploreScreen(
    filteredProperties: List<Property>,
    favorites: List<FavoriteItem>,
    filter: PropertyFilter,
    displayCurrency: String,
    isGridView: Boolean,
    visibleCount: Int,
    onUpdateFilter: ((PropertyFilter) -> PropertyFilter) -> Unit,
    onResetFilters: () -> Unit,
    onToggleViewMode: () -> Unit,
    onLoadMore: () -> Unit,
    onPropertyClick: (Property) -> Unit,
    onToggleFavorite: (Property) -> Unit
) {
    var showAdvancedFilters by remember { mutableStateOf(false) }
    val favoriteIds = favorites.map { it.propertyId }.toSet()
    val visibleItems = filteredProperties.take(visibleCount)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("explore_screen")
    ) {
        // Search & Filter Bar Header
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = filter.searchQuery,
                        onValueChange = { q -> onUpdateFilter { it.copy(searchQuery = q) } },
                        placeholder = { Text("Ciudad, barrio, título (ej. Villa Morra, Luque)...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        },
                        trailingIcon = {
                            if (filter.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onUpdateFilter { it.copy(searchQuery = "") } }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("explore_search_input")
                    )

                    IconButton(
                        onClick = { showAdvancedFilters = !showAdvancedFilters },
                        modifier = Modifier.testTag("toggle_advanced_filters_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros avanzados",
                            tint = if (showAdvancedFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onToggleViewMode,
                        modifier = Modifier.testTag("toggle_view_mode_btn")
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewAgenda else Icons.Default.GridView,
                            contentDescription = if (isGridView) "Vista en lista" else "Vista en cuadrícula"
                        )
                    }
                }

                // Quick Operation & Department Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OPERATION_OPTIONS.forEach { (code, label) ->
                        FilterChip(
                            selected = filter.operationType == code,
                            onClick = { onUpdateFilter { it.copy(operationType = code) } },
                            label = { Text(label) }
                        )
                    }
                    PARAGUAY_DEPARTMENTS.forEach { dept ->
                        FilterChip(
                            selected = filter.department == dept,
                            onClick = { onUpdateFilter { it.copy(department = dept) } },
                            label = { Text(dept) }
                        )
                    }
                }

                // Expandable Advanced Filters Panel
                AnimatedVisibility(visible = showAdvancedFilters) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Tipo de Inmueble",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PROPERTY_TYPES.forEach { (code, label) ->
                                    FilterChip(
                                        selected = filter.propertyType == code,
                                        onClick = { onUpdateFilter { it.copy(propertyType = code) } },
                                        label = { Text(label) }
                                    )
                                }
                            }

                            Text(
                                text = "Dormitorios, Baños y Cocheras Mínimas",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(0, 1, 2, 3, 4).forEach { beds ->
                                    FilterChip(
                                        selected = filter.minBedrooms == beds,
                                        onClick = { onUpdateFilter { it.copy(minBedrooms = beds) } },
                                        label = { Text(if (beds == 0) "Dorm: Todos" else "${beds}+ dorm.") }
                                    )
                                }
                                listOf(0, 1, 2, 3).forEach { parking ->
                                    FilterChip(
                                        selected = filter.minParking == parking,
                                        onClick = { onUpdateFilter { it.copy(minParking = parking) } },
                                        label = { Text(if (parking == 0) "Cochera: Todas" else "${parking}+ coch.") }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = filter.onlyFeatured,
                                    onClick = { onUpdateFilter { it.copy(onlyFeatured = !it.onlyFeatured) } },
                                    label = { Text("Solo Destacadas") }
                                )

                                OutlinedButton(
                                    onClick = onResetFilters,
                                    modifier = Modifier.testTag("reset_filters_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restablecer filtros")
                                }
                            }
                        }
                    }
                }

                // Result summary row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredProperties.size} propiedades encontradas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (filter != PropertyFilter()) {
                        Text(
                            text = "Filtros activos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (filteredProperties.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "No encontramos propiedades con esos filtros",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Prueba ampliando la búsqueda a otros barrios o restablece los filtros activos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onResetFilters) {
                        Text("Restablecer todos los filtros")
                    }
                }
            }
        } else if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 165.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visibleItems, key = { "grid_${it.id}" }) { prop ->
                    PropertyCard(
                        property = prop,
                        displayCurrency = displayCurrency,
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
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = onLoadMore,
                                modifier = Modifier.testTag("load_more_btn")
                            ) {
                                Text("Cargar más propiedades (${filteredProperties.size - visibleCount} restantes)")
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visibleItems, key = { "list_${it.id}" }) { prop ->
                    PropertyCard(
                        property = prop,
                        displayCurrency = displayCurrency,
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
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = onLoadMore,
                                modifier = Modifier.testTag("load_more_btn")
                            ) {
                                Text("Cargar más propiedades (${filteredProperties.size - visibleCount} restantes)")
                            }
                        }
                    }
                }
            }
        }
    }
}
