package net.primal.data.repository.mappers.remote

import net.primal.data.remote.api.feeds.model.AdvancedSearchQueryResponse
import net.primal.domain.feeds.AdvancedSearchParsedQuery

fun AdvancedSearchQueryResponse.asAdvancedSearchParsedQuery(): AdvancedSearchParsedQuery {
    return AdvancedSearchParsedQuery(
        includes = this.includes,
        excludes = this.excludes,
        hashtags = this.hashtags,
        kind = this.kind,
        postedBy = this.postedBy,
        replyingTo = this.replingTo,
        zappedBy = this.zappedBy,
        timeframe = this.timeframe,
        customTimeframeSince = this.customTimeframe.since,
        customTimeframeUntil = this.customTimeframe.until,
        scope = this.scope,
        sortBy = this.sortBy,
        orientation = this.orientation,
        minWords = this.minWords,
        maxWords = this.maxWords,
        minDuration = this.minDuration,
        maxDuration = this.maxDuration,
        minScore = this.minScore,
        minInteractions = this.minInteractions,
        minLikes = this.minLikes,
        minZaps = this.minZaps,
        minReplies = this.minReplies,
        minReposts = this.minReposts,
        following = this.following,
        userMentions = this.userMentions,
        sentiment = this.sentiment,
    )
}
