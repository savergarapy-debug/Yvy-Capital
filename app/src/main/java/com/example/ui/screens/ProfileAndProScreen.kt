package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Property
import com.example.data.model.PropertyInquiry
import com.example.data.model.UserProfile
import com.example.ui.theme.YvyForestDeep
import com.example.ui.theme.YvyForestPrimary
import com.example.ui.theme.YvyGoldAccent
import com.example.ui.util.CurrencyFormatter
import com.example.ui.viewmodel.UiState

@Composable
fun ProfileAndProScreen(
    userProfileState: UiState<UserProfile?>,
    agentPropertiesState: UiState<List<Property>>,
    agentInquiriesState: UiState<List<PropertyInquiry>>,
    isBusy: Boolean,
    initialTab: Int = 0,
    onSaveProfile: (displayName: String, phone: String, preferredCurrency: String, role: String, agencyName: String) -> Unit,
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
        agentName: String,
        agentAgency: String,
        agentWhatsapp: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onDeleteProperty: (String) -> Unit,
    onUpdateInquiryStatus: (inquiryId: String, newStatus: String) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val profile = (userProfileState as? UiState.Success)?.data

    var showPublishDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
            // TAB 1: YVY CAPITAL PRO (Gestión de Publicaciones, Consultas de Clientes y Planes)
            val myProperties = (agentPropertiesState as? UiState.Success)?.data ?: emptyList()
            val myInquiries = (agentInquiriesState as? UiState.Success)?.data ?: emptyList()

            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Yvy Capital Pro & Membresías
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = YvyGoldAccent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Yvy Capital Pro • Panel de Asesor",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                            }

                            Text(
                                text = "Plan actual: ${profile?.planTier ?: "FREE"} (Etapa de Lanzamiento sin cobros activos — compatible con futura integración de Google Play Billing).",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.88f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatPill("Publicaciones", "${myProperties.size}")
                                StatPill("Consultas / Leads", "${myInquiries.size}")
                                StatPill("Visitas Pendientes", "${myInquiries.count { it.status == "PENDING" }}")
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { showPublishDialog = true },
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
                                Text("Publicar Nueva Propiedad", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Consultas de Clientes Potenciales Recibidas
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

                // Mis Propiedades Publicadas
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prop.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${prop.neighborhood}, ${prop.city} • ${CurrencyFormatter.formatPrice(prop.price, prop.currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                }
            }
        }
    }

    // Modal para Publicar Nueva Propiedad en Yvy Capital Pro
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
        var whatsapp by remember { mutableStateOf(profile?.phone?.ifBlank { "+595981400100" } ?: "+595981400100") }

        AlertDialog(
            onDismissRequest = { if (!isBusy) showPublishDialog = false },
            title = { Text("Publicar Inmueble • Yvy Capital Pro") },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(400.dp)
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
}

@Composable
private fun StatPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
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
