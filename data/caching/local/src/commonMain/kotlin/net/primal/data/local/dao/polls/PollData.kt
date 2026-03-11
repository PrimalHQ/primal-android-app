package net.primal.data.local.dao.polls

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PollData(
    @PrimaryKey
    val postId: String,
    val authorId: String,
    val zapRecipientId: String? = null,
    val pollType: PollType,
    val endsAt: Long? = null,
    val valueMinimum: Long? = null,
    val valueMaximum: Long? = null,
    val options: List<PollOption>,
)
