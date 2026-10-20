package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FavoriteItem
import com.example.data.model.Property
import com.example.data.model.PropertyFilter
import com.example.data.model.PropertyInquiry
import com.example.data.model.UserProfile
import com.example.data.repository.YvyRepository
import com.example.ui.util.CurrencyFormatter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class YvyViewModel(
    private val repository: YvyRepository,
    val currentUserId: String
) : ViewModel() {

    private val _filter = MutableStateFlow(PropertyFilter())
    val filter: StateFlow<PropertyFilter> = _filter.asStateFlow()

    private val _displayCurrency = MutableStateFlow("PYG")
    val displayCurrency: StateFlow<String> = _displayCurrency.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _visibleCount = MutableStateFlow(PAGE_SIZE)
    val visibleCount: StateFlow<Int> = _visibleCount.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    // 1. Catálogo de propiedades activas en tiempo real
    val propertiesState: StateFlow<UiState<List<Property>>> = repository.observeActiveProperties()
        .map<List<Property>, UiState<List<Property>>> { list ->
            UiState.Success(list)
        }
        .catch { error ->
            Log.w(TAG, "Error observing active properties", error)
            emit(UiState.Error(error.localizedMessage ?: "No se pudo cargar el catálogo de propiedades."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // Propiedades filtradas y paginadas
    val filteredProperties: StateFlow<List<Property>> = combine(
        propertiesState,
        _filter
    ) { state, currentFilter ->
        val all = (state as? UiState.Success)?.data ?: emptyList()
        all.filter { prop ->
            val matchesQuery = currentFilter.searchQuery.isBlank() ||
                prop.title.contains(currentFilter.searchQuery, ignoreCase = true) ||
                prop.neighborhood.contains(currentFilter.searchQuery, ignoreCase = true) ||
                prop.city.contains(currentFilter.searchQuery, ignoreCase = true) ||
                prop.department.contains(currentFilter.searchQuery, ignoreCase = true) ||
                prop.description.contains(currentFilter.searchQuery, ignoreCase = true)

            val matchesDept = currentFilter.department == "Todos" ||
                prop.department.equals(currentFilter.department, ignoreCase = true)

            val matchesCity = currentFilter.city == "Todas" ||
                prop.city.equals(currentFilter.city, ignoreCase = true)

            val matchesOp = currentFilter.operationType == "ALL" ||
                prop.operationType.equals(currentFilter.operationType, ignoreCase = true)

            val matchesType = currentFilter.propertyType == "ALL" ||
                prop.propertyType.equals(currentFilter.propertyType, ignoreCase = true)

            val matchesBeds = prop.bedrooms >= currentFilter.minBedrooms
            val matchesBaths = prop.bathrooms >= currentFilter.minBathrooms
            val matchesParking = prop.parkingSpaces >= currentFilter.minParking
            val matchesArea = prop.areaTotalM2 >= currentFilter.minAreaM2
            val matchesFeatured = !currentFilter.onlyFeatured || prop.featured

            matchesQuery && matchesDept && matchesCity && matchesOp && matchesType &&
                matchesBeds && matchesBaths && matchesParking && matchesArea && matchesFeatured
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
    )

    // 2. Favoritos sincronizados del usuario
    val favoritesState: StateFlow<UiState<List<FavoriteItem>>> = repository.observeFavorites(currentUserId)
        .map<List<FavoriteItem>, UiState<List<FavoriteItem>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing favorites", error)
            emit(UiState.Error(error.localizedMessage ?: "Error al cargar favoritos."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // 3. Perfil del usuario
    val userProfileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { profile ->
            if (profile != null) {
                _displayCurrency.value = profile.preferredCurrency
            }
            UiState.Success(profile)
        }
        .catch { error ->
            Log.w(TAG, "Error observing user profile", error)
            emit(UiState.Error(error.localizedMessage ?: "Error al cargar el perfil."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // 4. Consultas enviadas por el cliente
    val userInquiriesState: StateFlow<UiState<List<PropertyInquiry>>> = repository.observeUserInquiries(currentUserId)
        .map<List<PropertyInquiry>, UiState<List<PropertyInquiry>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing user inquiries", error)
            emit(UiState.Error(error.localizedMessage ?: "Error al cargar solicitudes."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // 5. Consultas recibidas como Asesor (Yvy Capital Pro)
    val agentInquiriesState: StateFlow<UiState<List<PropertyInquiry>>> = repository.observeAgentInquiries(currentUserId)
        .map<List<PropertyInquiry>, UiState<List<PropertyInquiry>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing agent inquiries", error)
            emit(UiState.Error(error.localizedMessage ?: "Error al cargar consultas de clientes."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    // 6. Propiedades propias del asesor (Yvy Capital Pro)
    val agentPropertiesState: StateFlow<UiState<List<Property>>> = repository.observeAgentProperties(currentUserId)
        .map<List<Property>, UiState<List<Property>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing agent properties", error)
            emit(UiState.Error(error.localizedMessage ?: "Error al cargar tus publicaciones."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    init {
        initializeUserAndCatalog()
    }

    private fun initializeUserAndCatalog() {
        viewModelScope.launch {
            val firebaseUser = Firebase.auth.currentUser
            if (firebaseUser != null) {
                repository.ensureOrUpdateUserProfile(
                    displayName = firebaseUser.displayName ?: "Cliente Yvy Capital",
                    email = firebaseUser.email ?: "cliente@yvycapital.com.py"
                )
                repository.seedParaguayCatalogIfEmpty()
            }
        }
    }

    fun setDisplayCurrency(currency: String) {
        _displayCurrency.value = currency
    }

    fun toggleViewMode() {
        _isGridView.update { !it }
    }

    fun loadMoreItems() {
        _visibleCount.update { it + PAGE_SIZE }
    }

    fun updateFilter(transform: (PropertyFilter) -> PropertyFilter) {
        _filter.update(transform)
        _visibleCount.value = PAGE_SIZE
    }

    fun resetFilters() {
        _filter.value = PropertyFilter()
        _visibleCount.value = PAGE_SIZE
    }

    fun clearMessage() {
        _actionMessage.value = null
    }

    fun toggleFavorite(property: Property) {
        viewModelScope.launch {
            val currentFavs = (favoritesState.value as? UiState.Success)?.data ?: emptyList()
            val isFav = currentFavs.any { it.propertyId == property.id }
            repository.toggleFavorite(property, isFav)
                .onSuccess { added ->
                    _actionMessage.value = if (added) {
                        "Propiedad guardada en tus favoritos"
                    } else {
                        "Propiedad eliminada de favoritos"
                    }
                }
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "No se pudo actualizar favoritos"
                }
        }
    }

    fun submitInquiry(
        property: Property,
        userName: String,
        userEmail: String,
        userPhone: String,
        inquiryType: String,
        preferredDate: String,
        message: String,
        onSuccess: () -> Unit
    ) {
        if (userName.trim().length < 2) {
            _actionMessage.value = "Por favor, ingresa tu nombre completo."
            return
        }
        if (userPhone.trim().length < 6) {
            _actionMessage.value = "Ingresa un número de teléfono o WhatsApp válido."
            return
        }
        if (message.trim().length < 5) {
            _actionMessage.value = "Escribe un mensaje breve para el asesor."
            return
        }

        viewModelScope.launch {
            _isBusy.value = true
            repository.submitInquiry(
                property = property,
                userName = userName,
                userEmail = userEmail,
                userPhone = userPhone,
                inquiryType = inquiryType,
                preferredDate = preferredDate.ifBlank { "A coordinar" },
                message = message
            ).onSuccess {
                _isBusy.value = false
                _actionMessage.value = "Solicitud enviada con éxito al asesor inmobiliario."
                onSuccess()
            }.onFailure { err ->
                _isBusy.value = false
                _actionMessage.value = err.localizedMessage ?: "Error al enviar la solicitud."
            }
        }
    }

    fun updateInquiryStatus(inquiryId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateInquiryStatus(inquiryId, newStatus)
                .onSuccess {
                    _actionMessage.value = "Estado actualizado a: ${CurrencyFormatter.inquiryStatusLabel(newStatus)}"
                }
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "No se pudo actualizar el estado."
                }
        }
    }

    fun submitReport(
        propertyId: String,
        propertyTitle: String,
        reason: String,
        details: String,
        onSuccess: () -> Unit
    ) {
        if (details.trim().length < 5) {
            _actionMessage.value = "Por favor describe brevemente el motivo del reporte."
            return
        }
        viewModelScope.launch {
            _isBusy.value = true
            repository.submitReport(propertyId, propertyTitle, reason, details)
                .onSuccess {
                    _isBusy.value = false
                    _actionMessage.value = "Reporte recibido. Nuestro equipo de moderación revisará el anuncio."
                    onSuccess()
                }
                .onFailure { err ->
                    _isBusy.value = false
                    _actionMessage.value = err.localizedMessage ?: "No se pudo enviar el reporte."
                }
        }
    }

    fun saveUserProfile(
        displayName: String,
        phone: String,
        preferredCurrency: String,
        role: String,
        agencyName: String
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val email = Firebase.auth.currentUser?.email ?: "cliente@yvycapital.com.py"
            repository.ensureOrUpdateUserProfile(
                displayName = displayName,
                email = email,
                phone = phone,
                preferredCurrency = preferredCurrency,
                role = role,
                agencyName = agencyName
            ).onSuccess {
                _isBusy.value = false
                _displayCurrency.value = preferredCurrency
                _actionMessage.value = "Perfil y preferencias actualizados en la nube."
            }.onFailure { err ->
                _isBusy.value = false
                _actionMessage.value = err.localizedMessage ?: "Error al guardar el perfil."
            }
        }
    }

    fun publishNewProperty(
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
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val newProperty = Property(
                title = title.trim(),
                description = description.trim(),
                operationType = operationType,
                propertyType = propertyType,
                price = price,
                currency = currency,
                department = department.trim(),
                city = city.trim(),
                neighborhood = neighborhood.trim(),
                address = address.trim().ifBlank { "$neighborhood, $city" },
                latitude = -25.2867,
                longitude = -57.6470,
                areaTotalM2 = areaTotalM2.coerceAtLeast(1L),
                areaBuiltM2 = areaBuiltM2.coerceAtLeast(0L),
                bedrooms = bedrooms.coerceAtLeast(0L),
                bathrooms = bathrooms.coerceAtLeast(0L),
                parkingSpaces = parkingSpaces.coerceAtLeast(0L),
                amenities = amenities.take(15),
                imageUrls = listOf("custom_listing"),
                featured = false,
                status = "active",
                agentId = currentUserId,
                agentName = agentName.trim().ifBlank { "Asesor Yvy Capital Pro" },
                agentAgency = agentAgency.trim().ifBlank { "Yvy Capital Pro" },
                agentWhatsapp = agentWhatsapp.trim().ifBlank { "+595981000000" },
                verified = true
            )
            repository.createProperty(newProperty)
                .onSuccess {
                    _isBusy.value = false
                    _actionMessage.value = "Propiedad publicada exitosamente en el catálogo de Yvy Capital."
                    onSuccess()
                }
                .onFailure { err ->
                    _isBusy.value = false
                    _actionMessage.value = err.localizedMessage ?: "Error al publicar la propiedad."
                }
        }
    }

    fun deleteAgentProperty(propertyId: String) {
        viewModelScope.launch {
            repository.deleteProperty(propertyId)
                .onSuccess {
                    _actionMessage.value = "Anuncio eliminado correctamente."
                }
                .onFailure { err ->
                    _actionMessage.value = err.localizedMessage ?: "No se pudo eliminar el anuncio."
                }
        }
    }

    fun deleteAccountData(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isBusy.value = true
            repository.deleteUserProfile()
                .onSuccess {
                    _isBusy.value = false
                    onDeleted()
                }
                .onFailure { err ->
                    _isBusy.value = false
                    _actionMessage.value = err.localizedMessage ?: "Error al eliminar los datos de la cuenta."
                }
        }
    }

    fun reloadDemoCatalog() {
        viewModelScope.launch {
            _isBusy.value = true
            repository.seedParaguayCatalogIfEmpty()
                .onSuccess { count ->
                    _isBusy.value = false
                    _actionMessage.value = if (count > 0) {
                        "Se restauraron $count propiedades de demostración."
                    } else {
                        "El catálogo ya cuenta con propiedades activas."
                    }
                }
                .onFailure { err ->
                    _isBusy.value = false
                    _actionMessage.value = err.localizedMessage ?: "Error al sincronizar el catálogo."
                }
        }
    }

    private companion object {
        const val TAG = "YvyViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
        const val PAGE_SIZE = 4
    }
}
