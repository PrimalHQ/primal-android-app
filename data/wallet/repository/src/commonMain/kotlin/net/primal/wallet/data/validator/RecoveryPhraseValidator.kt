package net.primal.wallet.data.validator

import fr.acinq.bitcoin.MnemonicCode

class RecoveryPhraseValidator {

    fun isValid(phrase: String): Boolean {
        val words = phrase.trim()
            .lowercase()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

        if (words.size !in VALID_WORD_COUNTS) {
            return false
        }

        return runCatching {
            MnemonicCode.validate(words)
            true
        }.getOrDefault(false)
    }

    private companion object {
        val VALID_WORD_COUNTS = setOf(12, 15, 18, 21, 24)
    }
}
