package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdvisorMemberships
import com.example.data.model.MembershipRequest
import com.example.data.model.StaffUser
import com.example.data.model.UserMembership
import com.example.data.repository.MembershipsRepository
import com.example.data.repository.StaffRepository
import com.example.data.repository.UserSearchResult
import com.example.ui.components.StaffBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusBadgeTone
import com.example.ui.components.YvyButton
import com.example.ui.components.YvyButtonVariant
import com.example.ui.theme.YvyEditorialSerifFamily
import com.example.ui.theme.YvySansFamily
import com.example.ui.theme.YvyShapes
import com.example.ui.theme.YvySpacing
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StaffDashboardScreen(
    staffUser: StaffUser,
    staffRepository: StaffRepository? = null,
    membershipsRepository: MembershipsRepository? = null,
    onUseAppAsStaff: () -> Unit = {},
    onSignOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isAdmin = staffUser.role.equals("admin", ignoreCase = true)
    val roleLabel = if (isAdmin) "Administrador" else "Personal Autorizado"
    val roleBadgeTone = if (isAdmin) StatusBadgeTone.PRO_GOLD else StatusBadgeTone.MONTE_PRIMARY

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("staff_dashboard_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = YvySpacing.ScreenHorizontal, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Barra superior de encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Panel interno",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = YvyEditorialSerifFamily,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Yvy Capital • Gestión operativa interna",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = YvySansFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    StatusBadge(
                        text = roleLabel,
                        tone = roleBadgeTone,
                        leadingIcon = if (isAdmin) Icons.Outlined.AdminPanelSettings else Icons.Outlined.Shield
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Ficha de usuario de personal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = YvyShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = staffUser.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = staffUser.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        // Datos de sesión interna
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Identificador de personal (UID):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = staffUser.uid,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            StatusBadge(
                                text = "Activo",
                                tone = StatusBadgeTone.MONTE_PRIMARY,
                                leadingIcon = Icons.Outlined.CheckCircle
                            )
                        }
                    }
                }

                // Acceso a la app general con privilegios de personal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = YvyShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HomeWork,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Navegación general",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Accedé al catálogo, publicá inmuebles con Plan Senior y respondé consultas con tu insignia institucional.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        YvyButton(
                            text = "Usar la app como personal",
                            onClick = onUseAppAsStaff,
                            variant = YvyButtonVariant.PRIMARY,
                            leadingIcon = Icons.Outlined.HomeWork,
                            fullWidth = true,
                            testTag = "use_app_as_staff_btn"
                        )
                    }
                }

                // Módulos internos informativos
                Text(
                    text = "Herramientas de administración",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HomeWork,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Propiedades",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Moderación y verificación de anuncios publicados en catálogo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Handshake,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Mesa de Cierre",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Asistencia y soporte comercial a asesores con membresía activa.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Sección Equipo (solo para administradores)
                if (isAdmin) {
                    val staffList by (staffRepository?.observeAllStaff() ?: kotlinx.coroutines.flow.flowOf(emptyList()))
                        .collectAsStateWithLifecycle(initialValue = emptyList())
                    val scope = rememberCoroutineScope()
                    var isSyncingBadges by remember { mutableStateOf(false) }
                    var syncResultMsg by remember { mutableStateOf<String?>(null) }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Equipo Yvy Capital (${staffList.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            StaffBadge()
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = YvyShapes.Card,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Gestión centralizada de personal y sincronización de insignias públicas oficiales.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (staffList.isEmpty()) {
                                    Text(
                                        text = "Cargando miembros del equipo...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    staffList.forEach { member ->
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
                                                        text = member.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (member.active) {
                                                        StaffBadge()
                                                    }
                                                }
                                                Text(
                                                    text = "${member.email} · Rol: ${member.role}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            StatusBadge(
                                                text = if (member.active) "Activo" else "Inactivo",
                                                tone = if (member.active) StatusBadgeTone.MONTE_PRIMARY else StatusBadgeTone.NEUTRAL_OUTLINED
                                            )
                                        }
                                        if (member != staffList.last()) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                                YvyButton(
                                    text = if (isSyncingBadges) "Sincronizando..." else "Sincronizar insignias",
                                    onClick = {
                                        scope.launch {
                                            isSyncingBadges = true
                                            syncResultMsg = null
                                            val res = staffRepository?.syncAllStaffBadges()
                                            res?.onSuccess { count ->
                                                syncResultMsg = "Insignias sincronizadas con éxito ($count activas)."
                                            }?.onFailure { err ->
                                                syncResultMsg = "Error: ${err.localizedMessage ?: "No se pudo sincronizar"}"
                                            }
                                            isSyncingBadges = false
                                        }
                                    },
                                    variant = YvyButtonVariant.SECONDARY_OUTLINED,
                                    leadingIcon = Icons.Outlined.VerifiedUser,
                                    enabled = !isSyncingBadges && staffRepository != null,
                                    fullWidth = true,
                                    testTag = "sync_staff_badges_btn"
                                )

                                syncResultMsg?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SECCIONES DE ADMINISTRACIÓN DE MEMBRESÍAS
                // ==========================================
                if (isAdmin && membershipsRepository != null) {
                    val pendingRequests by membershipsRepository.observePendingRequests()
                        .collectAsStateWithLifecycle(emptyList())

                    // a) Solicitudes de plan
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_plan_requests_card"),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Solicitudes de plan (${pendingRequests.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                StatusBadge(
                                    text = "${pendingRequests.size} pendientes",
                                    tone = if (pendingRequests.isNotEmpty()) StatusBadgeTone.PRO_GOLD else StatusBadgeTone.NEUTRAL_OUTLINED
                                )
                            }
                            Text(
                                text = "Revisa comprobantes y aprueba o rechaza solicitudes de planes de asesores.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (pendingRequests.isEmpty()) {
                                Text(
                                    text = "No hay solicitudes pendientes en este momento.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                pendingRequests.forEach { req ->
                                    var selectedDuration by remember(req.id) { mutableIntStateOf(1) }
                                    var isProcessing by remember(req.id) { mutableStateOf(false) }
                                    var reqFeedback by remember(req.id) { mutableStateOf<String?>(null) }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = YvyShapes.Card,
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = req.userName.ifBlank { "Usuario" },
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = req.userEmail,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                StatusBadge(
                                                    text = AdvisorMemberships.displayNameForTier(req.planTier),
                                                    tone = StatusBadgeTone.PRO_GOLD
                                                )
                                            }

                                            Text(
                                                text = "Método: ${req.paymentMethod} • Comprobante: ${req.paymentReference}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )

                                            Text(
                                                text = "Duración a otorgar:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                listOf(1, 3, 6, 12).forEach { months ->
                                                    FilterChip(
                                                        selected = selectedDuration == months,
                                                        onClick = { selectedDuration = months },
                                                        label = { Text("$months m") },
                                                        modifier = Modifier.testTag("duration_chip_${months}_${req.id}")
                                                    )
                                                }
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            isProcessing = true
                                                            reqFeedback = null
                                                            membershipsRepository.approveRequest(req, selectedDuration, staffUser.uid)
                                                                .onSuccess {
                                                                    reqFeedback = "¡Aprobada exitosamente!"
                                                                }
                                                                .onFailure {
                                                                    reqFeedback = "Error: ${it.localizedMessage}"
                                                                }
                                                            isProcessing = false
                                                        }
                                                    },
                                                    enabled = !isProcessing,
                                                    modifier = Modifier.weight(1f).testTag("approve_request_btn_${req.id}")
                                                ) {
                                                    Text(if (isProcessing) "..." else "Aprobar ($selectedDuration m)")
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        scope.launch {
                                                            isProcessing = true
                                                            reqFeedback = null
                                                            membershipsRepository.rejectRequest(req.id, staffUser.uid)
                                                                .onSuccess {
                                                                    reqFeedback = "Solicitud rechazada."
                                                                }
                                                                .onFailure {
                                                                    reqFeedback = "Error: ${it.localizedMessage}"
                                                                }
                                                            isProcessing = false
                                                        }
                                                    },
                                                    enabled = !isProcessing,
                                                    modifier = Modifier.weight(1f).testTag("reject_request_btn_${req.id}")
                                                ) {
                                                    Text("Rechazar")
                                                }
                                            }

                                            reqFeedback?.let { msg ->
                                                Text(
                                                    text = msg,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // b) Membresías: buscar por email, ver plan, asignar, extender o cancelar
                    var searchEmail by remember { mutableStateOf("") }
                    var searchResults by remember { mutableStateOf<List<UserSearchResult>>(emptyList()) }
                    var selectedUser by remember { mutableStateOf<UserSearchResult?>(null) }
                    var userMembership by remember { mutableStateOf<UserMembership?>(null) }
                    var isSearching by remember { mutableStateOf(false) }
                    var isSavingMembership by remember { mutableStateOf(false) }
                    var membershipActionMsg by remember { mutableStateOf<String?>(null) }

                    val planOptions = listOf(
                        AdvisorMemberships.TIER_FREE,
                        AdvisorMemberships.TIER_INICIAL,
                        AdvisorMemberships.TIER_INTERMEDIO,
                        AdvisorMemberships.TIER_SENIOR
                    )
                    var manualPlanTier by remember { mutableStateOf(AdvisorMemberships.TIER_INTERMEDIO) }
                    var manualDurationMonths by remember { mutableIntStateOf(1) }
                    var manualNote by remember { mutableStateOf("") }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_memberships_management_card"),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Membresías",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Busca un usuario por su email para consultar su plan actual y asignar, extender o cancelar manualmente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchEmail,
                                    onValueChange = { searchEmail = it },
                                    label = { Text("Email del usuario") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("search_user_email_input")
                                )
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isSearching = true
                                            membershipActionMsg = null
                                            selectedUser = null
                                            userMembership = null
                                            membershipsRepository.searchUsersByEmail(searchEmail)
                                                .onSuccess { results ->
                                                    searchResults = results
                                                    if (results.isEmpty()) {
                                                        membershipActionMsg = "No se encontraron usuarios con ese email."
                                                    }
                                                }
                                                .onFailure {
                                                    membershipActionMsg = "Error: ${it.localizedMessage}"
                                                }
                                            isSearching = false
                                        }
                                    },
                                    enabled = !isSearching && searchEmail.isNotBlank(),
                                    modifier = Modifier.testTag("search_user_btn")
                                ) {
                                    Text(if (isSearching) "..." else "Buscar")
                                }
                            }

                            if (searchResults.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Resultados encontrados:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    searchResults.forEach { u ->
                                        val isThisSelected = selectedUser?.id == u.id
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("user_result_${u.id}"),
                                            shape = YvyShapes.Card,
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isThisSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isThisSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = u.displayName.ifBlank { "Sin nombre" }, fontWeight = FontWeight.Bold)
                                                    Text(text = "${u.email} • ID: ${u.id.take(8)}...", style = MaterialTheme.typography.bodySmall)
                                                }
                                                Button(
                                                    onClick = {
                                                        selectedUser = u
                                                        scope.launch {
                                                            isSearching = true
                                                            membershipActionMsg = null
                                                            membershipsRepository.getMembership(u.id)
                                                                .onSuccess { mem ->
                                                                    userMembership = mem
                                                                }
                                                                .onFailure {
                                                                    membershipActionMsg = "Error al leer membresía: ${it.localizedMessage}"
                                                                }
                                                            isSearching = false
                                                        }
                                                    }
                                                ) {
                                                    Text("Gestionar")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            selectedUser?.let { user ->
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                                Text(
                                    text = "Gestionando a: ${user.displayName} (${user.email})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Surface(
                                    shape = YvyShapes.Card,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val mem = userMembership
                                        if (mem != null) {
                                            Text(text = "Plan actual: ${AdvisorMemberships.displayNameForTier(mem.planTier)}", fontWeight = FontWeight.Bold)
                                            Text(text = "Estado: ${mem.status.uppercase()}")
                                            val expiryText = mem.expiresAt?.let {
                                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it.toDate())
                                            } ?: "Permanente / Sin vencimiento"
                                            Text(text = "Vencimiento: $expiryText", style = MaterialTheme.typography.bodySmall)
                                            if (!mem.note.isNullOrBlank()) {
                                                Text(text = "Nota: ${mem.note}", style = MaterialTheme.typography.bodySmall)
                                            }
                                        } else {
                                            Text(text = "Sin registro en memberships/{uid} (Plan efectivo: FREE)", fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }

                                Text(
                                    text = "Asignar o extender plan:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    planOptions.filter { it != AdvisorMemberships.TIER_FREE }.forEach { tier ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            RadioButton(
                                                selected = manualPlanTier == tier,
                                                onClick = { manualPlanTier = tier }
                                            )
                                            Text(text = AdvisorMemberships.displayNameForTier(tier))
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(1, 3, 6, 12).forEach { m ->
                                        FilterChip(
                                            selected = manualDurationMonths == m,
                                            onClick = { manualDurationMonths = m },
                                            label = { Text("$m ${if (m == 1) "mes" else "meses"}") }
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = manualNote,
                                    onValueChange = { manualNote = it },
                                    label = { Text("Nota administrativa (opcional)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                isSavingMembership = true
                                                membershipActionMsg = null
                                                membershipsRepository.setManualMembership(
                                                    userId = user.id,
                                                    planTier = manualPlanTier,
                                                    status = "active",
                                                    durationMonths = manualDurationMonths,
                                                    note = manualNote.ifBlank { null },
                                                    adminUid = staffUser.uid
                                                ).onSuccess {
                                                    membershipActionMsg = "Plan asignado/extendido exitosamente."
                                                    membershipsRepository.getMembership(user.id).onSuccess { userMembership = it }
                                                }.onFailure {
                                                    membershipActionMsg = "Error: ${it.localizedMessage}"
                                                }
                                                isSavingMembership = false
                                            }
                                        },
                                        enabled = !isSavingMembership,
                                        modifier = Modifier.weight(1f).testTag("assign_membership_btn")
                                    ) {
                                        Text(if (isSavingMembership) "..." else "Asignar / Extender")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                isSavingMembership = true
                                                membershipActionMsg = null
                                                membershipsRepository.setManualMembership(
                                                    userId = user.id,
                                                    planTier = AdvisorMemberships.TIER_FREE,
                                                    status = "cancelled",
                                                    durationMonths = null,
                                                    note = "Cancelado por admin",
                                                    adminUid = staffUser.uid
                                                ).onSuccess {
                                                    membershipActionMsg = "Membresía cancelada."
                                                    membershipsRepository.getMembership(user.id).onSuccess { userMembership = it }
                                                }.onFailure {
                                                    membershipActionMsg = "Error: ${it.localizedMessage}"
                                                }
                                                isSavingMembership = false
                                            }
                                        },
                                        enabled = !isSavingMembership,
                                        modifier = Modifier.weight(1f).testTag("cancel_membership_btn")
                                    ) {
                                        Text("Cancelar plan")
                                    }
                                }
                            }

                            membershipActionMsg?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // c) Recalcular contadores
                    var singleUserCountId by remember { mutableStateOf("") }
                    var isRecalculating by remember { mutableStateOf(false) }
                    var recalculateMsg by remember { mutableStateOf<String?>(null) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_recalculate_counters_card"),
                        shape = YvyShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Recalcular contadores",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Para cada asesor con propiedades, cuenta cuántas tienen status == 'active' y guarda el total en listingCounters/{agentId} con lastPropertyId = ''.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = singleUserCountId,
                                    onValueChange = { singleUserCountId = it },
                                    label = { Text("UID del asesor") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("recalculate_uid_input")
                                )
                                Button(
                                    onClick = {
                                        scope.launch {
                                            isRecalculating = true
                                            recalculateMsg = null
                                            membershipsRepository.recalculateListingCounters(singleUserCountId.trim())
                                                .onSuccess { count ->
                                                    recalculateMsg = "Contador recalculado para $singleUserCountId: $count publicaciones activas."
                                                }
                                                .onFailure {
                                                    recalculateMsg = "Error: ${it.localizedMessage}"
                                                }
                                            isRecalculating = false
                                        }
                                    },
                                    enabled = !isRecalculating && singleUserCountId.isNotBlank(),
                                    modifier = Modifier.testTag("recalculate_single_user_btn")
                                ) {
                                    Text(if (isRecalculating) "..." else "Recalcular")
                                }
                            }

                            YvyButton(
                                text = if (isRecalculating) "Recalculando contadores..." else "Recalcular todos los asesores",
                                onClick = {
                                    scope.launch {
                                        isRecalculating = true
                                        recalculateMsg = null
                                        membershipsRepository.recalculateAllListingCounters()
                                            .onSuccess { totalAgents ->
                                                recalculateMsg = "Contadores actualizados con éxito para $totalAgents asesores con propiedades."
                                            }
                                            .onFailure {
                                                recalculateMsg = "Error: ${it.localizedMessage}"
                                            }
                                        isRecalculating = false
                                    }
                                },
                                variant = YvyButtonVariant.SECONDARY_OUTLINED,
                                fullWidth = true,
                                enabled = !isRecalculating,
                                testTag = "recalculate_all_counters_btn"
                            )

                            recalculateMsg?.let { msg ->
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Aviso de seguridad interna
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = YvyShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Acceso auditado por Firebase Security Rules. Al terminar tus gestiones, cerrá tu sesión corporativa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Cerrar sesión
                YvyButton(
                    text = "Cerrar sesión",
                    onClick = onSignOut,
                    variant = YvyButtonVariant.SECONDARY_OUTLINED,
                    isDestructive = true,
                    leadingIcon = Icons.AutoMirrored.Outlined.Logout,
                    fullWidth = true,
                    testTag = "staff_logout_btn"
                )
            }
        }
    }
}
