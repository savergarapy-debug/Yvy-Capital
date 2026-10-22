package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ListingCounter
import com.example.data.model.MembershipRequest
import com.example.data.model.Property
import com.example.data.model.UserMembership
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MembershipsRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("El usuario debe iniciar sesión antes de realizar esta operación.")
    }

    // Caché en memoria de membresías activas para visualización de insignias de asesores
    private val _membershipsCache = MutableStateFlow<Map<String, UserMembership>>(emptyMap())
    val membershipsCache: StateFlow<Map<String, UserMembership>> = _membershipsCache.asStateFlow()
    private val memoryMap = ConcurrentHashMap<String, UserMembership>()

    /**
     * Escucha en tiempo real la membresía del usuario actual (/memberships/{userId}).
     */
    fun observeUserMembership(userId: String): Flow<UserMembership?> {
        val path = "memberships/$userId"
        return db.collection("memberships").document(userId)
            .snapshots()
            .map { snap ->
                if (snap.exists()) {
                    val m = snap.toObject(UserMembership::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    if (m != null) {
                        memoryMap[userId] = m
                        _membershipsCache.value = memoryMap.toMap()
                    }
                    m
                } else {
                    memoryMap.remove(userId)
                    _membershipsCache.value = memoryMap.toMap()
                    null
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                throw error
            }
    }

    /**
     * Obtiene y almacena en caché la membresía de un asesor para resolver su insignia sin sobrecostos.
     */
    suspend fun getOrFetchMembership(agentId: String): UserMembership? {
        if (agentId.isBlank()) return null
        memoryMap[agentId]?.let { return it }

        return try {
            val snap = db.collection("memberships").document(agentId).get().await()
            if (snap.exists()) {
                val m = snap.toObject(UserMembership::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (m != null) {
                    memoryMap[agentId] = m
                    _membershipsCache.value = memoryMap.toMap()
                }
                m
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Escucha las solicitudes de membresía del usuario en tiempo real (/membershipRequests).
     */
    fun observeUserMembershipRequests(userId: String): Flow<List<MembershipRequest>> {
        val path = "membershipRequests"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(MembershipRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    /**
     * Envía una solicitud formal de contratación de membresía en /membershipRequests/{requestId}.
     */
    suspend fun createMembershipRequest(
        planTier: String,
        paymentMethod: String,
        paymentReference: String,
        userName: String,
        userEmail: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val path = "membershipRequests"
        val requestId = "req_" + UUID.randomUUID().toString().replace("-", "").take(20)

        val payload = mapOf(
            "userId" to uid,
            "userEmail" to userEmail.ifBlank { auth.currentUser?.email ?: "" },
            "userName" to userName.ifBlank { auth.currentUser?.displayName ?: "Usuario Yvy Capital" },
            "planTier" to planTier,
            "paymentMethod" to paymentMethod.trim().take(50),
            "paymentReference" to paymentReference.trim().take(200),
            "status" to "PENDING",
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            db.collection(path).document(requestId).set(payload).await()
            requestId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    // --- FUNCIONES EXCLUSIVAS PARA ADMINISTRADORES ---

    /**
     * Escucha solicitudes de membresía pendientes para el panel de administración.
     */
    fun observePendingMembershipRequests(): Flow<List<MembershipRequest>> {
        val path = "membershipRequests"
        return db.collection(path)
            .whereEqualTo("status", "PENDING")
            .snapshots()
            .map { snap ->
                snap.toObjects(MembershipRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    /**
     * Aprueba una solicitud de membresía en un solo batch atómico.
     */
    suspend fun approveMembershipRequest(
        requestId: String,
        userId: String,
        planTier: String,
        durationMonths: Int,
        adminUid: String
    ): Result<Unit> = runCatching {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, durationMonths)
        val expiresTimestamp = Timestamp(cal.time)

        val membershipRef = db.collection("memberships").document(userId)
        val requestRef = db.collection("membershipRequests").document(requestId)

        val batch = db.batch()
        batch.set(
            membershipRef,
            mapOf(
                "planTier" to planTier,
                "status" to "active",
                "expiresAt" to expiresTimestamp,
                "assignedBy" to adminUid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.update(
            requestRef,
            mapOf(
                "status" to "APPROVED",
                "reviewedBy" to adminUid,
                "reviewedAt" to FieldValue.serverTimestamp()
            )
        )

        try {
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "memberships/$userId")
            throw e
        }
    }

    /**
     * Rechaza una solicitud de membresía.
     */
    suspend fun rejectMembershipRequest(
        requestId: String,
        adminUid: String
    ): Result<Unit> = runCatching {
        val requestRef = db.collection("membershipRequests").document(requestId)
        try {
            requestRef.update(
                mapOf(
                    "status" to "REJECTED",
                    "reviewedBy" to adminUid,
                    "reviewedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "membershipRequests/$requestId")
            throw e
        }
    }

    /**
     * Asigna, extiende o cancela una membresía manualmente.
     */
    suspend fun assignOrUpdateMembership(
        userId: String,
        planTier: String,
        status: String,
        expiresAt: Timestamp?,
        note: String?,
        adminUid: String
    ): Result<Unit> = runCatching {
        val docRef = db.collection("memberships").document(userId)
        val payload = mutableMapOf<String, Any?>(
            "planTier" to planTier,
            "status" to status,
            "expiresAt" to expiresAt,
            "assignedBy" to adminUid,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!note.isNullOrBlank()) {
            payload["note"] = note.trim()
        }

        try {
            docRef.set(payload.filterValues { it != null }).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "memberships/$userId")
            throw e
        }
    }

    /**
     * Busca un usuario por email en la colección /users.
     */
    suspend fun searchUserByEmail(email: String): Result<UserProfile?> = runCatching {
        val snap = db.collection("users")
            .whereEqualTo("email", email.trim().lowercase())
            .limit(1)
            .get()
            .await()

        if (snap.isEmpty) {
            // Intentar búsqueda exacta original si difiere
            val snapRaw = db.collection("users")
                .whereEqualTo("email", email.trim())
                .limit(1)
                .get()
                .await()
            if (snapRaw.isEmpty) null else snapRaw.documents[0].toObject(UserProfile::class.java)
        } else {
            snap.documents[0].toObject(UserProfile::class.java)
        }
    }

    /**
     * Recalcula los contadores de publicaciones activas (/listingCounters/{agentId})
     * contando las propiedades con status == 'active'.
     */
    suspend fun recalculateCounters(targetUserId: String? = null): Result<Int> = runCatching {
        val query = if (targetUserId.isNullOrBlank()) {
            db.collection("properties").whereEqualTo("status", "active")
        } else {
            db.collection("properties").whereEqualTo("agentId", targetUserId).whereEqualTo("status", "active")
        }

        val snap = query.get().await()
        val activeProps = snap.toObjects(Property::class.java)

        val countsByAgent = mutableMapOf<String, Long>()
        for (prop in activeProps) {
            if (prop.agentId.isNotBlank()) {
                countsByAgent[prop.agentId] = (countsByAgent[prop.agentId] ?: 0L) + 1L
            }
        }

        // Si se especificó un usuario específico que tiene 0 propiedades activas
        if (!targetUserId.isNullOrBlank() && !countsByAgent.containsKey(targetUserId)) {
            countsByAgent[targetUserId] = 0L
        }

        var updatedUsers = 0
        for ((agentId, count) in countsByAgent) {
            db.collection("listingCounters").document(agentId).set(
                mapOf(
                    "count" to count,
                    "lastPropertyId" to ""
                )
            ).await()
            updatedUsers++
        }
        updatedUsers
    }

    // Helper methods matching ViewModel & Dashboard signatures

    fun observeMembership(userId: String): Flow<UserMembership?> = observeUserMembership(userId)

    fun observeMyMembershipRequests(userId: String): Flow<List<MembershipRequest>> = observeUserMembershipRequests(userId)

    fun observePendingRequests(): Flow<List<MembershipRequest>> = observePendingMembershipRequests()

    fun getAgentMembershipTier(agentId: String): String? {
        val m = memoryMap[agentId]
        return if (m != null && m.isActive) m.planTier else null
    }

    suspend fun getMembership(userId: String): Result<UserMembership?> = runCatching {
        val snap = db.collection("memberships").document(userId).get().await()
        if (snap.exists()) {
            val mem = snap.toObject(UserMembership::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            if (mem != null) {
                memoryMap[userId] = mem
                _membershipsCache.value = memoryMap.toMap()
            }
            mem
        } else {
            null
        }
    }

    suspend fun approveRequest(request: MembershipRequest, durationMonths: Int, adminUid: String): Result<Unit> {
        return approveMembershipRequest(
            requestId = request.id,
            userId = request.userId,
            planTier = request.planTier,
            durationMonths = durationMonths,
            adminUid = adminUid
        )
    }

    suspend fun rejectRequest(requestId: String, adminUid: String): Result<Unit> {
        return rejectMembershipRequest(requestId, adminUid)
    }

    suspend fun setManualMembership(
        userId: String,
        planTier: String,
        status: String,
        durationMonths: Int?,
        note: String?,
        adminUid: String
    ): Result<Unit> = runCatching {
        val expiresAt = if (durationMonths != null && durationMonths > 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, durationMonths)
            Timestamp(cal.time)
        } else null

        assignOrUpdateMembership(
            userId = userId,
            planTier = planTier,
            status = status,
            expiresAt = expiresAt,
            note = note,
            adminUid = adminUid
        ).getOrThrow()
    }

    suspend fun searchUsersByEmail(email: String): Result<List<UserSearchResult>> = runCatching {
        val cleanEmail = email.trim().lowercase()
        val snap = db.collection("users")
            .whereGreaterThanOrEqualTo("email", cleanEmail)
            .whereLessThanOrEqualTo("email", cleanEmail + "\uf8ff")
            .limit(10)
            .get()
            .await()

        val list = mutableListOf<UserSearchResult>()
        for (doc in snap.documents) {
            val em = doc.getString("email") ?: ""
            val name = doc.getString("displayName") ?: ""
            list.add(UserSearchResult(id = doc.id, email = em, displayName = name))
        }

        if (list.isEmpty()) {
            val rawSnap = db.collection("users").whereEqualTo("email", email.trim()).get().await()
            for (doc in rawSnap.documents) {
                val em = doc.getString("email") ?: ""
                val name = doc.getString("displayName") ?: ""
                list.add(UserSearchResult(id = doc.id, email = em, displayName = name))
            }
        }
        list
    }

    suspend fun recalculateListingCounters(userId: String): Result<Int> {
        return recalculateCounters(userId)
    }

    suspend fun recalculateAllListingCounters(): Result<Int> {
        return recalculateCounters(null)
    }

    suspend fun createMembershipRequest(
        userId: String? = null,
        userEmail: String,
        userName: String,
        planTier: String,
        paymentMethod: String,
        paymentReference: String
    ): Result<String> {
        val targetUid = userId?.ifBlank { null } ?: requireUserId()
        val path = "membershipRequests"
        val requestId = "req_" + UUID.randomUUID().toString().replace("-", "").take(20)

        val payload = mapOf(
            "userId" to targetUid,
            "userEmail" to userEmail.ifBlank { auth.currentUser?.email ?: "" },
            "userName" to userName.ifBlank { auth.currentUser?.displayName ?: "Usuario Yvy Capital" },
            "planTier" to planTier,
            "paymentMethod" to paymentMethod.trim().take(50),
            "paymentReference" to paymentReference.trim().take(200),
            "status" to "PENDING",
            "createdAt" to FieldValue.serverTimestamp()
        )

        return runCatching {
            db.collection(path).document(requestId).set(payload).await()
            requestId
        }
    }
}

data class UserSearchResult(
    val id: String,
    val email: String,
    val displayName: String
)
