package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.util.PromoVideoGenerator
import com.example.ui.viewmodel.UiState
import com.google.firebase.auth.FirebaseAuth

enum class ProfileSubscreen {
    MENU,
    PERFIL,
    PUBLICAR,
    INFORMACION_CUENTA,
    CONTACTAR_IA,
    CONFIGURACION,
    CONVERTITE_EN_PRO
}

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
    val initialRoute = remember(initialTab) {
        when (initialTab) {
            1, 6 -> ProfileSubscreen.CONVERTITE_EN_PRO
            2 -> ProfileSubscreen.PUBLICAR
            else -> ProfileSubscreen.MENU
        }
    }
    var activeSubscreen by remember(initialRoute) { mutableStateOf(initialRoute) }

    val profile = (userProfileState as? UiState.Success)?.data
    val currentTier = profile?.planTier ?: AdvisorMemberships.TIER_FREE
    val currentPlanSpec = AdvisorMemberships.getSpec(currentTier)
    val isSeniorMember = AdvisorMemberships.isSeniorTier(currentTier)

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showBecomeAdvisorDialog by remember { mutableStateOf(false) }
    var showVideoGeneratorDialog by remember { mutableStateOf(false) }
    var previewingVideo by remember { mutableStateOf<PropertyPromoVideo?>(null) }
    var showSeniorCollaborationDialog by remember { mutableStateOf(false) }

    val myProperties = (agentPropertiesState as? UiState.Success)?.data ?: emptyList()
    val realActiveCount = remember(myProperties) {
        myProperties.count { it.status.equals("active", ignoreCase = true) && !it.id.startsWith("demo-") }
    }
    val myInquiries = (agentInquiriesState as? UiState.Success)?.data ?: emptyList()
    val myPromoVideos = (agentPromoVideosState as? UiState.Success)?.data ?: emptyList()

    // BackHandler returns to the main Perfil menu when inside any of the 6 subscreens
    BackHandler(enabled = activeSubscreen != ProfileSubscreen.MENU) {
        activeSubscreen = ProfileSubscreen.MENU
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_pro_screen")
    ) {
        when (activeSubscreen) {
            ProfileSubscreen.MENU -> {
                ProfileMainHubMenu(
                    profile = profile,
                    activePublicationsCount = realActiveCount,
                    onSelectSubscreen = { activeSubscreen = it }
                )
            }

            ProfileSubscreen.PERFIL -> {
                PerfilPersonalSubscreen(
                    profile = profile,
                    activePropertiesCount = realActiveCount,
                    isBusy = isBusy,
                    onBack = { activeSubscreen = ProfileSubscreen.MENU },
                    onSavePersonalData = { fullName, phone ->
                        onSaveProfile(
                            fullName,
                            phone,
                            profile?.preferredCurrency ?: "PYG",
                            profile?.role ?: "CLIENT",
                            profile?.agencyName ?: ""
                        )
                    },
                    onGoToPublish = { activeSubscreen = ProfileSubscreen.PUBLICAR }
                )
            }

            ProfileSubscreen.PUBLICAR -> {
                PublicarPropiedadSubscreen(
                    profile = profile,
                    myProperties = myProperties,
                    isBusy = isBusy,
                    onBack = { activeSubscreen = ProfileSubscreen.MENU },
                    onOpenAdvisorPlans = {
                        activeSubscreen = ProfileSubscreen.CONVERTITE_EN_PRO
                        showBecomeAdvisorDialog = true
                    },
                    onOpenSeniorVideoGenerator = {
                        if (isSeniorMember) {
                            showVideoGeneratorDialog = true
                        }
                    },
                    onDeleteProperty = onDeleteProperty,
                    onMessage = onMessage,
                    onPublishProperty = onPublishProperty
                )
            }

            ProfileSubscreen.INFORMACION_CUENTA -> {
                InformacionCuentaSubscreen(
                    profile = profile,
                    onBack = { activeSubscreen = ProfileSubscreen.MENU },
                    onSignOut = onSignOut,
                    onRequestDeleteAccount = { showDeleteConfirmDialog = true }
                )
            }

            ProfileSubscreen.CONTACTAR_IA -> {
                ContactarIaSubscreen(
                    onBack = { activeSubscreen = ProfileSubscreen.MENU }
                )
            }

            ProfileSubscreen.CONFIGURACION -> {
                ConfiguracionSubscreen(
                    profile = profile,
                    isBusy = isBusy,
                    onBack = { activeSubscreen = ProfileSubscreen.MENU },
                    onToggleExternalAgentMode = onToggleExternalAgentMode,
                    onOpenPrivacyDialog = { showPrivacyDialog = true }
                )
            }

            ProfileSubscreen.CONVERTITE_EN_PRO -> {
                ConvertiteEnProSubscreen(
                    profile = profile,
                    myProperties = myProperties,
                    myInquiries = myInquiries,
                    myPromoVideos = myPromoVideos,
                    isBusy = isBusy,
                    onBack = { activeSubscreen = ProfileSubscreen.MENU },
                    onOpenAdvisorPlansModal = { showBecomeAdvisorDialog = true },
                    onOpenPublishSubscreen = { activeSubscreen = ProfileSubscreen.PUBLICAR },
                    onOpenVideoGenerator = { showVideoGeneratorDialog = true },
                    onOpenSeniorCollaboration = { showSeniorCollaborationDialog = true },
                    onPreviewVideo = { previewingVideo = it },
                    onShareVideo = { PromoVideoGenerator.sharePromoVideo(context, it) },
                    onDeleteVideo = onDeletePromoVideo,
                    onToggleExternalAgentMode = onToggleExternalAgentMode,
                    onUpdateInquiryStatus = onUpdateInquiryStatus
                )
            }
        }
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
                        text = "3. Eliminación de Cuenta: Puedes solicitar y ejecutar la eliminación inmediata de tu perfil desde Información de la cuenta en cualquier momento.",
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

    // Ventana de 3 Planes de Membresía ("Convertite en Pro")
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
                activeSubscreen = ProfileSubscreen.CONVERTITE_EN_PRO
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

/**
 * 11. ESTRUCTURA PRINCIPAL DE LA PESTAÑA «PERFIL»
 * Menú limpio con las 6 opciones de navegación claramente identificadas:
 * 1. Perfil
 * 2. Publicar
 * 3. Información de la cuenta
 * 4. Contactar con la IA
 * 5. Configuración
 * 6. Convertite en Pro
 */
@Composable
private fun ProfileMainHubMenu(
    profile: UserProfile?,
    activePublicationsCount: Int,
    onSelectSubscreen: (ProfileSubscreen) -> Unit
) {
    val authUser = remember { FirebaseAuth.getInstance().currentUser }
    val displayName = profile?.displayName?.ifBlank { authUser?.displayName ?: "Usuario Yvy Capital" }
        ?: (authUser?.displayName ?: "Usuario Yvy Capital")
    val email = profile?.email?.ifBlank { authUser?.email ?: "" } ?: (authUser?.email ?: "")
    val currentTier = profile?.planTier ?: AdvisorMemberships.TIER_FREE
    val currentPlanSpec = AdvisorMemberships.getSpec(currentTier)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_main_menu_list")
    ) {
        // Cabecera de identidad del usuario autenticado
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = YvyForestDeep),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = YvyGoldAccent.copy(alpha = 0.22f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = YvyGoldAccent,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (email.isNotBlank()) {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.82f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = YvyGoldAccent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = currentPlanSpec?.let { "${it.name} • Publicaciones activas: $activePublicationsCount" }
                                    ?: "Cuenta Estándar • Publicaciones activas: $activePublicationsCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = YvyGoldAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 1. Perfil
        item {
            ProfileHubMenuItemCard(
                title = "Perfil",
                subtitle = "Información personal y resumen del vendedor",
                icon = Icons.Default.Person,
                testTag = "profile_menu_perfil",
                onClick = { onSelectSubscreen(ProfileSubscreen.PERFIL) }
            )
        }

        // 2. Publicar
        item {
            ProfileHubMenuItemCard(
                title = "Publicar",
                subtitle = "Nueva propiedad, subida de fotos y gestión de anuncios",
                icon = Icons.Default.AddBusiness,
                testTag = "profile_menu_publicar",
                onClick = { onSelectSubscreen(ProfileSubscreen.PUBLICAR) }
            )
        }

        // 3. Información de la cuenta
        item {
            ProfileHubMenuItemCard(
                title = "Información de la cuenta",
                subtitle = "Datos de autenticación, membresía y seguridad",
                icon = Icons.Default.VerifiedUser,
                testTag = "profile_menu_cuenta",
                onClick = { onSelectSubscreen(ProfileSubscreen.INFORMACION_CUENTA) }
            )
        }

        // 4. Contactar con la IA
        item {
            ProfileHubMenuItemCard(
                title = "Contactar con la IA",
                subtitle = "Consultas inmobiliarias asistidas por inteligencia artificial",
                icon = Icons.Default.AutoAwesome,
                testTag = "profile_menu_ia",
                onClick = { onSelectSubscreen(ProfileSubscreen.CONTACTAR_IA) }
            )
        }

        // 5. Configuración
        item {
            ProfileHubMenuItemCard(
                title = "Configuración",
                subtitle = "Preferencias operativas, modo agente externo y privacidad",
                icon = Icons.Default.Settings,
                testTag = "profile_menu_configuracion",
                onClick = { onSelectSubscreen(ProfileSubscreen.CONFIGURACION) }
            )
        }

        // 6. Convertite en Pro
        item {
            ProfileHubMenuItemCard(
                title = "Convertite en Pro",
                subtitle = "Planes Inicial, Intermedio y Senior para asesores e inmobiliarias",
                icon = Icons.Default.WorkspacePremium,
                isHighlighted = true,
                testTag = "profile_menu_pro",
                onClick = { onSelectSubscreen(ProfileSubscreen.CONVERTITE_EN_PRO) }
            )
        }
    }
}

@Composable
private fun ProfileHubMenuItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isHighlighted: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                YvyForestDeep
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isHighlighted) {
            BorderStroke(1.5.dp, YvyGoldAccent)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isHighlighted) {
                    YvyGoldAccent.copy(alpha = 0.2f)
                } else {
                    YvyForestPrimary.copy(alpha = 0.1f)
                },
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isHighlighted) YvyGoldAccent else YvyForestPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlighted) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isHighlighted) Color.White.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Abrir $title",
                tint = if (isHighlighted) YvyGoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 18. SUBPANTALLA «CONVERTITE EN PRO»
 * Contiene los 3 planes de membresía (Inicial, Intermedio y Senior), beneficios,
 * herramientas exclusivas Senior y gestión de consultas de clientes.
 */
