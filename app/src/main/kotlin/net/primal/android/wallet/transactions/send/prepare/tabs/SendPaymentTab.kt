package net.primal.android.wallet.transactions.send.prepare.tabs

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Directory
import net.primal.android.core.compose.icons.primaliconpack.Keyboard
import net.primal.android.core.compose.icons.primaliconpack.QrCode

enum class SendPaymentTab(val icon: ImageVector, @StringRes val labelResId: Int) {
    Nostr(icon = PrimalIcons.Directory, labelResId = R.string.wallet_send_payment_nostr_title),
    Scan(icon = PrimalIcons.QrCode, labelResId = R.string.wallet_send_payment_scan_qr_code_title),
    Text(icon = PrimalIcons.Keyboard, labelResId = R.string.wallet_send_payment_keyboard_title),
//    Image(icon = PrimalIcons.PhotoFromGallery, labelResId = R.string.wallet_send_payment_scan_image_title),
}
