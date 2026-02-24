package net.primal.wallet.data.parser.factory

import net.primal.domain.parser.WalletTextParser
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.parser.WalletTextParserImpl

object ParserFactory {
    fun createWalletTextParser(walletRepository: WalletRepository): WalletTextParser =
        WalletTextParserImpl(walletRepository = walletRepository)
}
