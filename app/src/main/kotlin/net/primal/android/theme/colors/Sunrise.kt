package net.primal.android.theme.colors

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val sunriseBrandText = Color(0xFF444444)
private val sunriseBackground1 = Color(0xFFF5F5F5)
private val sunriseBackground2 = Color(0xFFFFFFFF)
private val sunriseBackground3 = Color(0xFFE5E5E5)
private val sunriseBackground4 = Color(0xFFF5F5F5)
private val sunriseBackground5 = Color(0xFFE5E5E5)
private val sunriseForeground1 = Color(0xFF111111)
private val sunriseForeground2 = Color(0xFF111111)
private val sunriseForeground3 = Color(0xFF666666)
private val sunriseForeground4 = Color(0xFF808080)
private val sunriseForeground5 = Color(0xFF808080)
private val sunriseForeground6 = Color(0xFFC8C8C8)
private val sunriseAccent0 = Color(0xFFCA077C)
private val sunriseAccent1 = Color(0xFFCA079F)
private val sunriseAccent2 = Color(0xFFCA077C)
private val sunriseSuccessBright = Color(0xFF52CE0A)
private val sunriseSuccessDim = Color(0xFFC4F49F)
private val sunriseWarningBright = Color(0xFFC40000)
private val sunriseWarningDim = Color(0xFFFAC3C3)
private val sunriseReplied = Color(0xFF444444)
private val sunriseZapped = Color(0xFFFFA02F)
private val sunriseLiked = Color(0xFFCA079F)
private val sunriseReposted = Color(0xFF52CE0A)

val sunriseColorScheme = lightColorScheme(
    primary = sunriseAccent0,
    onPrimary = sunriseForeground1,
    secondary = sunriseAccent1,
    onSecondary = sunriseForeground1,
    tertiary = sunriseAccent2,
    onTertiary = sunriseForeground1,
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
    onBrand = sunriseBrandText,
    surfaceVariantAlt1 = sunriseBackground3,
    surfaceVariantAlt2 = sunriseBackground4,
    surfaceVariantAlt3 = sunriseBackground5,
    onSurfaceVariantAlt1 = sunriseForeground2,
    onSurfaceVariantAlt2 = sunriseForeground3,
    onSurfaceVariantAlt3 = sunriseForeground4,
    onSurfaceVariantAlt4 = sunriseForeground5,
    warning = sunriseWarningDim,
    successBright = sunriseSuccessBright,
    successDim = sunriseSuccessDim,
    replied = sunriseReplied,
    zapped = sunriseZapped,
    liked = sunriseLiked,
    reposted = sunriseReposted,
)
