package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// ============================================================================
// YVY CAPITAL — TIPOGRAFÍA EDITORIAL (FRAUNCES + MANROPE)
// ============================================================================

/**
 * Serif editorial: reservada para el nombre de la marca, los títulos de pantalla
 * y el precio en la ficha de propiedad.
 */
val YvyEditorialSerifFamily = FontFamily(
    Font(R.font.fraunces, FontWeight.Normal),
    Font(R.font.fraunces, FontWeight.SemiBold),
    Font(R.font.fraunces, FontWeight.Bold)
)

/**
 * Sans moderna y legible: usada en toda la interfaz, secciones, cuerpo, botones y metadatos.
 */
val YvySansFamily = FontFamily(
    Font(R.font.manrope, FontWeight.Normal),
    Font(R.font.manrope, FontWeight.Medium),
    Font(R.font.manrope, FontWeight.SemiBold),
    Font(R.font.manrope, FontWeight.Bold)
)

// Alias de compatibilidad
val PlayfairDisplayFamily = YvyEditorialSerifFamily
val PlusJakartaSansFamily = YvySansFamily

/**
 * Estilo específico para precios destacados con cifras tabulares ("tnum").
 */
val YvyPriceEditorialStyle = TextStyle(
    fontFamily = YvyEditorialSerifFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 26.sp,
    lineHeight = 32.sp,
    fontFeatureSettings = "tnum"
)

val YvyPriceCardStyle = TextStyle(
    fontFamily = YvySansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    fontFeatureSettings = "tnum"
)

val YvyPriceCompactCardStyle = TextStyle(
    fontFamily = YvySansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 19.sp,
    fontFeatureSettings = "tnum"
)

val Typography = Typography(
    // Marca y titulares mayores (26–28 sp, Fraunces)
    displayLarge = TextStyle(
        fontFamily = YvyEditorialSerifFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp
    ),
    displayMedium = TextStyle(
        fontFamily = YvyEditorialSerifFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 27.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.2).sp
    ),
    // Título de pantalla (26–28 sp, Fraunces)
    headlineLarge = TextStyle(
        fontFamily = YvyEditorialSerifFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 27.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.2).sp
    ),
    // Precio en ficha / título editorial secundario (Fraunces + cifras tabulares)
    headlineMedium = TextStyle(
        fontFamily = YvyEditorialSerifFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontFeatureSettings = "tnum"
    ),
    // Títulos de sección (18 sp, Manrope)
    headlineSmall = TextStyle(
        fontFamily = YvySansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = YvySansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    // Subtítulos y títulos de tarjeta (15–16 sp, Manrope)
    titleMedium = TextStyle(
        fontFamily = YvySansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        fontFeatureSettings = "tnum"
    ),
    titleSmall = TextStyle(
        fontFamily = YvySansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = "tnum"
    ),
    // Cuerpo (14–15 sp, Manrope)
    bodyLarge = TextStyle(
        fontFamily = YvySansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = YvySansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Metadatos (12 sp, Manrope)
    bodySmall = TextStyle(
        fontFamily = YvySansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontFeatureSettings = "tnum"
    ),
    labelLarge = TextStyle(
        fontFamily = YvySansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = "tnum"
    ),
    labelMedium = TextStyle(
        fontFamily = YvySansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = "tnum"
    ),
    labelSmall = TextStyle(
        fontFamily = YvySansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = "tnum"
    )
)
