package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.StaffUser
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de gestión de personal interno y seguridad (/staff/{uid}).
 * Escucha en tiempo real el documento del usuario logueado en /staff/{uid}.
 * Garantiza que la insignia pública /publicStaff/{uid} esté sincronizada
 * y revoca el acceso si el usuario es desactivado o removido.
 */
class StaffRepository(
    private val db: FirebaseFirestore,
    val userId: String,
    private val onAccessRevoked: (String) -> Unit = {}
) {

    constructor(context: Context, userId: String, onAccessRevoked: (String) -> Unit = {}) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        userId,
        onAccessRevoked
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var staffListener: ListenerRegistration? = null

    private val _isActiveStaff = MutableStateFlow(false)
    val isActiveStaff: StateFlow<Boolean> = _isActiveStaff.asStateFlow()

    private val _staffRole = MutableStateFlow<String?>(null)
    val staffRole: StateFlow<String?> = _staffRole.asStateFlow()

    private val _staffUser = MutableStateFlow<StaffUser?>(null)
    val staffUser: StateFlow<StaffUser?> = _staffUser.asStateFlow()

    private var wasStaffActiveInSession = false

    init {
        startListening()
    }

    fun startListening() {
        if (userId.isBlank()) return
        staffListener?.remove()

        val docRef = db.collection("staff").document(userId)
        staffListener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error escuchando staff/$userId", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val active = snapshot.getBoolean("active") == true
                val role = snapshot.getString("role") ?: "staff"
                val name = snapshot.getString("name")
                    ?: snapshot.getString("displayName")
                    ?: "Personal Yvy"
                val email = snapshot.getString("email")
                    ?: Firebase.auth.currentUser?.email
                    ?: ""

                if (active) {
                    val staff = StaffUser(
                        uid = userId,
                        name = name,
                        role = role,
                        email = email,
                        active = true
                    )
                    _staffUser.value = staff
                    _staffRole.value = role
                    _isActiveStaff.value = true
                    wasStaffActiveInSession = true

                    // Sincronizar insignia pública /publicStaff/{uid}
                    scope.launch {
                        ensurePublicBadgePublished(userId, name)
                    }
                } else {
                    handleStaffRevocation("Esta cuenta no tiene acceso de personal")
                }
            } else {
                if (wasStaffActiveInSession) {
                    handleStaffRevocation("Esta cuenta no tiene acceso de personal")
                } else {
                    _isActiveStaff.value = false
                    _staffRole.value = null
                    _staffUser.value = null
                }
            }
        }
    }

    private fun handleStaffRevocation(reason: String) {
        _isActiveStaff.value = false
        _staffRole.value = null
        _staffUser.value = null
        wasStaffActiveInSession = false

        scope.launch {
            try {
                db.collection("publicStaff").document(userId).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Error eliminando publicStaff/$userId tras revocación", e)
            } finally {
                Firebase.auth.signOut()
                onAccessRevoked(reason)
            }
        }
    }

    private suspend fun ensurePublicBadgePublished(uid: String, staffName: String) {
        try {
            val publicDoc = db.collection("publicStaff").document(uid)
            val snap = publicDoc.get().await()
            val existingName = snap.getString("name")
            if (!snap.exists() || existingName != staffName) {
                val payload = mapOf(
                    "uid" to uid,
                    "name" to staffName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                publicDoc.set(payload).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error publicando insignia en publicStaff/$uid", e)
        }
    }

    // --- MÉTODOS DE ADMINISTRADOR (Gestión de Equipo) ---

    fun observeAllStaff(): Flow<List<StaffUser>> {
        return db.collection("staff")
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { doc ->
                    val uid = doc.id
                    val name = doc.getString("name") ?: doc.getString("displayName") ?: "Personal Yvy"
                    val role = doc.getString("role") ?: "staff"
                    val email = doc.getString("email") ?: ""
                    val active = doc.getBoolean("active") == true
                    StaffUser(
                        uid = uid,
                        name = name,
                        role = role,
                        email = email,
                        active = active
                    )
                }
            }
            .catch { e ->
                Log.w(TAG, "Error listando personal para admin", e)
                emit(emptyList())
            }
    }

    suspend fun syncAllStaffBadges(): Result<Int> = runCatching {
        var syncedCount = 0
        try {
            val staffDocs = db.collection("staff").get().await()
            val activeStaffMap = mutableMapOf<String, String>() // uid -> name

            for (doc in staffDocs.documents) {
                val uid = doc.id
                val active = doc.getBoolean("active") == true
                val name = doc.getString("name") ?: doc.getString("displayName") ?: "Personal Yvy"
                if (active) {
                    activeStaffMap[uid] = name
                    // Asegurar que exista en publicStaff
                    val payload = mapOf(
                        "uid" to uid,
                        "name" to name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    db.collection("publicStaff").document(uid).set(payload).await()
                    syncedCount++
                } else {
                    // Si está inactivo, eliminar de publicStaff
                    db.collection("publicStaff").document(uid).delete().await()
                }
            }

            // Limpiar cualquier documento en publicStaff que ya no esté en staff o no sea activo
            val publicDocs = db.collection("publicStaff").get().await()
            for (pDoc in publicDocs.documents) {
                if (!activeStaffMap.containsKey(pDoc.id)) {
                    db.collection("publicStaff").document(pDoc.id).delete().await()
                }
            }

            syncedCount
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando insignias", e)
            throw e
        }
    }

    fun cleanup() {
        staffListener?.remove()
        staffListener = null
    }

    companion object {
        private const val TAG = "StaffRepository"
    }
}
