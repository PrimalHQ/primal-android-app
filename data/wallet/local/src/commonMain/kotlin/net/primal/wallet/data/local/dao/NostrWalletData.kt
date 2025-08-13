package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class NostrWalletData(
    @PrimaryKey
    val walletId: String,
    val relays: Encryptable<List<String>>,
    val pubkey: Encryptable<String>,
    val walletPubkey: Encryptable<String>,
    val walletPrivateKey: Encryptable<String>,
)
