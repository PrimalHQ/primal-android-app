package net.primal.tsunami

import kotlinx.coroutines.CoroutineDispatcher
import net.primal.core.utils.coroutines.createDispatcherProvider
import net.primal.tsunami.model.Transfer
import net.primal.tsunami.model.WalletInfo

class TsunamiWalletSdkStubImpl : TsunamiWalletSdk {
    override suspend fun createWallet(nsecStr: String): Result<String> {
        throw NotImplementedError()
    }

    override suspend fun getWalletInfo(walletId: String): Result<WalletInfo> {
        throw NotImplementedError()
    }

    override suspend fun getBalance(walletId: String): Result<String> {
        throw NotImplementedError()
    }

    override suspend fun destroyWallet(walletId: String): Result<Unit> {
        throw NotImplementedError()
    }

    override suspend fun createInvoice(walletId: String, amountSats: ULong): Result<String> {
        throw NotImplementedError()
    }

    override suspend fun payInvoice(walletId: String, invoice: String): Result<String> {
        throw NotImplementedError()
    }

    override suspend fun computeWalletId(nsecStr: String): Result<String> {
        throw NotImplementedError()
    }

    override suspend fun getTransfers(
        walletId: String,
        offset: ULong,
        limit: ULong,
    ): Result<List<Transfer>> {
        throw NotImplementedError()
    }

    override suspend fun createOnChainDepositAddress(walletId: String): Result<String> {
        throw NotImplementedError()
    }
}

/**
 * Create a stub instance of TsunamiWalletSdk.
 */
fun createTsunamiWalletSdk(dispatcher: CoroutineDispatcher = createDispatcherProvider().io()): TsunamiWalletSdk {
    return TsunamiWalletSdkStubImpl()
}
