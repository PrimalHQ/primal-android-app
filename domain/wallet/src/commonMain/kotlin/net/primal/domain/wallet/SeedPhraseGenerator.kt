package net.primal.domain.wallet

import net.primal.core.utils.Result

interface SeedPhraseGenerator {

    fun generate(wordCount: Int = DEFAULT_WORD_COUNT): Result<List<String>>

    companion object {
        const val DEFAULT_WORD_COUNT = 12
    }
}
