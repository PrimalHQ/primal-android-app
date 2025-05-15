package net.primal.android.scanner.analysis

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import javax.inject.Inject
import net.primal.android.scanner.domain.QrCodeDataType
import net.primal.android.scanner.domain.QrCodeResult

class GoogleBarcodeScanner @Inject constructor() : QrCodeResultDecoder {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
    )

    @OptIn(ExperimentalGetImage::class)
    override fun process(imageProxy: ImageProxy): QrCodeResult? =
        imageProxy.use {
            val mediaImage = it.image ?: return@use null

            val readyImage = InputImage.fromMediaImage(
                mediaImage,
                it.imageInfo.rotationDegrees,
            )

            val barcodes = runCatching { Tasks.await(scanner.process(readyImage)) }.getOrNull() ?: return@use null

            barcodes.firstNotNullOfOrNull {
                it.rawValue?.let { rawValue ->
                    QrCodeDataType.from(rawValue)?.let { type ->
                        QrCodeResult(value = rawValue, type = type)
                    }
                }
            }
        }
}