@Composable
private fun ConvertiteEnProSubscreen(
    profile: UserProfile?,
    myProperties: List<Property>,
    myInquiries: List<PropertyInquiry>,
    myPromoVideos: List<PropertyPromoVideo>,
    isBusy: Boolean,
    onBack: () -> Unit,
    onOpenAdvisorPlansModal: () -> Unit,
    onOpenPublishSubscreen: () -> Unit,
    onOpenVideoGenerator: () -> Unit,
    onOpenSeniorCollaboration: () -> Unit,
    onPreviewVideo: (PropertyPromoVideo) -> Unit,
    onShareVideo: (PropertyPromoVideo) -> Unit,
    onDeleteVideo: (String) -> Unit,
    onToggleExternalAgentMode: (Boolean) -> Unit,
    onUpdateInquiryStatus: (inquiryId: String, newStatus: String) -> Unit
) {
    val currentTier = profile?.planTier ?: AdvisorMemberships.TIER_FREE
    val currentPlanSpec = AdvisorMemberships.getSpec(currentTier)
    val isSeniorMember = AdvisorMemberships.isSeniorTier(currentTier)
    val totalViews = remember(myProperties) { myProperties.sumOf { it.viewsCount } }

    Column(modifier = Modifier.fillMaxSize()) {
        SubscreenTopBar(
            title = "Convertite en Pro",
            subtitle = "Planes para asesores e inmobiliarias y herramientas avanzadas",
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Cabecera de Membresía y Estadísticas Pro
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = YvyForestDeep)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = YvyGoldAccent
                                    )
                                    Text(
                                        text = currentPlanSpec?.name ?: "Yvy Capital Pro",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (currentPlanSpec != null) {
                                        "${currentPlanSpec.priceFormatted} • Participación ${currentPlanSpec.advisorParticipationPercent}"
                                    } else {
                                        "Elige tu plan Inicial, Intermedio o Senior para publicar y acceder a comisiones"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenAdvisorPlansModal,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = YvyGoldAccent),
                                modifier = Modifier.testTag("become_advisor_btn")
                            ) {
                                Text(if (currentPlanSpec == null) "Ver Planes" else "Cambiar Plan")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val maxAllowed = currentPlanSpec?.maxActiveListings ?: 0
                            StatPill(
                                label = "Activas / Límite",
                                value = "${myProperties.size} / $maxAllowed"
                            )
                            StatPill(
                                label = "Visualizaciones",
                                value = totalViews.toString()
                            )
                            StatPill(
                                label = "Comisión",
                                value = currentPlanSpec?.advisorParticipationPercent ?: "—"
                            )
                            StatPill(
                                label = "Consultas",
                                value = myInquiries.size.toString()
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onOpenPublishSubscreen,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = YvyGoldAccent,
                                    contentColor = YvyForestDeep
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("publish_property_btn")
                            ) {
                                Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ir a Publicar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Resumen comparativo de los 3 planes de membresía
            item {
                Text(
                    text = "Planes disponibles en Yvy Capital Pro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(AdvisorMemberships.ALL_PLANS, key = { it.code }) { plan ->
                val isCurrent = currentTier == plan.code
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) {
                            YvyForestPrimary.copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) YvyForestPrimary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plan.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${plan.priceFormatted} • Máx. ${plan.maxActiveListings} publicaciones",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = YvyForestPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (isCurrent) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = YvyForestPrimary
                                ) {
                                    Text(
                                        text = "PLAN ACTIVO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onOpenAdvisorPlansModal,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Seleccionar")
                                }
                            }
                        }

                        plan.benefits.forEach { benefit ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = YvyForestPrimary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Text(
                                    text = benefit,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Herramientas Exclusivas del Plan Senior (Videos IA/MP4 + Colaboración Directa)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = if (isSeniorMember) YvyForestPrimary else MaterialTheme.colorScheme.outline
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Generador de Videos Promocionales MP4 e IA",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isSeniorMember) {
                                        "Exclusivo Plan Senior Activo: convierte las fotos de tu propiedad en un video promocional MP4."
                                    } else {
                                        "Función bloqueada: requiere suscripción activa al Plan Senior (G. 249.000/mes)."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSeniorMember) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onOpenVideoGenerator,
                                    enabled = !isBusy,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_senior_video_generator_btn")
                                ) {
                                    Icon(Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Crear Video MP4")
                                }
                                OutlinedButton(
                                    onClick = onOpenSeniorCollaboration,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mesa Senior")
                                }
                            }

                            if (myPromoVideos.isNotEmpty()) {
                                HorizontalDivider()
                                Text(
                                    text = "Videos Generados (${myPromoVideos.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                myPromoVideos.forEach { vid ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
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
                                                Text(
                                                    text = vid.propertyTitle,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Formato ${vid.aspectRatio} • ${vid.durationSeconds}s",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Row {
                                                IconButton(onClick = { onPreviewVideo(vid) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Ver video",
                                                        tint = YvyForestPrimary
                                                    )
                                                }
                                                IconButton(onClick = { onShareVideo(vid) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Share,
                                                        contentDescription = "Compartir video MP4",
                                                        tint = YvyForestPrimary
                                                    )
                                                }
                                                IconButton(onClick = { onDeleteVideo(vid.id) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar video",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = onOpenAdvisorPlansModal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unlock_senior_video_btn")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Actualizar a Plan Senior para Crear Videos con IA")
                            }
                        }
                    }
                }
            }

            // Consultas recibidas de clientes
            item {
                Text(
                    text = "Consultas y Solicitudes de Clientes (${myInquiries.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (myInquiries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "Aún no tienes solicitudes pendientes. Cuando un interesado solicite una visita en tus propiedades, aparecerá aquí en tiempo real.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(myInquiries, key = { it.id }) { inq ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = inq.propertyTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cliente: ${inq.userName} • Tel: ${inq.userPhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = inq.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    "CONTACTED" to "Contactado",
                                    "VISIT_SCHEDULED" to "Visita Agendada",
                                    "CLOSED" to "Cerrado"
                                ).forEach { (statusKey, statusLabel) ->
                                    FilterChip(
                                        selected = inq.status == statusKey,
                                        onClick = { onUpdateInquiryStatus(inq.id, statusKey) },
                                        label = { Text(statusLabel) }
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
