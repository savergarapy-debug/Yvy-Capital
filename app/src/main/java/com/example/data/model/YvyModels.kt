package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

data class AdvisorMembershipSpec(
    val code: String,
    val name: String,
    val monthlyPricePyg: Long,
    val priceFormatted: String,
    val maxActiveListings: Int,
    val advisorParticipationPercent: String,
    val canFeatureListings: Boolean,
    val supportsExternalAgentMode: Boolean,
    val hasDirectYvyCollaboration: Boolean,
    val canGenerateVideos: Boolean,
    val benefits: List<String>
)

object AdvisorMemberships {
    const val TIER_FREE = "FREE"
    const val TIER_INICIAL = "PLAN_INICIAL"
    const val TIER_INTERMEDIO = "PLAN_INTERMEDIO"
    const val TIER_SENIOR = "PLAN_SENIOR"

    val ALL_PLANS: List<AdvisorMembershipSpec> = listOf(
        AdvisorMembershipSpec(
            code = TIER_INICIAL,
            name = "Plan Inicial",
            monthlyPricePyg = 99_000L,
            priceFormatted = "G. 99.000 mensuales",
            maxActiveListings = 5,
            advisorParticipationPercent = "3,5 %",
            canFeatureListings = false,
            supportsExternalAgentMode = false,
            hasDirectYvyCollaboration = false,
            canGenerateVideos = false,
            benefits = listOf(
                "Seguimiento de visualizaciones de las propiedades publicadas.",
                "Máximo de 5 publicaciones activas.",
                "Participación del asesor del 3,5 % por venta concretada, conforme a las condiciones comerciales aplicables.",
                "Acceso autorizado a las herramientas y bases de datos de clientes y propiedades que correspondan a su rol."
            )
        ),
        AdvisorMembershipSpec(
            code = TIER_INTERMEDIO,
            name = "Plan Intermedio",
            monthlyPricePyg = 189_000L,
            priceFormatted = "G. 189.000 mensuales",
            maxActiveListings = 10,
            advisorParticipationPercent = "4 %",
            canFeatureListings = true,
            supportsExternalAgentMode = true,
            hasDirectYvyCollaboration = false,
            canGenerateVideos = false,
            benefits = listOf(
                "Seguimiento de visualizaciones.",
                "Máximo de 10 publicaciones activas.",
                "Participación del asesor del 4 % por venta concretada, conforme a las condiciones comerciales aplicables.",
                "Acceso autorizado a las herramientas y bases de datos correspondientes.",
                "Publicaciones destacadas.",
                "Modalidad de agente inmobiliario externo a Yvy Capital."
            )
        ),
        AdvisorMembershipSpec(
            code = TIER_SENIOR,
            name = "Plan Senior",
            monthlyPricePyg = 249_000L,
            priceFormatted = "G. 249.000 mensuales",
            maxActiveListings = 10,
            advisorParticipationPercent = "4,5 %",
            canFeatureListings = true,
            supportsExternalAgentMode = true,
            hasDirectYvyCollaboration = true,
            canGenerateVideos = true,
            benefits = listOf(
                "Seguimiento de visualizaciones.",
                "Máximo de 10 publicaciones activas.",
                "Participación del asesor del 4,5 % por venta concretada, conforme a las condiciones comerciales aplicables.",
                "Acceso autorizado a las herramientas y bases de datos correspondientes.",
                "Publicaciones destacadas.",
                "Modalidad de agente inmobiliario externo.",
                "Acceso a consultas directas con asesores de Yvy Capital para colaborar en el cierre de operaciones."
            )
        )
    )

    fun getSpec(planTier: String?): AdvisorMembershipSpec? {
        return ALL_PLANS.firstOrNull { it.code == planTier }
    }

    fun maxListingsForTier(planTier: String?): Int {
        return getSpec(planTier)?.maxActiveListings ?: 0
    }

    fun canFeatureForTier(planTier: String?): Boolean {
        return getSpec(planTier)?.canFeatureListings ?: false
    }

    fun isSeniorTier(planTier: String?): Boolean {
        return planTier == TIER_SENIOR
    }

