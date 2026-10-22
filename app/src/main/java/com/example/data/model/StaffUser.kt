package com.example.data.model

/**
 * Representa a un usuario autorizado de la plantilla de personal o administración de Yvy Capital.
 * Sus credenciales residen en Firebase Auth y su autorización en Firestore en /staff/{uid}.
 */
data class StaffUser(
    val uid: String,
    val name: String,
    val role: String, // "admin" o "staff"
    val email: String,
    val active: Boolean = true
)
