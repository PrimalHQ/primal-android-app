package net.primal.wallet.data.generator

import fr.acinq.bitcoin.MnemonicCode
import korlibs.crypto.fillRandomBytes
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.core.utils.security.zeroOut

class RecoveryPhraseGenerator {

    /**
     * Generates a BIP39 mnemonic recovery phrase.
     *
     * The entropy used for generation is zeroed from memory immediately after use.
     *
     * @param wordCount Number of words (12, 15, 18, 21, or 24)
     * @return Result containing the mnemonic words
     */
    fun generate(wordCount: Int = DEFAULT_WORD_COUNT): Result<List<String>> =
        runCatching {
            val entropySize = ENTROPY_SIZE_MAP[wordCount]
                ?: error("Invalid word count: $wordCount. Must be one of: ${ENTROPY_SIZE_MAP.keys.sorted()}")

            val entropy = ByteArray(entropySize).also { fillRandomBytes(it) }
            try {
                MnemonicCode.toMnemonics(entropy)
            } finally {
                entropy.zeroOut()
            }
        }

    private companion object {
        const val DEFAULT_WORD_COUNT = 12

        val ENTROPY_SIZE_MAP = mapOf(
            12 to 16,
            15 to 20,
            18 to 24,
            21 to 28,
            24 to 32,
        )
    }
}
