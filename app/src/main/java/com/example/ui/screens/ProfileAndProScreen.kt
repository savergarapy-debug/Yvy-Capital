package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AddBusiness
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AdvisorMemberships
import com.example.data.model.EffectivePlan
import com.example.data.model.MembershipRequest
import com.example.data.model.Property
import com.example.data.model.PropertyInquiry
import com.example.data.model.PropertyPromoVideo
import com.example.data.model.UserProfile
import com.example.ui.components.AdvisorMembershipPlansDialog
import com.example.ui.components.EmptyState
import com.example.ui.components.PromoVideoPreviewDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.SeniorDirectCollaborationDialog
import com.example.ui.components.SeniorVideoGeneratorDialog
import com.example.ui.components.StaffBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusBadgeTone
import com.example.ui.components.YvyButton
import com.example.ui.components.YvyButtonVariant
import com.example.ui.components.YvyChip
import com.example.ui.components.YvyTopBar
import com.example.ui.theme.YvyPriceCardStyle
import com.example.ui.theme.YvyProGold
import com.example.ui.theme.YvyShapes
import com.example.ui.theme.YvySpacing
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
    userMembershipRequests: List<MembershipRequest> = emptyList(),
    effectivePlan: EffectivePlan = EffectivePlan(),
    isPublicStaff: Boolean = false,
    isStaffUser: (String) -> Boolean = { false },
    catalogProperties: List<Property> = emptyList(),
    displayCurrency: String = "PYG",
    isBusy: Boolean,
    initialTab: Int = 0,
    onSaveProfile: (displayName: String, phone: String, preferredCurrency: String, role: String, agencyName: String) -> Unit,
    onRequestPlan: (planTier: String, paymentMethod: String, paymentReference: String) -> Unit = { _, _, _ -> },
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
    val isStaff = effectivePlan.isStaff || isPublicStaff
    val currentTier = effectivePlan.tier
    val isSeniorMember = isStaff || AdvisorMemberships.isSeniorTier(currentTier)

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

    BackHandler(enabled = activeSubscreen != ProfileSubscreen.MENU) {
        activeSubscreen = ProfileSubscreen.MENU
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_pro_screen")
    ) {
        when (activeSubscreen) {
            ProfileSubscreen.MENU -> {
                ProfileMainHubMenu(
                    profile = profile,
                    effectivePlan = effectivePlan,
                    isStaff = isStaff,
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
                    effectivePlan = effectivePlan,
                    myProperties = myProperties,
                    isBusy = isBusy,
                    onBack = { activeSubscreen = ProfileSubscreen.MENU },
                    onOpenAdvisorPlans = {
                        activeSubscreen = ProfileSubscreen.CONVERTITE_EN_PRO
                        if (!isStaff) {
                            showBecomeAdvisorDialog = true
                        }
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
                    effectivePlan = effectivePlan,
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
                    effectivePlan = effectivePlan,
                    isBusy = isBusy,
                    onBack = { activeSubscreen = ProfileSubscreen.MENU },
                    onToggleExternalAgentMode = onToggleExternalAgentMode,
                    onOpenPrivacyDialog = { showPrivacyDialog = true }
                )
            }

            ProfileSubscreen.CONVERTITE_EN_PRO -> {
                ConvertiteEnProSubscreen(
                    profile = profile,
                    effectivePlan = effectivePlan,
                    isStaff = isStaff,
                    myProperties = myProperties,
                    myInquiries = myInquiries,
                    myPromoVideos = myPromoVideos,
                    userMembershipRequests = userMembershipRequests,
                    isBusy = isBusy,
                    isStaffUser = isStaffUser,
                    onBack = { activeSubscreen = ProfileSubscreen.MENU },
                    onOpenAdvisorPlansModal = { if (!isStaff) showBecomeAdvisorDialog = true },
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
                YvyButton(
                    text = "Entendido",
                    onClick = { showPrivacyDialog = false },
                    variant = YvyButtonVariant.PRIMARY
                )
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

    if (showBecomeAdvisorDialog) {
        AdvisorMembershipPlansDialog(
            currentProfile = profile,
            effectivePlan = effectivePlan,
            isBusy = isBusy,
            onRequestPlan = { chosenTier, paymentMethod, paymentReference ->
                onRequestPlan(chosenTier, paymentMethod, paymentReference)
                showBecomeAdvisorDialog = false
                activeSubscreen = ProfileSubscreen.CONVERTITE_EN_PRO
            },
            onDismiss = { showBecomeAdvisorDialog = false }
        )
    }

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
private fun ProfileMainHubMenu(
    profile: UserProfile?,
    effectivePlan: EffectivePlan,
    isStaff: Boolean,
    activePublicationsCount: Int,
    onSelectSubscreen: (ProfileSubscreen) -> Unit
) {
    val authUser = remember { FirebaseAuth.getInstance().currentUser }
    val displayName = profile?.displayName?.ifBlank { authUser?.displayName ?: "Usuario Yvy Capital" }
        ?: (authUser?.displayName ?: "Usuario Yvy Capital")
    val email = profile?.email?.ifBlank { authUser?.email ?: "" } ?: (authUser?.email ?: "")
    val currentTier = effectivePlan.tier
    val currentPlanSpec = AdvisorMemberships.getSpec(currentTier)

    Column(modifier = Modifier.fillMaxSize()) {
        YvyTopBar(title = "Mi perfil")

        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = YvySpacing.ScreenHorizontal,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_main_menu_list")
        ) {
            // Flat User Summary Card (#FFFFFF surface, 1.dp #E6E1D8 border)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = YvyShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (isStaff) {
                                    StaffBadge()
                                }
                            }
                            if (email.isNotBlank()) {
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isStaff) {
                                    StatusBadge(
                                        text = "Plan Senior · Personal Yvy Capital",
                                        tone = StatusBadgeTone.MONTE_PRIMARY
                                    )
                                } else {
                                    StatusBadge(
                                        text = currentPlanSpec?.name ?: "Cuenta Estándar",
                                        tone = if (currentPlanSpec != null) StatusBadgeTone.PRO_GOLD else StatusBadgeTone.NEUTRAL_OUTLINED
                                    )
                                }
                                StatusBadge(
                                    text = "Activas: $activePublicationsCount",
                                    tone = StatusBadgeTone.NEUTRAL_OUTLINED
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
                    icon = Icons.Outlined.Person,
                    testTag = "profile_menu_perfil",
                    onClick = { onSelectSubscreen(ProfileSubscreen.PERFIL) }
                )
            }

            // 2. Publicar
            item {
                ProfileHubMenuItemCard(
                    title = "Publicar",
                    icon = Icons.Outlined.AddBusiness,
                    badgeText = "$activePublicationsCount activas",
                    testTag = "profile_menu_publicar",
                    onClick = { onSelectSubscreen(ProfileSubscreen.PUBLICAR) }
                )
            }

            // 3. Información de la cuenta
            item {
                ProfileHubMenuItemCard(
                    title = "Información de la cuenta",
                    icon = Icons.Outlined.VerifiedUser,
                    testTag = "profile_menu_cuenta",
                    onClick = { onSelectSubscreen(ProfileSubscreen.INFORMACION_CUENTA) }
                )
            }

            // 4. Contactar con la IA
            item {
                ProfileHubMenuItemCard(
                    title = "Contactar con la IA",
                    icon = Icons.Outlined.AutoAwesome,
                    testTag = "profile_menu_ia",
                    onClick = { onSelectSubscreen(ProfileSubscreen.CONTACTAR_IA) }
                )
            }

            // 5. Configuración
            item {
                ProfileHubMenuItemCard(
                    title = "Configuración",
                    icon = Icons.Outlined.Settings,
                    testTag = "profile_menu_configuracion",
                    onClick = { onSelectSubscreen(ProfileSubscreen.CONFIGURACION) }
                )
            }

            // 6. Convertite en Pro o Beneficios de Personal
            item {
                ProfileHubMenuItemCard(
                    title = if (isStaff) "Beneficios del Personal" else "Convertite en Pro",
                    icon = Icons.Outlined.WorkspacePremium,
                    badgeText = if (isStaff) "Plan Senior" else (currentPlanSpec?.name ?: "Pro"),
                    isHighlighted = true,
                    testTag = "profile_menu_pro",
                    onClick = { onSelectSubscreen(ProfileSubscreen.CONVERTITE_EN_PRO) }
                )
            }
        }
    }
}

@Composable
private fun ProfileHubMenuItemCard(
    title: String,
    icon: ImageVector,
    badgeText: String? = null,
    isHighlighted: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = YvyShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isHighlighted) YvyProGold else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlighted) YvyProGold else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (badgeText != null) {
                StatusBadge(
                    text = badgeText,
                    tone = if (isHighlighted) StatusBadgeTone.PRO_GOLD else StatusBadgeTone.NEUTRAL_OUTLINED
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Abrir $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * SUBPANTALLA «CONVERTITE EN PRO»
 * Cumple contraste WCAG AA (4.5:1) en tarjetas de planes Pro: título en #1C1B19, precio en #B5532E (tnum),
 * beneficios en #1C1B19 y botón de selección de alto contraste.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConvertiteEnProSubscreen(
    profile: UserProfile?,
    effectivePlan: EffectivePlan,
    isStaff: Boolean,
    myProperties: List<Property>,
    myInquiries: List<PropertyInquiry>,
    myPromoVideos: List<PropertyPromoVideo>,
    userMembershipRequests: List<MembershipRequest> = emptyList(),
    isBusy: Boolean,
    isStaffUser: (String) -> Boolean = { false },
    onBack: () -> Unit,
    onOpenAdvisorPlansModal: () -> Unit,
    onOpenPublishSubscreen: () -> Unit,
    onOpenVideoGenerator: () -> Unit,
    onOpenSeniorCollaboration: () -> Unit,
    onPreviewVideo: (PropertyPromoVideo) -> Unit,
    onShareVideo: (PropertyPromoVideo) -> Unit,
    onDeleteVideo: (String) -> Unit,
    @Suppress("UNUSED_PARAMETER") onToggleExternalAgentMode: (Boolean) -> Unit,
    onUpdateInquiryStatus: (inquiryId: String, newStatus: String) -> Unit
) {
    val currentTier = effectivePlan.tier
    val currentPlanSpec = AdvisorMemberships.getSpec(currentTier)
    val isSeniorMember = isStaff || AdvisorMemberships.isSeniorTier(currentTier)
    val totalViews = remember(myProperties) { myProperties.sumOf { it.viewsCount } }

    Column(modifier = Modifier.fillMaxSize()) {
        SubscreenTopBar(
            title = if (isStaff) "Beneficios del Personal" else "Convertite en Pro",
            subtitle = "",
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = YvySpacing.ScreenHorizontal,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val latestReq = userMembershipRequests.firstOrNull()
            if (latestReq != null && !isStaff) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("membership_request_status_card"),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(
                            containerColor = when (latestReq.status) {
                                "APPROVED" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                "REJECTED" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            }
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                                Text(
                                    text = "Estado de Solicitud de Membresía",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                StatusBadge(
                                    text = when (latestReq.status) {
                                        "APPROVED" -> "APROBADA"
                                        "REJECTED" -> "RECHAZADA"
                                        else -> "PENDIENTE"
                                    },
                                    tone = when (latestReq.status) {
                                        "APPROVED" -> StatusBadgeTone.MONTE_PRIMARY
                                        "REJECTED" -> StatusBadgeTone.NEUTRAL_OUTLINED
                                        else -> StatusBadgeTone.PRO_GOLD
                                    }
                                )
                            }
                            if (latestReq.status == "PENDING") {
                                Text(
                                    text = "Solicitud enviada. Un administrador de Yvy Capital la activará al confirmar el pago.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "Plan: ${AdvisorMemberships.displayNameForTier(latestReq.planTier)} • Pago: ${latestReq.paymentMethod} • Ref: ${latestReq.paymentReference}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Cabecera de Membresía y Estadísticas Pro (flat surface card, high contrast)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = YvyShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            Text(
                                text = if (isStaff) "Plan Senior · Personal Yvy Capital" else (currentPlanSpec?.name ?: "Yvy Capital Pro"),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isStaff) {
                                StaffBadge()
                            } else {
                                StatusBadge(
                                    text = if (currentPlanSpec != null) "Membresía activa" else "Cuenta Estándar",
                                    tone = if (currentPlanSpec != null) StatusBadgeTone.PRO_GOLD else StatusBadgeTone.NEUTRAL_OUTLINED
                                )
                            }
                        }

                        if (isStaff) {
                            Text(
                                text = "Membresía bonificada para personal · Participación ${effectivePlan.participationPercent}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (currentPlanSpec != null) {
                            Text(
                                text = "${currentPlanSpec.priceFormatted} · Participación ${currentPlanSpec.advisorParticipationPercent}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 4 Indicadores con el mismo ancho exacto y etiquetas de una sola línea
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val maxAllowed = if (isStaff) 10 else (currentPlanSpec?.maxActiveListings ?: 0)
                            StatPill(
                                label = "Activas",
                                value = "${myProperties.size}/$maxAllowed",
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "Vistas",
                                value = totalViews.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "Comisión",
                                value = effectivePlan.participationPercent,
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "Consultas",
                                value = myInquiries.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            YvyButton(
                                text = "Ir a publicar",
                                onClick = onOpenPublishSubscreen,
                                variant = YvyButtonVariant.PRIMARY,
                                leadingIcon = Icons.Outlined.AddBusiness,
                                fullWidth = isStaff,
                                modifier = if (isStaff) Modifier.fillMaxWidth() else Modifier.weight(1f),
                                testTag = "publish_property_btn"
                            )

                            if (!isStaff) {
                                YvyButton(
                                    text = if (currentPlanSpec == null) "Ver planes" else "Cambiar plan",
                                    onClick = onOpenAdvisorPlansModal,
                                    variant = YvyButtonVariant.SECONDARY_OUTLINED,
                                    modifier = Modifier.weight(1f),
                                    testTag = "become_advisor_btn"
                                )
                            }
                        }
                    }
                }
            }

            if (!isStaff) {
                // Planes disponibles en Yvy Capital Pro
                item {
                    SectionHeader(title = "Planes disponibles en Yvy Capital Pro")
                }

                items(AdvisorMemberships.ALL_PLANS, key = { it.code }) { plan ->
                    val isCurrent = currentTier == plan.code
                    val isSenior = plan.code == AdvisorMemberships.TIER_SENIOR

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isCurrent || isSenior) 1.5.dp else 1.dp,
                            color = when {
                                isCurrent -> MaterialTheme.colorScheme.primary
                                isSenior -> YvyProGold
                                else -> MaterialTheme.colorScheme.outline
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = plan.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSenior) {
                                        StatusBadge(
                                            text = "Recomendado",
                                            tone = StatusBadgeTone.PRO_GOLD
                                        )
                                    } else if (isCurrent) {
                                        StatusBadge(
                                            text = "Activo",
                                            tone = StatusBadgeTone.MONTE_PRIMARY
                                        )
                                    }
                                }

                                // Precio en tierra roja (#B5532E) con cifras tabulares (tnum) y alto contraste WCAG AA
                                Text(
                                    text = plan.priceFormatted,
                                    style = YvyPriceCardStyle,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    softWrap = false
                                )

                                Text(
                                    text = "Máx. ${plan.maxActiveListings} publicaciones activas · Comisión ${plan.advisorParticipationPercent}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                            // Lista de beneficios con texto #1C1B19 sobre fondo #FFFFFF
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                plan.benefits.forEach { benefit ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Text(
                                            text = benefit,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Botón a lo ancho de la tarjeta con alto contraste WCAG AA
                            YvyButton(
                                text = if (isCurrent) "Plan actual" else "Seleccionar",
                                onClick = onOpenAdvisorPlansModal,
                                variant = if (isCurrent) YvyButtonVariant.SECONDARY_OUTLINED else YvyButtonVariant.PRIMARY,
                                fullWidth = true,
                                testTag = "plan_select_btn_${plan.code}"
                            )
                        }
                    }
                }
            } else {
                // Tarjeta informativa para personal de Yvy Capital (sin botones de pago ni upgrade)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Plan Senior · Personal Yvy Capital",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tu cuenta de personal cuenta con todos los beneficios del Plan Senior bonificados permanentemente: hasta 10 inmuebles en catálogo, generación de videos promocionales MP4 y acceso a Mesa de Cierre.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Herramientas Exclusivas del Plan Senior
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = YvyShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                imageVector = Icons.Outlined.Videocam,
                                contentDescription = null,
                                tint = YvyProGold
                            )
                            Text(
                                text = "Generador de videos promocionales MP4",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            StatusBadge(
                                text = "Senior",
                                tone = StatusBadgeTone.PRO_GOLD
                            )
                        }

                        Text(
                            text = if (isStaff) {
                                "Beneficio exclusivo de personal de Yvy Capital: convierte las fotos de tu propiedad en un video promocional MP4."
                            } else if (isSeniorMember) {
                                "Exclusivo Plan Senior activo: convierte las fotos de tu propiedad en un video promocional MP4."
                            } else {
                                "Requiere suscripción activa al Plan Senior (G. 249.000/mes)."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isSeniorMember) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                YvyButton(
                                    text = "Crear video MP4",
                                    onClick = onOpenVideoGenerator,
                                    enabled = !isBusy,
                                    variant = YvyButtonVariant.PRIMARY,
                                    leadingIcon = Icons.Outlined.MovieCreation,
                                    modifier = Modifier.weight(1f),
                                    testTag = "open_senior_video_generator_btn"
                                )
                                YvyButton(
                                    text = "Mesa Senior",
                                    onClick = onOpenSeniorCollaboration,
                                    variant = YvyButtonVariant.SECONDARY_OUTLINED,
                                    leadingIcon = Icons.Outlined.Handshake,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (myPromoVideos.isNotEmpty()) {
                                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    text = "Videos generados (${myPromoVideos.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                myPromoVideos.forEach { vid ->
                                    Surface(
                                        shape = YvyShapes.FieldAndButton,
                                        color = MaterialTheme.colorScheme.background,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                                                    text = vid.propertyTitle,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Formato ${vid.aspectRatio} · ${vid.durationSeconds}s",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Row {
                                                IconButton(onClick = { onPreviewVideo(vid) }) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.PlayArrow,
                                                        contentDescription = "Ver video",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                IconButton(onClick = { onShareVideo(vid) }) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Share,
                                                        contentDescription = "Compartir video MP4",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                IconButton(onClick = { onDeleteVideo(vid.id) }) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.DeleteOutline,
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
                            YvyButton(
                                text = "Actualizar a Plan Senior",
                                onClick = onOpenAdvisorPlansModal,
                                variant = YvyButtonVariant.SECONDARY_OUTLINED,
                                leadingIcon = Icons.Outlined.Lock,
                                fullWidth = true,
                                testTag = "unlock_senior_video_btn"
                            )
                        }
                    }
                }
            }

            // Consultas recibidas de clientes
            item {
                SectionHeader(
                    title = "Consultas y solicitudes de clientes (${myInquiries.size})"
                )
            }

            if (myInquiries.isEmpty()) {
                item {
                    EmptyState(
                        title = "Aún no tienes solicitudes pendientes",
                        description = "Cuando un interesado solicite una visita en tus propiedades, aparecerá aquí en tiempo real.",
                        icon = Icons.Outlined.Inbox
                    )
                }
            } else {
                items(myInquiries, key = { it.id }) { inq ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = inq.propertyTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Cliente: ${inq.userName} · Tel: ${inq.userPhone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (isStaffUser(inq.userId)) {
                                    StaffBadge()
                                }
                            }
                            Text(
                                text = inq.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    "CONTACTED" to "Contactado",
                                    "VISIT_SCHEDULED" to "Visita agendada",
                                    "CLOSED" to "Cerrado"
                                ).forEach { (statusKey, statusLabel) ->
                                    YvyChip(
                                        label = statusLabel,
                                        selected = inq.status == statusKey,
                                        onClick = { onUpdateInquiryStatus(inq.id, statusKey) }
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
private fun StatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = YvyShapes.FieldAndButton,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
