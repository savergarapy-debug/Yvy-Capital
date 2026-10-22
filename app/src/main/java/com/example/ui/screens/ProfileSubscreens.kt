package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Countertops
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KingBed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AdvisorMemberships
import com.example.data.model.Property
import com.example.data.model.UserProfile
import com.example.ui.components.YvyButton
import com.example.ui.components.YvyButtonVariant
import com.example.ui.components.YvyTopBar
import com.example.ui.theme.YvyForestDeep
import com.example.ui.theme.YvyForestPrimary
import com.example.ui.theme.YvyGoldAccent
import com.example.ui.theme.YvyShapes
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.DeepLinkHandler
import com.example.ui.util.ParaguayLocations
import com.example.ui.util.PromoVideoGenerator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun SubscreenTopBar(
    title: String,
    subtitle: String = "",
    onBack: () -> Unit
) {
    YvyTopBar(
        title = title,
        onBack = onBack,
        backButtonTestTag = "profile_subscreen_back_btn"
    )
}

/**
 * 12. SUBPANTALLA «PERFIL»
 * A. Información personal: Nombre, Apellido, Número de celular, Correo electrónico
 * B. Información del vendedor: Publicaciones activas: X
 */
@Composable
fun PerfilPersonalSubscreen(
    profile: UserProfile?,
    activePropertiesCount: Int,
    isBusy: Boolean,
    onBack: () -> Unit,
    onSavePersonalData: (fullName: String, phone: String) -> Unit,
    onGoToPublish: () -> Unit
) {
    val authUser = remember { FirebaseAuth.getInstance().currentUser }
    val rawDisplayName = (profile?.displayName?.ifBlank { authUser?.displayName ?: "" } ?: (authUser?.displayName ?: "")).trim()
    val initialFirstName = remember(rawDisplayName) {
        rawDisplayName.substringBefore(" ").trim()
    }
    val initialLastName = remember(rawDisplayName) {
        if (rawDisplayName.contains(" ")) rawDisplayName.substringAfter(" ").trim() else ""
    }
    val realEmail = (profile?.email?.ifBlank { authUser?.email ?: "" } ?: (authUser?.email ?: "")).trim()
    val initialPhone = (profile?.phone?.ifBlank { authUser?.phoneNumber ?: "" } ?: (authUser?.phoneNumber ?: "")).trim()

    var firstName by remember(initialFirstName) { mutableStateOf(initialFirstName) }
    var lastName by remember(initialLastName) { mutableStateOf(initialLastName) }
    var phone by remember(initialPhone) { mutableStateOf(initialPhone) }

    Column(modifier = Modifier.fillMaxSize()) {
        SubscreenTopBar(
            title = "Perfil",
            subtitle = "Información personal y resumen del vendedor",
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // A. Información personal
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = YvyForestPrimary.copy(alpha = 0.12f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = YvyForestPrimary
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Información personal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Datos sincronizados con tu cuenta en Yvy Capital",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Resumen de lectura clara de los 4 campos requeridos
                        PersonalFieldReadRow(
                            icon = Icons.Default.Person,
                            label = "Nombre",
                            value = firstName.ifBlank { "Sin especificar en tu perfil" },
                            isEmpty = firstName.isBlank()
                        )
                        PersonalFieldReadRow(
                            icon = Icons.Default.Badge,
                            label = "Apellido",
                            value = lastName.ifBlank { "Sin especificar en tu perfil" },
                            isEmpty = lastName.isBlank()
                        )
                        PersonalFieldReadRow(
                            icon = Icons.Default.Phone,
                            label = "Número de celular",
                            value = phone.ifBlank { "Sin número registrado" },
                            isEmpty = phone.isBlank()
                        )
                        PersonalFieldReadRow(
                            icon = Icons.Default.Email,
                            label = "Correo electrónico",
                            value = realEmail.ifBlank { "Sin correo asociado" },
                            isEmpty = realEmail.isBlank()
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text(
                            text = "Actualizar datos personales",
                            style = MaterialTheme.typography.labelLarge,
                            color = YvyForestPrimary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("Nombre") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_first_name_input")
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("Apellido") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_last_name_input")
                            )
                        }

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Número de celular (+595...)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_phone_input")
                        )

                        OutlinedTextField(
                            value = realEmail.ifBlank { "Autenticado con Google" },
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Correo electrónico (Cuenta vinculada)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val combinedName = listOf(firstName.trim(), lastName.trim())
                                    .filter { it.isNotBlank() }
                                    .joinToString(" ")
                                onSavePersonalData(combinedName, phone.trim())
                            },
                            enabled = !isBusy && firstName.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_profile_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isBusy) "Guardando..." else "Guardar información personal")
                        }
                    }
                }
            }

            // B. Información del vendedor
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, YvyForestPrimary.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = YvyGoldAccent.copy(alpha = 0.18f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = YvyForestDeep
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Información del vendedor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Estado de tus inmuebles publicados en Yvy Capital",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = YvyForestPrimary.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("seller_active_publications_badge")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Publicaciones activas: $activePropertiesCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = YvyForestPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = YvyForestPrimary
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onGoToPublish,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ir a Publicar propiedad")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalFieldReadRow(
    icon: ImageVector,
    label: String,
    value: String,
    isEmpty: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = YvyForestPrimary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isEmpty) FontWeight.Normal else FontWeight.SemiBold,
                color = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 13 & 14. SUBPANTALLA «PUBLICAR» Y FORMULARIO «NUEVA PROPIEDAD»
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicarPropiedadSubscreen(
    profile: UserProfile?,
    myProperties: List<Property>,
    isBusy: Boolean,
    onBack: () -> Unit,
    onOpenAdvisorPlans: () -> Unit,
    onOpenSeniorVideoGenerator: () -> Unit,
    onDeleteProperty: (String) -> Unit,
    onMessage: (String) -> Unit,
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
    ) -> Unit
) {
    val context = LocalContext.current
    val currentTier = profile?.planTier ?: AdvisorMemberships.TIER_FREE
    val currentPlanSpec = AdvisorMemberships.getSpec(currentTier)
    val isSeniorMember = AdvisorMemberships.isSeniorTier(currentTier)
    val canFeature = AdvisorMemberships.canFeatureForTier(currentTier)

    var showNewPropertyForm by remember { mutableStateOf(true) }

    // A. Tipo de propiedad y operación
    var propertyType by remember { mutableStateOf("HOUSE") }
    var operationType by remember { mutableStateOf("SALE") }

    // B. Fotografías seleccionadas
    val selectedPhotoUris = remember { mutableStateListOf<Uri>() }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedPhotoUris.clear()
            selectedPhotoUris.addAll(uris)
        }
    }

    // C. Ubicación del inmueble (ParaguayLocations)
    var department by remember { mutableStateOf("Asunción") }
    val availableCities = remember(department) {
        ParaguayLocations.citiesForPublishDepartment(department)
    }
    var city by remember { mutableStateOf("Asunción") }
    LaunchedEffect(department) {
        val validCities = ParaguayLocations.citiesForPublishDepartment(department)
        if (!validCities.contains(city)) {
            city = validCities.firstOrNull() ?: department
        }
    }
    var neighborhood by remember { mutableStateOf("Villa Morra") }
    var address by remember { mutableStateOf("") }

    // D. Precio y moneda del inmueble
    var currency by remember { mutableStateOf("PYG") }
    var priceText by remember { mutableStateOf("850000000") }

    // Datos generales de identificación
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var areaTotalText by remember { mutableStateOf("320") }
    var bathroomsText by remember { mutableStateOf("3") }
    var parkingSpacesText by remember { mutableStateOf("2") }
    var featuredListing by remember { mutableStateOf(false) }
    var whatsapp by remember(profile?.phone) {
        mutableStateOf(profile?.phone?.ifBlank { "+595981400100" } ?: "+595981400100")
    }

    // E & F. Características específicas para «Casa» (Venta y Alquiler)
    var bedroomsText by remember { mutableStateOf("3") }
    var cocinaComedor by remember { mutableStateOf("Integrada") } // "Integrada" | "Separada"
    var hasPiscina by remember { mutableStateOf(false) }          // Sí / No
    var hasQuincho by remember { mutableStateOf(true) }           // Sí / No
    var hasSuite by remember { mutableStateOf(true) }             // Sí / No
    var suiteCountText by remember { mutableStateOf("1") }

    var validationError by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        SubscreenTopBar(
            title = "Publicar",
            subtitle = "Nueva propiedad y gestión de avisos activos",
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Cabecera de acción «Nueva propiedad»
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = YvyShapes.Card,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Nueva propiedad",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        YvyButton(
                            text = if (showNewPropertyForm) "Formulario activo" else "Nueva propiedad",
                            onClick = { showNewPropertyForm = !showNewPropertyForm },
                            variant = if (showNewPropertyForm) YvyButtonVariant.SECONDARY_OUTLINED else YvyButtonVariant.PRIMARY,
                            leadingIcon = Icons.Default.AddBusiness,
                            fullWidth = true,
                            testTag = "toggle_new_property_form_btn"
                        )
                    }
                }
            }

            if (showNewPropertyForm) {
                // SECCIÓN A: Tipo de propiedad y operación
                item {
                    PublishSectionCard(
                        stepTitle = "A. Tipo de propiedad y operación",
                        stepSubtitle = "Selecciona la categoría real del catálogo y el tipo de operación",
                        icon = Icons.Default.HomeWork
                    ) {
                        Text(
                            text = "Tipo de operación",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("SALE" to "Venta", "RENT" to "Alquiler").forEach { (code, label) ->
                                FilterChip(
                                    selected = operationType == code,
                                    onClick = { operationType = code },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("publish_op_$code")
                                )
                            }
                        }

                        Text(
                            text = "Categoría del inmueble",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        val propertyCategories = listOf(
                            "HOUSE" to "Casa",
                            "APARTMENT" to "Departamento",
                            "LAND" to "Terreno",
                            "OFFICE" to "Oficina",
                            "COMMERCIAL" to "Local Comercial",
                            "ESTANCIA" to "Estancia"
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            propertyCategories.forEach { (code, label) ->
                                FilterChip(
                                    selected = propertyType == code,
                                    onClick = {
                                        propertyType = code
                                        validationError = null
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("publish_type_$code")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Título de la propiedad") },
                            placeholder = { Text("Ej: Casa familiar con jardín en $neighborhood") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("publish_title_input")
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción del inmueble") },
                            placeholder = { Text("Describe distribución, entorno, estado de conservación y ventajas...") },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("publish_description_input")
                        )
                    }
                }

                // SECCIÓN B: Fotografías y contenido multimedia
                item {
                    PublishSectionCard(
                        stepTitle = "B. Fotografías y contenido multimedia",
                        stepSubtitle = "Agrega imágenes claras del inmueble para la galería de la publicación",
                        icon = Icons.Default.AddAPhoto
                    ) {
                        // Recuadro claro de «Subir fotos»
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = YvyForestPrimary.copy(alpha = 0.06f),
                            border = BorderStroke(1.5.dp, YvyForestPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("upload_photos_box")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 22.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = YvyForestPrimary.copy(alpha = 0.14f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = "Subir fotos",
                                            tint = YvyForestPrimary
                                        )
                                    }
                                }
                                Text(
                                    text = "Subir fotos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = YvyForestPrimary
                                )
                                Text(
                                    text = if (selectedPhotoUris.isEmpty()) {
                                        "Toca aquí para seleccionar fotografías desde tu galería"
                                    } else {
                                        "${selectedPhotoUris.size} fotografía(s) lista(s) para publicar"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (selectedPhotoUris.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(selectedPhotoUris) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(92.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(YvyForestPrimary.copy(alpha = 0.14f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddAPhoto,
                                                contentDescription = "Foto seleccionada",
                                                tint = YvyForestPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = uri.lastPathSegment?.takeLast(10) ?: "Foto",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = YvyForestPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.65f),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(24.dp)
                                                .clickable { selectedPhotoUris.remove(uri) }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Quitar foto",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Mensaje informativo discreto debajo del recuadro de subida de fotos
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("senior_video_benefit_notice")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MovieCreation,
                                        contentDescription = null,
                                        tint = YvyGoldAccent,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Text(
                                        text = "Beneficio exclusivo del plan Senior: convierte las fotos de tu propiedad en un video con inteligencia artificial para presentar mejor tu inmueble.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSeniorMember) {
                                    OutlinedButton(
                                        onClick = onOpenSeniorVideoGenerator,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MovieCreation,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Abrir Generador de Video con IA (Plan Senior activo)")
                                    }
                                }
                            }
                        }
                    }
                }

                // SECCIÓN C: Ubicación del inmueble
                item {
                    PublishSectionCard(
                        stepTitle = "C. Ubicación del inmueble",
                        stepSubtitle = "Selecciona departamento y ciudad oficial de Paraguay",
                        icon = Icons.Default.LocationOn
                    ) {
                        Text(
                            text = "Departamento o Capital (18 entidades oficiales)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ParaguayLocations.officialDepartments) { dept ->
                                FilterChip(
                                    selected = department == dept,
                                    onClick = {
                                        department = dept
                                        val validCities = ParaguayLocations.citiesForPublishDepartment(dept)
                                        city = validCities.firstOrNull() ?: dept
                                    },
                                    label = { Text(dept) },
                                    modifier = Modifier.testTag("publish_dept_$dept")
                                )
                            }
                        }

                        Text(
                            text = "Ciudad / Distrito en $department",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(availableCities) { cityOption ->
                                FilterChip(
                                    selected = city == cityOption,
                                    onClick = { city = cityOption },
                                    label = { Text(cityOption) },
                                    modifier = Modifier.testTag("publish_city_$cityOption")
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = neighborhood,
                                onValueChange = { neighborhood = it },
                                label = { Text("Barrio / Zona") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("publish_neighborhood_input")
                            )
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Dirección referencial") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // SECCIÓN D: Precio y moneda del inmueble
                item {
                    val numericPrice = priceText.toDoubleOrNull() ?: 0.0
                    val formattedPreview = CurrencyFormatter.formatPrice(numericPrice, currency)

                    PublishSectionCard(
                        stepTitle = "D. Precio y moneda del inmueble",
                        stepSubtitle = "El precio se mostrará en toda la aplicación en la moneda elegida aquí",
                        icon = Icons.Default.Payments
                    ) {
                        Text(
                            text = "Moneda de publicación",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = currency == "PYG",
                                onClick = { currency = "PYG" },
                                label = {
                                    Text(
                                        text = "Guaraníes (PYG, ₲)",
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                modifier = Modifier.testTag("publish_currency_pyg")
                            )
                            FilterChip(
                                selected = currency == "USD",
                                onClick = { currency = "USD" },
                                label = {
                                    Text(
                                        text = "Dólares estadounidenses (USD, $)",
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                modifier = Modifier.testTag("publish_currency_usd")
                            )
                        }

                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { input ->
                                priceText = input.filter { it.isDigit() }
                            },
                            label = {
                                Text(
                                    if (currency == "USD") "Monto en Dólares (USD $)"
                                    else "Monto en Guaraníes (PYG ₲)"
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("publish_price_input")
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = YvyForestPrimary.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("publish_price_preview_box")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vista previa del precio:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formattedPreview,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = YvyForestPrimary
                                )
                            }
                        }
                    }
                }

                // SECCIÓN E y F: Características específicas para «Casa» (Venta y Alquiler)
                if (propertyType == "HOUSE") {
                    item {
                        PublishSectionCard(
                            stepTitle = "E. Características específicas para «Casa»",
                            stepSubtitle = "Aplicable a Casas en Venta y en Alquiler",
                            icon = Icons.Default.Bed
                        ) {
                            // 1. Cantidad de dormitorios
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bed,
                                    contentDescription = null,
                                    tint = YvyForestPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "1. Cantidad de dormitorios (obligatorio)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            OutlinedTextField(
                                value = bedroomsText,
                                onValueChange = { input ->
                                    bedroomsText = input.filter { it.isDigit() }
                                },
                                label = { Text("Cantidad de dormitorios") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("house_bedrooms_input")
                            )

                            // 2. Cocina-comedor (Integrada / Separada)
                            FeatureOptionSelectorRow(
                                icon = Icons.Default.Countertops,
                                label = "2. Cocina-comedor",
                                options = listOf("Integrada" to "Integrada", "Separada" to "Separada"),
                                selectedValue = cocinaComedor,
                                onSelect = { cocinaComedor = it },
                                testTagPrefix = "house_kitchen"
                            )

                            // 3. Piscina (Sí / No)
                            FeatureOptionSelectorRow(
                                icon = Icons.Default.Pool,
                                label = "3. Piscina",
                                options = listOf("Sí" to "Sí", "No" to "No"),
                                selectedValue = if (hasPiscina) "Sí" else "No",
                                onSelect = { hasPiscina = (it == "Sí") },
                                testTagPrefix = "house_pool"
                            )

                            // 4. Quincho (Sí / No)
                            FeatureOptionSelectorRow(
                                icon = Icons.Default.OutdoorGrill,
                                label = "4. Quincho",
                                options = listOf("Sí" to "Sí", "No" to "No"),
                                selectedValue = if (hasQuincho) "Sí" else "No",
                                onSelect = { hasQuincho = (it == "Sí") },
                                testTagPrefix = "house_quincho"
                            )

                            // 5. Dormitorios en suite (Sí / No + cantidad condicional)
                            FeatureOptionSelectorRow(
                                icon = Icons.Default.KingBed,
                                label = "5. Dormitorios en suite",
                                options = listOf("Sí" to "Sí", "No" to "No"),
                                selectedValue = if (hasSuite) "Sí" else "No",
                                onSelect = {
                                    hasSuite = (it == "Sí")
                                    if (!hasSuite) {
                                        suiteCountText = "0"
                                    } else if (suiteCountText == "0" || suiteCountText.isBlank()) {
                                        suiteCountText = "1"
                                    }
                                },
                                testTagPrefix = "house_suite"
                            )

                            if (hasSuite) {
                                OutlinedTextField(
                                    value = suiteCountText,
                                    onValueChange = { input ->
                                        suiteCountText = input.filter { it.isDigit() }
                                    },
                                    label = { Text("¿Cuántos dormitorios son en suite?") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("house_suite_count_input")
                                )
                            }
                        }
                    }
                } else {
                    // G. Oficinas y H. Otros tipos de propiedad (sin campos exclusivos de Casa)
                    item {
                        val categoryLabel = CurrencyFormatter.propertyTypeLabel(propertyType)
                        PublishSectionCard(
                            stepTitle = "Características de $categoryLabel",
                            stepSubtitle = "Estructura modular preparada para $categoryLabel",
                            icon = Icons.Default.SquareFoot
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = YvyForestPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (propertyType == "OFFICE") {
                                            "Se mantienen los campos base existentes para Oficina. Las características específicas adicionales para oficinas quedan preparadas en la arquitectura para su próxima definición."
                                        } else {
                                            "Se mantienen los campos base existentes para $categoryLabel sin mezclar atributos exclusivos de Casa."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Superficie, baños, cocheras y contacto del vendedor
                item {
                    PublishSectionCard(
                        stepTitle = "Dimensiones y contacto de publicación",
                        stepSubtitle = "Superficie total, sanitarios, estacionamiento y WhatsApp de contacto",
                        icon = Icons.Default.SquareFoot
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = areaTotalText,
                                onValueChange = { areaTotalText = it.filter { c -> c.isDigit() } },
                                label = { Text("Superficie (m²)") },
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
                            OutlinedTextField(
                                value = parkingSpacesText,
                                onValueChange = { parkingSpacesText = it.filter { c -> c.isDigit() } },
                                label = { Text("Cocheras") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("Número de celular / WhatsApp de contacto (+595...)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (canFeature) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
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
                                            text = "Publicación Destacada",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Incluida en tu ${currentPlanSpec?.name ?: "Plan Pro"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = featuredListing,
                                        onCheckedChange = { featuredListing = it }
                                    )
                                }
                            }
                        }

                        val err = validationError
                        if (err != null) {
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (currentTier == AdvisorMemberships.TIER_FREE) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = YvyGoldAccent.copy(alpha = 0.16f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Para publicar anuncios activos en el catálogo se requiere un plan de asesor activo (Inicial, Intermedio o Senior).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    OutlinedButton(
                                        onClick = onOpenAdvisorPlans,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Elegir Plan Pro para Publicar")
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val priceVal = priceText.toDoubleOrNull() ?: 0.0
                                if (priceVal <= 0.0) {
                                    validationError = "Ingresa un precio válido mayor a cero."
                                    return@Button
                                }
                                val bedsVal = if (propertyType == "HOUSE") {
                                    val parsedBeds = bedroomsText.toLongOrNull()
                                    if (parsedBeds == null || parsedBeds < 0L) {
                                        validationError = "Indica una cantidad de dormitorios válida (0 o más)."
                                        return@Button
                                    }
                                    parsedBeds
                                } else {
                                    0L
                                }

                                val suiteCountVal = if (propertyType == "HOUSE" && hasSuite) {
                                    val parsedSuites = suiteCountText.toLongOrNull()
                                    if (parsedSuites == null || parsedSuites < 1L || (bedsVal > 0L && parsedSuites > bedsVal)) {
                                        validationError = "Indica una cantidad coherente de dormitorios en suite (entre 1 y $bedsVal)."
                                        return@Button
                                    }
                                    parsedSuites
                                } else {
                                    0L
                                }

                                validationError = null
                                val areaVal = (areaTotalText.toLongOrNull() ?: 120L).coerceAtLeast(1L)
                                val bathsVal = (bathroomsText.toLongOrNull() ?: 1L).coerceAtLeast(0L)
                                val parkVal = (parkingSpacesText.toLongOrNull() ?: 0L).coerceAtLeast(0L)

                                val builtAmenities = buildList {
                                    if (propertyType == "HOUSE") {
                                        add("Cocina-comedor: $cocinaComedor")
                                        add("Piscina: ${if (hasPiscina) "Sí" else "No"}")
                                        add("Quincho: ${if (hasQuincho) "Sí" else "No"}")
                                        add(
                                            if (hasSuite) "Dormitorios en suite: $suiteCountVal"
                                            else "Dormitorios en suite: No"
                                        )
                                    } else {
                                        add("Categoría: ${CurrencyFormatter.propertyTypeLabel(propertyType)}")
                                    }
                                }

                                val finalTitle = title.trim().ifBlank {
                                    "${CurrencyFormatter.propertyTypeLabel(propertyType)} en $neighborhood, $city"
                                }
                                val finalDescription = description.trim().ifBlank {
                                    "${CurrencyFormatter.propertyTypeLabel(propertyType)} en ${CurrencyFormatter.operationLabel(operationType).lowercase()} ubicada en $neighborhood, $city ($department)."
                                }

                                onPublishProperty(
                                    finalTitle,
                                    finalDescription,
                                    operationType,
                                    propertyType,
                                    priceVal,
                                    currency,
                                    department,
                                    city,
                                    neighborhood.ifBlank { city },
                                    address.ifBlank { "$neighborhood, $city" },
                                    areaVal,
                                    areaVal,
                                    bedsVal,
                                    bathsVal,
                                    parkVal,
                                    builtAmenities,
                                    featuredListing && canFeature,
                                    profile?.displayName?.ifBlank { "Asesor Yvy Capital" } ?: "Asesor Yvy Capital",
                                    profile?.agencyName?.ifBlank { "Yvy Capital" } ?: "Yvy Capital",
                                    whatsapp.ifBlank { "+595981400100" }
                                ) {
                                    title = ""
                                    description = ""
                                    selectedPhotoUris.clear()
                                    onMessage("Propiedad publicada en $currency correctamente.")
                                }
                            },
                            enabled = !isBusy,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_new_property_btn")
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBusy) "Publicando inmueble..." else "Publicar propiedad",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Mis propiedades publicadas actualmente
            item {
                Text(
                    text = "Mis publicaciones registradas (${myProperties.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (myProperties.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Text(
                            text = "Aún no tienes propiedades activas publicadas con tu cuenta.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(myProperties, key = { it.id }) { prop ->
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prop.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${prop.neighborhood}, ${prop.city} (${prop.department}) • ${CurrencyFormatter.formatPrice(prop.price, prop.currency)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = { DeepLinkHandler.shareProperty(context, prop, prop.currency) }
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
                                            contentDescription = "Eliminar propiedad",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            if (prop.amenities.isNotEmpty()) {
                                Text(
                                    text = prop.amenities.joinToString(" • "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = YvyForestPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublishSectionCard(
    stepTitle: String,
    stepSubtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = YvyForestPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = YvyForestPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = stepTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stepSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            content()
        }
    }
}

@Composable
private fun FeatureOptionSelectorRow(
    icon: ImageVector,
    label: String,
    options: List<Pair<String, String>>,
    selectedValue: String,
    onSelect: (String) -> Unit,
    testTagPrefix: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = YvyForestPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { (value, text) ->
                val selected = selectedValue == value
                FilterChip(
                    selected = selected,
                    onClick = { onSelect(value) },
                    label = { Text(text) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = YvyForestPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = YvyForestPrimary
                    ),
                    modifier = Modifier.testTag("${testTagPrefix}_$value")
                )
            }
        }
    }
}

/**
 * 15. SUBPANTALLA «INFORMACIÓN DE LA CUENTA»
 */
@Composable
fun InformacionCuentaSubscreen(
    profile: UserProfile?,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onRequestDeleteAccount: () -> Unit
) {
    val authUser = remember { FirebaseAuth.getInstance().currentUser }
    val currentTier = profile?.planTier ?: AdvisorMemberships.TIER_FREE
    val planSpec = AdvisorMemberships.getSpec(currentTier)

    Column(modifier = Modifier.fillMaxSize()) {
        SubscreenTopBar(
            title = "Información de la cuenta",
            subtitle = "Credenciales de acceso, membresía activa y seguridad",
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = YvyForestPrimary
                            )
                            Text(
                                text = "Estado de autenticación y suscripción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        PersonalFieldReadRow(
                            icon = Icons.Default.Email,
                            label = "Cuenta de acceso (Google Sign-In)",
                            value = profile?.email?.ifBlank { authUser?.email ?: "Autenticado" }
                                ?: (authUser?.email ?: "Autenticado"),
                            isEmpty = false
                        )
                        PersonalFieldReadRow(
                            icon = Icons.Default.Security,
                            label = "Identificador único de cuenta (UID)",
                            value = profile?.id?.ifBlank { authUser?.uid ?: "—" } ?: (authUser?.uid ?: "—"),
                            isEmpty = false
                        )
                        PersonalFieldReadRow(
                            icon = Icons.Default.WorkspacePremium,
                            label = "Membresía actual",
                            value = planSpec?.let { "${it.name} (${it.priceFormatted})" }
                                ?: "Cuenta Estándar Gratuita",
                            isEmpty = false
                        )
                        PersonalFieldReadRow(
                            icon = Icons.Default.Badge,
                            label = "Límite de publicaciones simultáneas",
                            value = "${planSpec?.maxActiveListings ?: 0} publicaciones activas según tu plan",
                            isEmpty = false
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Sesión y privacidad de cuenta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sign_out_btn")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar Sesión")
                        }

                        OutlinedButton(
                            onClick = onRequestDeleteAccount,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("delete_account_btn")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Eliminar Cuenta y Datos Personales")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 16. SUBPANTALLA «CONTACTAR CON LA IA»
 * Reutiliza GeminiInteractionsApiService en PromoVideoGenerator.kt
 */
data class AiChatMessage(
    val sender: String, // "USER" | "AI"
    val text: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContactarIaSubscreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isConfigured = remember { PromoVideoGenerator.isGeminiKeyConfigured() }
    val messages = remember {
        mutableStateListOf(
            AiChatMessage(
                sender = "AI",
                text = "Hola, soy el asistente inmobiliario de Yvy Capital. Puedo ayudarte con consultas sobre redacción de anuncios, inversión inmobiliaria en Paraguay, precios en Guaraníes o Dólares y planes Pro."
            )
        )
    }
    var userPrompt by remember { mutableStateOf("") }
    var lastInteractionId by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        SubscreenTopBar(
            title = "Contactar con la IA",
            subtitle = "Asistencia inteligente para consultas inmobiliarias en Yvy Capital",
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (!isConfigured) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = YvyGoldAccent.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, YvyGoldAccent.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = YvyForestDeep
                            )
                            Text(
                                text = "Integración Gemini API lista: configura GEMINI_API_KEY en el panel de Secrets de AI Studio para habilitar respuestas en vivo del modelo gemini-3.5-flash.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Consultas frecuentes sugeridas:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                val quickPrompts = listOf(
                    "¿Cómo redactar una buena descripción para una casa en venta?",
                    "¿Qué diferencia hay entre los planes Inicial, Intermedio y Senior?",
                    "¿Qué zonas de Asunción y Central tienen alta demanda de alquiler?"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPrompts.forEach { sample ->
                        FilterChip(
                            selected = false,
                            onClick = { userPrompt = sample },
                            label = { Text(sample, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
            }

            items(messages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isUser) YvyForestPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(0.88f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isUser) "Tú" else "Asistente IA • Yvy Capital",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isUser) YvyGoldAccent else YvyForestPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userPrompt,
                    onValueChange = { userPrompt = it },
                    placeholder = { Text("Escribe tu consulta inmobiliaria...") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input")
                )
                Button(
                    onClick = {
                        val promptToSend = userPrompt.trim()
                        if (promptToSend.isNotEmpty() && !isSending) {
                            userPrompt = ""
                            messages.add(AiChatMessage("USER", promptToSend))
                            isSending = true
                            scope.launch {
                                val res = PromoVideoGenerator.askYvyAiAssistant(
                                    userPrompt = promptToSend,
                                    previousInteractionId = lastInteractionId
                                )
                                res.onSuccess { (reply, interactionId) ->
                                    if (interactionId.isNotBlank()) {
                                        lastInteractionId = interactionId
                                    }
                                    messages.add(AiChatMessage("AI", reply))
                                }.onFailure { err ->
                                    messages.add(
                                        AiChatMessage(
                                            "AI",
                                            err.message ?: "No fue posible conectar con el servicio de IA en este momento."
                                        )
                                    )
                                }
                                isSending = false
                            }
                        }
                    },
                    enabled = userPrompt.isNotBlank() && !isSending,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("ai_chat_send_btn")
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar consulta")
                    }
                }
            }
        }
    }
}

/**
 * 17. SUBPANTALLA «CONFIGURACIÓN»
 */
@Composable
fun ConfiguracionSubscreen(
    profile: UserProfile?,
    isBusy: Boolean,
    onBack: () -> Unit,
    onToggleExternalAgentMode: (Boolean) -> Unit,
    onOpenPrivacyDialog: () -> Unit
) {
    val currentTier = profile?.planTier ?: AdvisorMemberships.TIER_FREE
    val supportsExternal = AdvisorMemberships.getSpec(currentTier)?.supportsExternalAgentMode == true

    Column(modifier = Modifier.fillMaxSize()) {
        SubscreenTopBar(
            title = "Configuración",
            subtitle = "Ajustes operativos, modalidad de agente y términos legales",
            onBack = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = YvyForestPrimary
                            )
                            Text(
                                text = "Preferencias operativas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Modalidad de Agente Inmobiliario Externo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (supportsExternal) {
                                        "Permite operar como firma o corredor externo a Yvy Capital."
                                    } else {
                                        "Disponible a partir del Plan Intermedio y Plan Senior."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = (profile?.externalAgentMode == true) && supportsExternal,
                                onCheckedChange = { onToggleExternalAgentMode(it) },
                                enabled = supportsExternal && !isBusy,
                                modifier = Modifier.testTag("config_external_agent_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text(
                            text = "Regla de visualización monetaria activa",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Cada propiedad en Yvy Capital conserva y muestra su precio exclusivamente en la moneda definida por el autor de la publicación (Guaraníes ₲ o Dólares estadounidenses $), sin reconversiones automáticas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Marco legal y privacidad",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = onOpenPrivacyDialog,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("privacy_policy_btn")
                        ) {
                            Icon(Icons.Default.PrivacyTip, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Política de Privacidad y Términos de Uso")
                        }
                    }
                }
            }
        }
    }
}
