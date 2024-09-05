package net.primal.android.notes.feed.model

import net.primal.android.nostr.model.primal.content.ContentZapConfigItem
import net.primal.android.nostr.model.primal.content.ContentZapDefault
import net.primal.android.nostr.model.primal.content.DEFAULT_ZAP_CONFIG
import net.primal.android.nostr.model.primal.content.DEFAULT_ZAP_DEFAULT
import net.primal.android.user.domain.WalletPreference

data class ZappingState(
    val walletConnected: Boolean = false,
    val walletPreference: WalletPreference = WalletPreference.Undefined,
    val walletBalanceInBtc: String? = null,
    val zapDefault: ContentZapDefault = DEFAULT_ZAP_DEFAULT,
    val zapsConfig: List<ContentZapConfigItem> = DEFAULT_ZAP_CONFIG,
)
