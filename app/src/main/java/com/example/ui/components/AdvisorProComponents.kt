package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import com.example.data.model.AdvisorMembershipSpec
import com.example.data.model.AdvisorMemberships
import com.example.data.model.Property
import com.example.data.model.PropertyPromoVideo
import com.example.data.model.UserProfile
import com.example.ui.theme.YvyForestDeep
import com.example.ui.theme.YvyForestMedium
import com.example.ui.theme.YvyForestPrimary
import com.example.ui.theme.YvyGoldAccent
import com.example.ui.theme.YvyPriceCardStyle
import com.example.ui.theme.YvyProGold
import com.example.ui.theme.YvyShapes
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.PromoVideoGenerator
import kotlinx.coroutines.delay

/**
 * Ventana compacta, clara y visualmente atractiva que presenta los 3 planes de membresía
 * para asesores inmobiliarios al pulsar "Conviértete en Asesor".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvisorMembershipPlansDialog(
    currentProfile: UserProfile?,
    isBusy: Boolean,
    onRequestPlan: (
        planTier: String,
        paymentMethod: String,
        paymentReference: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val initialTier = remember(currentProfile?.planTier) {
        if (currentProfile?.planTier in listOf(
                AdvisorMemberships.TIER_INICIAL,
                AdvisorMemberships.TIER_INTERMEDIO,
                AdvisorMemberships.TIER_SENIOR
            )
        ) {
            currentProfile?.planTier ?: AdvisorMemberships.TIER_SENIOR
        } else {
            AdvisorMemberships.TIER_SENIOR
        }
    }

    var selectedPlanCode by remember { mutableStateOf(initialTier) }
    val paymentMethods = listOf("Transferencia bancaria", "Billetera electrónica", "Pago en oficina")
    var selectedPaymentMethod by remember { mutableStateOf(paymentMethods.first()) }
    var paymentReference by remember { mutableStateOf("") }
    var referenceError by remember { mutableStateOf(false) }

    val selectedSpec: AdvisorMembershipSpec = remember(selectedPlanCode) {
        AdvisorMemberships.getSpec(selectedPlanCode) ?: AdvisorMemberships.ALL_PLANS.last()
    }

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.WorkspacePremium,
                contentDescription = null,
                tint = YvyGoldAccent,
                modifier = Modifier.size(34.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Solicitar plan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Elige tu plan y registra tu comprobante de pago",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
                    .testTag("membership_plans_dialog_list")
            ) {
                item {
                    Text(
                        text = "1. Selecciona tu plan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(AdvisorMemberships.ALL_PLANS, key = { it.code }) { plan ->
                    val isSelected = selectedPlanCode == plan.code
                    val isSenior = plan.code == AdvisorMemberships.TIER_SENIOR

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPlanCode = plan.code }
                            .testTag("plan_card_${plan.code}"),
                        shape = YvyShapes.Card,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedPlanCode = plan.code }
                                    )
                                    Text(
                                        text = plan.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = plan.priceFormatted,
                                    style = YvyPriceCardStyle,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                text = "Límite: ${plan.maxActiveListings} propiedades activas • Comisión ${plan.advisorParticipationPercent}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. Método de pago",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        paymentMethods.forEach { method ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentMethod = method },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == method,
                                    onClick = { selectedPaymentMethod = method }
                                )
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            when (selectedPaymentMethod) {
                                "Transferencia bancaria" -> {
                                    Text(
                                        text = "Datos de transferencia:\nBanco Continental • Cta Cte: 01-234567-89\nTitular: Yvy Capital S.A. • RUC: 80099887-1",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                "Billetera electrónica" -> {
                                    Text(
                                        text = "Envío Tigo Money / Billetera Personal:\nNúmero: (0981) 123-456 • Titular: Yvy Capital S.A.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "Pago en Oficina:\nPaseo La Galería, Torre 1, Piso 14, Asunción.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3. Referencia o comprobante",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    OutlinedTextField(
                        value = paymentReference,
                        onValueChange = {
                            paymentReference = it
                            if (referenceError && it.isNotBlank()) referenceError = false
                        },
                        label = { Text("Nro. de comprobante o referencia") },
                        placeholder = { Text("Ej: Transf. 984321 / Recibo oficina") },
                        isError = referenceError,
                        supportingText = {
                            if (referenceError) {
                                Text("Ingresa el número de comprobante o detalle de pago")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_reference_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (paymentReference.isBlank()) {
                        referenceError = true
                    } else {
                        onRequestPlan(selectedPlanCode, selectedPaymentMethod, paymentReference.trim())
                    }
                },
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = YvyForestPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("submit_plan_request_btn")
            ) {
                Text(
                    text = if (isBusy) "Enviando..." else "Solicitar plan",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isBusy
            ) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Generador de Videos Promocionales de Propiedades (Exclusivo para Membresía Plan Senior).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeniorVideoGeneratorDialog(
    availableProperties: List<Property>,
    displayCurrency: String,
    isBusy: Boolean,
    onGenerateVideo: (
        property: Property,
        aspectRatio: String,
        selectedImages: List<String>,
        includeBackgroundMusic: Boolean,
        customHeadline: String,
        preferAiInteractions: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPropertyIndex by remember { mutableIntStateOf(0) }
    val selectedProperty = availableProperties.getOrNull(selectedPropertyIndex) ?: availableProperties.firstOrNull()

    var aspectRatio by remember { mutableStateOf("9:16") }
    var includeMusic by remember { mutableStateOf(true) }
    var preferAiMode by remember { mutableStateOf(false) }

    val selectedScenes = remember {
        mutableStateListOf(
            "fachada_principal",
            "area_social_quincho",
            "suite_principal",
            "terraza_piscina"
        )
    }

    var customHeadline by remember(selectedProperty?.id) {
        mutableStateOf(
            if (selectedProperty != null) {
                "${CurrencyFormatter.operationLabel(selectedProperty.operationType)}: ${selectedProperty.title}"
            } else {
                "Propiedad Destacada en Yvy Capital"
            }
        )
    }

    // Animación en vivo de previsualización del video
    var activeSlidePreview by remember { mutableIntStateOf(0) }
    LaunchedEffect(selectedScenes.size) {
        while (true) {
            delay(1800L)
            if (selectedScenes.isNotEmpty()) {
                activeSlidePreview = (activeSlidePreview + 1) % selectedScenes.size
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Default.MovieCreation,
                contentDescription = null,
                tint = YvyGoldAccent,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Column {
                Text(
                    text = "Generador de Videos Promocionales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Herramienta exclusiva • Membresía Plan Senior",
                    style = MaterialTheme.typography.labelMedium,
                    color = YvyForestPrimary
                )
            }
        },
        text = {
            if (selectedProperty == null) {
                Text(
                    text = "Publica primero una propiedad o carga el catálogo para generar su video promocional.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                        .testTag("senior_video_generator_list")
                ) {
                    // 1. Selección de propiedad
                    item {
                        Text(
                            text = "1. Selecciona la propiedad:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            availableProperties.take(5).forEachIndexed { idx, prop ->
                                FilterChip(
                                    selected = selectedPropertyIndex == idx,
                                    onClick = { selectedPropertyIndex = idx },
                                    label = {
                                        Text(
                                            text = "${prop.title.take(30)} (${prop.city})",
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // 2. Vista previa animada en tiempo real
                    item {
                        val currentSceneKey = selectedScenes.getOrElse(activeSlidePreview) { "fachada_principal" }
                        val currentSceneLabel = PromoVideoGenerator.SCENE_OPTIONS
                            .firstOrNull { it.first == currentSceneKey }?.second ?: "Escena Principal"
                        val formattedPrice = CurrencyFormatter.formatPrice(
                            selectedProperty.price,
                            selectedProperty.currency,
                            displayCurrency
                        )

                        Card(
                            shape = YvyShapes.Card,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (aspectRatio == "9:16") 210.dp else 155.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = YvyGoldAccent
                                        ) {
                                            Text(
                                                text = "PREVIEW $aspectRatio • ESCENA ${(activeSlidePreview % selectedScenes.size.coerceAtLeast(1)) + 1}/${selectedScenes.size.coerceAtLeast(1)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = YvyForestDeep,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        if (includeMusic) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.MusicNote,
                                                    contentDescription = null,
                                                    tint = YvyGoldAccent,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Audio",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = YvyGoldAccent
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = currentSceneLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = formattedPrice,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = YvyGoldAccent,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = customHeadline,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${selectedProperty.neighborhood}, ${selectedProperty.city} • ${selectedProperty.areaTotalM2} m² • ${selectedProperty.bedrooms} Dorm.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Formato y música
                    item {
                        Text(
                            text = "2. Formato de salida y audio:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            FilterChip(
                                selected = aspectRatio == "9:16",
                                onClick = { aspectRatio = "9:16" },
                                label = { Text("Vertical 9:16 (Reels/TikTok)") }
                            )
                            FilterChip(
                                selected = aspectRatio == "16:9",
                                onClick = { aspectRatio = "16:9" },
                                label = { Text("Horizontal 16:9") }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Incluir música ambiental inmobiliaria",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Switch(
                                checked = includeMusic,
                                onCheckedChange = { includeMusic = it }
                            )
                        }
                    }

                    // 4. Selección de imágenes / escenas (1 a 6)
                    item {
                        Text(
                            text = "3. Escenas e imágenes a incluir (${selectedScenes.size}/6):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            PromoVideoGenerator.SCENE_OPTIONS.forEach { (key, label) ->
                                val isChosen = key in selectedScenes
                                FilterChip(
                                    selected = isChosen,
                                    onClick = {
                                        if (isChosen && selectedScenes.size > 1) {
                                            selectedScenes.remove(key)
                                        } else if (!isChosen && selectedScenes.size < 6) {
                                            selectedScenes.add(key)
                                        }
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    // 5. Titular promocional
                    item {
                        OutlinedTextField(
                            value = customHeadline,
                            onValueChange = { customHeadline = it },
                            label = { Text("Titular superpuesto en el video") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 6. Motor de renderizado (Automatizado sin costo vs IA Generativa)
                    item {
                        Text(
                            text = "4. Motor de generación:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            FilterChip(
                                selected = !preferAiMode,
                                onClick = { preferAiMode = false },
                                label = { Text("Renderizador MP4 Local (Gratis)") }
                            )
                            FilterChip(
                                selected = preferAiMode,
                                onClick = { preferAiMode = true },
                                label = { Text("IA Omni Flash (Opcional)") }
                            )
                        }
                        Text(
                            text = if (!preferAiMode) {
                                "Recomendado: Genera el archivo .mp4 en tu dispositivo con transiciones animadas, precios y datos clave sin costo por cada video."
                            } else {
                                "Utiliza Gemini Omni Flash (v1beta/interactions) si configuraste GEMINI_API_KEY en Secrets, con respaldo automático al renderizador MP4 local."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedProperty != null) {
                Button(
                    onClick = {
                        onGenerateVideo(
                            selectedProperty,
                            aspectRatio,
                            selectedScenes.toList(),
                            includeMusic,
                            customHeadline,
                            preferAiMode
                        )
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YvyForestPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("generate_promo_video_confirm_btn")
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBusy) "Renderizando MP4..." else "Generar Video MP4")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isBusy
            ) {
                Text("Cerrar")
            }
        }
    )
}

/**
 * Reproductor interactivo y diálogo de exportación del video promocional MP4 generado.
 */
