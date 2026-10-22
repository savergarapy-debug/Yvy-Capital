package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FavoriteItem
import com.example.data.model.Property
import com.example.data.model.PropertyInquiry
import com.example.ui.components.EmptyState
import com.example.ui.components.PropertyCard
import com.example.ui.components.StaffBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusBadgeTone
import com.example.ui.components.YvyButton
import com.example.ui.components.YvyButtonVariant
import com.example.ui.components.YvyChip
import com.example.ui.components.YvyTopBar
import com.example.ui.theme.YvyShapes
import com.example.ui.theme.YvySpacing
import com.example.ui.util.CurrencyFormatter
import com.example.ui.viewmodel.UiState

@Composable
fun FavoritesScreen(
    allPropertiesState: UiState<List<Property>>,
    favoritesState: UiState<List<FavoriteItem>>,
    userInquiriesState: UiState<List<PropertyInquiry>>,
    @Suppress("UNUSED_PARAMETER") displayCurrency: String = "PYG",
    isStaffAgent: (String) -> Boolean = { false },
    onNavigateToExplore: () -> Unit,
    onPropertyClick: (Property) -> Unit,
    onToggleFavorite: (Property) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val savedCount = (favoritesState as? UiState.Success)?.data?.size ?: 0
    val inquiriesCount = (userInquiriesState as? UiState.Success)?.data?.size ?: 0
    val catalog = (allPropertiesState as? UiState.Success)?.data ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("favorites_screen")
    ) {
        // Sober Top Bar without filler subtitle or dark green gradient
        YvyTopBar(title = "Mis favoritos y visitas")

        // Segmented Chip Selector on flat #FFFFFF surface
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = YvySpacing.ScreenHorizontal, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                YvyChip(
                    label = "Guardadas ($savedCount)",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    leadingIcon = Icons.Outlined.FavoriteBorder,
                    modifier = Modifier.weight(1f),
                    testTag = "tab_saved_properties"
                )
                YvyChip(
                    label = "Solicitudes y visitas ($inquiriesCount)",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    leadingIcon = Icons.Outlined.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    testTag = "tab_user_inquiries"
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

        if (selectedTab == 0) {
            when (favoritesState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(YvySpacing.ScreenHorizontal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(favoritesState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    val favItems = favoritesState.data

                    if (favItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(YvySpacing.ScreenHorizontal),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                title = "Aún no tienes propiedades en favoritos",
                                description = "Las propiedades que guardes tocando el ícono del corazón aparecerán aquí para consultarlas fácilmente.",
                                icon = Icons.Outlined.FavoriteBorder,
                                actionLabel = "Ir a explorar propiedades",
                                onActionClick = onNavigateToExplore,
                                actionTestTag = "empty_fav_explore_btn"
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 156.dp),
                            contentPadding = PaddingValues(
                                horizontal = YvySpacing.ScreenHorizontal,
                                vertical = 16.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(favItems, key = { it.propertyId }) { fav ->
                                val resolvedProperty = catalog.find { it.id == fav.propertyId }
                                    ?: Property(
                                        id = fav.propertyId,
                                        title = fav.title,
                                        price = fav.price,
                                        currency = fav.currency,
                                        operationType = fav.operationType,
                                        propertyType = fav.propertyType,
                                        city = fav.city,
                                        neighborhood = fav.neighborhood,
                                        areaTotalM2 = fav.areaTotalM2,
                                        bedrooms = fav.bedrooms,
                                        bathrooms = fav.bathrooms
                                    )
                                PropertyCard(
                                    property = resolvedProperty,
                                    displayCurrency = resolvedProperty.currency,
                                    isFavorite = true,
                                    onCardClick = { onPropertyClick(resolvedProperty) },
                                    onFavoriteClick = { onToggleFavorite(resolvedProperty) },
                                    compact = true,
                                    isStaff = isStaffAgent(resolvedProperty.agentId)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            when (userInquiriesState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(YvySpacing.ScreenHorizontal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userInquiriesState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    val inquiries = userInquiriesState.data
                    if (inquiries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(YvySpacing.ScreenHorizontal),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                title = "Sin solicitudes ni visitas registradas",
                                description = "Cuando envíes una solicitud de visita o consultes disponibilidad desde la ficha de una propiedad, podrás seguir su estado aquí.",
                                icon = Icons.Outlined.CalendarMonth,
                                actionLabel = "Explorar propiedades",
                                onActionClick = onNavigateToExplore
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = YvySpacing.ScreenHorizontal,
                                vertical = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(inquiries, key = { it.id }) { inq ->
                                val linkedProperty = catalog.find { it.id == inq.propertyId }
                                InquiryStatusCard(
                                    inquiry = inq,
                                    linkedProperty = linkedProperty,
                                    isStaffAgent = linkedProperty?.let { isStaffAgent(it.agentId) } ?: false,
                                    onOpenProperty = { prop -> onPropertyClick(prop) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InquiryStatusCard(
    inquiry: PropertyInquiry,
    linkedProperty: Property?,
    isStaffAgent: Boolean = false,
    onOpenProperty: (Property) -> Unit
) {
    val statusCode = inquiry.status.uppercase().trim()
    val statusTone = when (statusCode) {
        "VISIT_SCHEDULED" -> StatusBadgeTone.MONTE_PRIMARY
        "CONTACTED" -> StatusBadgeTone.TERRACOTTA_ACCENT
        "CLOSED" -> StatusBadgeTone.NEUTRAL_OUTLINED
        else -> StatusBadgeTone.TERRACOTTA_ACCENT
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = YvyShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusBadge(
                    text = CurrencyFormatter.inquiryTypeLabel(inquiry.inquiryType),
                    tone = StatusBadgeTone.NEUTRAL_OUTLINED
                )
                StatusBadge(
                    text = CurrencyFormatter.inquiryStatusLabel(inquiry.status),
                    tone = statusTone
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.HomeWork,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = inquiry.propertyTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            if (inquiry.preferredDate.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Fecha solicitada: ${inquiry.preferredDate}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (inquiry.message.isNotBlank()) {
                Text(
                    text = inquiry.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (linkedProperty != null) {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = "Asesor: ${linkedProperty.agentName.ifBlank { "Yvy Capital" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isStaffAgent) {
                            StaffBadge()
                        }
                    }
                    YvyButton(
                        text = "Ver propiedad",
                        onClick = { onOpenProperty(linkedProperty) },
                        variant = YvyButtonVariant.TEXT
                    )
                }
            }
        }
    }
}
