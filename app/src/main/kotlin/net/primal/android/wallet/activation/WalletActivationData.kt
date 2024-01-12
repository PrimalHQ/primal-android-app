package net.primal.android.wallet.activation

import net.primal.android.wallet.activation.regions.Region

data class WalletActivationData(
    val name: String = "",
    val email: String = "",
    val country: Region? = null,
    val state: Region? = null,
)