@Composable
fun PromoVideoPreviewDialog(
    video: PropertyPromoVideo,
    onShareMp4: (PropertyPromoVideo) -> Unit,
    onDismiss: () -> Unit
) {
    val slides = video.selectedImages.ifEmpty { listOf("fachada_principal", "area_social_quincho") }
    var currentSlideIndex by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "promo_video_anim")
    val progressAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slide_progress"
    )

    LaunchedEffect(slides.size) {
        while (true) {
            delay(2000L)
            currentSlideIndex = (currentSlideIndex + 1) % slides.size
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = YvyGoldAccent,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Reproduciendo Video Promocional",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val sceneKey = slides[currentSlideIndex % slides.size]
                val sceneTitle = PromoVideoGenerator.SCENE_OPTIONS
                    .firstOrNull { it.first == sceneKey }?.second ?: "Escena #${currentSlideIndex + 1}"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (video.aspectRatio == "9:16") 250.dp else 180.dp)
                        .clip(YvyShapes.Card)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "YVY CAPITAL • ${video.aspectRatio}",
                                style = MaterialTheme.typography.labelSmall,
                                color = YvyGoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Escena ${currentSlideIndex + 1}/${slides.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = sceneTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = YvyGoldAccent
                        )
                        Text(
                            text = video.priceLabel,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = video.overlayHeadline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${video.locationLabel}\n${video.highlightsLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progressAnim },
                            color = YvyGoldAccent,
                            trackColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text(
                    text = "Archivo MP4 listo (${video.durationSeconds}s) • ${if (video.includeBackgroundMusic) "Con pista musical" else "Sin música"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onShareMp4(video) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = YvyForestPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("share_promo_video_mp4_btn")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Compartir MP4")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

/**
 * Diálogo de Consultas Directas con Asesores de Yvy Capital para Colaborar en Cierre de Operaciones (Exclusivo Plan Senior).
 */
@Composable
fun SeniorDirectCollaborationDialog(
    advisorName: String,
    advisorPhone: String,
    properties: List<Property>,
    onSendCollaborationMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedPropertyTitle by remember {
        mutableStateOf(properties.firstOrNull()?.title ?: "Operación en cartera")
    }
    var notes by remember {
        mutableStateOf("Solicito asistencia de la Mesa Senior de Yvy Capital para estructurar la reserva, contrato de compraventa y cierre conjunto de esta operación.")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Handshake,
                contentDescription = null,
                tint = YvyGoldAccent,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Mesa de Cierre Directo • Yvy Capital Senior",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Como miembro del Plan Senior (participación del 4,5 % por venta concretada), cuentas con acompañamiento directo de asesores sénior de Yvy Capital para colaborar en la negociación, tasación y cierre legal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = selectedPropertyTitle,
                    onValueChange = { selectedPropertyTitle = it },
                    label = { Text("Propiedad u operación a cerrar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Detalle de la colaboración requerida") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = {
                        val text = "Hola Mesa Senior de Yvy Capital, soy $advisorName ($advisorPhone) con Membresía Senior. Solicito colaboración para el cierre de \"$selectedPropertyTitle\": $notes"
                        val uri = Uri.parse("https://wa.me/595981400100?text=${Uri.encode(text)}")
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (_: Exception) {
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Canal Directo WhatsApp Yvy Capital")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSendCollaborationMessage("Solicitud de cierre conjunto enviada a la Mesa Senior de Yvy Capital para \"$selectedPropertyTitle\".")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = YvyForestPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Registrar Solicitud de Cierre")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
