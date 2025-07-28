package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NostrWalletData(
    @PrimaryKey
    val walletId: String,
    val relays: List<String>,
    val pubkey: String,
    val walletPubkey: String,
    val walletPrivateKey: String,
)
