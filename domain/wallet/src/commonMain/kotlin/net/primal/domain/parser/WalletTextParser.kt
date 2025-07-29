package net.primal.domain.parser

import net.primal.core.utils.Result
import net.primal.domain.wallet.DraftTx

interface WalletTextParser {
    suspend fun parseAndQueryText(userId: String, text: String): Result<DraftTx>
}
