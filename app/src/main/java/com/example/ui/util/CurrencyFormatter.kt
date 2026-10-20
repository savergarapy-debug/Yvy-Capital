package com.example.ui.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

object CurrencyFormatter {
    // Tipo de cambio referencial PYG / USD en Paraguay (~7.800 Gs por 1 USD)
    const val EXCHANGE_RATE_PYG_PER_USD = 7800.0

    fun formatPrice(
        amount: Double,
        sourceCurrency: String,
        displayCurrency: String = sourceCurrency
    ): String {
        val convertedAmount = convert(amount, sourceCurrency, displayCurrency)
        return if (displayCurrency == "USD") {
            val nf = NumberFormat.getNumberInstance(Locale("es", "PY")).apply {
                maximumFractionDigits = 0
            }
            "USD ${nf.format(convertedAmount.roundToLong())}"
        } else {
            val nf = NumberFormat.getNumberInstance(Locale("es", "PY")).apply {
                maximumFractionDigits = 0
            }
            "Gs. ${nf.format(convertedAmount.roundToLong())}"
        }
    }

    fun formatSecondaryPrice(amount: Double, sourceCurrency: String): String {
        val targetCurrency = if (sourceCurrency == "PYG") "USD" else "PYG"
        return "≈ ${formatPrice(amount, sourceCurrency, targetCurrency)}"
    }

    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount
        return if (fromCurrency == "PYG" && toCurrency == "USD") {
            amount / EXCHANGE_RATE_PYG_PER_USD
        } else if (fromCurrency == "USD" && toCurrency == "PYG") {
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
            "LAND" -> "Terreno"
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
