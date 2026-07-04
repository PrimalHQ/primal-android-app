package net.primal.wallet.data.local.dao

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable
import net.primal.shared.data.local.encryption.asEncryptable

@Entity
data class WalletSettings(
    @PrimaryKey
    val walletId: String,
    val spamThresholdAmountInSats: Encryptable<Long> = 1L.asEncryptable(),
)
