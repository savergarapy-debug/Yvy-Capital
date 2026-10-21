package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AdvisorMemberships
import com.example.data.model.Property
import com.example.data.model.PropertyInquiry
import com.example.data.model.PropertyPromoVideo
import com.example.data.model.UserProfile
import com.example.ui.components.AdvisorMembershipPlansDialog
import com.example.ui.components.PromoVideoPreviewDialog
import com.example.ui.components.SeniorDirectCollaborationDialog
import com.example.ui.components.SeniorVideoGeneratorDialog
import com.example.ui.theme.YvyForestDeep
import com.example.ui.theme.YvyForestPrimary
import com.example.ui.theme.YvyGoldAccent
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DeepLinkHandler
import com.example.ui.util.PromoVideoGenerator
import com.example.ui.viewmodel.UiState

@Composable
fun ProfileAndProScreen(
    userProfileState: UiState<UserProfile?>,
    agentPropertiesState: UiState<List<Property>>,
    agentInquiriesState: UiState<List<PropertyInquiry>>,
    agentPromoVideosState: UiState<List<PropertyPromoVideo>> = UiState.Success(emptyList()),
    catalogProperties: List<Property> = emptyList(),
    displayCurrency: String = "PYG",
    isBusy: Boolean,
    initialTab: Int = 0,
    onSaveProfile: (displayName: String, phone: String, preferredCurrency: String, role: String, agencyName: String) -> Unit,
    onActivateAdvisorPlan: (planTier: String, displayName: String, phone: String, agencyName: String, externalAgentMode: Boolean) -> Unit = { _, _, _, _, _ -> },
    onToggleExternalAgentMode: (Boolean) -> Unit = {},
    onPublishProperty: (
        title: String,
        description: String,
        operationType: String,
        propertyType: String,
        price: Double,
        currency: String,
        department: String,
        city: String,
        neighborhood: String,
        address: String,
        areaTotalM2: Long,
        areaBuiltM2: Long,
        bedrooms: Long,
        bathrooms: Long,
        parkingSpaces: Long,
        amenities: List<String>,
        featured: Boolean,
        agentName: String,
        agentAgency: String,
        agentWhatsapp: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onGeneratePromoVideo: (
        property: Property,
        aspectRatio: String,
        selectedImages: List<String>,
        includeBackgroundMusic: Boolean,
        customHeadline: String,
        preferAiInteractions: Boolean,
        onSuccess: (PropertyPromoVideo) -> Unit
    ) -> Unit = { _, _, _, _, _, _, _ -> },
    onDeletePromoVideo: (String) -> Unit = {},
    onDeleteProperty: (String) -> Unit,
    onUpdateInquiryStatus: (inquiryId: String, newStatus: String) -> Unit,
    onMessage: (String) -> Unit = {},
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val profile = (userProfileState as? UiState.Success)?.data
    val currentTier = profile?.planTier ?: AdvisorMemberships.TIER_FREE
    val currentPlanSpec = AdvisorMemberships.getSpec(currentTier)
    val isSeniorMember = AdvisorMemberships.isSeniorTier(currentTier)

    var showPublishDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showBecomeAdvisorDialog by remember { mutableStateOf(false) }
    var showVideoGeneratorDialog by remember { mutableStateOf(false) }
    var previewingVideo by remember { mutableStateOf<PropertyPromoVideo?>(null) }
    var showSeniorCollaborationDialog by remember { mutableStateOf(false) }

    val myProperties = (agentPropertiesState as? UiState.Success)?.data ?: emptyList()
    val myInquiries = (agentInquiriesState as? UiState.Success)?.data ?: emptyList()
    val myPromoVideos = (agentPromoVideosState as? UiState.Success)?.data ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_pro_screen")
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Mi Perfil & Cuenta") },
                modifier = Modifier.testTag("tab_profile")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Yvy Capital Pro") },
                modifier = Modifier.testTag("tab_yvy_pro")
            )
        }

        if (selectedTab == 0) {
            var displayName by remember(profile?.displayName) {
                mutableStateOf(profile?.displayName ?: "Cliente Yvy Capital")
            }
            var phone by remember(profile?.phone) {
                mutableStateOf(profile?.phone?.ifBlank { "+595981" } ?: "+595981")
            }
            var preferredCurrency by remember(profile?.preferredCurrency) {
                mutableStateOf(profile?.preferredCurrency ?: "PYG")
            }
            var role by remember(profile?.role) {
                mutableStateOf(profile?.role ?: "CLIENT")
            }
            var agencyName by remember(profile?.agencyName) {
                mutableStateOf(profile?.agencyName ?: "")
            }

            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Datos Personales y Preferencias",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Text(
                                        text = profile?.email ?: "Sesión activa con Google",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Nombre completo") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Teléfono / WhatsApp (+595...)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Moneda Principal Preferida",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilterChip(
                                    selected = preferredCurrency == "PYG",
                                    onClick = { preferredCurrency = "PYG" },
                                    label = { Text("Guaraní Paraguayo (PYG)") }
                                )
                                FilterChip(
                                    selected = preferredCurrency == "USD",
                                    onClick = { preferredCurrency = "USD" },
                                    label = { Text("Dólar (USD)") }
                                )
                            }

                            Text(
                                text = "Modo de Cuenta",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilterChip(
                                    selected = role == "CLIENT",
                                    onClick = { role = "CLIENT" },
                                    label = { Text("Comprador / Inquilino") }
                                )
                                FilterChip(
                                    selected = role == "AGENT",
                                    onClick = { role = "AGENT" },
                                    label = { Text("Asesor Inmobiliario (Pro)") }
                                )
                            }

                            if (role == "AGENT") {
                                OutlinedTextField(
                                    value = agencyName,
                                    onValueChange = { agencyName = it },
                                    label = { Text("Inmobiliaria o Firma Asesora") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Button(
                                onClick = {
                                    onSaveProfile(displayName, phone, preferredCurrency, role, agencyName)
                                },
                                enabled = !isBusy,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_profile_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isBusy) "Guardando..." else "Guardar Cambios")
                            }
                        }
                    }
                }

                // Item destacado: Conviértete en Asesor (Abre ventana con los 3 Planes: Inicial, Intermedio y Senior)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = YvyForestPrimary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Yvy Capital Pro",
                                    tint = YvyGoldAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "Yvy Capital Pro • Planes de Asesor",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (currentPlanSpec != null) {
                                            "Plan activo: ${currentPlanSpec.name} (${currentPlanSpec.priceFormatted})"
                                        } else {
                                            "Plan Inicial (G. 99.000) • Intermedio (G. 189.000) • Senior (G. 249.000)"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = YvyGoldAccent
                                    )
                                }
                            }

                            Text(
                                text = "Compara los 3 planes de membresía con seguimiento de visualizaciones, hasta 10 publicaciones activas, participación del 3,5 % al 4,5 % por venta concretada y herramientas exclusivas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Button(
                                onClick = { showBecomeAdvisorDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = YvyGoldAccent,
                                    contentColor = YvyForestDeep
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("become_advisor_btn")
                            ) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Conviértete en Asesor",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Privacidad, Términos, Cerrar Sesión y Eliminación de Cuenta
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Seguridad, Privacidad y Cuenta",
                                style = MaterialTheme.typography.titleMedium
                            )

                            OutlinedButton(
                                onClick = { showPrivacyDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("privacy_terms_btn")
                            ) {
                                Icon(Icons.Default.PrivacyTip, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Política de Privacidad y Términos de Uso")
                            }

                            OutlinedButton(
                                onClick = onSignOut,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sign_out_btn")
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cerrar Sesión")
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            TextButton(
                                onClick = { showDeleteConfirmDialog = true },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("delete_account_btn")
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Solicitar eliminación de mi cuenta y datos")
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 1: YVY CAPITAL PRO (Panel de Asesor, Planes, Videos Senior, Leads y Publicaciones)
            val totalViews = myProperties.sumOf { it.viewsCount }
            val maxListings = currentPlanSpec?.maxActiveListings ?: 0
            val customActiveCount = myProperties.count { !it.id.startsWith("demo-") && !it.id.startsWith("py_") }

            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Header Yvy Capital Pro & Resumen de Membresía
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = YvyForestPrimary
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = YvyGoldAccent
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Yvy Capital Pro • Panel de Asesor",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = AdvisorMemberships.displayNameForTier(currentTier),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = YvyGoldAccent
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = { showBecomeAdvisorDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YvyGoldAccent)
                                ) {
                                    Text("Planes", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatPill(
                                    label = "Cupo Activo",
                                    value = if (maxListings > 0) "$customActiveCount / $maxListings" else "${myProperties.size}"
                                )
                                StatPill(
                                    label = "Visualizaciones",
                                    value = "$totalViews"
                                )
                                StatPill(
                                    label = "Participación",
                                    value = currentPlanSpec?.advisorParticipationPercent ?: "—"
                                )
                                StatPill(
                                    label = "Consultas",
                                    value = "${myInquiries.size}"
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    if (currentPlanSpec == null) {
                                        showBecomeAdvisorDialog = true
                                    } else {
                                        showPublishDialog = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = YvyGoldAccent,
                                    contentColor = YvyForestDeep
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("publish_property_btn")
                            ) {
                                Icon(Icons.Default.AddBusiness, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentPlanSpec == null) {
                                        "Elegir Plan para Publicar Propiedad"
                                    } else {
                                        "Publicar Nueva Propiedad"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 2. Herramienta Exclusiva Plan Senior: Generador de Videos Promocionales MP4
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSeniorMember) {
                                YvyForestDeep
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isSeniorMember) Icons.Default.MovieCreation else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = YvyGoldAccent
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Generador de Videos Promocionales MP4",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSeniorMember) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Beneficio Exclusivo • Membresía Plan Senior (G. 249.000/mes)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = YvyGoldAccent
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Crea videos promocionales en formato vertical (9:16 Reels/TikTok/WhatsApp) u horizontal (16:9) a partir de las imágenes y datos de tu propiedad, con música ambiental y exportación en MP4.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSeniorMember) {
                                    Color.White.copy(alpha = 0.88f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Button(
                                onClick = {
                                    if (isSeniorMember) {
                                        showVideoGeneratorDialog = true
                                    } else {
                                        onMessage("El Generador de Videos Promocionales requiere el Plan Senior (G. 249.000 mensuales).")
                                        showBecomeAdvisorDialog = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSeniorMember) YvyGoldAccent else YvyForestPrimary,
                                    contentColor = if (isSeniorMember) YvyForestDeep else Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_video_generator_btn")
                            ) {
                                Icon(
                                    imageVector = if (isSeniorMember) Icons.Default.Videocam else Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSeniorMember) {
                                        "Crear Video Promocional de Propiedad"
                                    } else {
                                        "Desbloquear con Plan Senior (G. 249.000/mes)"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isSeniorMember && myPromoVideos.isNotEmpty()) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                                Text(
                                    text = "Videos Promocionales Generados (${myPromoVideos.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = YvyGoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                myPromoVideos.forEach { video ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White.copy(alpha = 0.10f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = video.propertyTitle,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Formato ${video.aspectRatio} • ${video.selectedImages.size} escenas • ${video.priceLabel}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                            }
                                            Row {
                                                IconButton(
                                                    onClick = { previewingVideo = video },
                                                    modifier = Modifier.testTag("play_promo_video_btn_${video.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Reproducir video",
                                                        tint = YvyGoldAccent
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { PromoVideoGenerator.sharePromoVideo(context, video) },
                                                    modifier = Modifier.testTag("share_promo_video_btn_${video.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Share,
                                                        contentDescription = "Compartir MP4",
                                                        tint = Color.White
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onDeletePromoVideo(video.id) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar video",
                                                        tint = Color(0xFFFF8A80)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Herramientas según Nivel de Plan (Agente Externo y Colaboración Directa Yvy Capital)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Herramientas Profesionales de tu Membresía",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Modalidad de Agente Externo (Plan Intermedio y Plan Senior)
                            val canUseExternalMode = currentPlanSpec?.supportsExternalAgentMode == true
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Badge,
                                            contentDescription = null,
                                            tint = YvyForestPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Modalidad de Agente Externo a Yvy Capital",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = if (canUseExternalMode) {
                                            "Habilitada en tu ${currentPlanSpec?.name}. Opera como corredor o inmobiliaria externa asociada."
                                        } else {
                                            "Disponible desde el Plan Intermedio (G. 189.000/mes) y Plan Senior."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = profile?.externalAgentMode == true && canUseExternalMode,
                                    onCheckedChange = { enabled ->
                                        if (canUseExternalMode) {
                                            onToggleExternalAgentMode(enabled)
                                        } else {
                                            onMessage("La modalidad de agente externo requiere Plan Intermedio o Plan Senior.")
                                            showBecomeAdvisorDialog = true
                                        }
                                    }
                                )
                            }

                            HorizontalDivider()

                            // Consultas Directas con Asesores de Yvy Capital para Cierre (Plan Senior)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Handshake,
                                            contentDescription = null,
                                            tint = YvyGoldAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Consultas Directas Yvy Capital para Cierre",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = if (isSeniorMember) {
                                            "Incluido en tu Plan Senior: colabora con asesores de Yvy Capital para cerrar ventas."
                                        } else {
                                            "Exclusivo del Plan Senior (G. 249.000/mes)."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (isSeniorMember) {
                                            showSeniorCollaborationDialog = true
                                        } else {
                                            onMessage("Las consultas directas para cierre de operaciones requieren el Plan Senior.")
                                            showBecomeAdvisorDialog = true
                                        }
                                    }
                                ) {
                                    Text(if (isSeniorMember) "Consultar" else "Mejorar")
                                }
                            }
                        }
                    }
                }

                // 4. Consultas de Clientes Potenciales Recibidas
                item {
                    Text(
                        text = "Consultas y Solicitudes de Visita Recibidas (${myInquiries.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (myInquiries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Aún no tienes consultas pendientes para tus publicaciones.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(myInquiries, key = { "agent_inq_${it.id}" }) { inq ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = inq.propertyTitle,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Cliente: ${inq.userName} • Tel: ${inq.userPhone}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${CurrencyFormatter.inquiryTypeLabel(inq.inquiryType)} (${inq.preferredDate}): ${inq.message}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (inq.status != "CLOSED") {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { onUpdateInquiryStatus(inq.id, "CONTACTED") }
                                        ) {
                                            Text("Marcar Contactado")
                                        }
                                        OutlinedButton(
                                            onClick = { onUpdateInquiryStatus(inq.id, "VISIT_SCHEDULED") }
                                        ) {
                                            Text("Agendar Visita")
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Solicitud Cerrada",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Mis Propiedades Publicadas con Seguimiento de Visualizaciones
                item {
                    Text(
                        text = "Mis Anuncios Publicados (${myProperties.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                items(myProperties, key = { "agent_prop_${it.id}" }) { prop ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = prop.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (prop.featured) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = YvyGoldAccent.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = "DESTACADA",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = YvyForestDeep,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${prop.neighborhood}, ${prop.city} • ${CurrencyFormatter.formatPrice(prop.price, prop.currency)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            DeepLinkHandler.shareProperty(context, prop, displayCurrency)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Compartir propiedad",
                                            tint = YvyForestPrimary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteProperty(prop.id) },
                                        modifier = Modifier.testTag("delete_prop_btn_${prop.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar anuncio",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            // Seguimiento de visualizaciones en tiempo real (Incluido en los 3 planes)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = YvyForestPrimary.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = YvyForestPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${prop.viewsCount} visualizaciones acumuladas",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = YvyForestPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal para Publicar Nueva Propiedad en Yvy Capital Pro (con soporte de Publicación Destacada según el plan)
    if (showPublishDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var operationType by remember { mutableStateOf("SALE") }
        var propertyType by remember { mutableStateOf("HOUSE") }
        var currency by remember { mutableStateOf("PYG") }
        var priceText by remember { mutableStateOf("950000000") }
        var department by remember { mutableStateOf("Asunción") }
        var city by remember { mutableStateOf("Asunción") }
        var neighborhood by remember { mutableStateOf("Villa Morra") }
        var areaTotalText by remember { mutableStateOf("360") }
        var bedroomsText by remember { mutableStateOf("3") }
        var bathroomsText by remember { mutableStateOf("3") }
        var featuredListing by remember { mutableStateOf(false) }
        var whatsapp by remember { mutableStateOf(profile?.phone?.ifBlank { "+595981400100" } ?: "+595981400100") }
        val canFeature = AdvisorMemberships.canFeatureForTier(currentTier)

        AlertDialog(
            onDismissRequest = { if (!isBusy) showPublishDialog = false },
            title = { Text("Publicar Inmueble • ${currentPlanSpec?.name ?: "Yvy Capital Pro"}") },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(420.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Título del anuncio (mín. 3 caract.)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = operationType == "SALE",
                                onClick = { operationType = "SALE" },
                                label = { Text("Venta") }
                            )
                            FilterChip(
                                selected = operationType == "RENT",
                                onClick = { operationType = "RENT" },
                                label = { Text("Alquiler") }
                            )
                            FilterChip(
                                selected = currency == "PYG",
                                onClick = { currency = "PYG" },
                                label = { Text("PYG (Gs.)") }
                            )
                            FilterChip(
                                selected = currency == "USD",
                                onClick = { currency = "USD" },
                                label = { Text("USD ($)") }
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                            label = { Text("Precio ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("Ciudad") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = neighborhood,
                                onValueChange = { neighborhood = it },
                                label = { Text("Barrio") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = areaTotalText,
                                onValueChange = { areaTotalText = it.filter { c -> c.isDigit() } },
                                label = { Text("m² Total") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = bedroomsText,
                                onValueChange = { bedroomsText = it.filter { c -> c.isDigit() } },
                                label = { Text("Dorm.") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = bathroomsText,
                                onValueChange = { bathroomsText = it.filter { c -> c.isDigit() } },
                                label = { Text("Baños") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = YvyGoldAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Publicación Destacada",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = if (canFeature) {
                                            "Incluida en tu ${currentPlanSpec?.name}"
                                        } else {
                                            "Requiere Plan Intermedio o Plan Senior"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = featuredListing && canFeature,
                                    onCheckedChange = { featuredListing = it },
                                    enabled = canFeature
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp público de contacto (+595...)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción detallada (mín. 10 caract.)") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val priceVal = priceText.toDoubleOrNull() ?: 1000000.0
                        val areaVal = areaTotalText.toLongOrNull() ?: 120L
                        val bedsVal = bedroomsText.toLongOrNull() ?: 2L
                        val bathsVal = bathroomsText.toLongOrNull() ?: 2L
                        onPublishProperty(
                            title.ifBlank { "Residencia en $neighborhood" },
                            description.ifBlank { "Propiedad verificada en $neighborhood, $city. Excelente ubicación y terminaciones de primera calidad." },
                            operationType,
                            propertyType,
                            priceVal,
                            currency,
                            department,
                            city,
                            neighborhood,
                            "$neighborhood, $city",
                            areaVal,
                            areaVal,
                            bedsVal,
                            bathsVal,
                            2L,
                            listOf("Quincho con parrilla", "Seguridad 24h", "Piscina"),
                            featuredListing && canFeature,
                            profile?.displayName ?: "Asesor Yvy Capital",
                            profile?.agencyName?.ifBlank { "Yvy Capital Pro" } ?: "Yvy Capital Pro",
                            whatsapp
                        ) {
                            showPublishDialog = false
                        }
                    },
                    enabled = !isBusy
                ) {
                    Text(if (isBusy) "Publicando..." else "Publicar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Política de Privacidad y Términos — Yvy Capital") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. Protección de Datos Personales (Paraguay - Ley N° 6534/20): Yvy Capital protege tus datos personales mediante reglas de aislamiento estricto (Zero-Trust) en Firebase Firestore.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. Uso de Información de Contacto: Tus datos de contacto solo se comparten con el asesor inmobiliario cuando envías voluntariamente una consulta o solicitud de visita.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. Eliminación de Cuenta: Puedes solicitar y ejecutar la eliminación inmediata de tu perfil desde esta misma pantalla en cualquier momento.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirmar eliminación de cuenta") },
            text = {
                Text("¿Estás seguro de que deseas eliminar los datos de tu perfil en Yvy Capital y cerrar tu sesión actual? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sí, eliminar mi cuenta")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Ventana de 3 Planes de Membresía ("Conviértete en Asesor")
    if (showBecomeAdvisorDialog) {
        AdvisorMembershipPlansDialog(
            currentProfile = profile,
            isBusy = isBusy,
            onSelectAndActivatePlan = { chosenTier, agency, externalMode ->
                onActivateAdvisorPlan(
                    chosenTier,
                    profile?.displayName ?: "Asesor Yvy Capital",
                    profile?.phone?.ifBlank { "+595981" } ?: "+595981",
                    agency,
                    externalMode
                )
                showBecomeAdvisorDialog = false
                selectedTab = 1
            },
            onDismiss = { showBecomeAdvisorDialog = false }
        )
    }

    // Generador de Videos Promocionales (Exclusivo Plan Senior)
    if (showVideoGeneratorDialog && isSeniorMember) {
        val candidateProps = myProperties.ifEmpty { catalogProperties }
        SeniorVideoGeneratorDialog(
            availableProperties = candidateProps,
            displayCurrency = displayCurrency,
            isBusy = isBusy,
            onGenerateVideo = { prop, ratio, scenes, music, headline, preferAi ->
                onGeneratePromoVideo(prop, ratio, scenes, music, headline, preferAi) { createdVideo ->
                    showVideoGeneratorDialog = false
                    previewingVideo = createdVideo
                }
            },
            onDismiss = { showVideoGeneratorDialog = false }
        )
    }

    // Reproductor / Vista previa del video promocional generado
    val currentPreviewVideo = previewingVideo
    if (currentPreviewVideo != null) {
        PromoVideoPreviewDialog(
            video = currentPreviewVideo,
            onShareMp4 = { vid ->
                PromoVideoGenerator.sharePromoVideo(context, vid)
            },
            onDismiss = { previewingVideo = null }
        )
    }

    // Mesa de Cierre Directo con Asesores de Yvy Capital (Exclusivo Plan Senior)
    if (showSeniorCollaborationDialog && isSeniorMember) {
        SeniorDirectCollaborationDialog(
            advisorName = profile?.displayName ?: "Asesor Senior",
            advisorPhone = profile?.phone?.ifBlank { "+595981" } ?: "+595981",
            properties = myProperties.ifEmpty { catalogProperties },
            onSendCollaborationMessage = { msg -> onMessage(msg) },
            onDismiss = { showSeniorCollaborationDialog = false }
        )
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = YvyGoldAccent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}
