package net.primal.android.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

private val midnightBrand0 = Color(0xFF00E0FF)
private val midnightBrand1 = Color(0xFF0090F8)
private val midnightBrand2 = Color(0xFF4C00C7)
private val midnightBrandText = Color(0xFFD5D5D5)
private val midnightBackground1 = Color(0xFF000000)
private val midnightBackground2 = Color(0xFF000000)
private val midnightBackground3 = Color(0xFF222222)
private val midnightForeground1 = Color(0xFFFFFFFF)
private val midnightForeground2 = Color(0xFFAAAAAA)
private val midnightForeground3 = Color(0xFFAAAAAA)
private val midnightForeground4 = Color(0xFF757575)
private val midnightForeground5 = Color(0xFF666666)
private val midnightForeground6 = Color(0xFF444444)
private val midnightAccent1 = Color(0xFF2394EF)
private val midnightAccent2 = Color(0xFF0C7DD8)
private val midnightSuccessBright = Color(0xFF66E205)
private val midnightSuccessDim = Color(0xFF142D01)
private val midnightWarningBright = Color(0xFFE20505)
private val midnightWarningDim = Color(0xFF480101)

val midnightColorScheme = darkColorScheme(
    primary = midnightAccent1,
    onPrimary = midnightForeground1,
    secondary = midnightAccent2,
    onSecondary = midnightForeground1,
    background = midnightBackground1,
    onBackground = midnightForeground1,
    surface = midnightBackground1,
    onSurface = midnightForeground1,
    surfaceVariant = midnightBackground2,
    onSurfaceVariant = midnightForeground1,
    error = midnightWarningBright,
    outline = midnightForeground6,
)

val midnightExtraColorScheme = extraColorScheme(
    brand0 = midnightBrand0,
    brand1 = midnightBrand1,
    brand2 = midnightBrand2,
    onBrand = midnightBrandText,
    surfaceVariantAlt = midnightBackground3,
    onSurfaceVariantAlt1 = midnightForeground2,
    onSurfaceVariantAlt2 = midnightForeground3,
    onSurfaceVariantAlt3 = midnightForeground4,
    onSurfaceVariantAlt4 = midnightForeground5,
    warning = midnightWarningDim,
    successBright = midnightSuccessBright,
    successDim = midnightSuccessDim,
)
