package net.primal.android.auth.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val GradientColorBlueLight = Color(0x1F2586ED)
private val GradientColorBlue = Color(0xFF2572ED)
private val GradientColorPurple = Color(0xFF5B09AD)

private const val GradientStopStart = 0.00f
private const val GradientStop1 = 0.57f
private const val GradientStop2 = 0.71f
private const val GradientStopEnd = 1.00f
private const val GradientStartFraction = 6

fun onboardingGradientBrush(size: Size) =
    Brush.linearGradient(
        colorStops = arrayOf(
            GradientStopStart to GradientColorBlueLight,
            GradientStop1 to GradientColorBlue,
            GradientStop2 to GradientColorBlue,
            GradientStopEnd to GradientColorPurple,
        ),
        start = Offset(x = -size.width / GradientStartFraction, y = -size.height / GradientStartFraction),
        end = Offset(x = size.width, y = size.height),
    )

const val OnboardingGradientAlpha = 0.18f
val OnboardingBackgroundColor = Color.White
val OnboardingTextColor = Color(0xFF111111)
