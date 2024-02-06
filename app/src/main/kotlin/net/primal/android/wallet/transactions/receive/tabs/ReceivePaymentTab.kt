package net.primal.android.wallet.transactions.receive.tabs

import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletBitcoinPayment
import net.primal.android.core.compose.icons.primaliconpack.WalletLightningPayment
import net.primal.android.core.compose.icons.primaliconpack.WalletLightningPaymentFilled
import net.primal.android.wallet.domain.Network
import net.primal.android.wallet.ui.WalletTab

enum class ReceivePaymentTab(val data: WalletTab, val network: Network) {
    Lightning(
        data = WalletTab(
            unselectedIcon = PrimalIcons.WalletLightningPayment,
            selectedIcon = PrimalIcons.WalletLightningPaymentFilled,
            labelResId = R.string.wallet_receive_lightning_transaction_title,
        ),
        network = Network.Lightning,
    ),
    Bitcoin(
        data = WalletTab(
            unselectedIcon = PrimalIcons.WalletBitcoinPayment,
            selectedIcon = PrimalIcons.WalletBitcoinPayment,
            labelResId = R.string.wallet_receive_btc_transaction_title,
        ),
        network = Network.Bitcoin,
    ),
    ;

    companion object {
        fun valueOfOrThrow(data: WalletTab): ReceivePaymentTab {
            return ReceivePaymentTab.entries.find { it.data == data } ?: error("Unknown receive wallet tab.")
        }
    }
}
