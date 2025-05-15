package net.primal.android.scanner.analysis

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import net.primal.android.scanner.domain.QrCodeResult

class QrCodeAnalyzer(
    private val decoder: QrCodeResultDecoder,
    private val onQrCodeDetected: (QrCodeResult) -> Unit,
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        decoder.process(image)?.let {
            onQrCodeDetected(it)
        }
    }

    companion object {
        val AnalysisTargetSize = android.util.Size(1280, 720)
    }
}
