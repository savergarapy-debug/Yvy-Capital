package com.example.ui.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.Property
import com.example.data.model.PropertyFilter

sealed interface DeepLinkTarget {
    val rawUrl: String

    data class PropertyDetail(
        val propertyId: String,
        override val rawUrl: String
    ) : DeepLinkTarget

    data class AdvisorCatalog(
        val agentId: String,
        val agentName: String? = null,
        override val rawUrl: String
    ) : DeepLinkTarget

    data class ExploreFiltered(
        val department: String = "Todos",
        val operationType: String = "ALL",
        val propertyType: String = "ALL",
        val searchQuery: String = "",
        override val rawUrl: String
    ) : DeepLinkTarget

    data class InvalidLink(
        val reason: String,
        override val rawUrl: String
    ) : DeepLinkTarget
}

/**
 * Gestor de enlaces directos (Android App Links y esquema nativo yvycapital://) y compartición nativa
 * con el resumen completo de la propiedad mediante Intent.ACTION_SEND.
 */
object DeepLinkHandler {

    const val CANONICAL_DOMAIN = "yvycapital.com.py"
    const val CANONICAL_BASE_URL = "https://yvycapital.com.py"
    const val CUSTOM_SCHEME = "yvycapital"

    private val VALID_ID_REGEX = Regex("^[a-zA-Z0-9_\\-]{1,128}$")
    private val ALLOWED_HOSTS = setOf("yvycapital.com.py", "www.yvycapital.com.py")

    fun isValidId(id: String?): Boolean {
        return !id.isNullOrBlank() && VALID_ID_REGEX.matches(id.trim())
    }

    fun buildPropertyLink(propertyId: String): String {
        val cleanId = propertyId.trim()
        return "$CANONICAL_BASE_URL/propiedad/$cleanId"
    }

    fun buildPropertyCustomSchemeLink(propertyId: String): String {
        val cleanId = propertyId.trim()
        return "$CUSTOM_SCHEME://propiedad/$cleanId"
    }

    fun buildAdvisorLink(agentId: String, agentName: String? = null): String {
        val cleanId = agentId.trim().ifBlank { "asesor_yvy" }
        val base = "$CANONICAL_BASE_URL/asesor/$cleanId"
        return if (!agentName.isNullOrBlank()) {
            "$base?nombre=${Uri.encode(agentName.trim())}"
        } else {
            base
        }
    }

    fun buildExploreLink(filter: PropertyFilter): String {
        val builder = Uri.parse("$CANONICAL_BASE_URL/explorar").buildUpon()
        if (filter.department != "Todos") {
            builder.appendQueryParameter("departamento", filter.department)
        }
        if (filter.operationType != "ALL") {
            builder.appendQueryParameter("operacion", filter.operationType)
        }
        if (filter.propertyType != "ALL") {
            builder.appendQueryParameter("tipo", filter.propertyType)
        }
        if (filter.searchQuery.isNotBlank()) {
            builder.appendQueryParameter("q", filter.searchQuery.trim())
        }
        return builder.build().toString()
    }

    fun parseRawString(rawInput: String?): DeepLinkTarget? {
        val trimmed = rawInput?.trim() ?: return null
        if (trimmed.isEmpty()) return null

        // Si el usuario pega un texto completo compartido por WhatsApp, extraer el enlace o el ID
        val extractedUrl = extractUrlFromText(trimmed)
        if (extractedUrl != null) {
            return parseUri(Uri.parse(extractedUrl), extractedUrl)
        }

        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("$CUSTOM_SCHEME://", ignoreCase = true)
        ) {
            return parseUri(Uri.parse(trimmed), trimmed)
        }

        if (isValidId(trimmed)) {
            return DeepLinkTarget.PropertyDetail(
                propertyId = trimmed,
                rawUrl = buildPropertyLink(trimmed)
            )
        }

