package net.primal.tsunami

import net.primal.tsunami.model.Transfer

interface TsunamiWalletSdk {
    suspend fun createWallet(nsecStr: String): Result<String>
    suspend fun destroyWallet(walletId: String): Result<Unit>
    suspend fun computeWalletId(nsecStr: String): Result<String>
    suspend fun getWalletInfo(walletId: String): Result<String?>
    suspend fun getBalance(walletId: String): Result<String>
    suspend fun createInvoice(walletId: String, amountSats: ULong): Result<String>
    suspend fun payInvoice(walletId: String, invoice: String): Result<String>
    suspend fun getTransfers(
        walletId: String,
        offset: ULong,
        limit: ULong,
    ): Result<List<Transfer>>
}
