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
import com.example.data.model.Property
import com.example.data.model.UserProfile
import com.example.ui.components.MetricBadge
import com.example.ui.components.PropertyArtworkHeader
import com.example.ui.components.PublicWebFallbackPreviewDialog
import com.example.ui.components.SharePropertyDirectLinkDialog
import com.example.ui.theme.YvyForestDeep
import com.example.ui.theme.YvyForestPrimary
import com.example.ui.theme.YvyGoldAccent
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DeepLinkHandler

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailScreen(
    property: Property,
    userProfile: UserProfile?,
    isFavorite: Boolean,
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
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = CurrencyFormatter.operationLabel(property.operationType),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = CurrencyFormatter.propertyTypeLabel(property.propertyType),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    if (property.verified) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificada",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verificada",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = property.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Price Card (Single Currency Display aligned with active currency switch)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Precio de publicación",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatPrice(
                                    amount = property.price,
                                    sourceCurrency = property.currency
                                ),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (property.currency.uppercase() == "USD") "USD ($)" else "PYG (₲)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Location Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${property.neighborhood}, ${property.city} • Dpto. ${property.department}",
                            style = MaterialTheme.typography.titleSmall
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBadge(Icons.Default.SquareFoot, "Total: ${property.areaTotalM2} m²")
                    if (property.areaBuiltM2 > 0) {
                        MetricBadge(Icons.Default.SquareFoot, "Construido: ${property.areaBuiltM2} m²")
                    }
                    if (property.bedrooms > 0) {
                        MetricBadge(Icons.Default.Bed, "${property.bedrooms} Dormitorios")
                    }
                    if (property.bathrooms > 0) {
                        MetricBadge(Icons.Default.Bathtub, "${property.bathrooms} Baños")
                    }
                    if (property.parkingSpaces > 0) {
                        MetricBadge(Icons.Default.DirectionsCar, "${property.parkingSpaces} Cocheras")
                    }
                }

                HorizontalDivider()

                // Descripción detallada
                Text(
                    text = "Descripción del Inmueble",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = property.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                )

                // Comodidades y Equipamiento
                if (property.amenities.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Comodidades y Equipamiento",
                        style = MaterialTheme.typography.titleLarge
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        property.amenities.forEach { amenity ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = amenity,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 3. Ubicación Geográfica y Acceso a Google Maps
                Text(
                    text = "Ubicación y Mapa",
                    style = MaterialTheme.typography.titleLarge
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color(0xFFDCE8E0))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                // Stylized urban street grid
                                for (i in 1..5) {
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(0f, h * (i / 6f)),
                                        end = Offset(w, h * (i / 6f)),
                                        strokeWidth = 6f
                                    )
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(w * (i / 6f), 0f),
                                        end = Offset(w * (i / 6f), h),
                                        strokeWidth = 6f
                                    )
                                }
                                // Main avenue diagonal
                                drawLine(
                                    color = YvyGoldAccent.copy(alpha = 0.6f),
                                    start = Offset(0f, h * 0.8f),
                                    end = Offset(w, h * 0.2f),
                                    strokeWidth = 12f
                                )
                                // Location Pin
                                drawCircle(
                                    color = YvyForestPrimary.copy(alpha = 0.25f),
                                    radius = 36f,
                                    center = Offset(w * 0.5f, h * 0.5f)
                                )
                                drawCircle(
                                    color = YvyForestPrimary,
                                    radius = 14f,
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
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Coord: ${property.latitude}, ${property.longitude}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
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
                                modifier = Modifier.testTag("open_map_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Abrir Mapa")
                            }
                        }
                    }
                }

                // 4. Información del Asesor y Acciones de Contacto / Visita / WhatsApp
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = YvyForestPrimary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = property.agentName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = property.agentAgency,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    defaultInquiryType = "VISIT"
                                    showInquiryDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("request_visit_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = YvyForestPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Solicitar Visita")
                            }

                            OutlinedButton(
                                onClick = {
                                    defaultInquiryType = "INFO"
                                    showInquiryDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("contact_agent_btn")
                            ) {
                                Text("Consultar Info")
                            }
                        }

                        // Botón WhatsApp oficial cuando el asesor tiene número público autorizado
                        if (property.agentWhatsapp.isNotBlank()) {
                            Button(
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("whatsapp_contact_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1F7A4C),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Contactar por WhatsApp (${property.agentWhatsapp})")
                            }
                        }

                        // Botón de Enlace Directo Compartible de la Propiedad (App Link)
                        OutlinedButton(
                            onClick = { showShareDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_direct_link_card_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compartir Enlace Directo a esta Propiedad")
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
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "VISIT" to "Visita",
                            "INFO" to "Información",
                            "AVAILABILITY" to "Disponibilidad"
                        ).forEach { (code, label) ->
                            FilterChip(
                                selected = inquiryType == code,
                                onClick = { inquiryType = code },
                                label = { Text(label) }
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
