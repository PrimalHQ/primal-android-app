package net.primal.domain.builder

import net.primal.domain.wallet.DraftTx
import net.primal.domain.wallet.TxRequest

interface TxRequestBuilder {
    fun build(draftTx: DraftTx): Result<TxRequest>
}
