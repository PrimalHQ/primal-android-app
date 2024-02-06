package net.primal.android.wallet.transactions.send.prepare.tabs

import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Directory
import net.primal.android.core.compose.icons.primaliconpack.Keyboard
import net.primal.android.core.compose.icons.primaliconpack.QrCode
import net.primal.android.wallet.ui.WalletTab

enum class SendPaymentTab(val data: WalletTab) {
    Nostr(
        data = WalletTab(
            unselectedIcon = PrimalIcons.Directory,
            selectedIcon = PrimalIcons.Directory,
            labelResId = R.string.wallet_send_payment_nostr_title,
        ),
    ),
    Scan(
        data = WalletTab(
            unselectedIcon = PrimalIcons.QrCode,
            selectedIcon = PrimalIcons.QrCode,
            labelResId = R.string.wallet_send_payment_scan_qr_code_title,
        ),
    ),
    Text(
        data = WalletTab(
            unselectedIcon = PrimalIcons.Keyboard,
            selectedIcon = PrimalIcons.Keyboard,
            labelResId = R.string.wallet_send_payment_keyboard_title,
        ),
    ),
    ;

    companion object {
        fun valueOfOrThrow(data: WalletTab): SendPaymentTab {
            return SendPaymentTab.entries.find { it.data == data } ?: error("Unknown send wallet tab.")
        }
    }
}
