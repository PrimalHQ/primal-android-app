package net.primal.android.notes.feed.zaps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.primal.domain.utils.canZap
import net.primal.domain.zaps.ZappingState

@Composable
fun ZapHost(
    zapHostState: ZapHostState,
    onZap: (Long, String?) -> Unit,
    onGoToWallet: () -> Unit,
) {
    if (zapHostState.showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = zapHostState.zappingState,
            onDismissRequest = { zapHostState.dismissCantZap() },
            onGoToWallet = onGoToWallet,
        )
    }

    if (zapHostState.showZapOptions && zapHostState.receiverName != null) {
        ZapBottomSheet(
            onDismissRequest = { zapHostState.dismissZapOptions() },
            receiverName = zapHostState.receiverName,
            zappingState = zapHostState.zappingState,
            onZap = { zapAmount, zapDescription ->
                if (zapHostState.zappingState.canZap(zapAmount)) {
                    onZap(zapAmount, zapDescription)
                } else {
                    zapHostState.showCantZapWarning()
                }
            },
        )
    }
}

@Stable
class ZapHostState(
    val zappingState: ZappingState,
    val receiverName: String? = null,
) {
    var showCantZapWarning by mutableStateOf(false)
        internal set

    var showZapOptions by mutableStateOf(false)
        internal set

    fun dismissCantZap() {
        showCantZapWarning = false
    }

    fun showCantZapWarning() {
        showCantZapWarning = true
    }

    fun dismissZapOptions() {
        showZapOptions = false
    }

    fun showZapOptionsOrShowWarning() {
        if (zappingState.walletConnected) {
            showZapOptions = true
        } else {
            showCantZapWarning = true
        }
    }
}

@Composable
fun rememberZapHostState(zappingState: ZappingState, receiverName: String? = null): ZapHostState =
    remember(zappingState, receiverName) {
        ZapHostState(
            zappingState = zappingState,
            receiverName = receiverName,
        )
    }
