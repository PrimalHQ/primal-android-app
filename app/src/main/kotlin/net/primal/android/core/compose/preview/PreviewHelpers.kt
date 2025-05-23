package net.primal.android.core.compose.preview

import androidx.camera.core.ImageProxy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.RippleDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.LocalPrimalTheme
import net.primal.android.LocalQrCodeDecoder
import net.primal.android.scanner.analysis.QrCodeResultDecoder
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.ContentDisplaySettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimalPreview(
    primalTheme: PrimalTheme,
    displaySettings: ContentDisplaySettings = ContentDisplaySettings(),
    content: @Composable () -> Unit,
) {
    val primalRippleConfiguration = RippleConfiguration(
        color = AppTheme.colorScheme.outline,
        rippleAlpha = RippleDefaults.RippleAlpha,
    )
    CompositionLocalProvider(
        LocalPrimalTheme provides primalTheme,
        LocalRippleConfiguration provides primalRippleConfiguration,
        LocalContentDisplaySettings provides displaySettings,
        LocalQrCodeDecoder provides EmptyQrCodeResultDecoder,
    ) {
        PrimalTheme(primalTheme = primalTheme, content = content)
    }
}

private val EmptyQrCodeResultDecoder = object : QrCodeResultDecoder {
    override fun process(imageProxy: ImageProxy): QrCodeResult? = null
}
