package net.primal.android.wallet.domain

import kotlinx.serialization.Serializable

@Serializable(with = WalletKycLevelSerializer::class)
enum class WalletKycLevel(val id: Int) {
    None(id = 0),
    Email(id = 1),
    IdDocument(id = 2),
    ;

    companion object {
        fun valueOf(id: Int): WalletKycLevel? = enumValues<WalletKycLevel>().find { it.id == id }
    }
}
