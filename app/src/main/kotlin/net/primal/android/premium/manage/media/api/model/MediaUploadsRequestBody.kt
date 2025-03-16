package net.primal.android.premium.manage.media.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.domain.nostr.NostrEvent

@Serializable
data class MediaUploadsRequestBody(
    @SerialName("event_from_user") val eventFromUser: NostrEvent,
    @SerialName("limit") val limit: Int? = null,
    @SerialName("until") val until: Long? = null,
    @SerialName("since") val since: Long? = null,
    @SerialName("offset") val offset: Long? = null,
)
