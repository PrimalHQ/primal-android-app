package net.primal.data.repository.mappers.remote

import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.domain.model.FeedPageSnapshot

internal fun FeedResponse.asFeedPageSnapshot(): FeedPageSnapshot {
    return FeedPageSnapshot(
        paging = this.paging,
        metadata = this.metadata,
        notes = this.notes,
        articles = this.articles,
        reposts = this.reposts,
        zaps = this.zaps,
        referencedEvents = this.referencedEvents,
        primalEventStats = this.primalEventStats,
        primalEventUserStats = this.primalEventUserStats,
        cdnResources = this.cdnResources,
        primalLinkPreviews = this.primalLinkPreviews,
        primalRelayHints = this.primalRelayHints,
        blossomServers = this.blossomServers,
        primalUserNames = this.primalUserNames,
        primalLegendProfiles = this.primalLegendProfiles,
        primalPremiumInfo = this.primalPremiumInfo,
        genericReposts = this.genericReposts,
        pictureNotes = this.pictureNotes,
    )
}

internal fun FeedPageSnapshot.asFeedResponse(): FeedResponse {
    return FeedResponse(
        paging = this.paging,
        metadata = this.metadata,
        notes = this.notes,
        articles = this.articles,
        reposts = this.reposts,
        zaps = this.zaps,
        referencedEvents = this.referencedEvents,
        primalEventStats = this.primalEventStats,
        primalEventUserStats = this.primalEventUserStats,
        cdnResources = this.cdnResources,
        primalLinkPreviews = this.primalLinkPreviews,
        primalRelayHints = this.primalRelayHints,
        blossomServers = this.blossomServers,
        primalUserNames = this.primalUserNames,
        primalLegendProfiles = this.primalLegendProfiles,
        primalPremiumInfo = this.primalPremiumInfo,
        genericReposts = this.genericReposts,
        pictureNotes = this.pictureNotes,
    )
}
