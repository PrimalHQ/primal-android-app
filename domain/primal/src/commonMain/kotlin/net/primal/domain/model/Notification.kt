package net.primal.domain.model

import net.primal.domain.NotificationType

data class Notification(
    val ownerId: String,
    val createdAt: Long,
    val type: NotificationType,
    val seenGloballyAt: Long? = null,
    val actionUserId: String? = null,
    val actionPostId: String? = null,
    val satsZapped: Long? = null,
    val actionByUser: ProfileData?,
    val actionOnPost: FeedPost?,
)
