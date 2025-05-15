package net.primal.android.scanner.analysis

import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import net.primal.android.scanner.domain.QrCodeDataType
import net.primal.android.scanner.domain.QrCodeResult
import net.sourceforge.zbar.Config
import net.sourceforge.zbar.Image
import net.sourceforge.zbar.ImageScanner
import net.sourceforge.zbar.Symbol

class ZbarQrCodeScanner @Inject constructor() : QrCodeResultDecoder {

    private val imageScanner = ImageScanner()

    init {
        imageScanner.setConfig(0, Config.X_DENSITY, 3)
        imageScanner.setConfig(0, Config.Y_DENSITY, 3)
        imageScanner.setConfig(Symbol.QRCODE, Config.ENABLE, 1)
    }

    override fun process(imageProxy: ImageProxy): QrCodeResult? {
        val width = imageProxy.width
        val height = imageProxy.height

        val imageBytes = imageProxy.use {
            // The CameraX supports "ImageFormat.YUV_420_888" format by default.
            // We need to process only the first Y plane since the codes are black and white.
            it.planes[0].buffer.toByteArray()
        }

        return scanBytes(bytes = imageBytes, width = width, height = height)
            ?: scanBytes(bytes = imageBytes.invertColors(), width = width, height = height)
    }

    private fun scanBytes(
        bytes: ByteArray,
        width: Int,
        height: Int,
    ): QrCodeResult? {
        // Alternative options: GREY, Y800, JPEG.
        val barcode = Image(width, height, "GREY")
        barcode.data = bytes

        return imageScanner.scanImage(barcode)
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
                        QrCodeResult(value = dataBytes, type = dataType)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
    }

    @Suppress("MagicNumber")
    private fun ByteArray.invertColors(): ByteArray {
        val invertedByteArray = ByteArray(this.size)
        for (i in this.indices) {
            invertedByteArray[i] = (255 - this[i].toInt()).toByte()
        }
        return invertedByteArray
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }
    }
}
