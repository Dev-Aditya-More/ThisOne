package com.aditya1875.thisone.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aditya1875.thisone.R

val InterFamily = FontFamily(
    Font(R.font.inter_regular,  FontWeight.Normal),
    Font(R.font.inter_medium,   FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold,     FontWeight.Bold),
)

// ── Type scale ────────────────────────────────────────────────────────────────

val ThisOneTypography = Typography(

    // ── Display ── (hero text, splash screens)
    displayLarge = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 57.sp,
        lineHeight   = 64.sp,
        letterSpacing= (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Bold,
        fontSize     = 45.sp,
        lineHeight   = 52.sp,
        letterSpacing= 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 36.sp,
        lineHeight   = 44.sp,
        letterSpacing= 0.sp,
    ),

    // ── Headline ── (screen titles, section headers)
    headlineLarge = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 32.sp,
        lineHeight   = 40.sp,
        letterSpacing= (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 28.sp,
        lineHeight   = 36.sp,
        letterSpacing= (-0.25).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 24.sp,
        lineHeight   = 32.sp,
        letterSpacing= 0.sp,
    ),

    // ── Title ── (card titles, list items)
    titleLarge = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 22.sp,
        lineHeight   = 28.sp,
        letterSpacing= 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing= 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing= 0.1.sp,
    ),

    // ── Body ── (descriptions, meme captions, input text)
    bodyLarge = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing= 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing= 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing= 0.4.sp,
    ),

    // ── Label ── (buttons, chips, nav labels, tags)
    labelLarge = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing= 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing= 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 11.sp,
        lineHeight   = 16.sp,
        letterSpacing= 0.5.sp,
    ),
)