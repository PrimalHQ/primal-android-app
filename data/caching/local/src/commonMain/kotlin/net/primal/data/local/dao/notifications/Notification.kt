package net.primal.data.local.dao.notifications

import androidx.room3.Embedded
import androidx.room3.Relation
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.events.EventUriNostr
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.profiles.ProfileData
import net.primal.data.local.dao.streams.StreamData

data class Notification(
    @Embedded
    val data: NotificationData,

    @Relation(entityColumns = ["ownerId"], parentColumns = ["actionUserId"])
    val actionByUser: ProfileData?,

    @Relation(entityColumns = ["postId"], parentColumns = ["actionPostId"])
    val actionPost: PostData? = null,

    @Relation(entityColumns = ["eventId"], parentColumns = ["actionPostId"])
    val actionPostStats: EventStats? = null,

    @Relation(entityColumns = ["eventId"], parentColumns = ["actionPostId"])
    val actionPostUserStats: EventUserStats? = null,

    @Relation(entityColumns = ["eventId"], parentColumns = ["actionPostId"])
    val actionPostUris: List<EventUri> = emptyList(),

    @Relation(entityColumns = ["eventId"], parentColumns = ["actionPostId"])
    val actionPostNostrUris: List<EventUriNostr> = emptyList(),

    @Relation(entityColumns = ["aTag"], parentColumns = ["actionPostId"])
    val liveActivity: StreamData? = null,
)
