package net.primal.android.wallet.api

import net.primal.android.wallet.api.model.DepositRequestBody
import net.primal.android.wallet.api.model.TransactionsRequestBody
import net.primal.android.wallet.api.model.WalletUserInfoResponse
import net.primal.android.wallet.api.model.WithdrawRequestBody

interface WalletApi {

    suspend fun getWalletUserKycLevel(userId: String): Int

    suspend fun getWalletUserInfo(userId: String): WalletUserInfoResponse

    suspend fun requestActivationCodeToEmail(
        userId: String,
        name: String,
        email: String,
    )

    suspend fun activateWallet(userId: String, code: String): String

    suspend fun balance(userId: String): Double

    suspend fun withdraw(userId: String, body: WithdrawRequestBody)

    suspend fun deposit(userId: String, body: DepositRequestBody)

    suspend fun transactions(userId: String, body: TransactionsRequestBody)
}
