package net.primal.data.local.dao.events

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.primal.domain.links.CdnImage
import net.primal.domain.premium.PrimalLegendProfile

@Entity(
    indices = [
        Index(value = ["zapSenderId", "eventId", "zapRequestAt"]),
    ],
)
data class EventZap(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val eventId: String,
    val zapSenderId: String,
    val zapReceiverId: String,
    val zapRequestAt: Long,
    val zapReceiptAt: Long,
    val amountInBtc: Double,
    val message: String?,
    val zapSenderDisplayName: String? = null,
    val zapSenderHandle: String? = null,
    val zapSenderInternetIdentifier: String? = null,
    val zapSenderAvatarCdnImage: CdnImage? = null,
    val zapSenderPrimalLegendProfile: PrimalLegendProfile? = null,
)
