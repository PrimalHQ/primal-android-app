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
fun ExtraColorSchemeProvider(extraColorScheme: ExtraColorScheme, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtraColors provides extraColorScheme, content = content)
}

@Stable
class ExtraColorScheme(
    onBrand: Color,
    surfaceVariantAlt1: Color,
    surfaceVariantAlt2: Color,
    surfaceVariantAlt3: Color,
    onSurfaceVariantAlt1: Color,
    onSurfaceVariantAlt2: Color,
    onSurfaceVariantAlt3: Color,
    onSurfaceVariantAlt4: Color,
    warning: Color,
    successBright: Color,
    successDim: Color,
    replied: Color,
    zapped: Color,
    liked: Color,
    reposted: Color,
) {
    var onBrand by mutableStateOf(onBrand, structuralEqualityPolicy())
        internal set

    var surfaceVariantAlt1 by mutableStateOf(surfaceVariantAlt1, structuralEqualityPolicy())
        internal set

    var surfaceVariantAlt2 by mutableStateOf(surfaceVariantAlt2, structuralEqualityPolicy())
        internal set

    var surfaceVariantAlt3 by mutableStateOf(surfaceVariantAlt3, structuralEqualityPolicy())
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

    var replied by mutableStateOf(replied, structuralEqualityPolicy())
        internal set

    var zapped by mutableStateOf(zapped, structuralEqualityPolicy())
        internal set

    var liked by mutableStateOf(liked, structuralEqualityPolicy())
        internal set

    var reposted by mutableStateOf(reposted, structuralEqualityPolicy())
        internal set

    fun copy(
        onBrand: Color = this.onBrand,
        surfaceVariantAlt1: Color = this.surfaceVariantAlt1,
        surfaceVariantAlt2: Color = this.surfaceVariantAlt2,
        surfaceVariantAlt3: Color = this.surfaceVariantAlt3,
        onSurfaceVariantAlt1: Color = this.onSurfaceVariantAlt1,
        onSurfaceVariantAlt2: Color = this.onSurfaceVariantAlt2,
        onSurfaceVariantAlt3: Color = this.onSurfaceVariantAlt3,
        onSurfaceVariantAlt4: Color = this.onSurfaceVariantAlt4,
        warning: Color = this.warning,
        successBright: Color = this.successBright,
        successDim: Color = this.successDim,
        replied: Color = this.replied,
        zapped: Color = this.zapped,
        liked: Color = this.liked,
        reposted: Color = this.reposted,
    ): ExtraColorScheme =
        ExtraColorScheme(
            onBrand = onBrand,
            surfaceVariantAlt1 = surfaceVariantAlt1,
            surfaceVariantAlt2 = surfaceVariantAlt2,
            surfaceVariantAlt3 = surfaceVariantAlt3,
            onSurfaceVariantAlt1 = onSurfaceVariantAlt1,
            onSurfaceVariantAlt2 = onSurfaceVariantAlt2,
            onSurfaceVariantAlt3 = onSurfaceVariantAlt3,
            onSurfaceVariantAlt4 = onSurfaceVariantAlt4,
            warning = warning,
            successBright = successBright,
            successDim = successDim,
            replied = replied,
            zapped = zapped,
            liked = liked,
            reposted = reposted,
        )
}

fun extraColorScheme(
    onBrand: Color,
    surfaceVariantAlt1: Color,
    surfaceVariantAlt2: Color,
    surfaceVariantAlt3: Color,
    onSurfaceVariantAlt1: Color,
    onSurfaceVariantAlt2: Color,
    onSurfaceVariantAlt3: Color,
    onSurfaceVariantAlt4: Color,
    warning: Color,
    successBright: Color,
    successDim: Color,
    replied: Color,
    zapped: Color,
    liked: Color,
    reposted: Color,
): ExtraColorScheme =
    ExtraColorScheme(
        onBrand = onBrand,
        surfaceVariantAlt1 = surfaceVariantAlt1,
        surfaceVariantAlt2 = surfaceVariantAlt2,
        surfaceVariantAlt3 = surfaceVariantAlt3,
        onSurfaceVariantAlt1 = onSurfaceVariantAlt1,
        onSurfaceVariantAlt2 = onSurfaceVariantAlt2,
        onSurfaceVariantAlt3 = onSurfaceVariantAlt3,
        onSurfaceVariantAlt4 = onSurfaceVariantAlt4,
        warning = warning,
        successBright = successBright,
        successDim = successDim,
        replied = replied,
        zapped = zapped,
        liked = liked,
        reposted = reposted,
    )

internal val LocalExtraColors =
    staticCompositionLocalOf<ExtraColorScheme> { error("No extra colors provided.") }
