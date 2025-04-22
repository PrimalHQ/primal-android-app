package net.primal.android.notes.feed.model

import net.primal.android.user.domain.WalletPreference
import net.primal.domain.notifications.ContentZapConfigItem
import net.primal.domain.notifications.ContentZapDefault
import net.primal.domain.notifications.DEFAULT_ZAP_CONFIG
import net.primal.domain.notifications.DEFAULT_ZAP_DEFAULT

data class ZappingState(
    val walletConnected: Boolean = false,
    val walletPreference: WalletPreference = WalletPreference.Undefined,
    val walletBalanceInBtc: String? = null,
    val zapDefault: ContentZapDefault = DEFAULT_ZAP_DEFAULT,
    val zapsConfig: List<ContentZapConfigItem> = DEFAULT_ZAP_CONFIG,
)
