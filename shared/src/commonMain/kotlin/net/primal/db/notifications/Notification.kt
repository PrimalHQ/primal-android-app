package net.primal.db.notifications

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.db.events.EventStats
import net.primal.db.events.EventUri
import net.primal.db.events.EventUriNostr
import net.primal.db.events.EventUserStats
import net.primal.db.notes.PostData
import net.primal.db.profiles.ProfileData

data class Notification(
    @Embedded
    val data: NotificationData,

    @Relation(entityColumn = "ownerId", parentColumn = "actionUserId")
    val actionByUser: ProfileData?,

    @Relation(entityColumn = "postId", parentColumn = "actionPostId")
    val actionPost: PostData? = null,

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionEventStats: EventStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionPostUserStats: EventUserStats? = null,

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionPostUris: List<EventUri> = emptyList(),

    @Relation(entityColumn = "eventId", parentColumn = "actionPostId")
    val actionPostNostrUris: List<EventUriNostr> = emptyList(),
)