        return DeepLinkTarget.InvalidLink(
            reason = "El formato del enlace o identificador no es válido.",
            rawUrl = trimmed
        )
    }

    private fun extractUrlFromText(text: String): String? {
        val regex = Regex("(https?://[^\\s]+|yvycapital://[^\\s]+)")
        return regex.find(text)?.value?.trimEnd('.', ',', ')', ']')
    }

    fun parseUri(uri: Uri?, rawSource: String = uri?.toString().orEmpty()): DeepLinkTarget? {
        if (uri == null) return null
        val scheme = uri.scheme?.lowercase() ?: return null

        if (scheme == "https" || scheme == "http") {
            val host = uri.host?.lowercase().orEmpty()
            if (host !in ALLOWED_HOSTS) {
                return DeepLinkTarget.InvalidLink(
                    reason = "El dominio '$host' no pertenece a Yvy Capital.",
                    rawUrl = rawSource
                )
            }
            val segments = uri.pathSegments.orEmpty()
            if (segments.isEmpty()) {
                return DeepLinkTarget.ExploreFiltered(rawUrl = rawSource)
            }
            val section = segments[0].lowercase()
            return when (section) {
                "propiedad", "properties" -> {
                    val propId = segments.getOrNull(1)?.trim()
                        ?: uri.getQueryParameter("id")?.trim()
                    if (isValidId(propId)) {
                        DeepLinkTarget.PropertyDetail(
                            propertyId = propId!!,
                            rawUrl = rawSource
                        )
                    } else {
                        DeepLinkTarget.InvalidLink(
                            reason = "El enlace de la propiedad no contiene un identificador válido.",
                            rawUrl = rawSource
                        )
                    }
                }

                "asesor" -> {
                    val agentId = segments.getOrNull(1)?.trim()
                        ?: uri.getQueryParameter("id")?.trim()
                    val agentName = uri.getQueryParameter("nombre")?.trim()
                    if (isValidId(agentId)) {
                        DeepLinkTarget.AdvisorCatalog(
                            agentId = agentId!!,
                            agentName = agentName,
                            rawUrl = rawSource
                        )
                    } else {
                        DeepLinkTarget.InvalidLink(
                            reason = "El identificador del asesor no es válido.",
                            rawUrl = rawSource
                        )
                    }
                }

                "explorar" -> {
                    val dept = uri.getQueryParameter("departamento")?.takeIf { it.isNotBlank() } ?: "Todos"
                    val op = uri.getQueryParameter("operacion")?.uppercase()?.takeIf { it in setOf("ALL", "SALE", "RENT") } ?: "ALL"
                    val type = uri.getQueryParameter("tipo")?.uppercase()?.takeIf {
                        it in setOf("ALL", "HOUSE", "APARTMENT", "LAND", "OFFICE", "COMMERCIAL", "ESTANCIA")
                    } ?: "ALL"
                    val q = uri.getQueryParameter("q")?.take(80).orEmpty()
                    DeepLinkTarget.ExploreFiltered(
                        department = dept,
                        operationType = op,
                        propertyType = type,
                        searchQuery = q,
                        rawUrl = rawSource
                    )
                }

                else -> DeepLinkTarget.InvalidLink(
                    reason = "Ruta no reconocida en Yvy Capital: /$section",
                    rawUrl = rawSource
                )
            }
        }

        if (scheme == CUSTOM_SCHEME) {
            val hostSection = uri.host?.lowercase().orEmpty()
            val segments = uri.pathSegments.orEmpty()
            return when (hostSection) {
                "propiedad", "properties" -> {
                    val propId = segments.firstOrNull()?.trim() ?: uri.getQueryParameter("id")?.trim()
                    if (isValidId(propId)) {
                        DeepLinkTarget.PropertyDetail(
                            propertyId = propId!!,
                            rawUrl = rawSource
                        )
                    } else {
                        DeepLinkTarget.InvalidLink(
                            reason = "Identificador de propiedad inválido en esquema yvycapital://",
                            rawUrl = rawSource
                        )
                    }
                }

                "asesor" -> {
                    val agentId = segments.firstOrNull()?.trim() ?: uri.getQueryParameter("id")?.trim()
                    val agentName = uri.getQueryParameter("nombre")?.trim()
                    if (isValidId(agentId)) {
                        DeepLinkTarget.AdvisorCatalog(
                            agentId = agentId!!,
                            agentName = agentName,
                            rawUrl = rawSource
                        )
                    } else {
                        DeepLinkTarget.InvalidLink(
                            reason = "Identificador de asesor inválido en esquema yvycapital://",
                            rawUrl = rawSource
                        )
                    }
                }

                "explorar" -> {
                    val dept = uri.getQueryParameter("departamento")?.takeIf { it.isNotBlank() } ?: "Todos"
                    val op = uri.getQueryParameter("operacion")?.uppercase()?.takeIf { it in setOf("ALL", "SALE", "RENT") } ?: "ALL"
                    val type = uri.getQueryParameter("tipo")?.uppercase()?.takeIf {
                        it in setOf("ALL", "HOUSE", "APARTMENT", "LAND", "OFFICE", "COMMERCIAL", "ESTANCIA")
                    } ?: "ALL"
                    val q = uri.getQueryParameter("q")?.take(80).orEmpty()
                    DeepLinkTarget.ExploreFiltered(
                        department = dept,
                        operationType = op,
                        propertyType = type,
                        searchQuery = q,
                        rawUrl = rawSource
                    )
                }

                else -> DeepLinkTarget.InvalidLink(
                    reason = "Ruta nativa no reconocida: $rawSource",
                    rawUrl = rawSource
                )
            }
        }

        return DeepLinkTarget.InvalidLink(
            reason = "Esquema de enlace no soportado: $scheme",
            rawUrl = rawSource
        )
    }

    fun buildPropertyShareMessage(property: Property, displayCurrency: String): String {
        val mainPrice = CurrencyFormatter.formatPrice(
            amount = property.price,
            sourceCurrency = property.currency,
            displayCurrency = displayCurrency
        )
        val opLabel = CurrencyFormatter.operationLabel(property.operationType)
        val typeLabel = CurrencyFormatter.propertyTypeLabel(property.propertyType)
        val directLink = buildPropertyLink(property.id)
        val nativeLink = buildPropertyCustomSchemeLink(property.id)

        return buildString {
            appendLine("🏡 ${property.title}")
            appendLine("🏷️ $opLabel • $typeLabel — Yvy Capital Paraguay")
            appendLine("📍 Ubicación: ${property.neighborhood}, ${property.city} (${property.department})")
            if (property.address.isNotBlank()) {
                appendLine("🗺️ Dirección: ${property.address}")
            }
            appendLine("💰 Precio: $mainPrice")
            appendLine(
                "📐 Superficie: ${property.areaTotalM2} m² totales" +
                    (if (property.areaBuiltM2 > 0) " (${property.areaBuiltM2} m² construidos)" else "")
            )
            appendLine("🛏️ ${property.bedrooms} dormitorios | 🚿 ${property.bathrooms} baños | 🚗 ${property.parkingSpaces} cocheras")
            if (property.amenities.isNotEmpty()) {
                appendLine("✨ Comodidades: ${property.amenities.take(5).joinToString(", ")}")
            }
            if (property.agentName.isNotBlank()) {
                appendLine("👤 Asesor: ${property.agentName} (${property.agentAgency})")
            }
            appendLine()
            appendLine("🔗 Abrir directamente en Yvy Capital:")
            appendLine(directLink)
            append("📱 Enlace directo app: $nativeLink")
        }.trim()
    }

    fun shareProperty(context: Context, property: Property, displayCurrency: String) {
        val shareText = buildPropertyShareMessage(property, displayCurrency)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${property.title} — Yvy Capital")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Compartir propiedad en Yvy Capital"))
    }

    fun shareAdvisorPortfolio(context: Context, agentId: String, agentName: String, agencyName: String) {
        val link = buildAdvisorLink(agentId, agentName)
        val shareText = buildString {
            appendLine("👔 Cartera Inmobiliaria de $agentName ($agencyName) en Yvy Capital Paraguay")
            appendLine("Explora mis propiedades activas directamente en la aplicación:")
            append(link)
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Cartera de $agentName — Yvy Capital")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Compartir cartera de asesor"))
    }

    fun shareExploreFilter(context: Context, filter: PropertyFilter) {
        val link = buildExploreLink(filter)
        val shareText = buildString {
            appendLine("🔎 Búsqueda de Propiedades — Yvy Capital Paraguay")
            if (filter.department != "Todos") {
                appendLine("📍 Departamento: ${filter.department}")
            }
            if (filter.operationType != "ALL") {
                appendLine("🏷️ Operación: ${CurrencyFormatter.operationLabel(filter.operationType)}")
            }
            if (filter.propertyType != "ALL") {
                appendLine("🏢 Categoría: ${CurrencyFormatter.propertyTypeLabel(filter.propertyType)}")
            }
            if (filter.searchQuery.isNotBlank()) {
                appendLine("📝 Término: ${filter.searchQuery.trim()}")
            }
            appendLine()
            append("🔗 Abrir búsqueda en Yvy Capital: $link")
        }.trim()
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Búsqueda en Yvy Capital")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Compartir búsqueda"))
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
