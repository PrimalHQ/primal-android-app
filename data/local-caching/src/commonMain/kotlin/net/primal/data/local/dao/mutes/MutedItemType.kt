package net.primal.data.local.dao.mutes

import kotlinx.serialization.Serializable

@Serializable
enum class MutedItemType {
    User,
    Hashtag,
    Word,
    Thread,
}
