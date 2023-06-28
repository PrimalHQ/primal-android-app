package net.primal.android.theme.colors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color


@Composable
fun ExtraColorSchemeProvider(
    extraColorScheme: ExtraColorScheme,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalExtraColors provides extraColorScheme, content = content)
}

@Stable
class ExtraColorScheme(
    brand0: Color,
    brand1: Color,
    brand2: Color,
    onBrand: Color,
    surfaceVariantAlt: Color,
    onSurfaceVariantAlt1: Color,
    onSurfaceVariantAlt2: Color,
    onSurfaceVariantAlt3: Color,
    onSurfaceVariantAlt4: Color,
    warning: Color,
    successBright: Color,
    successDim: Color,
) {
    var brand0 by mutableStateOf(brand0, structuralEqualityPolicy())
        internal set

    var brand1 by mutableStateOf(brand1, structuralEqualityPolicy())
        internal set

    var brand2 by mutableStateOf(brand2, structuralEqualityPolicy())
        internal set

    var onBrand by mutableStateOf(onBrand, structuralEqualityPolicy())
        internal set

    var surfaceVariantAlt by mutableStateOf(surfaceVariantAlt, structuralEqualityPolicy())
        internal set

    var onSurfaceVariantAlt1 by mutableStateOf(onSurfaceVariantAlt1, structuralEqualityPolicy())
        internal set

    var onSurfaceVariantAlt2 by mutableStateOf(onSurfaceVariantAlt2, structuralEqualityPolicy())
        internal set

    var onSurfaceVariantAlt3 by mutableStateOf(onSurfaceVariantAlt3, structuralEqualityPolicy())
        internal set

    var onSurfaceVariantAlt4 by mutableStateOf(onSurfaceVariantAlt4, structuralEqualityPolicy())
        internal set

    var warning by mutableStateOf(warning, structuralEqualityPolicy())
        internal set

    var successBright by mutableStateOf(successBright, structuralEqualityPolicy())
        internal set

    var successDim by mutableStateOf(successDim, structuralEqualityPolicy())
        internal set

    fun copy(
        brand0: Color = this.brand0,
        brand1: Color = this.brand1,
        brand2: Color = this.brand2,
        onBrand: Color = this.onBrand,
        surfaceVariantAlt: Color = this.surfaceVariantAlt,
        onSurfaceVariantAlt1: Color = this.onSurfaceVariantAlt1,
        onSurfaceVariantAlt2: Color = this.onSurfaceVariantAlt2,
        onSurfaceVariantAlt3: Color = this.onSurfaceVariantAlt3,
        onSurfaceVariantAlt4: Color = this.onSurfaceVariantAlt4,
        warning: Color = this.warning,
        successBright: Color = this.successBright,
        successDim: Color = this.successDim
    ): ExtraColorScheme = ExtraColorScheme(
        brand0 = brand0,
        brand1 = brand1,
        brand2 = brand2,
        onBrand = onBrand,
        surfaceVariantAlt = surfaceVariantAlt,
        onSurfaceVariantAlt1 = onSurfaceVariantAlt1,
        onSurfaceVariantAlt2 = onSurfaceVariantAlt2,
        onSurfaceVariantAlt3 = onSurfaceVariantAlt3,
        onSurfaceVariantAlt4 = onSurfaceVariantAlt4,
        warning = warning,
        successBright = successBright,
        successDim = successDim,
    )
}

fun extraColorScheme(
    brand0: Color,
    brand1: Color,
    brand2: Color,
    onBrand: Color,
    surfaceVariantAlt: Color,
    onSurfaceVariantAlt1: Color,
    onSurfaceVariantAlt2: Color,
    onSurfaceVariantAlt3: Color,
    onSurfaceVariantAlt4: Color,
    warning: Color,
    successBright: Color,
    successDim: Color,
): ExtraColorScheme = ExtraColorScheme(
    brand0 = brand0,
    brand1 = brand1,
    brand2 = brand2,
    onBrand = onBrand,
    surfaceVariantAlt = surfaceVariantAlt,
    onSurfaceVariantAlt1 = onSurfaceVariantAlt1,
    onSurfaceVariantAlt2 = onSurfaceVariantAlt2,
    onSurfaceVariantAlt3 = onSurfaceVariantAlt3,
    onSurfaceVariantAlt4 = onSurfaceVariantAlt4,
    warning = warning,
    successBright = successBright,
    successDim = successDim
)

internal val LocalExtraColors =
    staticCompositionLocalOf<ExtraColorScheme> { error("No extra colors provided.") }
