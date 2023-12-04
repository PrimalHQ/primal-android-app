package net.primal.android.wallet.api

import net.primal.android.wallet.api.model.WalletUserInfoResponse

interface WalletApi {

    suspend fun getWalletUserKycLevel(userId: String): Int

    suspend fun getWalletUserInfo(userId: String): WalletUserInfoResponse

    suspend fun requestActivationCodeToEmail(name: String, email: String)

    suspend fun activateWallet(code: String): String
}
