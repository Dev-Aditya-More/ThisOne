package com.aditya1875.thisone.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ── Shape scale ───────────────────────────────────────────────────────────────
// Purposefully rounded — meme app should feel playful, not corporate.

val ThisOneShapes = Shapes(
    // Chips, small badges, snackbars
    extraSmall = RoundedCornerShape(8.dp),

    // Input fields, small cards, bottom sheet handles
    small = RoundedCornerShape(12.dp),

    // Standard cards, dialogs, search bar
    medium = RoundedCornerShape(16.dp),

    // Meme result cards, bottom sheets, modals
    large = RoundedCornerShape(24.dp),

    // FAB, full-bleed bottom sheet, hero cards
    extraLarge = RoundedCornerShape(32.dp),
)