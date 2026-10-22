package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import com.example.data.model.Property
import com.example.data.model.StaffUser
import com.example.data.repository.MembershipsRepository
import com.example.data.repository.PublicStaffRepository
import com.example.data.repository.StaffRepository
import com.example.data.repository.YvyRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.StaffLoginScreen
import com.example.ui.auth.signOut
import com.example.ui.components.DeepLinkCenterDialog
import com.example.ui.components.DeepLinkNotFoundDialog
import com.example.ui.components.PublicWebFallbackPreviewDialog
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileAndProScreen
import com.example.ui.screens.PropertyDetailScreen
import com.example.ui.screens.StaffDashboardScreen
import com.example.ui.theme.YvyCapitalTheme
import com.example.ui.viewmodel.DeepLinkResolutionState
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.YvyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val incomingDeepLinkFlow = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extractDeepLinkFromIntent(intent)
        enableEdgeToEdge()
        setContent {
            val pendingDeepLink by incomingDeepLinkFlow.collectAsStateWithLifecycle()
            YvyCapitalTheme {
                AppNavigation(
                    pendingDeepLinkUrl = pendingDeepLink,
                    onConsumeDeepLink = { incomingDeepLinkFlow.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractDeepLinkFromIntent(intent)
    }

    private fun extractDeepLinkFromIntent(intent: Intent?) {
        val dataUri = intent?.dataString
        if (!dataUri.isNullOrBlank()) {
            incomingDeepLinkFlow.value = dataUri
            return
        }
        val deferredReferrer = intent?.getStringExtra("referrer")
        if (!deferredReferrer.isNullOrBlank()) {
            incomingDeepLinkFlow.value = deferredReferrer
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

enum class AuthScreenMode {
    CLIENT,
    STAFF
}

@Composable
fun AppNavigation(
    auth: FirebaseAuth = Firebase.auth,
    pendingDeepLinkUrl: String? = null,
    onConsumeDeepLink: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    val db = remember {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank() && dbId != "(default)") {
                FirebaseFirestore.getInstance(dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (_: Exception) {
            FirebaseFirestore.getInstance()
        }
    }

    var authScreenMode by remember { mutableStateOf(AuthScreenMode.CLIENT) }
    var activeStaffUser by remember { mutableStateOf<StaffUser?>(null) }
    var useAppAsStaff by remember(user?.uid) { mutableStateOf(false) }
    var revocationAlert by remember { mutableStateOf<String?>(null) }

    val staffRepository = remember(user?.uid) {
        if (user != null) {
            StaffRepository(
                db = db,
                userId = user.uid,
                onAccessRevoked = { msg ->
                    revocationAlert = msg
                    activeStaffUser = null
                    useAppAsStaff = false
                }
            )
        } else null
    }

    val publicStaffRepository = remember(db) {
        PublicStaffRepository(db)
    }

    val membershipsRepository = remember(db) {
        MembershipsRepository(db)
    }

    val liveStaffUser by (staffRepository?.staffUser ?: MutableStateFlow(null))
        .collectAsStateWithLifecycle()
    val isLiveStaffActive by (staffRepository?.isActiveStaff ?: MutableStateFlow(false))
        .collectAsStateWithLifecycle()

    LaunchedEffect(liveStaffUser, isLiveStaffActive) {
        if (isLiveStaffActive && liveStaffUser != null) {
            activeStaffUser = liveStaffUser
        } else if (!isLiveStaffActive && activeStaffUser != null && user != null) {
            activeStaffUser = null
            useAppAsStaff = false
        }
    }

    if (user == null) {
        when (authScreenMode) {
            AuthScreenMode.CLIENT -> {
                AuthScreen(
                    onAuthSuccess = {
                        // AuthStateListener updates currentUser automatically
                    },
                    onNavigateToStaffLogin = {
                        authScreenMode = AuthScreenMode.STAFF
                    }
                )
            }
            AuthScreenMode.STAFF -> {
                StaffLoginScreen(
                    onBack = {
                        authScreenMode = AuthScreenMode.CLIENT
                    },
                    onStaffLoginSuccess = { staff ->
                        activeStaffUser = staff
                    }
                )
            }
        }
    } else {
        if (activeStaffUser != null && !useAppAsStaff) {
            StaffDashboardScreen(
                staffUser = activeStaffUser!!,
                staffRepository = staffRepository,
                membershipsRepository = membershipsRepository,
                onUseAppAsStaff = {
                    useAppAsStaff = true
                },
                onSignOut = {
                    auth.signOut()
                    activeStaffUser = null
                    useAppAsStaff = false
                    authScreenMode = AuthScreenMode.CLIENT
                }
            )
        } else {
            YvyMainWorkspace(
                currentUserId = user.uid,
                staffRepository = staffRepository,
                publicStaffRepository = publicStaffRepository,
                membershipsRepository = membershipsRepository,
                pendingDeepLinkUrl = pendingDeepLinkUrl,
                onConsumeDeepLink = onConsumeDeepLink,
                onBackToStaffDashboard = if (activeStaffUser != null) {
                    { useAppAsStaff = false }
                } else null
            )
        }
    }

    revocationAlert?.let { alertMsg ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { revocationAlert = null },
            title = { Text("Aviso de acceso") },
            text = { Text(alertMsg) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { revocationAlert = null }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

enum class YvyNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Inicio", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    EXPLORE("Explorar", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_explore"),
    FAVORITES("Favoritos", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "nav_favorites"),
    PROFILE("Perfil", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
}

@Composable
fun YvyMainWorkspace(
    currentUserId: String,
    staffRepository: StaffRepository? = null,
    publicStaffRepository: PublicStaffRepository? = null,
    membershipsRepository: MembershipsRepository? = null,
    pendingDeepLinkUrl: String? = null,
    onConsumeDeepLink: () -> Unit = {},
    onBackToStaffDashboard: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: YvyViewModel = viewModel(
        key = "$currentUserId-${staffRepository != null}",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                YvyViewModel(
                    repository = YvyRepository(db),
                    staffRepository = staffRepository,
                    publicStaffRepository = publicStaffRepository,
                    membershipsRepository = membershipsRepository,
                    currentUserId = currentUserId
                )
            }
        }
    )

    val propertiesState by viewModel.propertiesState.collectAsStateWithLifecycle()
    val filteredProperties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val favoritesState by viewModel.favoritesState.collectAsStateWithLifecycle()
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val userInquiriesState by viewModel.userInquiriesState.collectAsStateWithLifecycle()
    val agentPropertiesState by viewModel.agentPropertiesState.collectAsStateWithLifecycle()
    val agentInquiriesState by viewModel.agentInquiriesState.collectAsStateWithLifecycle()
    val agentPromoVideosState by viewModel.agentPromoVideosState.collectAsStateWithLifecycle()
    val userMembershipRequests by viewModel.userMembershipRequests.collectAsStateWithLifecycle()
    val effectivePlan by viewModel.effectivePlan.collectAsStateWithLifecycle()
    val staffRole by viewModel.staffRole.collectAsStateWithLifecycle()
    val publicStaffMap by viewModel.publicStaffMap.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val displayCurrency by viewModel.displayCurrency.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()
    val visibleCount by viewModel.visibleCount.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val deepLinkResolution by viewModel.deepLinkResolution.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(YvyNavTab.HOME) }
    var profileInitialSubTab by remember { mutableIntStateOf(0) }
    var selectedProperty by remember { mutableStateOf<Property?>(null) }
    var openExploreFilterOnEntry by remember { mutableStateOf(false) }
    var showDeepLinkCenterDialog by remember { mutableStateOf(false) }
    var webFallbackPreviewProperty by remember { mutableStateOf<Property?>(null) }
    var showWebFallbackModal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pendingDeepLinkUrl) {
        val url = pendingDeepLinkUrl
        if (!url.isNullOrBlank()) {
            viewModel.resolveDeepLink(url)
            onConsumeDeepLink()
        }
    }

    LaunchedEffect(deepLinkResolution) {
        when (val res = deepLinkResolution) {
            is DeepLinkResolutionState.ResolvedProperty -> {
                selectedProperty = res.property
                viewModel.showMessage("Propiedad abierta desde enlace directo: ${res.property.title}")
                viewModel.clearDeepLinkResolution()
            }
            is DeepLinkResolutionState.ResolvedExplore -> {
                selectedProperty = null
                currentTab = YvyNavTab.EXPLORE
                viewModel.showMessage(res.summaryMessage)
                viewModel.clearDeepLinkResolution()
            }
            else -> Unit
        }
    }

    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    // Back button returns to Home tab when on a secondary tab and no detail is open
    BackHandler(enabled = selectedProperty == null && currentTab != YvyNavTab.HOME) {
        currentTab = YvyNavTab.HOME
    }

    if (onBackToStaffDashboard != null) {
        BackHandler(enabled = selectedProperty == null && currentTab == YvyNavTab.HOME) {
            onBackToStaffDashboard()
        }
    }

    LaunchedEffect(selectedProperty?.id) {
        val propId = selectedProperty?.id
        if (!propId.isNullOrBlank()) {
            viewModel.recordPropertyView(propId)
        }
    }

    val favoritesList = (favoritesState as? UiState.Success)?.data ?: emptyList()
    val userProfile = (userProfileState as? UiState.Success)?.data
    val activeCatalogList = (propertiesState as? UiState.Success)?.data ?: emptyList()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!isExpandedScreen && selectedProperty == null) {
                    androidx.compose.foundation.layout.Column {
                        androidx.compose.material3.HorizontalDivider(
                            thickness = 1.dp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.outline
                        )
                        NavigationBar(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            YvyNavTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (tab == YvyNavTab.PROFILE) {
                                            profileInitialSubTab = 0
                                        }
                                        currentTab = tab
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = { Text(tab.title) },
                                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                        selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                        unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen && selectedProperty == null) {
                    NavigationRail(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                    ) {
                        YvyNavTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    if (tab == YvyNavTab.PROFILE) {
                                        profileInitialSubTab = 0
                                    }
                                    currentTab = tab
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) },
                                colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                                    selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                    unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                    androidx.compose.material3.VerticalDivider(
                        thickness = 1.dp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.outline
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    val activeProp = selectedProperty
                    if (activeProp != null) {
                        val isStaffAdmin = staffRole?.equals("admin", ignoreCase = true) == true
                        PropertyDetailScreen(
                            property = activeProp,
                            userProfile = userProfile,
                            isFavorite = favoritesList.any { it.propertyId == activeProp.id },
                            isAgentStaff = publicStaffMap.containsKey(activeProp.agentId),
                            isAdmin = isStaffAdmin,
                            agentMembershipTier = viewModel.getAgentMembershipTier(activeProp.agentId),
                            displayCurrency = displayCurrency,
                            isBusy = isBusy,
                            onBack = { selectedProperty = null },
                            onToggleFavorite = { viewModel.toggleFavorite(activeProp) },
                            onToggleVerified = if (isStaffAdmin) {
                                { newVerified ->
                                    viewModel.updatePropertyVerified(activeProp.id, newVerified)
                                    selectedProperty = activeProp.copy(verified = newVerified)
                                }
                            } else null,
                            onSubmitInquiry = { name, email, phone, type, date, msg, onDone ->
                                viewModel.submitInquiry(
                                    property = activeProp,
                                    userName = name,
                                    userEmail = email,
                                    userPhone = phone,
                                    inquiryType = type,
                                    preferredDate = date,
                                    message = msg,
                                    onSuccess = onDone
                                )
                            },
                                    onSubmitReport = { reason, details, onDone ->
                                viewModel.submitReport(
                                    propertyId = activeProp.id,
                                    propertyTitle = activeProp.title,
                                    reason = reason,
                                    details = details,
                                    onSuccess = onDone
                                )
                            },
                            onMessage = { msg -> viewModel.showMessage(msg) }
                        )
                    } else {
                        when (currentTab) {
                            YvyNavTab.HOME -> {
                                HomeScreen(
                                    propertiesState = propertiesState,
                                    favorites = favoritesList,
                                    displayCurrency = displayCurrency,
                                    onToggleCurrency = { viewModel.setDisplayCurrency(it) },
                                    isStaffAgent = { publicStaffMap.containsKey(it) },
                                    onSearchSubmit = { query ->
                                        viewModel.updateFilter { it.copy(searchQuery = query) }
                                        currentTab = YvyNavTab.EXPLORE
                                    },
                                    onQuickOperationSelect = { op ->
                                        viewModel.updateFilter { it.copy(operationType = op) }
                                        currentTab = YvyNavTab.EXPLORE
                                    },
                                    onQuickCategorySelect = { cat ->
                                        viewModel.updateFilter { it.copy(propertyType = cat) }
                                        currentTab = YvyNavTab.EXPLORE
                                    },
                                    onNavigateToExplore = {
                                        currentTab = YvyNavTab.EXPLORE
                                    },
                                    onOpenExploreFilter = {
                                        openExploreFilterOnEntry = true
                                        currentTab = YvyNavTab.EXPLORE
                                    },
                                    onNavigateToFavorites = {
                                        currentTab = YvyNavTab.FAVORITES
                                    },
                                    onNavigateToPro = {
                                        profileInitialSubTab = 6
                                        currentTab = YvyNavTab.PROFILE
                                    },
                                    onPropertyClick = { selectedProperty = it },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onReloadDemo = { viewModel.reloadDemoCatalog() },
                                    onOpenDeepLinkCenter = { showDeepLinkCenterDialog = true }
                                )
                            }

                            YvyNavTab.EXPLORE -> {
                                ExploreScreen(
                                    filteredProperties = filteredProperties,
                                    allCatalogProperties = activeCatalogList,
                                    favorites = favoritesList,
                                    filter = filter,
                                    displayCurrency = displayCurrency,
                                    isGridView = isGridView,
                                    visibleCount = visibleCount,
                                    autoOpenFilter1 = openExploreFilterOnEntry,
                                    onConsumeAutoOpenFilter1 = { openExploreFilterOnEntry = false },
                                    isStaffAgent = { publicStaffMap.containsKey(it) },
                                    onUpdateFilter = { viewModel.updateFilter(it) },
                                    onResetFilters = { viewModel.resetFilters() },
                                    onToggleViewMode = { viewModel.toggleViewMode() },
                                    onLoadMore = { viewModel.loadMoreItems() },
                                    onPropertyClick = { selectedProperty = it },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                                )
                            }

                            YvyNavTab.FAVORITES -> {
                                FavoritesScreen(
                                    allPropertiesState = propertiesState,
                                    favoritesState = favoritesState,
                                    userInquiriesState = userInquiriesState,
                                    displayCurrency = displayCurrency,
                                    isStaffAgent = { publicStaffMap.containsKey(it) },
                                    onNavigateToExplore = { currentTab = YvyNavTab.EXPLORE },
                                    onPropertyClick = { selectedProperty = it },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                                )
                            }

                            YvyNavTab.PROFILE -> {
                                ProfileAndProScreen(
                                    userProfileState = userProfileState,
                                    agentPropertiesState = agentPropertiesState,
                                    agentInquiriesState = agentInquiriesState,
                                    agentPromoVideosState = agentPromoVideosState,
                                    userMembershipRequests = userMembershipRequests,
                                    effectivePlan = effectivePlan,
                                    isPublicStaff = publicStaffMap.containsKey(currentUserId) || effectivePlan.isStaff,
                                    isStaffUser = { publicStaffMap.containsKey(it) },
                                    catalogProperties = activeCatalogList,
                                    displayCurrency = displayCurrency,
                                    isBusy = isBusy,
                                    initialTab = profileInitialSubTab,
                                    onSaveProfile = { name, phone, currency, role, agency ->
                                        viewModel.saveUserProfile(name, phone, currency, role, agency)
                                    },
                                    onRequestPlan = { planTier, method, ref ->
                                        viewModel.submitMembershipRequest(planTier, method, ref)
                                    },
                                    onToggleExternalAgentMode = { enabled ->
                                        viewModel.toggleExternalAgentMode(enabled)
                                    },
                                    onPublishProperty = { title, desc, op, type, price, curr, dept, city, neigh, addr, areaT, areaB, beds, baths, park, amen, featured, agName, agAgency, agWa, onDone ->
                                        viewModel.publishNewProperty(
                                            title, desc, op, type, price, curr, dept, city, neigh, addr,
                                            areaT, areaB, beds, baths, park, amen, featured, agName, agAgency, agWa, onDone
                                        )
                                    },
                                    onGeneratePromoVideo = { prop, ratio, scenes, music, headline, preferAi, onDone ->
                                        viewModel.generateAndSavePromoVideo(
                                            context = context,
                                            property = prop,
                                            aspectRatio = ratio,
                                            selectedImages = scenes,
                                            includeBackgroundMusic = music,
                                            customHeadline = headline,
                                            preferAiInteractions = preferAi,
                                            onSuccess = onDone
                                        )
                                    },
                                    onDeletePromoVideo = { viewModel.deletePromoVideo(it) },
                                    onDeleteProperty = { viewModel.deleteAgentProperty(it) },
                                    onUpdateInquiryStatus = { id, status ->
                                        viewModel.updateInquiryStatus(id, status)
                                    },
                                    onMessage = { msg -> viewModel.showMessage(msg) },
                                    onSignOut = {
                                        signOut(
                                            context = context,
                                            credentialManager = credentialManager,
                                            onSignOutComplete = {},
                                            scope = scope
                                        )
                                    },
                                    onDeleteAccount = {
                                        viewModel.deleteAccountData {
                                            signOut(
                                                context = context,
                                                credentialManager = credentialManager,
                                                onSignOutComplete = {},
                                                scope = scope
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Caso C: Modal de Propiedad Eliminada, Despublicada o Enlace No Disponible
        val notFoundState = deepLinkResolution as? DeepLinkResolutionState.NotFoundOrUnavailable
        if (notFoundState != null) {
            DeepLinkNotFoundDialog(
                rawUrl = notFoundState.rawUrl,
                title = notFoundState.title,
                message = notFoundState.message,
                onGoHome = {
                    selectedProperty = null
                    currentTab = YvyNavTab.HOME
                    viewModel.clearDeepLinkResolution()
                },
                onExploreProperties = {
                    selectedProperty = null
                    viewModel.resetFilters()
                    currentTab = YvyNavTab.EXPLORE
                    viewModel.clearDeepLinkResolution()
                },
                onDismiss = {
                    viewModel.clearDeepLinkResolution()
                }
            )
        }

        // Centro de Enlaces Directos (Pegar URL o Verificar Casos A, B y C)
        if (showDeepLinkCenterDialog) {
            DeepLinkCenterDialog(
                activeProperties = activeCatalogList,
                displayCurrency = displayCurrency,
                onResolveLink = { url ->
                    showDeepLinkCenterDialog = false
                    viewModel.resolveDeepLink(url)
                },
                onPreviewWebFallback = { prop ->
                    showDeepLinkCenterDialog = false
                    webFallbackPreviewProperty = prop
                    showWebFallbackModal = true
                },
                onDismiss = { showDeepLinkCenterDialog = false }
            )
        }

        // Caso B: Vista Previa de Página Pública de Destino (cuando el destinatario no tiene la app)
        val previewProp = webFallbackPreviewProperty
        if (showWebFallbackModal && previewProp != null) {
            PublicWebFallbackPreviewDialog(
                property = previewProp,
                displayCurrency = displayCurrency,
                onOpenDirectlyInApp = {
                    showWebFallbackModal = false
                    selectedProperty = previewProp
                    viewModel.showMessage("Abriendo propiedad directamente en Yvy Capital")
                },
                onCopyDeferredLink = { url ->
                    viewModel.showMessage("Enlace diferido copiado al portapapeles: $url")
                },
                onDismiss = { showWebFallbackModal = false }
            )
        }
    }
}
