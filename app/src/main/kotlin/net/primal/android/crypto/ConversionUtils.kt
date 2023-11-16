package net.primal.android.crypto

import org.spongycastle.util.encoders.Hex

fun String.hexToNoteHrp() =
    Bech32.encodeBytes(
        hrp = "note",
        data = Hex.decode(this),
        encoding = Bech32.Encoding.Bech32,
    )

fun String.hexToNpubHrp() =
    Bech32.encodeBytes(
        hrp = "npub",
        data = Hex.decode(this),
        encoding = Bech32.Encoding.Bech32,
    )

fun String.hexToNsecHrp() =
    Bech32.encodeBytes(
        hrp = "nsec",
        data = Hex.decode(this),
        encoding = Bech32.Encoding.Bech32,
    )

fun String.bech32ToHex() = Bech32.decodeBytes(bech32 = this).second.toHex()

fun ByteArray.toNsec() = Bech32.encodeBytes(hrp = "nsec", this, Bech32.Encoding.Bech32)

fun ByteArray.toNpub() = Bech32.encodeBytes(hrp = "npub", this, Bech32.Encoding.Bech32)

fun ByteArray.toNote() = Bech32.encodeBytes(hrp = "note", this, Bech32.Encoding.Bech32)

fun ByteArray.toHex() = String(Hex.encode(this))

fun Int.toByteArray(): ByteArray {
    val bytes = ByteArray(4)
    (0..3).forEach {
        bytes[3 - it] = ((this ushr (8 * it)) and 0xFFFF).toByte()
    }
    return bytes
}

fun String.bechToBytes(hrp: String? = null): ByteArray {
    val decodedForm = Bech32.decodeBytes(this)
    hrp?.also {
        if (it != decodedForm.first) {
            throw IllegalArgumentException("Expected $it but obtained ${decodedForm.first}")
        }
    }
    return decodedForm.second
}

/**
 * Interpret the string as Bech32 encoded and return hrp and ByteArray as Pair
 */
fun String.bechToBytesWithHrp() = Bech32.decodeBytes(this).run { Pair(first, second) }
