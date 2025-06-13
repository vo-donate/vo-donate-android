package dev.kevin.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.kevin.core.ui.R // Make sure this R import is correct for your 'core.ui' module

// Define the Maruburi Font Family
val MaruburiFamily = FontFamily(
    Font(R.font.maruburi_light, FontWeight.Light),       // Assuming you have maruburi_light.ttf
    Font(R.font.maruburi_regular, FontWeight.Normal),    // Assuming you have maruburi_regular.ttf
    Font(R.font.maruburi_regular, FontWeight.W400),      // Explicitly for Normal/W400
    Font(R.font.maruburi_regular, FontWeight.Medium),     // Assuming you have maruburi_medium.ttf
    Font(R.font.maruburi_bold, FontWeight.Bold),         // Assuming you have maruburi_bold.ttf
    Font(R.font.maruburi_semibold, FontWeight.SemiBold), // Assuming you have maruburi_semibold.ttf
    // Add other weights or styles (like Italic) if you have them
    // e.g., Font(R.font.maruburi_italic, FontWeight.Normal, FontStyle.Italic)
)

val AppFontFamily = FontFamily.Default

// Define the AppTypography using Maruburi as the default
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.Light, // Adjust weight as per Maruburi's design for display
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.Light, // Adjust
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.Normal, // Adjust
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.SemiBold, // Adjust
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.SemiBold, // Adjust
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.SemiBold, // Adjust
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.Bold, // Adjust
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.SemiBold, // Adjust
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.Medium, // Adjust
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.Light, // Adjust or Normal
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.SemiBold, // Adjust
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.Medium, // Adjust
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MaruburiFamily,
        fontWeight = FontWeight.Medium, // Adjust
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)