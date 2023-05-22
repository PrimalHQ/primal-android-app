package net.primal.android.feed.db

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["feedId", "postId"],
    foreignKeys = [
        ForeignKey(
            entity = PostData::class,
            parentColumns = arrayOf("postId"),
            childColumns = arrayOf("postId"),
        ),
        ForeignKey(
            entity = Feed::class,
            parentColumns = arrayOf("hex"),
            childColumns = arrayOf("feedId"),
        ),
    ]
)
data class FeedPostDataCrossRef(
    val feedId: String,
    val postId: String,
)
