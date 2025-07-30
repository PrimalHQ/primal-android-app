package net.primal.wallet.data.builder.factory

import net.primal.domain.builder.TxRequestBuilder
import net.primal.wallet.data.builder.TxRequestBuilderImpl

object TxRequestBuilderFactory {
    fun createTxRequestBuilder(): TxRequestBuilder = TxRequestBuilderImpl()
}
