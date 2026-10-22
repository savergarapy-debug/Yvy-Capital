package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// YVY CAPITAL — TOKENS DE COLOR (SISTEMA VISUAL EDITORIAL & BOUTIQUE)
// ============================================================================

// Tema Claro (Por defecto)
val YvyBackgroundLight = Color(0xFFF7F4EE)       // Arena cálido principal
val YvySurfaceLight = Color(0xFFFFFFFF)          // Superficies y tarjetas planas
val YvyPlaceholderLight = Color(0xFFECE7DE)      // Fondo neutro para propiedades sin foto
val YvyTextPrimaryLight = Color(0xFF1C1B19)      // Texto principal (WCAG AAA sobre blanco/arena)
val YvyTextSecondaryLight = Color(0xFF6B675F)    // Texto secundario (WCAG AA sobre blanco/arena)
val YvyBorderLight = Color(0xFFE6E1D8)           // Bordes finos de 1 dp

// Identidad de Marca y Acentos
val YvyMonteGreen = Color(0xFF1F3D2B)            // Primario: verde monte (botones, nav activa, enlaces)
val YvyMonteGreenSoft = Color(0xFFE9EFEA)        // Contenedor primario sutil sin degradados
val YvyTerracotta = Color(0xFFB5532E)            // Acento: tierra roja (precios destacados, En venta/alquiler, estados activos)
val YvyTerracottaSoft = Color(0xFFF7EBE6)        // Contenedor acento suave
val YvyProGold = Color(0xFFB8954A)               // Dorado: exclusivo y puntual para elementos Pro
val YvyProGoldSoft = Color(0xFFF6F0E2)           // Fondo puntual suave para insignia Pro

// Estados Semánticos
val YvySuccess = Color(0xFF1F3D2B)
val YvyWarning = Color(0xFF9A6324)
val YvyError = Color(0xFFB33929)

// Tema Oscuro (Adaptado sin degradados ni brillos)
val YvyDarkBackground = Color(0xFF121412)        // Fondo principal oscuro
val YvyDarkSurface = Color(0xFF1B1E1B)           // Superficies y tarjetas oscuras
val YvyDarkCard = Color(0xFF222622)              // Placeholder / contenedor secundario oscuro
val YvyDarkBorder = Color(0xFF2E332E)            // Bordes finos en tema oscuro
val YvyDarkTextPrimary = Color(0xFFF4F1EA)       // Texto principal en tema oscuro
val YvyDarkTextSecondary = Color(0xFFA8A39A)     // Texto secundario en tema oscuro
val YvyDarkMonteGreen = Color(0xFF86B396)        // Primario legible sobre fondo oscuro
val YvyDarkTerracotta = Color(0xFFD97A56)        // Tierra roja adaptada a fondo oscuro

// Alias de compatibilidad semántica para el resto del código
val YvyForestDeep = YvyMonteGreen
val YvyForestPrimary = YvyMonteGreen
val YvyForestMedium = YvyMonteGreen
val YvySageLight = YvyMonteGreenSoft
val YvyBeigeWarm = YvyBackgroundLight
val YvyBeigeSurface = YvySurfaceLight
val YvySandCard = YvyPlaceholderLight
val YvyGoldAccent = YvyProGold
val YvyGoldLight = YvyProGoldSoft
val YvyCharcoal = YvyTextPrimaryLight
val YvySlate = YvyTextSecondaryLight
val YvyBorder = YvyBorderLight
val YvyGreenBright = YvyDarkMonteGreen
val YvyBeigeText = YvyDarkTextPrimary
