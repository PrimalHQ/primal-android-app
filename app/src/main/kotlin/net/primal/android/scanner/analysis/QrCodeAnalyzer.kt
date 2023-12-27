package net.primal.android.scanner.analysis

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import net.sourceforge.zbar.Config
import net.sourceforge.zbar.Image
import net.sourceforge.zbar.ImageScanner
import net.sourceforge.zbar.Symbol

class QrCodeAnalyzer(
    private val onQrCodeDetected: (QrCodeResult) -> Unit,
) : ImageAnalysis.Analyzer {

    private val imageScanner = ImageScanner()

    init {
        setupImageScanner()
    }

    private fun setupImageScanner() {
        imageScanner.setConfig(0, Config.X_DENSITY, 3)
        imageScanner.setConfig(0, Config.Y_DENSITY, 3)
        imageScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1)
    }

    override fun analyze(image: ImageProxy) {
        val imageBytes = image.use {
            // The CameraX supports "ImageFormat.YUV_420_888" format by default.
            // We need to process only the first Y plane since the codes are black and white.
            it.planes[0].buffer.toByteArray()
        }

        // Alternative options might be:
        // I420, YV12, NV12, UYVY, YUY2, BGR3, BGR4, YVU9, GREY, Y800, JPEG.
        val barcode = Image(image.width, image.height, "GREY") // Y800
        barcode.data = imageBytes

        imageScanner.scanImage(barcode)
            .takeIf { it != 0 }
            ?.let {
                // In order to retrieve QR codes containing null bytes we need to
                // use getDataBytes() rather than getData() which uses C strings.
                // Weirdly ZBar transforms all data to UTF-8, even the data returned
                // by getDataBytes() so we have to decode it as UTF-8.
                val dataBytes = imageScanner.results.firstNotNullOfOrNull {
                    when {
                        it.dataBytes != null -> {
                            val symDataFromBytes = String(it.dataBytes, StandardCharsets.UTF_8)
                            symDataFromBytes.ifEmpty { null }
                        }

                        else -> null
                    }
                }

                if (!dataBytes.isNullOrEmpty()) {
                    val dataType = QrCodeDataType.from(dataBytes)
                    if (dataType != null) {
                        onQrCodeDetected(
                            QrCodeResult(
                                value = dataBytes,
                                type = dataType,
                            ),
                        )
                    }
                }
            }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }
    }

    companion object {
        val AnalysisTargetSize = android.util.Size(1080, 1920)
    }
}
