package net.primal.data.local.dao.feeds

import androidx.room.Entity
import net.primal.domain.feeds.FeedSpecKind

@Entity(primaryKeys = ["ownerId", "dvmEventId", "specKindFilter"])
data class RecommendedDvmFeedCrossRef(
    val ownerId: String,
    val dvmEventId: String,
    val specKindFilter: String,
    val position: Int,
)

const val ALL_SPEC_KINDS_FILTER = "ALL"

fun FeedSpecKind?.asSpecKindFilter(): String = this?.name ?: ALL_SPEC_KINDS_FILTER
