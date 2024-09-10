package net.primal.android.notes.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = [
        "feedSpec",
        "eventId",
    ],
    indices = [
        Index(value = ["feedSpec"]),
        Index(value = ["eventId"]),
    ],
)
data class FeedPostDataCrossRef(
    val feedSpec: String,
    val eventId: String,
    val orderIndex: Int,
)
