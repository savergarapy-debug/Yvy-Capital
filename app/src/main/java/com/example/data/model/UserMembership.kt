package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Modelo de membresía asignada almacenada en /memberships/{uid}.
 * Gestionada y escrita exclusivamente por administradores de Yvy Capital.
 */
@IgnoreExtraProperties
data class UserMembership(
    @DocumentId val id: String = "",
    val planTier: String = AdvisorMemberships.TIER_FREE,
    val status: String = "active", // "active", "expired", "cancelled"
    val expiresAt: Timestamp? = null,
    val assignedBy: String = "",
    val note: String? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val isActive: Boolean
        get() {
            if (status != "active") return false
            val expiry = expiresAt?.toDate()?.time ?: return true
            return expiry > System.currentTimeMillis()
        }
}

/**
 * Contador de publicaciones activas almacenado en /listingCounters/{uid}.
 * Mantenido transaccionalmente por la aplicación y validado en Firestore rules.
 */
@IgnoreExtraProperties
data class ListingCounter(
    @DocumentId val id: String = "",
    val count: Long = 0L,
    val lastPropertyId: String = ""
)

/**
 * Solicitud formal de contratación de membresía en /membershipRequests/{requestId}.
 * Creada por el usuario con comprobante de pago y aprobada/rechazada por un admin.
 */
@IgnoreExtraProperties
data class MembershipRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val planTier: String = "",
    val paymentMethod: String = "",
    val paymentReference: String = "",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val reviewedBy: String? = null,
    @ServerTimestamp val reviewedAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)
