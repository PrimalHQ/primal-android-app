@file:Suppress("SpellCheckingInspection", "unused")

package io.github.anvell.filetype.matchers

import io.github.anvell.filetype.extensions.fromBigEndian

/**
 * ISO base media file format helper.
 */
internal class IsoBmff private constructor(
    val majorBrand: String,
    val minorVersion: String,
    val compatibleBrands: Set<String>,
) {

    companion object {
        fun checkFormat(buffer: ByteArray, majorBrand: String): Boolean {
            if (!detect(buffer)) return false
            val media = decode(buffer) ?: return false

            if (media.majorBrand == majorBrand) return true

            return if (media.majorBrand in setOf("mif1", "msf1")) {
                majorBrand in media.compatibleBrands
            } else {
                false
            }
        }

        private fun detect(buffer: ByteArray): Boolean {
            if (buffer.size < 16) return false

            val flag = buffer
                .sliceArray(4 until 8)
                .decodeToString()
            if (flag != "ftyp") return false

            val ftypLength = Int
                .fromBigEndian(buffer)
                .toUInt()

            return buffer.size.toUInt() >= ftypLength
        }

        private fun decode(buffer: ByteArray): IsoBmff? {
            if (buffer.size < 16) return null

            val majorBrand = buffer
                .sliceArray(8 until 12)
                .decodeToString()
            val minorVersion = buffer
                .sliceArray(12 until 16)
                .decodeToString()
            val ftypLength = Int
                .fromBigEndian(buffer)
                .toUInt()
            val compatibleBrands = buildSet {
                for (i in 16 until ftypLength.toInt() step 4) {
                    if (buffer.size >= i + 4) {
                        add(buffer.sliceArray(i until i + 4).decodeToString())
                    }
                }
            }

            return IsoBmff(majorBrand, minorVersion, compatibleBrands)
        }
    }
}
