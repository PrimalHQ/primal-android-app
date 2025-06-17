package net.primal.android.core.compose

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class UiDensityMode {
    Normal,
    Comfortable,
    Compact,
    Unsupported,
}

fun Dp.detectUiDensityModeFromMaxHeight(): UiDensityMode {
    return when {
        this > 730.dp -> UiDensityMode.Normal
        this < 730.dp && this > 680.dp -> UiDensityMode.Comfortable
        this < 680.dp && this > 580.dp -> UiDensityMode.Compact
        else -> UiDensityMode.Unsupported
    }
}

fun Dp.detectUiDensityModeFromMaxWidth(): UiDensityMode {
    return when {
        this >= 360.dp -> UiDensityMode.Normal
        this in 320.dp..359.dp -> UiDensityMode.Comfortable
        this in 280.dp..319.dp -> UiDensityMode.Compact
        else -> UiDensityMode.Unsupported
    }
}

fun UiDensityMode?.isCompactOrLower() = this == UiDensityMode.Compact || this == UiDensityMode.Unsupported
