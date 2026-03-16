package net.primal.android.scanner.analysis

import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.nio.ByteBuffer
import javax.inject.Inject
import net.primal.android.scanner.domain.QrCodeDataType
import net.primal.android.scanner.domain.QrCodeResult

class ZxingQrCodeScanner @Inject constructor() : QrCodeResultDecoder {

    private val reader = QRCodeReader()

    private val hints = mapOf(
        DecodeHintType.TRY_HARDER to true,
    )

    override fun process(imageProxy: ImageProxy): QrCodeResult? {
        val width = imageProxy.width
        val height = imageProxy.height

        val yuvBytes = imageProxy.use {
            it.planes[0].buffer.toByteArray()
        }

        return scanBytes(bytes = yuvBytes, width = width, height = height)
            ?: scanBytes(bytes = yuvBytes.invertColors(), width = width, height = height)
    }

    private fun scanBytes(
        bytes: ByteArray,
        width: Int,
        height: Int,
    ): QrCodeResult? {
        val source = PlanarYUVLuminanceSource(
            bytes,
            width,
            height,
            0,
            0,
            width,
            height,
            false,
        )
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val result = runCatching { reader.decode(bitmap, hints) }.getOrNull() ?: return null

        val text = result.text ?: return null
        val dataType = QrCodeDataType.from(text) ?: return null

        return QrCodeResult(value = text, type = dataType)
    }

    @Suppress("MagicNumber")
    private fun ByteArray.invertColors(): ByteArray {
        val inverted = ByteArray(this.size)
        for (i in this.indices) {
            inverted[i] = (255 - this[i].toInt()).toByte()
        }
        return inverted
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }
    }
}
