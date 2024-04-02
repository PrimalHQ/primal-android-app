package net.primal.android.scanner.domain

import java.time.Instant

data class QrCodeResult(
    val value: String,
    val type: QrCodeDataType,
    val timestamp: Instant = Instant.now(),
) {
    fun equalValues(other: QrCodeResult?): Boolean = this.value == other?.value
}
