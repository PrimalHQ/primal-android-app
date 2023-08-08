package net.primal.android.theme.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color


private val sunsetBrand0 = Color(0xFFFA9A43)
private val sunsetBrand1 = Color(0xFFEF404A)
private val sunsetBrand2 = Color(0xFF5B12A4)
private val sunsetBrandText = Color(0xFFD5D5D5)
private val sunsetBackground1 = Color(0xFF000000)
private val sunsetBackground2 = Color(0xFF121212)
private val sunsetBackground3 = Color(0xFF222222)
private val sunsetForeground1 = Color(0xFFFFFFFF)
private val sunsetForeground2 = Color(0xFFAAAAAA)
private val sunsetForeground3 = Color(0xFFAAAAAA)
private val sunsetForeground4 = Color(0xFF757575)
private val sunsetForeground5 = Color(0xFF666666)
private val sunsetForeground6 = Color(0xFF444444)
private val sunsetAccent1 = Color(0xFFCA079F)
private val sunsetAccent2 = Color(0xFFAB268E)
private val sunsetSuccessBright = Color(0xFF66E205)
private val sunsetSuccessDim = Color(0xFF142D01)
private val sunsetWarningBright = Color(0xFFE20505)
private val sunsetWarningDim = Color(0xFF480101)

val sunsetColorScheme = darkColorScheme(
    primary = sunsetAccent1,
    onPrimary = sunsetForeground1,
    secondary = sunsetAccent2,
    onSecondary = sunsetForeground1,
    background = sunsetBackground1,
    onBackground = sunsetForeground1,
    surface = sunsetBackground1,
    onSurface = sunsetForeground1,
    surfaceVariant = sunsetBackground2,
    onSurfaceVariant = sunsetForeground1,
    error = sunsetWarningBright,
    outline = sunsetForeground6,
)

val sunsetExtraColorScheme = extraColorScheme(
    brand0 = sunsetBrand0,
    brand1 = sunsetBrand1,
    brand2 = sunsetBrand2,
    onBrand = sunsetBrandText,
    surfaceVariantAlt = sunsetBackground3,
    onSurfaceVariantAlt1 = sunsetForeground2,
    onSurfaceVariantAlt2 = sunsetForeground3,
    onSurfaceVariantAlt3 = sunsetForeground4,
    onSurfaceVariantAlt4 = sunsetForeground5,
    warning = sunsetWarningDim,
    successBright = sunsetSuccessBright,
    successDim = sunsetSuccessDim,
)
