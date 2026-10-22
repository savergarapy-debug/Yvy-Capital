package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AdvisorMemberships
import com.example.data.model.FavoriteItem
import com.example.data.model.Property
import com.example.data.model.PropertyInquiry
import com.example.data.model.PropertyPromoVideo
import com.example.data.model.PropertyReport
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class YvyRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("El usuario debe iniciar sesión con Google antes de realizar esta operación.")
    }

    // --- 1. CATÁLOGO DE PROPIEDADES ---

    fun observeActiveProperties(): Flow<List<Property>> {
        val path = "properties"
        return db.collection(path)
            .whereEqualTo("status", "active")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Property::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observePropiedadesActivas(): Flow<List<com.example.data.model.Propiedad>> {
        return observeActiveProperties().map { list ->
            list.map { com.example.data.model.Propiedad.fromProperty(it) }
        }
    }

    suspend fun guardarPropiedad(propiedad: com.example.data.model.Propiedad): Result<String> {
        return createProperty(propiedad.toProperty())
    }

    fun observeAgentProperties(agentId: String): Flow<List<Property>> {
        val path = "properties"
        return db.collection(path)
            .whereEqualTo("agentId", agentId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Property::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun getPropertyById(propertyId: String): Result<Property?> = runCatching {
        val path = "properties/$propertyId"
        try {
            val doc = db.collection("properties").document(propertyId).get().await()
            doc.toObject(Property::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            throw e
        }
    }

    suspend fun createProperty(property: Property): Result<String> = runCatching {
        val uid = requireUserId()
        val path = "properties"
        val propId = if (property.id.isNotBlank() && !property.id.startsWith("demo-") && !property.id.startsWith("py_")) {
            property.id
        } else {
            "prop_" + UUID.randomUUID().toString().replace("-", "").take(20)
        }
        val propRef = db.collection(path).document(propId)
        val counterRef = db.collection("listingCounters").document(uid)
        val isActive = property.status == "active"

        try {
            db.runTransaction { transaction ->
                val newCount = if (isActive) {
                    val counterSnap = transaction.get(counterRef)
                    val currentCount = if (counterSnap.exists()) counterSnap.getLong("count") ?: 0L else 0L
                    currentCount + 1L
                } else {
                    0L
                }

                val payload = mapOf(
                    "title" to property.title,
                    "description" to property.description,
                    "operationType" to property.operationType,
                    "propertyType" to property.propertyType,
                    "price" to property.price,
                    "currency" to property.currency,
                    "department" to property.department,
                    "city" to property.city,
                    "neighborhood" to property.neighborhood,
                    "address" to property.address,
                    "latitude" to property.latitude,
                    "longitude" to property.longitude,
                    "areaTotalM2" to property.areaTotalM2,
                    "areaBuiltM2" to property.areaBuiltM2,
                    "bedrooms" to property.bedrooms,
                    "bathrooms" to property.bathrooms,
                    "parkingSpaces" to property.parkingSpaces,
                    "amenities" to property.amenities,
                    "imageUrls" to property.imageUrls,
                    "featured" to property.featured,
                    "status" to property.status,
                    "agentId" to uid,
                    "agentName" to property.agentName,
                    "agentAgency" to property.agentAgency,
                    "agentWhatsapp" to property.agentWhatsapp,
                    "verified" to false, // verified siempre false al publicar
                    "viewsCount" to property.viewsCount.coerceAtLeast(0L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                transaction.set(propRef, payload)

                if (isActive) {
                    transaction.set(
                        counterRef,
                        mapOf(
                            "count" to newCount,
                            "lastPropertyId" to propId
                        )
                    )
                }
                propId
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                throw IllegalStateException("Alcanzaste el límite de publicaciones activas de tu plan. Pausá o vendé una propiedad, o mejorá tu plan.")
            }
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun incrementPropertyViews(propertyId: String): Result<Long> = runCatching {
        requireUserId()
        val path = "properties/$propertyId"
        try {
            val docRef = db.collection("properties").document(propertyId)
            val snap = docRef.get().await()
            if (!snap.exists()) return@runCatching 0L
            val currentViews = snap.getLong("viewsCount") ?: 0L
            val nextViews = currentViews + 1L
            docRef.update(
                mapOf(
                    "viewsCount" to nextViews,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            nextViews
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun updatePropertyStatus(propertyId: String, newStatus: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val path = "properties/$propertyId"
        val propRef = db.collection("properties").document(propertyId)
        val counterRef = db.collection("listingCounters").document(uid)

        try {
            db.runTransaction { transaction ->
                val propSnap = transaction.get(propRef)
                val oldStatus = propSnap.getString("status") ?: "active"

                val isBecomingActive = oldStatus != "active" && newStatus == "active"
                val isCeasingActive = oldStatus == "active" && newStatus != "active"

                var newCount: Long? = null
                if (isBecomingActive || isCeasingActive) {
                    val counterSnap = transaction.get(counterRef)
                    val currentCount = if (counterSnap.exists()) counterSnap.getLong("count") ?: 0L else 0L
                    newCount = if (isBecomingActive) {
                        currentCount + 1L
                    } else {
                        (currentCount - 1L).coerceAtLeast(0L)
                    }
                }

                transaction.update(
                    propRef,
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                if (newCount != null) {
                    transaction.set(
                        counterRef,
                        mapOf(
                            "count" to newCount,
                            "lastPropertyId" to propertyId
                        )
                    )
                }
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                throw IllegalStateException("Alcanzaste el límite de publicaciones activas de tu plan. Pausá o vendé una propiedad, o mejorá tu plan.")
            }
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun deleteProperty(propertyId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val path = "properties/$propertyId"
        val propRef = db.collection("properties").document(propertyId)
        val counterRef = db.collection("listingCounters").document(uid)

        try {
            db.runTransaction { transaction ->
                val propSnap = transaction.get(propRef)
                val status = propSnap.getString("status") ?: "active"
                val wasActive = status == "active"

                var newCount: Long? = null
                if (wasActive) {
                    val counterSnap = transaction.get(counterRef)
                    val currentCount = if (counterSnap.exists()) counterSnap.getLong("count") ?: 0L else 0L
                    newCount = (currentCount - 1L).coerceAtLeast(0L)
                }

                transaction.delete(propRef)

                if (wasActive && newCount != null) {
                    transaction.set(
                        counterRef,
                        mapOf(
                            "count" to newCount,
                            "lastPropertyId" to propertyId
                        )
                    )
                }
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                throw IllegalStateException("Alcanzaste el límite de publicaciones activas de tu plan. Pausá o vendé una propiedad, o mejorá tu plan.")
            }
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    suspend fun updatePropertyVerified(propertyId: String, verified: Boolean): Result<Unit> = runCatching {
        requireUserId()
        val path = "properties/$propertyId"
        try {
            db.collection("properties").document(propertyId).update(
                mapOf(
                    "verified" to verified,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    // --- 2. PERFIL DE USUARIO Y PREFERENCIAS ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                throw error
            }
    }

    suspend fun ensureOrUpdateUserProfile(
        displayName: String,
        email: String,
        phone: String = "",
        preferredCurrency: String = "PYG",
        role: String = "CLIENT",
        agencyName: String = ""
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid"
        try {
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val createPayload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to displayName.ifBlank { "Usuario Yvy Capital" },
                    "email" to email.ifBlank { "cliente@yvycapital.com.py" },
                    "preferredCurrency" to preferredCurrency,
                    "role" to role,
                    "planTier" to "FREE",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (phone.isNotBlank()) createPayload["phone"] = phone
                if (agencyName.isNotBlank()) createPayload["agencyName"] = agencyName
                docRef.set(createPayload).await()
            } else {
                val updatePayload = mutableMapOf<String, Any>(
                    "displayName" to displayName.ifBlank { "Usuario Yvy Capital" },
                    "preferredCurrency" to preferredCurrency,
                    "role" to role,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (phone.isNotBlank()) updatePayload["phone"] = phone
                if (agencyName.isNotBlank()) updatePayload["agencyName"] = agencyName
                docRef.update(updatePayload).await()
            }
            val updatedDoc = docRef.get().await()
            updatedDoc.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: UserProfile(id = uid, userId = uid, displayName = displayName, email = email)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun activateAdvisorMembership(
        planTier: String,
        displayName: String,
        email: String,
        phone: String,
        agencyName: String,
        externalAgentMode: Boolean
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid"
        val spec = AdvisorMemberships.getSpec(planTier)
            ?: throw IllegalArgumentException("Plan de membresía no válido: $planTier")
        val cleanExternalMode = externalAgentMode && spec.supportsExternalAgentMode

        try {
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val createPayload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "displayName" to displayName.ifBlank { "Asesor Yvy Capital" },
                    "email" to email.ifBlank { "asesor@yvycapital.com.py" },
                    "preferredCurrency" to "PYG",
                    "role" to "AGENT",
                    "planTier" to spec.code,
                    "activeListingsCount" to 0L,
                    "externalAgentMode" to cleanExternalMode,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (phone.isNotBlank()) createPayload["phone"] = phone
                if (agencyName.isNotBlank()) createPayload["agencyName"] = agencyName
                docRef.set(createPayload).await()
            } else {
                val currentCount = (existing.getLong("activeListingsCount") ?: 0L).coerceIn(0L, 10L)
                val updatePayload = mutableMapOf<String, Any>(
                    "displayName" to displayName.ifBlank { "Asesor Yvy Capital" },
                    "role" to "AGENT",
                    "planTier" to spec.code,
                    "activeListingsCount" to currentCount,
                    "externalAgentMode" to cleanExternalMode,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (phone.isNotBlank()) updatePayload["phone"] = phone
                if (agencyName.isNotBlank()) updatePayload["agencyName"] = agencyName
                docRef.update(updatePayload).await()
            }
            val updatedDoc = docRef.get().await()
            updatedDoc.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: UserProfile(id = uid, userId = uid, displayName = displayName, email = email, role = "AGENT", planTier = spec.code)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun updateExternalAgentMode(enabled: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid"
        try {
            val docRef = db.collection("users").document(uid)
            val snap = docRef.get().await()
            val tier = snap.getString("planTier") ?: AdvisorMemberships.TIER_FREE
            val spec = AdvisorMemberships.getSpec(tier)
            if (enabled && (spec == null || !spec.supportsExternalAgentMode)) {
                throw IllegalStateException("La modalidad de agente inmobiliario externo solo está disponible en el Plan Intermedio y Plan Senior.")
            }
            docRef.update(
                mapOf(
                    "externalAgentMode" to enabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun deleteUserProfile(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid"
        try {
            db.collection("users").document(uid).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    // --- 2B. VIDEOS PROMOCIONALES (EXCLUSIVO PLAN SENIOR) ---

    fun observeAgentPromoVideos(agentId: String): Flow<List<PropertyPromoVideo>> {
        val path = "property_videos"
        return db.collection(path)
            .whereEqualTo("agentId", agentId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PropertyPromoVideo::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun createPromoVideo(video: PropertyPromoVideo): Result<String> = runCatching {
        val uid = requireUserId()
        val path = "property_videos"
        try {
            val staffSnap = db.collection("staff").document(uid).get().await()
            val isActiveStaff = staffSnap.exists() && staffSnap.getBoolean("active") == true

            val userSnap = db.collection("users").document(uid).get().await()
            val currentTier = userSnap.getString("planTier") ?: AdvisorMemberships.TIER_FREE
            if (!isActiveStaff && !AdvisorMemberships.isSeniorTier(currentTier)) {
                throw IllegalStateException("El Generador de Videos Promocionales es exclusivo para asesores con Membresía Plan Senior o personal de Yvy Capital.")
            }

            val docRef = if (video.id.isNotBlank()) {
                db.collection(path).document(video.id)
            } else {
                db.collection(path).document()
            }
            val payload = mapOf(
                "propertyId" to video.propertyId,
                "propertyTitle" to video.propertyTitle,
                "agentId" to uid,
                "planTierAtCreation" to AdvisorMemberships.TIER_SENIOR,
                "generationMode" to video.generationMode,
                "aspectRatio" to video.aspectRatio,
                "selectedImages" to video.selectedImages.take(6),
                "includeBackgroundMusic" to video.includeBackgroundMusic,
                "overlayHeadline" to video.overlayHeadline.take(160),
                "priceLabel" to video.priceLabel.take(80),
                "locationLabel" to video.locationLabel.take(120),
                "highlightsLabel" to video.highlightsLabel.take(200),
                "localVideoPath" to video.localVideoPath.take(500),
                "durationSeconds" to video.durationSeconds.coerceIn(3L, 30L),
                "status" to "READY",
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun deletePromoVideo(videoId: String): Result<Unit> = runCatching {
        requireUserId()
        val path = "property_videos/$videoId"
        try {
            db.collection("property_videos").document(videoId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    // --- 3. FAVORITOS SINCRONIZADOS ---

    fun observeFavorites(userId: String): Flow<List<FavoriteItem>> {
        val path = "users/$userId/favorites"
        return db.collection("users").document(userId).collection("favorites")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FavoriteItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun toggleFavorite(property: Property, isCurrentlyFavorite: Boolean): Result<Boolean> = runCatching {
        val uid = requireUserId()
        val path = "users/$uid/favorites/${property.id}"
        try {
            val favRef = db.collection("users").document(uid).collection("favorites").document(property.id)
            if (isCurrentlyFavorite) {
                favRef.delete().await()
                false
            } else {
                val payload = mapOf(
                    "userId" to uid,
                    "propertyId" to property.id,
                    "title" to property.title,
                    "price" to property.price,
                    "currency" to property.currency,
                    "operationType" to property.operationType,
                    "propertyType" to property.propertyType,
                    "city" to property.city,
                    "neighborhood" to property.neighborhood,
                    "areaTotalM2" to property.areaTotalM2,
                    "bedrooms" to property.bedrooms,
                    "bathrooms" to property.bathrooms,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                favRef.set(payload).await()
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    // --- 4. CONSULTAS Y SOLICITUDES DE VISITA ---

    fun observeUserInquiries(userId: String): Flow<List<PropertyInquiry>> {
        val path = "inquiries"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PropertyInquiry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeAgentInquiries(agentId: String): Flow<List<PropertyInquiry>> {
        val path = "inquiries"
        return db.collection(path)
            .whereEqualTo("agentId", agentId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PropertyInquiry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun submitInquiry(
        property: Property,
        userName: String,
        userEmail: String,
        userPhone: String,
        inquiryType: String,
        preferredDate: String,
        message: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val path = "inquiries"
        try {
            val docRef = db.collection(path).document()
            val payload = mapOf(
                "propertyId" to property.id,
                "propertyTitle" to property.title,
                "userId" to uid,
                "userName" to userName.trim(),
                "userEmail" to userEmail.trim(),
                "userPhone" to userPhone.trim(),
                "agentId" to property.agentId.ifBlank { uid },
                "inquiryType" to inquiryType,
                "preferredDate" to preferredDate.trim(),
                "message" to message.trim(),
                "status" to "PENDING",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun updateInquiryStatus(inquiryId: String, newStatus: String): Result<Unit> = runCatching {
        requireUserId()
        val path = "inquiries/$inquiryId"
        try {
            db.collection("inquiries").document(inquiryId).update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    // --- 5. DENUNCIAS / REPORTES DE ANUNCIOS ---

    suspend fun submitReport(
        propertyId: String,
        propertyTitle: String,
        reason: String,
        details: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val path = "reports"
        try {
            val docRef = db.collection(path).document()
            val payload = mapOf(
                "propertyId" to propertyId,
                "propertyTitle" to propertyTitle,
                "reporterId" to uid,
                "reason" to reason,
                "details" to details.trim(),
                "status" to "OPEN",
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    // --- 6. DATOS DEMOSTRATIVOS INICIALES PARA PARAGUAY (Claramente identificados) ---

    suspend fun seedParaguayCatalogIfEmpty(): Result<Int> = runCatching {
        val uid = requireUserId()
        val existing = db.collection("properties")
            .whereEqualTo("status", "active")
            .limit(1)
            .get()
            .await()
        if (!existing.isEmpty) return@runCatching 0

        val sampleProperties = getParaguayDemoProperties(uid)
        for (prop in sampleProperties) {
            createProperty(prop).getOrThrow()
        }
        sampleProperties.size
    }

    companion object {
        fun getParaguayDemoProperties(ownerUid: String): List<Property> = listOf(
            Property(
                id = "py_asuncion_villamorra_01",
                title = "Penthouse Botánico en Villa Morra [DEMO]",
                description = "Exclusivo penthouse de diseño contemporáneo sobre Av. Mariscal López en el corazón de Villa Morra, Asunción. Cuenta con amplia terraza ajardinada, quincho climatizado con parrilla de acero inoxidable, piscina propia con borde infinito y vista despejada a la ciudad. Seguridad 24 horas y generador para áreas comunes y privadas.",
                operationType = "SALE",
                propertyType = "APARTMENT",
                price = 2150000000.0,
                currency = "PYG",
                department = "Asunción",
                city = "Asunción",
                neighborhood = "Villa Morra",
                address = "Av. Mariscal López esq. Senador Long",
                latitude = -25.2914,
                longitude = -57.5808,
                areaTotalM2 = 285L,
                areaBuiltM2 = 240L,
                bedrooms = 3L,
                bathrooms = 4L,
                parkingSpaces = 3L,
                amenities = listOf("Piscina", "Quincho con parrilla", "Seguridad 24h", "Generador propio", "Gimnasio", "Domótica"),
                imageUrls = listOf("penthouse_villamorra", "terrace_asuncion"),
                featured = true,
                status = "active",
                agentId = ownerUid,
                agentName = "Arq. Valeria Ayala (Asesora Demo)",
                agentAgency = "Yvy Capital Asunción",
                agentWhatsapp = "+595981400100",
                verified = true
            ),
            Property(
                id = "py_cordillera_sanber_02",
                title = "Residencia del Lago en San Bernardino [DEMO]",
                description = "Residencia de fin de semana y vivienda permanente en condominio cerrado de San Bernardino con vista panorámica al Lago Ypacaraí. Arquitectura orgánica en piedra natural y madera de lapacho certificada, galería cubierta de doble altura, piscina sinfín y jardín parquizado con árboles nativos.",
                operationType = "SALE",
                propertyType = "HOUSE",
                price = 385000.0,
                currency = "USD",
                department = "Cordillera",
                city = "San Bernardino",
                neighborhood = "Sadi - Vista al Lago",
                address = "Av. Guillermo Naumann Km 44",
                latitude = -25.3087,
                longitude = -57.2964,
                areaTotalM2 = 850L,
                areaBuiltM2 = 410L,
                bedrooms = 4L,
                bathrooms = 5L,
                parkingSpaces = 4L,
                amenities = listOf("Vista al Lago Ypacaraí", "Piscina infinita", "Quincho con parrilla", "Barrio Cerrado", "Jardín tropical"),
                imageUrls = listOf("casa_san_bernardino", "lago_ypacarai"),
                featured = true,
                status = "active",
                agentId = ownerUid,
                agentName = "Lic. Rodrigo Caballero (Asesor Demo)",
                agentAgency = "Yvy Capital Cordillera",
                agentWhatsapp = "+595981400200",
                verified = true
            ),
            Property(
                id = "py_asuncion_laslomas_03",
                title = "Departamento Ejecutivo Eje Corporativo [DEMO]",
                description = "Departamento moderno a pasos del Paseo La Galería y Shopping del Sol en el barrio Las Lomas (Carmelitas). Ideal para ejecutivos o inversión de renta inmediata. Totalmente equipado con aires acondicionados inverter, balcón con parrilla y amenities tipo resort en azotea.",
                operationType = "RENT",
                propertyType = "APARTMENT",
                price = 7800000.0,
                currency = "PYG",
                department = "Asunción",
                city = "Asunción",
                neighborhood = "Las Lomas (Carmelitas)",
                address = "Av. Santa Teresa c/ Aviadores del Chaco",
                latitude = -25.2839,
                longitude = -57.5632,
                areaTotalM2 = 98L,
                areaBuiltM2 = 86L,
                bedrooms = 2L,
                bathrooms = 2L,
                parkingSpaces = 1L,
                amenities = listOf("Coworking", "Piscina en Rooftop", "Seguridad 24h", "Parrilla en balcón", "Pet Friendly"),
                imageUrls = listOf("depto_santa_teresa"),
                featured = true,
                status = "active",
                agentId = ownerUid,
                agentName = "Arq. Valeria Ayala (Asesora Demo)",
                agentAgency = "Yvy Capital Asunción",
                agentWhatsapp = "+595981400100",
                verified = true
            ),
            Property(
                id = "py_central_luque_04",
                title = "Casa Familiar en Barrio Cerrado Rakiura / Luque [DEMO]",
                description = "Amplia residencia familiar a 8 minutos del Aeropuerto Internacional Silvio Pettirossi y del Parque Ñu Guasú. Planta baja integrada con jardín interior, cocina amoblada con isla de cuarzo, área de servicio independiente y seguridad perimetral monitoreada.",
                operationType = "SALE",
                propertyType = "HOUSE",
                price = 1490000000.0,
                currency = "PYG",
                department = "Central",
                city = "Luque",
                neighborhood = "Loma Merlo",
                address = "Autopista Silvio Pettirossi c/ América",
                latitude = -25.2562,
                longitude = -57.5125,
                areaTotalM2 = 420L,
                areaBuiltM2 = 265L,
                bedrooms = 3L,
                bathrooms = 3L,
                parkingSpaces = 2L,
                amenities = listOf("Barrio Cerrado", "Cancha de Tenis", "Quincho con parrilla", "Seguridad 24h", "Parque infantil"),
                imageUrls = listOf("casa_luque"),
                featured = false,
                status = "active",
                agentId = ownerUid,
                agentName = "Ing. Marcos Villalba (Asesor Demo)",
                agentAgency = "Yvy Capital Gran Asunción",
                agentWhatsapp = "+595981400300",
                verified = true
            ),
            Property(
                id = "py_altoparana_cde_05",
                title = "Oficina Corporativa Paraná Country / CDE [DEMO]",
                description = "Piso corporativo luminoso en zona estratégica de Ciudad del Este, Alto Paraná. Planta libre modular con piso técnico elevado, fibra óptica redundante, sala de directorio vidriada y estacionamiento exclusivo para clientes.",
                operationType = "RENT",
                propertyType = "OFFICE",
                price = 1450.0,
                currency = "USD",
                department = "Alto Paraná",
                city = "Ciudad del Este",
                neighborhood = "Área 1 / Paraná Country",
                address = "Av. San Blas y Monseñor Rodríguez",
                latitude = -25.5097,
                longitude = -54.6111,
                areaTotalM2 = 160L,
                areaBuiltM2 = 160L,
                bedrooms = 0L,
                bathrooms = 2L,
                parkingSpaces = 3L,
                amenities = listOf("Generador 100%", "Control biométrico", "Sala de reuniones", "Seguridad 24h"),
                imageUrls = listOf("oficina_cde"),
                featured = false,
                status = "active",
                agentId = ownerUid,
                agentName = "Lic. Camila Franco (Asesora Demo)",
                agentAgency = "Yvy Capital Alto Paraná",
                agentWhatsapp = "+595983500100",
                verified = true
            ),
            Property(
                id = "py_itapua_encarnacion_06",
                title = "Terreno Frente a la Costanera de Encarnación [DEMO]",
                description = "Lote de inversión inmobiliaria con frente sobre la Costanera República del Paraguay en Encarnación, Itapúa. Coeficiente de edificabilidad ideal para desarrollo de edificio residencial o apart-hotel con vista directa al Río Paraná.",
                operationType = "SALE",
                propertyType = "LAND",
                price = 195000.0,
                currency = "USD",
                department = "Itapúa",
                city = "Encarnación",
                neighborhood = "Costanera San José",
                address = "Av. Costanera República del Paraguay",
                latitude = -27.3385,
                longitude = -55.8668,
                areaTotalM2 = 640L,
                areaBuiltM2 = 0L,
                bedrooms = 0L,
                bathrooms = 0L,
                parkingSpaces = 0L,
                amenities = listOf("Frente al Río Paraná", "Servicios subterráneos", "Apto Edificio en Altura", "Título listo a transferir"),
                imageUrls = listOf("terreno_encarnacion"),
                featured = false,
                status = "active",
                agentId = ownerUid,
                agentName = "Lic. Rodrigo Caballero (Asesor Demo)",
                agentAgency = "Yvy Capital Sur",
                agentWhatsapp = "+595985600200",
                verified = true
            )
        )
    }
}
