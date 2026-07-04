package net.primal.wallet.data.local.dao

import androidx.room3.Entity
import androidx.room3.Index
import net.primal.shared.data.local.encryption.Encryptable

@Entity(
    primaryKeys = ["userId", "walletId"],
    indices = [
        Index(value = ["walletId"]),
        Index(value = ["lightningAddress"], unique = true),
    ],
)
data class WalletUserLink(
    val userId: String,
    val walletId: String,
    val lightningAddress: Encryptable<String>? = null,
)
