package net.primal.android.scanner.analysis

import androidx.camera.core.ImageProxy
import net.primal.android.scanner.domain.QrCodeResult

interface QrCodeResultDecoder {
    fun process(imageProxy: ImageProxy): QrCodeResult?
}
