package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Repositorio para la colección pública /publicStaff/{uid}.
 * Mantiene un caché en memoria de todos los UIDs del personal con insignia oficial de Yvy Capital,
 * evitando lecturas repetidas a Firestore al renderizar listas o tarjetas.
 */
class PublicStaffRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _publicStaffMap = MutableStateFlow<Map<String, String>>(emptyMap()) // uid -> name
    val publicStaffMap: StateFlow<Map<String, String>> = _publicStaffMap.asStateFlow()

    init {
        scope.launch {
            try {
                db.collection("publicStaff").snapshots().collect { snapshot ->
                    val map = mutableMapOf<String, String>()
                    for (doc in snapshot.documents) {
                        val uid = doc.getString("uid") ?: doc.id
                        val name = doc.getString("name") ?: "Equipo Yvy Capital"
                        map[uid] = name
                    }
                    _publicStaffMap.value = map
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error escuchando publicStaff", e)
            }
        }
    }

    fun isPublicStaff(uid: String): Boolean {
        if (uid.isBlank()) return false
        return _publicStaffMap.value.containsKey(uid)
    }

    fun getStaffName(uid: String): String? {
        return _publicStaffMap.value[uid]
    }

    fun observePublicStaffUids(): Flow<Set<String>> {
        return _publicStaffMap.map { it.keys }
    }

    companion object {
        private const val TAG = "PublicStaffRepo"
    }
}
