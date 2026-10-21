package com.example.ui.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

object CurrencyFormatter {
    // Tipo de cambio referencial conservado únicamente por compatibilidad con utilidades existentes
    const val EXCHANGE_RATE_PYG_PER_USD = 7800.0

    /**
     * Formatea el precio exclusivamente en la moneda original elegida por el autor de la publicación:
     * - PYG -> Guaraní paraguayo (₲)
     * - USD -> Dólar estadounidense ($)
     * No realiza conversiones automáticas entre monedas ni muestra dos monedas simultáneamente.
     */
    fun formatPrice(
        amount: Double,
        sourceCurrency: String,
        @Suppress("UNUSED_PARAMETER") displayCurrency: String = sourceCurrency
    ): String {
        if (amount.isNaN() || amount.isInfinite() || amount <= 0.0) {
            return "Consultar precio"
        }
        val authorCurrency = sourceCurrency.uppercase().trim().ifBlank { "PYG" }

        val nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-PY")).apply {
            maximumFractionDigits = 0
        }
        return if (authorCurrency == "USD") {
            "$ ${nf.format(amount.roundToLong())}"
        } else {
            "₲ ${nf.format(amount.roundToLong())}"
        }
    }

    fun formatSecondaryPrice(amount: Double, sourceCurrency: String): String {
        return formatPrice(amount, sourceCurrency, sourceCurrency)
    }

    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val from = fromCurrency.uppercase()
        val to = toCurrency.uppercase()
        if (from == to) return amount
        return if (from == "PYG" && to == "USD") {
            amount / EXCHANGE_RATE_PYG_PER_USD
        } else if (from == "USD" && to == "PYG") {
            amount * EXCHANGE_RATE_PYG_PER_USD
        } else {
            amount
        }
    }

    fun operationLabel(operationType: String): String {
        return when (operationType.uppercase()) {
            "SALE" -> "En Venta"
            "RENT" -> "En Alquiler"
            else -> operationType
        }
    }

    fun propertyTypeLabel(propertyType: String): String {
        return when (propertyType.uppercase()) {
            "HOUSE" -> "Casa"
            "APARTMENT" -> "Departamento"
            "LAND" -> "Terreno / Lote"
            "OFFICE" -> "Oficina"
            "COMMERCIAL" -> "Local Comercial"
            "ESTANCIA" -> "Estancia / Quinta"
            else -> propertyType
        }
    }

    fun inquiryStatusLabel(status: String): String {
        return when (status.uppercase()) {
            "PENDING" -> "Pendiente de respuesta"
            "CONTACTED" -> "Asesor en contacto"
            "VISIT_SCHEDULED" -> "Visita agendada"
            "CLOSED" -> "Cerrada"
            else -> status
        }
    }

    fun inquiryTypeLabel(type: String): String {
        return when (type.uppercase()) {
            "VISIT" -> "Solicitud de Visita"
            "AVAILABILITY" -> "Consulta de Disponibilidad"
            else -> "Solicitud de Información"
        }
    }
}

/**
 * Catálogo geográfico estructurado de Paraguay:
 * Incluye Asunción (Capital independiente) y los 17 departamentos oficiales con sus principales ciudades/distritos.
 */
object ParaguayLocations {

    val departmentsWithAll: List<String> = listOf(
        "Todos",
        "Asunción",
        "Central",
        "Cordillera",
        "Alto Paraná",
        "Itapúa",
        "Amambay",
        "Boquerón",
        "Caaguazú",
        "Caazapá",
        "Canindeyú",
        "Concepción",
        "Guairá",
        "Misiones",
        "Ñeembucú",
        "Paraguarí",
        "Presidente Hayes",
        "San Pedro",
        "Alto Paraguay"
    )

    val officialDepartments: List<String> = departmentsWithAll.drop(1)

