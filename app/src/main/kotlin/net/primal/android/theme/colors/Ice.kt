package net.primal.android.theme.colors

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val iceBrand0 = Color(0xFF00E0FF)
private val iceBrand1 = Color(0xFF0090F8)
private val iceBrand2 = Color(0xFF4C00C7)
private val iceBrandText = Color(0xFF444444)
private val iceBackground1 = Color(0xFFF5F5F5)
private val iceBackground2 = Color(0xFFFFFFFF)
private val iceBackground3 = Color(0xFFE5E5E5)
private val iceForeground1 = Color(0xFF111111)
private val iceForeground2 = Color(0xFF111111)
private val iceForeground3 = Color(0xFF666666)
private val iceForeground4 = Color(0xFF808080)
private val iceForeground5 = Color(0xFF808080)
private val iceForeground6 = Color(0xFFC8C8C8)
private val iceAccent1 = Color(0xFF2394EF)
private val iceAccent2 = Color(0xFF0C7DD8)
private val iceSuccessBright = Color(0xFF52CE0A)
private val iceSuccessDim = Color(0xFFC4F49F)
private val iceWarningBright = Color(0xFFC40000)
private val iceWarningDim = Color(0xFFFAC3C3)

val iceColorScheme = lightColorScheme(
    primary = iceAccent1,
    onPrimary = iceForeground1,
    secondary = iceAccent2,
    onSecondary = iceForeground1,
    background = iceBackground1,
    onBackground = iceForeground1,
    surface = iceBackground1,
    onSurface = iceForeground1,
    surfaceVariant = iceBackground2,
    onSurfaceVariant = iceForeground1,
    error = iceWarningBright,
    outline = iceForeground6,
)

val iceExtraColorScheme = extraColorScheme(
    brand0 = iceBrand0,
    brand1 = iceBrand1,
    brand2 = iceBrand2,
    onBrand = iceBrandText,
    surfaceVariantAlt = iceBackground3,
    onSurfaceVariantAlt1 = iceForeground2,
    onSurfaceVariantAlt2 = iceForeground3,
    onSurfaceVariantAlt3 = iceForeground4,
    onSurfaceVariantAlt4 = iceForeground5,
    warning = iceWarningDim,
    successBright = iceSuccessBright,
    successDim = iceSuccessDim,
)
