package net.primal.db.notes

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = [
        "ownerId",
        "feedSpec",
        "eventId",
    ],
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["feedSpec"]),
        Index(value = ["eventId"]),
    ],
)
data class FeedPostDataCrossRef(
    val ownerId: String,
    val feedSpec: String,
    val eventId: String,
    val orderIndex: Int,
)