    private val citiesMap: Map<String, List<String>> = mapOf(
        "Asunción" to listOf(
            "Asunción"
        ),
        "Central" to listOf(
            "Luque",
            "San Lorenzo",
            "Fernando de la Mora",
            "Lambaré",
            "Mariano Roque Alonso",
            "Limpio",
            "Capiatá",
            "Ñemby",
            "Villa Elisa",
            "Areguá",
            "Ypacaraí",
            "Itauguá",
            "San Antonio",
            "Villeta",
            "Guarambaré",
            "Itá",
            "J. Augusto Saldívar",
            "Nueva Italia",
            "Ypané"
        ),
        "Cordillera" to listOf(
            "San Bernardino",
            "Caacupé",
            "Altos",
            "Atyrá",
            "Eusebio Ayala",
            "Piribebuy",
            "Emboscada",
            "Tobatí",
            "Arroyos y Esteros",
            "Itacurubí de la Cordillera"
        ),
        "Alto Paraná" to listOf(
            "Ciudad del Este",
            "Hernandarias",
            "Presidente Franco",
            "Minga Guazú",
            "Santa Rita",
            "San Alberto",
            "Juan León Mallorquín",
            "Naranjal"
        ),
        "Itapúa" to listOf(
            "Encarnación",
            "Cambyretá",
            "Carmen del Paraná",
            "Hohenau",
            "Obligado",
            "Bella Vista",
            "Colonias Unidas",
            "Fram",
            "San Juan del Paraná",
            "Coronel Bogado"
        ),
        "Amambay" to listOf(
            "Pedro Juan Caballero",
            "Bella Vista Norte",
            "Capitán Bado",
            "Zanja Pytã"
        ),
        "Boquerón" to listOf(
            "Filadelfia",
            "Loma Plata",
            "Mariscal Estigarribia",
            "Boquerón"
        ),
        "Caaguazú" to listOf(
            "Coronel Oviedo",
            "Caaguazú",
            "Doctor Juan Eulogio Estigarribia (Campo 9)",
            "Repatriación",
            "San José de los Arroyos"
        ),
        "Caazapá" to listOf(
            "Caazapá",
            "San Juan Nepomuceno",
            "Yuty",
            "Abaí"
        ),
        "Canindeyú" to listOf(
            "Salto del Guairá",
            "Curuguaty",
            "Katueté",
            "Nueva Esperanza"
        ),
        "Concepción" to listOf(
            "Concepción",
            "Horqueta",
            "Yby Yaú",
            "Vallemí",
            "Loreto"
        ),
        "Guairá" to listOf(
            "Villarrica",
            "Independencia (Colonia Independencia)",
            "Mbocayaty",
            "Iturbe"
        ),
        "Misiones" to listOf(
            "San Juan Bautista",
            "San Ignacio Guazú",
            "Ayolas",
            "Villa Florida",
            "Santa Rosa"
        ),
        "Ñeembucú" to listOf(
            "Pilar",
            "Alberdi",
            "San Ignacio",
            "Humaitá",
            "Paso de Patria"
        ),
        "Paraguarí" to listOf(
            "Paraguarí",
            "Yaguarón",
            "Pirayú",
            "Carapeguá",
            "La Colmena",
            "Ybycuí",
            "Sapucai"
        ),
        "Presidente Hayes" to listOf(
            "Villa Hayes",
            "Nueva Asunción (Chaco'i)",
            "Benjamín Aceval",
            "Nanawa",
            "Pozo Colorado"
        ),
        "San Pedro" to listOf(
            "San Pedro de Ycuamandyyú",
            "Santaní (San Estanislao)",
            "Santa Rosa del Aguaray",
            "Choré"
        ),
        "Alto Paraguay" to listOf(
            "Fuerte Olimpo",
            "Puerto Casado",
            "Bahía Negra",
            "Carmelo Peralta"
        )
    )

    fun citiesForDepartment(
        department: String,
        dynamicCities: List<String> = emptyList()
    ): List<String> {
        val baseList = if (department == "Todos" || department.isBlank()) {
            listOf(
                "Asunción",
                "Luque",
                "San Bernardino",
                "Ciudad del Este",
                "Encarnación",
                "San Lorenzo",
                "Fernando de la Mora",
                "Lambaré",
                "Mariano Roque Alonso",
                "Nueva Asunción (Chaco'i)",
                "Areguá",
                "Altos",
                "Hernandarias",
                "Pedro Juan Caballero",
                "Villarrica",
                "Coronel Oviedo",
                "Cambyretá"
            )
        } else {
            citiesMap[department].orEmpty()
        }

        val combined = (baseList + dynamicCities.filter { it.isNotBlank() })
            .distinctBy { it.lowercase().trim() }
        return listOf("Todas") + combined
    }

    fun citiesForPublishDepartment(department: String): List<String> {
        return citiesMap[department]?.ifEmpty { listOf(department) } ?: listOf("Asunción")
    }
}
