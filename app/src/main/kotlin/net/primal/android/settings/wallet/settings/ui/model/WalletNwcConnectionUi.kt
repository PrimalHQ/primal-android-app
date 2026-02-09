package net.primal.android.settings.wallet.settings.ui.model

import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.domain.connections.nostr.model.NwcConnection
import net.primal.domain.connections.primal.model.PrimalNwcConnectionInfo

data class WalletNwcConnectionUi(
    val appName: String,
    val dailyBudgetInBtc: String?,
    val nwcPubkey: String,
)

fun NwcConnection.asWalletNwcConnectionUi() =
    WalletNwcConnectionUi(
        appName = appName,
        dailyBudgetInBtc = dailyBudgetSats?.toBtc()?.toString(),
        nwcPubkey = secretPubKey,
    )

fun PrimalNwcConnectionInfo.asWalletNwcConnectionUi() =
    WalletNwcConnectionUi(
        appName = appName,
        dailyBudgetInBtc = dailyBudgetInBtc,
        nwcPubkey = nwcPubkey,
    )
