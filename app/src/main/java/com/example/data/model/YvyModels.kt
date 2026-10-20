package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

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
    val planTier: String = "FREE", // "FREE", "PRO_MONTHLY", "PRO_ANNUAL"
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
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
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
    val onlyFeatured: Boolean = false
)
