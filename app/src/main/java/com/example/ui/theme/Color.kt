package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Authentic KungaCV Design Tokens from vend-pixel/kungacv/frontend/kungacv_client/tailwind.config.js & styles/globals.css
// Brand Blue Ramp (#1080F0 bright -> #0040D0 deep, signature action #0070F0)
val Brand50 = Color(0xFFEEF6FF)
val Brand100 = Color(0xFFD9EBFF)
val Brand200 = Color(0xFFB6D8FF)
val Brand300 = Color(0xFF7DBBFF)
val Brand400 = Color(0xFF4DA3FF)
val Brand500 = Color(0xFF1080F0)
val Brand600 = Color(0xFF0070F0) // Signature KungaCV Electric Blue
val Brand700 = Color(0xFF0050E0) // Link & active focus color
val Brand800 = Color(0xFF0040D0)
val Brand900 = Color(0xFF00309B)

// Ink Scale (#0A0A0B near-black ink navigation & typography -> #F5F6F8 subtle surface)
val Ink950 = Color(0xFF0A0A0B)
val Ink900 = Color(0xFF111114)
val Ink850 = Color(0xFF17171A)
val Ink800 = Color(0xFF1E1E22)
val Ink700 = Color(0xFF2A2A2F)
val Ink600 = Color(0xFF3A3A42)
val Ink500 = Color(0xFF6B6B76)
val Ink400 = Color(0xFF8A8A93)
val Ink300 = Color(0xFFB4B4BD)
val Ink200 = Color(0xFFD7D7DE)
val Ink100 = Color(0xFFECECF1)
val Ink50 = Color(0xFFF5F6F8)

// Warm Paper & Workspace Surfaces from globals.css
val WarmPaperBg = Color(0xFFF5F4F0)
val WarmInputSurface = Color(0xFFFAF9F5)
val WarmBorder = Color(0xFFDEDDD6)
val BrandTintBorder = Color(0xFFD5E4F8)
val CoverLetterStageBg = Color(0xFFEEF0F4)
val CrispPaperWhite = Color(0xFFFFFFFF)

// Semantic Status Colors
val CrimsonError = Color(0xFFA32929)
val CrimsonErrorBg = Color(0xFFFFF3F3)
val CrimsonErrorBorder = Color(0xFFF0BCBC)
val EmeraldSuccess = Color(0xFF15733B)
val EmeraldSuccessBg = Color(0xFFE9F7EF)

// Legacy compatibility aliases used by existing components
val OxfordNavy = Brand700
val OxfordNavyDark = Ink950
val SteelBlue = Brand600
val CeruleanLight = Brand400
val BurnishedGold = Brand600
val BurnishedGoldLight = Brand300
val BurnishedGoldContainer = Brand50
val OnBurnishedGoldContainer = Brand800
val AlabasterPaper = WarmPaperBg
val CrispIvorySurface = CrispPaperWhite
val SoftSlateSurface = WarmInputSurface
val EditorialBorder = WarmBorder
val CharcoalInk = Ink950
val MutedSlateInk = Ink500
val MidnightInkBg = Ink950
val DeepNavySlateSurface = Ink900
val ElevatedNavyCard = Ink800
val DarkTextPrimary = Ink50
val DarkTextSecondary = Ink400
