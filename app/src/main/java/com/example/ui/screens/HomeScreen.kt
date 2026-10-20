package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Villa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
    onNavigateToFavorites: () -> Unit,
    onNavigateToPro: () -> Unit,
    onPropertyClick: (Property) -> Unit,
    onToggleFavorite: (Property) -> Unit,
    onReloadDemo: () -> Unit
) {
    val favoriteIds = favorites.map { it.propertyId }.toSet()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Hero Header con buscador destacado y selector de moneda PYG / USD
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(YvyForestDeep, YvyForestPrimary, YvyForestMedium)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Yvy Capital",
                                style = MaterialTheme.typography.headlineLarge,
                                color = YvyBeigeWarm
                            )
                            Text(
                                text = "Inversiones & Raíces en Paraguay",
                                style = MaterialTheme.typography.bodySmall,
                                color = YvyGoldAccent
                            )
                        }

                        // Currency Toggle Pill (PYG / USD)
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier
                                .clickable {
                                    val next = if (displayCurrency == "PYG") "USD" else "PYG"
                                    onToggleCurrency(next)
                                }
                                .testTag("currency_toggle_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyExchange,
                                    contentDescription = "Cambiar moneda",
                                    tint = YvyGoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (displayCurrency == "PYG") "Moneda: PYG (Gs.)" else "Moneda: USD ($)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Encuentra tu próximo hogar, oficina o inversión en Asunción, Central y todo el Paraguay.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = YvyBeigeWarm.copy(alpha = 0.9f)
                    )

                    // Prominent Search Bar Trigger
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToExplore)
                            .testTag("home_search_bar"),
                        shape = RoundedCornerShape(16.dp),
                        color = YvyBeigeWarm,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar propiedades",
                                tint = YvyForestPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Buscar por ciudad, barrio o tipo...",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = YvyForestDeep
                                )
                                Text(
                                    text = "Villa Morra • Las Lomas • San Bernardino • Luque • CDE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = YvyForestDeep.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }

                    // Quick Operation Buttons: Comprar, Alquilar, Explorar, Favoritos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            label = "Comprar",
                            subtitle = "Venta",
                            onClick = { onQuickOperationSelect("SALE") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_buy_btn")
                        )
                        QuickActionButton(
                            label = "Alquilar",
                            subtitle = "Renta",
                            onClick = { onQuickOperationSelect("RENT") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_rent_btn")
                        )
                        QuickActionButton(
                            label = "Favoritos",
                            subtitle = "${favorites.size} guardados",
                            onClick = onNavigateToFavorites,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_favorites_btn")
                        )
                    }
                }
            }
        }

        // 2. Categorías Inmobiliarias
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Categorías Inmobiliarias",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
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

        // 3. Estado del Catálogo: Destacadas y Últimas Publicaciones
        when (propertiesState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
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
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
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
                val featuredList = allProperties.filter { it.featured }.ifEmpty { allProperties.take(3) }

                if (allProperties.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "El catálogo está listo para inicializar",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Carga las propiedades iniciales de referencia en Asunción, San Bernardino, Luque, Ciudad del Este y Encarnación.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(16.dp))
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
                    // Propiedades Destacadas (Carrusel Horizontal)
                    item {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Propiedades Destacadas",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = "Selección premium en Asunción y Cordillera",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "Ver todas",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable(onClick = onNavigateToExplore)
                                        .padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(featuredList, key = { "featured_${it.id}" }) { prop ->
                                    PropertyCard(
                                        property = prop,
                                        displayCurrency = displayCurrency,
                                        isFavorite = favoriteIds.contains(prop.id),
                                        onCardClick = { onPropertyClick(prop) },
                                        onFavoriteClick = { onToggleFavorite(prop) },
                                        modifier = Modifier.width(310.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Últimas Publicaciones
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Últimas Publicaciones",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Catálogo actualizado en tiempo real",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(allProperties, key = { "latest_${it.id}" }) { prop ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            PropertyCard(
                                property = prop,
                                displayCurrency = displayCurrency,
                                isFavorite = favoriteIds.contains(prop.id),
                                onCardClick = { onPropertyClick(prop) },
                                onFavoriteClick = { onToggleFavorite(prop) }
                            )
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
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = YvyForestPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Yvy Capital Pro",
                            tint = YvyGoldAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Yvy Capital Pro • Asesores & Desarrolladoras",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "¿Eres asesor inmobiliario o propietario en Paraguay? Publica inmuebles, recibe solicitudes de visita verificadas y gestiona tu cartera desde Yvy Capital Pro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = YvyBeigeWarm.copy(alpha = 0.88f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.14f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = YvyGoldAccent
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.primary
        )
    )
}
