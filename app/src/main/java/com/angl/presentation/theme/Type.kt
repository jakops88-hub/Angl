package com.angl.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.angl.R

/**
 * Dark Luxury Typography System - "The Angl Aesthetic"
 * 
 * IMPORTANT: This implementation uses LOCAL font files placed in res/font/
 * Required files:
 * - cinzel_bold.ttf (Elegant serif for headlines)
 * - montserrat_medium.ttf (Modern sans-serif for body)
 * - montserrat_bold.ttf (Bold weight for emphasis)
 * 
 * Cinzel: Elegant serif for all headings and important text
 * Montserrat: Modern sans-serif for body text and UI elements
 */

// TEMPORARY DEBUG: Use default fonts to rule out resource crash
// Cinzel font family (elegant serif for headings)
// Uses local TTF file: res/font/cinzel_bold.ttf
// val CinzelFontFamily = FontFamily(
//     Font(R.font.cinzel_bold, FontWeight.Bold)
// )
val CinzelFontFamily = FontFamily.Default

// Montserrat font family (modern sans-serif for body)
// Uses local TTF files: res/font/montserrat_medium.ttf and montserrat_bold.ttf
// val MontserratFontFamily = FontFamily(
//     Font(R.font.montserrat_medium, FontWeight.Medium),
//     Font(R.font.montserrat_bold, FontWeight.Bold)
// )
val MontserratFontFamily = FontFamily.Default

// Luxury Typography with custom LOCAL fonts and tight letter spacing
val Typography = Typography(
    // Display styles - Cinzel for maximum impact
    displayLarge = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.5).sp,  // Tight spacing for headlines
        color = OffWhite
    ),
    displayMedium = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.5).sp,
        color = OffWhite
    ),
    displaySmall = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp,
        color = OffWhite
    ),
    
    // Headline styles - Cinzel for emphasis
    headlineLarge = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,  // Tight spacing per requirements
        color = OffWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp,
        color = OffWhite
    ),
    headlineSmall = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp,
        color = OffWhite
    ),
    
    // Title styles - Cinzel for important text
    titleLarge = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = OffWhite
    ),
    titleMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = OffWhite
    ),
    titleSmall = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = OffWhite
    ),
    
    // Body styles - Montserrat for readability
    bodyLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = OffWhite
    ),
    bodyMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = OffWhite
    ),
    bodySmall = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = OffWhite
    ),
    
    // Label styles - Montserrat for UI elements (buttons with wide spacing)
    labelLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 1.sp,  // Wide spacing for buttons per requirements
        color = OffWhite
    ),
    labelMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,  // Wide spacing for buttons
        color = OffWhite
    ),
    labelSmall = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,  // Wide spacing for buttons
        color = OffWhite
    )
)
