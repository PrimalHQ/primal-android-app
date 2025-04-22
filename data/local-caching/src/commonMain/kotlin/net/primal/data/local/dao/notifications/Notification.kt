package net.primal.data.local.dao.notifications

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUri
import net.primal.data.local.dao.events.EventUriNostr
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.notes.PostData
import net.primal.data.local.dao.profiles.ProfileData

data class Notification(
    @Embedded
    val data: NotificationData,

    @Relation(entityColumn = "ownerId", parentColumn = "actionUserId")
    val actionByUser: ProfileData?,

    @Relation(entityColumn = "postId", parentColumn = "actionPostId")
    val actionPost: PostData? = null,

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionPostStats: EventStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionPostUserStats: EventUserStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionPostUris: List<EventUri> = emptyList(),

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionPostNostrUris: List<EventUriNostr> = emptyList(),
)