    fun displayNameForTier(planTier: String?): String {
        return when (planTier) {
            TIER_INICIAL -> "Plan Inicial (G. 99.000/mes)"
            TIER_INTERMEDIO -> "Plan Intermedio (G. 189.000/mes)"
            TIER_SENIOR -> "Plan Senior (G. 249.000/mes)"
            else -> "Sin membresía de asesor activa (Cliente)"
        }
    }
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val preferredCurrency: String = "PYG", // "PYG" or "USD"
    val role: String = "CLIENT", // "CLIENT" or "AGENT"
    val agencyName: String = "",
    val planTier: String = "FREE", // "FREE", "PLAN_INICIAL", "PLAN_INTERMEDIO", "PLAN_SENIOR"
    val activeListingsCount: Long = 0L,
    val externalAgentMode: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Property(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val operationType: String = "SALE", // "SALE" or "RENT"
    val propertyType: String = "HOUSE", // "HOUSE", "APARTMENT", "LAND", "OFFICE", "COMMERCIAL", "ESTANCIA"
    val price: Double = 0.0,
    val currency: String = "PYG", // "PYG" or "USD"
    val department: String = "Asunción",
    val city: String = "Asunción",
    val neighborhood: String = "Villa Morra",
    val address: String = "",
    val latitude: Double = -25.2867,
    val longitude: Double = -57.6470,
    val areaTotalM2: Long = 0L,
    val areaBuiltM2: Long = 0L,
    val bedrooms: Long = 0L,
    val bathrooms: Long = 0L,
    val parkingSpaces: Long = 0L,
    val amenities: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val featured: Boolean = false,
    val status: String = "active", // "active", "reserved", "sold", "paused"
    val agentId: String = "",
    val agentName: String = "",
    val agentAgency: String = "",
    val agentWhatsapp: String = "",
    val verified: Boolean = true,
    val viewsCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PropertyPromoVideo(
    @DocumentId val id: String = "",
    val propertyId: String = "",
    val propertyTitle: String = "",
    val agentId: String = "",
    val planTierAtCreation: String = "PLAN_SENIOR",
    val generationMode: String = "AUTOMATED_SLIDESHOW", // "AI_INTERACTIONS_API" or "AUTOMATED_SLIDESHOW"
    val aspectRatio: String = "9:16", // "9:16" or "16:9"
    val selectedImages: List<String> = emptyList(),
    val includeBackgroundMusic: Boolean = true,
    val overlayHeadline: String = "",
    val priceLabel: String = "",
    val locationLabel: String = "",
    val highlightsLabel: String = "",
    val localVideoPath: String = "",
    val durationSeconds: Long = 6L,
    val status: String = "READY",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FavoriteItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val propertyId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val currency: String = "PYG",
    val operationType: String = "SALE",
    val propertyType: String = "HOUSE",
    val city: String = "",
    val neighborhood: String = "",
    val areaTotalM2: Long = 0L,
    val bedrooms: Long = 0L,
    val bathrooms: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PropertyInquiry(
    @DocumentId val id: String = "",
    val propertyId: String = "",
    val propertyTitle: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val agentId: String = "",
    val inquiryType: String = "INFO", // "INFO", "VISIT", "AVAILABILITY"
    val preferredDate: String = "",
    val message: String = "",
    val status: String = "PENDING", // "PENDING", "CONTACTED", "VISIT_SCHEDULED", "CLOSED"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PropertyReport(
    @DocumentId val id: String = "",
    val propertyId: String = "",
    val propertyTitle: String = "",
    val reporterId: String = "",
    val reason: String = "INACCURATE_PRICE", // "FRAUD", "INACCURATE_PRICE", "DUPLICATE", "SOLD_UNAVAILABLE", "INAPPROPRIATE"
    val details: String = "",
    val status: String = "OPEN", // "OPEN", "REVIEWED", "DISMISSED"
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class PropertyFilter(
    val searchQuery: String = "",
    val department: String = "Todos",
    val city: String = "Todas",
    val operationType: String = "ALL", // "ALL", "SALE", "RENT"
    val propertyType: String = "ALL", // "ALL", "HOUSE", "APARTMENT", "LAND", "OFFICE", "COMMERCIAL", "ESTANCIA"
    val minBedrooms: Int = 0,
    val minBathrooms: Int = 0,
    val minParking: Int = 0,
    val minAreaM2: Int = 0,
    val onlyFeatured: Boolean = false,
    val sortOrder: String = "NONE" // "NONE", "PRICE_ASC", "PRICE_DESC"
)
