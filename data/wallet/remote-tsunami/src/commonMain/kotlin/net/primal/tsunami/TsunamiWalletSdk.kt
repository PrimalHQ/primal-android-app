package net.primal.tsunami

import net.primal.tsunami.model.OnChainWithdrawalFees
import net.primal.tsunami.model.Transfer
import net.primal.tsunami.model.WalletInfo

interface TsunamiWalletSdk {
    suspend fun createWallet(nsecStr: String): Result<String>
    suspend fun destroyWallet(walletId: String): Result<Unit>
    suspend fun computeWalletId(nsecStr: String): Result<String>
    suspend fun getWalletInfo(walletId: String): Result<WalletInfo>
    suspend fun getBalance(walletId: String): Result<String>
    suspend fun createInvoice(walletId: String, amountSats: ULong): Result<String>
    suspend fun payLightning(walletId: String, invoice: String): Result<String>
    suspend fun getTransfers(
        walletId: String,
        offset: ULong,
        limit: ULong,
        order: String,
    ): Result<List<Transfer>>

    suspend fun createOnChainDepositAddress(walletId: String): Result<String>
    suspend fun estimateOnChainWithdrawalFees(
        walletId: String,
        withdrawalAddress: String,
        amountSats: ULong?,
    ): Result<OnChainWithdrawalFees>
}
