package net.primal.wallet.data.parser.factory

import WalletTextParserImpl
import net.primal.domain.parser.WalletTextParser
import net.primal.domain.wallet.WalletRepository

object ParserFactory {
    fun createWalletTextParser(walletRepository: WalletRepository): WalletTextParser =
        WalletTextParserImpl(walletRepository = walletRepository)
}
