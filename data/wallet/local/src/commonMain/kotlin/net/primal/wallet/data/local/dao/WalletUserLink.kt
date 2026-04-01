package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.Index
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
