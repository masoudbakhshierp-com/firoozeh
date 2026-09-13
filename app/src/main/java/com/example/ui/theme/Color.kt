package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// Modern Gradient Dark Theme Palette (No pure black #000000)
// Deep charcoal, slate, and rich dark emerald tones for high-end automotive feel
// ============================================================================
val DarkCanvasStart = Color(0xFF0F171E) // Deep slate-teal dark canvas top
val DarkCanvasEnd = Color(0xFF15222B) // Slate dark canvas bottom
val DarkSurfaceBase = Color(0xFF1A2630) // Elevated dark card surface (never pure black)
val DarkSurfaceElevated = Color(0xFF22323E) // Modals, bottom sheets, dialogs
val DarkSurfaceVariant = Color(0xFF131D24) // Input fields, pills, inner search bars
val DarkOutline = Color(0xFF293C48) // Modern card borders
val DarkOutlineVariant = Color(0xFF1E2D36)

val DarkOnSurface = Color(0xFFF1F5F9) // High-contrast clean off-white text
val DarkOnSurfaceMuted = Color(0xFF94A3B8) // Secondary slate text

// Brand Accents
val CleanGreenPrimary = Color(0xFF10B981) // Vibrant emerald
val CleanGreenPrimaryDark = Color(0xFF6EE7B7) // Light emerald text for containers
val CleanGreenPrimaryLight = Color(0xFF133E33) // Dark emerald pill / active container
val CleanGreenAccent = Color(0xFF34D399) // Medium success green
val CleanGreenDarkHeader = Color(0xFF064E3B) // Rich header green

// Secondary / Warning & Pending Accents
val CleanOrangeAccent = Color(0xFFF59E0B) // Amber accent
val CleanWarningBg = Color(0xFF332612) // Warning pill container
val CleanWarningText = Color(0xFFFBBF24) // Warning text

// Alert / Error
val CleanRedError = Color(0xFFF87171)
val CleanRedContainer = Color(0xFF381A1A)

// Mapped Clean Surfaces (Ensures zero pure-white or pure-black across all components)
val CleanLightBackground = DarkCanvasStart
val CleanLightSurface = DarkSurfaceBase
val CleanLightSurfaceVariant = DarkSurfaceVariant
val CleanLightOnSurface = DarkOnSurface
val CleanLightOnSurfaceMuted = DarkOnSurfaceMuted
val CleanLightOutline = DarkOutline
val CleanLightOutlineVariant = DarkOutlineVariant

// Dark Surfaces
val CleanDarkBackground = DarkCanvasStart
val CleanDarkSurface = DarkSurfaceBase
val CleanDarkSurfaceVariant = DarkSurfaceVariant
val CleanDarkOnSurface = DarkOnSurface
val CleanDarkOutline = DarkOutline
val CleanDarkOutlineVariant = DarkOutlineVariant

// ============================================================================
// Modern Gradients
// ============================================================================
val ModernAppBackgroundGradient = Brush.verticalGradient(
    colors = listOf(DarkCanvasStart, DarkCanvasEnd)
)

val ModernCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1E2C37), Color(0xFF16222B))
)

val ModernHeaderGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0A6C58))
)

val ModernPrimaryButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF059669), Color(0xFF10B981))
)

val ModernWarningGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFB45309), Color(0xFFD97706))
)

val ModernBottomNavGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF18242D), Color(0xFF121B22))
)

// Aliases for backwards compatibility with existing screen references
val CleanBluePrimary = CleanGreenPrimary
val CleanBluePrimaryLight = CleanGreenAccent
val CleanBlueOnPrimary = Color(0xFFFFFFFF)
val CleanBlueContainer = CleanGreenPrimaryLight
val CleanBlueOnContainer = CleanGreenPrimaryDark

val CleanTealAccent = CleanGreenAccent
val CleanTealContainer = CleanGreenPrimaryLight

val CleanPurpleAccent = CleanOrangeAccent
val CleanPurpleContainer = CleanWarningBg

val CleanRedAccent = CleanRedError

val EmeraldMint = CleanGreenPrimaryLight
val EmeraldDarkGreen = CleanGreenPrimary
val GoldLight = CleanWarningBg
val GoldAccent = CleanWarningText

// Semantic Status Colors
val StatusPendingBg = CleanWarningBg
val StatusPendingText = CleanWarningText

val StatusAssignedBg = CleanWarningBg
val StatusAssignedText = CleanWarningText

val StatusInspectionBg = CleanGreenPrimaryLight
val StatusInspectionText = CleanGreenPrimaryDark

val StatusWorkshopBg = CleanGreenPrimaryLight
val StatusWorkshopText = CleanGreenAccent

val StatusSettledBg = CleanGreenPrimaryLight
val StatusSettledText = CleanGreenAccent

