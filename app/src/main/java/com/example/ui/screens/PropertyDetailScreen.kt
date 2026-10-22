package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.outlined.Bathtub
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material.icons.outlined.Verified
import com.example.data.model.AdvisorMemberships
import com.example.data.model.Property
import com.example.data.model.UserProfile
import com.example.ui.components.MetricBadge
import com.example.ui.components.PropertyArtworkHeader
import com.example.ui.components.PublicWebFallbackPreviewDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.SharePropertyDirectLinkDialog
import com.example.ui.components.StaffBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusBadgeTone
import com.example.ui.components.YvyButton
import com.example.ui.components.YvyButtonVariant
import com.example.ui.theme.YvyEditorialSerifFamily
import com.example.ui.theme.YvyForestDeep
import com.example.ui.theme.YvyForestPrimary
import com.example.ui.theme.YvyGoldAccent
import com.example.ui.theme.YvyPriceEditorialStyle
import com.example.ui.theme.YvyShapes
import com.example.ui.theme.YvySpacing
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DeepLinkHandler

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailScreen(
    property: Property,
    userProfile: UserProfile?,
    isFavorite: Boolean,
    isAgentStaff: Boolean = false,
    agentMembershipTier: String? = null,
    isAdmin: Boolean = false,
    onToggleVerified: ((Boolean) -> Unit)? = null,
    displayCurrency: String,
    isBusy: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSubmitInquiry: (
        userName: String,
        userEmail: String,
        userPhone: String,
        inquiryType: String,
        preferredDate: String,
        message: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onSubmitReport: (reason: String, details: String, onSuccess: () -> Unit) -> Unit,
    onMessage: (String) -> Unit = {}
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var selectedGalleryIndex by remember { mutableIntStateOf(0) }
    var showInquiryDialog by remember { mutableStateOf(false) }
    var defaultInquiryType by remember { mutableStateOf("VISIT") }
    var showReportDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showWebFallbackPreview by remember { mutableStateOf(false) }

    val galleryLabels = listOf(
        "Fachada Principal y Entorno",
        "Área Social y Quincho",
        "Suite Principal y Vistas"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("property_detail_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Galería de Imágenes con controles superiores (Atrás, Compartir, Favorito)
        item {
            Box {
                PropertyArtworkHeader(
                    property = property,
                    height = 290.dp,
                    variantIndex = selectedGalleryIndex
                )

                // Top Bar Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("detail_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showShareDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("detail_share_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir propiedad",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .testTag("detail_favorite_btn")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Quitar de favoritos" else "Guardar en favoritos",
                                tint = if (isFavorite) Color(0xFFFF6B6B) else Color.White
                            )
                        }
                    }
                }

                // Gallery Selector Pills at Bottom of Header
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Vista ${selectedGalleryIndex + 1}/3: ${galleryLabels[selectedGalleryIndex]}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(3) { idx ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedGalleryIndex == idx) YvyGoldAccent else Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.clickable { selectedGalleryIndex = idx }
                            ) {
                                Text(
                                    text = "Foto ${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedGalleryIndex == idx) YvyForestDeep else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Información Principal, Precio Dual PYG/USD y Superficies
        item {
            Column(
                modifier = Modifier.padding(YvySpacing.ScreenHorizontal),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(
                        text = CurrencyFormatter.operationLabel(property.operationType),
                        tone = StatusBadgeTone.TERRACOTTA_ACCENT
                    )
                    StatusBadge(
                        text = CurrencyFormatter.propertyTypeLabel(property.propertyType),
                        tone = StatusBadgeTone.NEUTRAL_OUTLINED
                    )
                    if (property.verified) {
                        StatusBadge(
                            text = "Verificada",
                            tone = StatusBadgeTone.MONTE_PRIMARY,
                            leadingIcon = Icons.Outlined.Verified
                        )
                    }
                }

                Text(
                    text = property.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = YvyEditorialSerifFamily
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Price Card (Editorial Serif + Tabular Numerals in Terracotta)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = YvyShapes.Card
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Precio de publicación",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatPrice(
                                    amount = property.price,
                                    sourceCurrency = property.currency
                                ),
                                style = YvyPriceEditorialStyle,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(
                            text = if (property.currency.uppercase() == "USD") "USD ($)" else "PYG (₲)",
                            tone = StatusBadgeTone.NEUTRAL_OUTLINED
                        )
                    }
                }

                // Location Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Ubicación",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${property.neighborhood}, ${property.city} • Dpto. ${property.department}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (property.address.isNotBlank()) {
                            Text(
                                text = property.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Superficies y Características Principales
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBadge(Icons.Outlined.SquareFoot, "Total: ${property.areaTotalM2} m²")
                    if (property.areaBuiltM2 > 0) {
                        MetricBadge(Icons.Outlined.SquareFoot, "Construido: ${property.areaBuiltM2} m²")
                    }
                    if (property.bedrooms > 0) {
                        MetricBadge(Icons.Outlined.Bed, "${property.bedrooms} dormitorios")
                    }
                    if (property.bathrooms > 0) {
                        MetricBadge(Icons.Outlined.Bathtub, "${property.bathrooms} baños")
                    }
                    if (property.parkingSpaces > 0) {
                        MetricBadge(Icons.Outlined.DirectionsCar, "${property.parkingSpaces} cocheras")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Descripción detallada
                SectionHeader(title = "Descripción del inmueble")
                Text(
                    text = property.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Comodidades y Equipamiento
                if (property.amenities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    SectionHeader(title = "Comodidades y equipamiento")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        property.amenities.forEach { amenity ->
                            Surface(
                                shape = YvyShapes.FieldAndButton,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = amenity,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // 3. Ubicación Geográfica y Acceso a Google Maps
                SectionHeader(title = "Ubicación y mapa")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = YvyShapes.Card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            val gridLineColor = MaterialTheme.colorScheme.surface
                            val accentLineColor = MaterialTheme.colorScheme.outline
                            val pinColor = MaterialTheme.colorScheme.secondary
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                for (i in 1..5) {
                                    drawLine(
                                        color = gridLineColor,
                                        start = Offset(0f, h * (i / 6f)),
                                        end = Offset(w, h * (i / 6f)),
                                        strokeWidth = 6f
                                    )
                                    drawLine(
                                        color = gridLineColor,
                                        start = Offset(w * (i / 6f), 0f),
                                        end = Offset(w * (i / 6f), h),
                                        strokeWidth = 6f
                                    )
                                }
                                drawLine(
                                    color = accentLineColor,
                                    start = Offset(0f, h * 0.8f),
                                    end = Offset(w, h * 0.2f),
                                    strokeWidth = 10f
                                )
                                drawCircle(
                                    color = pinColor.copy(alpha = 0.22f),
                                    radius = 32f,
                                    center = Offset(w * 0.5f, h * 0.5f)
                                )
                                drawCircle(
                                    color = pinColor,
                                    radius = 12f,
                                    center = Offset(w * 0.5f, h * 0.5f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${property.neighborhood}, ${property.city}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Coord: ${property.latitude}, ${property.longitude}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            YvyButton(
                                text = "Abrir Mapa",
                                onClick = {
                                    val geoUri = Uri.parse(
                                        "geo:${property.latitude},${property.longitude}?q=${property.latitude},${property.longitude}(${Uri.encode(property.title)})"
                                    )
                                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        val webUri = Uri.parse(
                                            "https://www.google.com/maps/search/?api=1&query=${property.latitude},${property.longitude}"
                                        )
                                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                    }
                                },
                                variant = YvyButtonVariant.SECONDARY_OUTLINED,
                                leadingIcon = Icons.Outlined.Map,
                                testTag = "open_map_btn"
                            )
                        }
                    }
                }

                // 4. Información del Asesor y Acciones de Contacto / Visita / WhatsApp
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = YvyShapes.Card,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = property.agentName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isAgentStaff) {
                                        StaffBadge()
                                    } else if (!agentMembershipTier.isNullOrBlank() && agentMembershipTier != "FREE") {
                                        StatusBadge(
                                            text = AdvisorMemberships.getSpec(agentMembershipTier)?.name ?: "Asesor Pro",
                                            tone = StatusBadgeTone.PRO_GOLD
                                        )
                                    }
                                }
                                Text(
                                    text = if (isAgentStaff) "Equipo Yvy Capital" else property.agentAgency,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            YvyButton(
                                text = "Solicitar Visita",
                                onClick = {
                                    defaultInquiryType = "VISIT"
                                    showInquiryDialog = true
                                },
                                variant = YvyButtonVariant.PRIMARY,
                                leadingIcon = Icons.Outlined.CalendarMonth,
                                modifier = Modifier.weight(1f),
                                testTag = "request_visit_btn"
                            )

                            YvyButton(
                                text = "Consultar Info",
                                onClick = {
                                    defaultInquiryType = "INFO"
                                    showInquiryDialog = true
                                },
                                variant = YvyButtonVariant.SECONDARY_OUTLINED,
                                modifier = Modifier.weight(1f),
                                testTag = "contact_agent_btn"
                            )
                        }

                        // Botón WhatsApp oficial cuando el asesor tiene número público autorizado
                        if (property.agentWhatsapp.isNotBlank()) {
                            YvyButton(
                                text = "Contactar por WhatsApp (${property.agentWhatsapp})",
                                onClick = {
                                    val cleanPhone = property.agentWhatsapp.replace(Regex("[^0-9]"), "")
                                    val directUrl = DeepLinkHandler.buildPropertyLink(property.id)
                                    val text = "Hola ${property.agentName}, vi la propiedad \"${property.title}\" en Yvy Capital ($directUrl) y me gustaría recibir más información."
                                    val waUri = Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(text)}")
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, waUri))
                                    } catch (_: Exception) {
                                    }
                                },
                                variant = YvyButtonVariant.PRIMARY,
                                leadingIcon = Icons.AutoMirrored.Filled.Chat,
                                fullWidth = true,
                                testTag = "whatsapp_contact_btn"
                            )
                        }

                        // Botón de Enlace Directo Compartible de la Propiedad (App Link)
                        YvyButton(
                            text = "Compartir Enlace Directo a esta Propiedad",
                            onClick = { showShareDialog = true },
                            variant = YvyButtonVariant.SECONDARY_OUTLINED,
                            leadingIcon = Icons.Outlined.Link,
                            fullWidth = true,
                            testTag = "share_direct_link_card_btn"
                        )
                    }
                }

                // Control exclusivo para administradores: interruptor de verificación oficial
                if (isAdmin && onToggleVerified != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("admin_verified_card"),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Verified,
                                        contentDescription = null,
                                        tint = if (property.verified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Propiedad Verificada",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = if (property.verified) "Verificada oficialmente por administración de Yvy Capital" else "Sin verificar (anuncio en revisión)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = property.verified,
                                onCheckedChange = { onToggleVerified(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.testTag("admin_verified_switch")
                            )
                        }
                    }
                }

                // 5. Reportar anuncio sospechoso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.testTag("report_property_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reportar anuncio sospechoso o inexacto",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Modal de Solicitud de Visita / Consulta
    if (showInquiryDialog) {
        var userName by remember { mutableStateOf(userProfile?.displayName ?: "") }
        var userEmail by remember { mutableStateOf(userProfile?.email ?: "") }
        var userPhone by remember { mutableStateOf(userProfile?.phone?.ifBlank { "+595981" } ?: "+595981") }
        var inquiryType by remember { mutableStateOf(defaultInquiryType) }
        var preferredDate by remember { mutableStateOf("Sábado 10:00 hs") }
        var message by remember {
            mutableStateOf("Hola, estoy interesado en \"${property.title}\" y me gustaría coordinar una visita o conocer su disponibilidad.")
        }

        AlertDialog(
            onDismissRequest = { if (!isBusy) showInquiryDialog = false },
            title = {
                Text(
                    text = if (inquiryType == "VISIT") "Solicitar Visita a Propiedad" else "Enviar Consulta al Asesor",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "VISIT" to "Visita",
                            "INFO" to "Información",
                            "AVAILABILITY" to "Disponibilidad"
                        ).forEach { (code, label) ->
                            FilterChip(
                                selected = inquiryType == code,
                                onClick = { inquiryType = code },
                                label = {
                                    Text(
                                        text = label,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Tu nombre y apellido") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = userPhone,
                        onValueChange = { userPhone = it },
                        label = { Text("Teléfono / WhatsApp (+595...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (inquiryType == "VISIT") {
                        OutlinedTextField(
                            value = preferredDate,
                            onValueChange = { preferredDate = it },
                            label = { Text("Día y horario sugerido") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Mensaje para el asesor") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitInquiry(
                            userName,
                            userEmail.ifBlank { "cliente@yvycapital.com.py" },
                            userPhone,
                            inquiryType,
                            preferredDate,
                            message
                        ) {
                            showInquiryDialog = false
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.testTag("submit_inquiry_confirm_btn")
                ) {
                    Text(if (isBusy) "Enviando..." else "Enviar Solicitud")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showInquiryDialog = false },
                    enabled = !isBusy
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Reporte de Anuncio
    if (showReportDialog) {
        var selectedReason by remember { mutableStateOf("INACCURATE_PRICE") }
        var details by remember { mutableStateOf("") }

        val reasons = listOf(
            "INACCURATE_PRICE" to "Precio o datos incorrectos",
            "SOLD_UNAVAILABLE" to "Ya se vendió o alquiló",
            "DUPLICATE" to "Propiedad duplicada",
            "FRAUD" to "Anuncio sospechoso / fraude"
        )

        AlertDialog(
            onDismissRequest = { if (!isBusy) showReportDialog = false },
            title = { Text("Reportar Anuncio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selecciona el motivo para que el equipo de moderación de Yvy Capital verifique esta publicación:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    reasons.forEach { (code, label) ->
                        FilterChip(
                            selected = selectedReason == code,
                            onClick = { selectedReason = code },
                            label = { Text(label) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Detalles adicionales") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitReport(selectedReason, details) {
                            showReportDialog = false
                        }
                    },
                    enabled = !isBusy
                ) {
                    Text("Enviar Reporte")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showShareDialog) {
        SharePropertyDirectLinkDialog(
            property = property,
            displayCurrency = displayCurrency,
            onDismiss = { showShareDialog = false },
            onLinkCopied = { msg -> onMessage(msg) },
            onOpenWebFallbackPreview = {
                showShareDialog = false
                showWebFallbackPreview = true
            }
        )
    }

    if (showWebFallbackPreview) {
        PublicWebFallbackPreviewDialog(
            property = property,
            displayCurrency = displayCurrency,
            onOpenDirectlyInApp = {
                showWebFallbackPreview = false
                onMessage("Ya estás viendo la ficha directa de \"${property.title}\" en Yvy Capital.")
            },
            onCopyDeferredLink = { url ->
                onMessage("Enlace diferido guardado en portapapeles: $url")
            },
            onDismiss = { showWebFallbackPreview = false }
        )
    }
}
