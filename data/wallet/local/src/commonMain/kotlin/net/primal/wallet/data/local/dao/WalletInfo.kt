package net.primal.wallet.data.local.dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlin.uuid.ExperimentalUuidApi
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.encryption.EncryptableLong
import net.primal.shared.data.local.serialization.EncryptableTypeConverters

@Entity
@OptIn(ExperimentalUuidApi::class)
data class WalletInfo(
    @PrimaryKey
    val walletId: String,
    val userId: String,
    val lightningAddress: String?,
    val type: WalletType,
    val balanceInBtc: Double? = null,
    val maxBalanceInBtc: Double? = null,
    @field:TypeConverters(EncryptableTypeConverters::class) val lastUpdatedAt: EncryptableLong? = null,
)
