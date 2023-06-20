package net.primal.android.feed.db

import androidx.room.Entity

@Entity(
    primaryKeys = ["feedDirective", "eventId"],
)
data class FeedPostDataCrossRef(
    val feedDirective: String,
    val eventId: String,
)
