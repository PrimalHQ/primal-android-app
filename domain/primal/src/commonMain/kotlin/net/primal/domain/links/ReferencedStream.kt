package net.primal.domain.links

import kotlinx.serialization.Serializable
import net.primal.domain.membership.PrimalLegendProfile
import net.primal.domain.streams.StreamStatus

@Serializable
data class ReferencedStream(
    val naddr: String,
    val title: String?,
    val currentParticipants: Int?,
    val totalParticipants: Int?,
    val startedAt: Long?,
    val endedAt: Long?,
    val status: StreamStatus,
    val mainHostId: String,
    val mainHostName: String,
    val mainHostIsLive: Boolean,
    val mainHostAvatarCdnImage: CdnImage?,
    val mainHostLegendProfile: PrimalLegendProfile?,
    val mainHostInternetIdentifier: String?,
) {
    val duration
        get() = startedAt?.let { started ->
            endedAt?.let { ended -> ended - started }
        }
}
