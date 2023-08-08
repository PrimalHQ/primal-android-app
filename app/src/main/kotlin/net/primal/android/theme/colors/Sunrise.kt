package net.primal.android.theme.colors

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color


private val sunriseBrand0 = Color(0xFFFA9A43)
private val sunriseBrand1 = Color(0xFFEF404A)
private val sunriseBrand2 = Color(0xFF5B12A4)
private val sunriseBrandText = Color(0xFF444444)
private val sunriseBackground1 = Color(0xFFF5F5F5)
private val sunriseBackground2 = Color(0xFFFFFFFF)
private val sunriseBackground3 = Color(0xFFE5E5E5)
private val sunriseForeground1 = Color(0xFF111111)
private val sunriseForeground2 = Color(0xFF111111)
private val sunriseForeground3 = Color(0xFF666666)
private val sunriseForeground4 = Color(0xFF808080)
private val sunriseForeground5 = Color(0xFF808080)
private val sunriseForeground6 = Color(0xFFC8C8C8)
private val sunriseAccent1 = Color(0xFFCA079F)
private val sunriseAccent2 = Color(0xFFAB268E)
private val sunriseSuccessBright = Color(0xFF52CE0A)
private val sunriseSuccessDim = Color(0xFFC4F49F)
private val sunriseWarningBright = Color(0xFFC40000)
private val sunriseWarningDim = Color(0xFFFAC3C3)

val sunriseColorScheme = lightColorScheme(
    primary = sunriseAccent1,
    onPrimary = sunriseForeground1,
    secondary = sunriseAccent2,
    onSecondary = sunriseForeground1,
    background = sunriseBackground1,
    onBackground = sunriseForeground1,
    surface = sunriseBackground1,
    onSurface = sunriseForeground1,
    surfaceVariant = sunriseBackground2,
    onSurfaceVariant = sunriseForeground1,
    error = sunriseWarningBright,
    outline = sunriseForeground6,
)

val sunriseExtraColorScheme = extraColorScheme(
    brand0 = sunriseBrand0,
    brand1 = sunriseBrand1,
    brand2 = sunriseBrand2,
    onBrand = sunriseBrandText,
    surfaceVariantAlt = sunriseBackground3,
    onSurfaceVariantAlt1 = sunriseForeground2,
    onSurfaceVariantAlt2 = sunriseForeground3,
    onSurfaceVariantAlt3 = sunriseForeground4,
    onSurfaceVariantAlt4 = sunriseForeground5,
    warning = sunriseWarningDim,
    successBright = sunriseSuccessBright,
    successDim = sunriseSuccessDim,
)
