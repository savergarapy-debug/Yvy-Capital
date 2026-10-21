package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Clase de datos que representa un listado de bienes raíces de Yvy Capital en Firestore.
 *
 * Cumple con los requisitos de serialización de Firebase Firestore:
 * - Anotación [@IgnoreExtraProperties] para tolerar cambios de esquema futuros.
 * - Identificador de documento mapeado automáticamente con [@DocumentId].
 * - Valores por defecto en todas las propiedades para generar el constructor vacío requerido por `.toObject(Propiedad::class.java)`.
 * - Sellos de tiempo gestionados por el servidor con [@ServerTimestamp].
 */
@IgnoreExtraProperties
data class Propiedad(
    @DocumentId val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val moneda: String = "PYG", // "PYG" (Guaraníes) o "USD" (Dólares)
    val ubicacion: String = "", // Ej: "Villa Morra, Asunción"
    val departamento: String = "Asunción",
    val ciudad: String = "Asunción",
    val barrio: String = "",
    val direccion: String = "",
    val latitud: Double = -25.2867,
    val longitud: Double = -57.6470,
    val tipoOperacion: String = "SALE", // "SALE" (Venta) o "RENT" (Alquiler)
    val tipoInmueble: String = "HOUSE", // "HOUSE", "APARTMENT", "LAND", "OFFICE", "COMMERCIAL", "ESTANCIA"
    val superficieTotalM2: Long = 0L,
    val superficieConstruidaM2: Long = 0L,
    val habitaciones: Long = 0L,
    val banos: Long = 0L,
    val estacionamientos: Long = 0L,
    val comodidades: List<String> = emptyList(),
    val imagenesUrl: List<String> = emptyList(),
    val destacada: Boolean = false,
    val estado: String = "active", // "active", "reserved", "sold", "paused"
    val asesorId: String = "",
    val asesorNombre: String = "",
    val asesorInmobiliaria: String = "",
    val asesorWhatsapp: String = "",
    val verificada: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    /**
     * Convierte la instancia de [Propiedad] al modelo del catálogo [Property]
     * utilizado en las colecciones de Firestore.
     */
    fun toProperty(): Property = Property(
        id = id,
        title = titulo,
        description = descripcion,
        operationType = tipoOperacion,
        propertyType = tipoInmueble,
        price = precio,
        currency = moneda,
        department = departamento,
        city = ciudad,
        neighborhood = barrio.ifBlank { ubicacion },
        address = direccion.ifBlank { ubicacion },
        latitude = latitud,
        longitude = longitud,
        areaTotalM2 = superficieTotalM2,
        areaBuiltM2 = superficieConstruidaM2,
        bedrooms = habitaciones,
        bathrooms = banos,
        parkingSpaces = estacionamientos,
        amenities = comodidades,
        imageUrls = imagenesUrl,
        featured = destacada,
        status = estado,
        agentId = asesorId,
        agentName = asesorNombre,
        agentAgency = asesorInmobiliaria,
        agentWhatsapp = asesorWhatsapp,
        verified = verificada,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        /**
         * Crea una instancia de [Propiedad] a partir de un documento [Property].
         */
        fun fromProperty(property: Property): Propiedad = Propiedad(
            id = property.id,
            titulo = property.title,
            descripcion = property.description,
            precio = property.price,
            moneda = property.currency,
            ubicacion = "${property.neighborhood}, ${property.city} (${property.department})",
            departamento = property.department,
            ciudad = property.city,
            barrio = property.neighborhood,
            direccion = property.address,
            latitud = property.latitude,
            longitud = property.longitude,
            tipoOperacion = property.operationType,
            tipoInmueble = property.propertyType,
            superficieTotalM2 = property.areaTotalM2,
            superficieConstruidaM2 = property.areaBuiltM2,
            habitaciones = property.bedrooms,
            banos = property.bathrooms,
            estacionamientos = property.parkingSpaces,
            comodidades = property.amenities,
            imagenesUrl = property.imageUrls,
            destacada = property.featured,
            estado = property.status,
            asesorId = property.agentId,
            asesorNombre = property.agentName,
            asesorInmobiliaria = property.agentAgency,
            asesorWhatsapp = property.agentWhatsapp,
            verificada = property.verified,
            createdAt = property.createdAt,
            updatedAt = property.updatedAt
        )
    }
}
