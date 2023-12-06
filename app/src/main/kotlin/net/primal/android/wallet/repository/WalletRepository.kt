package net.primal.android.wallet.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.domain.PrimalWallet
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.api.WalletApi
import net.primal.android.wallet.api.model.WalletUserInfoResponse
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.WalletKycLevel

class WalletRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val accountsStore: UserAccountsStore,
    private val walletApi: WalletApi,
) {
    suspend fun fetchUserWalletInfoAndUpdateUserAccount(userId: String) {
        withContext(dispatcherProvider.io()) {
            val walletInfo = walletApi.getWalletUserInfo(userId)
            walletInfo.storeWalletInfoLocally(userId = userId)
        }
    }

    suspend fun activateWallet(userId: String, code: String): String {
        return withContext(dispatcherProvider.io()) {
            val lightningAddress = walletApi.activateWallet(code)
            // TODO Store lightning address
            // walletInfo.storeWalletInfoLocally(userId = userId)
            lightningAddress
        }
    }

    suspend fun requestActivationCodeToEmail(name: String, email: String) {
        withContext(dispatcherProvider.io()) {
            walletApi.requestActivationCodeToEmail(name, email)
        }
    }

    suspend fun withdraw(userId: String, body: WithdrawRequestBody) {
        withContext(dispatcherProvider.io()) {
            walletApi.withdraw(userId, body)
        }
    }

    suspend fun updateWalletPreference(userId: String, walletPreference: WalletPreference) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(walletPreference = walletPreference)
        }
    }

    private suspend fun WalletUserInfoResponse.storeWalletInfoLocally(userId: String) {
        val kycLevel = WalletKycLevel.valueOf(kycLevel) ?: return
        val primalWallet = PrimalWallet(kycLevel = kycLevel, lightningAddress = this.lightningAddress)
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(primalWallet = primalWallet)
        }
    }
}
