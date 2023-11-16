package net.primal.android.feed.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = [
        "feedDirective",
        "eventId",
    ],
    indices = [
        Index(value = ["feedDirective"]),
        Index(value = ["eventId"]),
    ],
)
data class FeedPostDataCrossRef(
    val feedDirective: String,
    val eventId: String,
)
