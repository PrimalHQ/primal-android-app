package net.primal.android.notifications.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.events.db.EventStats
import net.primal.android.events.db.EventUri
import net.primal.android.events.db.EventUriNostr
import net.primal.android.events.db.EventUserStats
import net.primal.android.notes.db.PostData
import net.primal.android.profile.db.ProfileData

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
