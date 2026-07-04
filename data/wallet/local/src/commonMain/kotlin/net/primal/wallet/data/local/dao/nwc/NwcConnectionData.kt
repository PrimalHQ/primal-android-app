package net.primal.wallet.data.local.dao.nwc

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import net.primal.shared.data.local.encryption.Encryptable

@Entity
data class NwcConnectionData(
    @PrimaryKey
    val secretPubKey: String,
    val walletId: String,
    val userId: String,
    val servicePubKey: String,
    val servicePrivateKey: Encryptable<String>,
    val relay: Encryptable<String>,
    val appName: Encryptable<String>,
    val dailyBudgetSats: Encryptable<Long>?,
)
