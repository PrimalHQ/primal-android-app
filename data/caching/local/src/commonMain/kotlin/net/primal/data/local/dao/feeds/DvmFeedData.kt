package net.primal.data.local.dao.feeds

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.feeds.FeedSpecKind

@Entity
data class DvmFeedData(
    @PrimaryKey val eventId: String,
    val dvmId: String,
    val dvmPubkey: String,
    val dvmLnUrl: String?,
    val avatarUrl: String?,
    val title: String,
    val description: String?,
    val amountInSats: String?,
    val primalSubscriptionRequired: Boolean?,
    val kind: FeedSpecKind?,
    val primalSpec: String?,
    val isPrimalFeed: Boolean?,
)
