package net.primal.android.wallet.activation

import net.primal.android.wallet.activation.regions.Region

data class WalletActivationData(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val dateOfBirth: Long? = null,
    val country: Region? = null,
    val state: Region? = null,
)
