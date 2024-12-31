package net.primal.android.articles.api.mediator

import androidx.room.withTransaction
import net.primal.android.articles.api.model.ArticleResponse
import net.primal.android.core.ext.asMapByKey
import net.primal.android.db.PrimalDatabase
import net.primal.android.nostr.db.eventRelayHintsUpserter
import net.primal.android.nostr.ext.flatMapAsEventHintsPO
import net.primal.android.nostr.ext.flatMapAsWordCount
import net.primal.android.nostr.ext.flatMapNotNullAsCdnResource
import net.primal.android.nostr.ext.mapAsEventZapDO
import net.primal.android.nostr.ext.mapAsMapPubkeyToListOfBlossomServers
import net.primal.android.nostr.ext.mapAsPostDataPO
import net.primal.android.nostr.ext.mapAsProfileDataPO
import net.primal.android.nostr.ext.mapNotNullAsArticleDataPO
import net.primal.android.nostr.ext.mapNotNullAsEventStatsPO
import net.primal.android.nostr.ext.mapNotNullAsEventUserStatsPO
import net.primal.android.nostr.ext.mapNotNullAsPostDataPO
import net.primal.android.nostr.ext.mapReferencedEventsAsHighlightDataPO
import net.primal.android.nostr.ext.parseAndMapPrimalLegendProfiles
import net.primal.android.nostr.ext.parseAndMapPrimalPremiumInfo
import net.primal.android.nostr.ext.parseAndMapPrimalUserNames
import net.primal.android.thread.db.ArticleCommentCrossRef

suspend fun ArticleResponse.persistToDatabaseAsTransaction(userId: String, database: PrimalDatabase) {
    val cdnResources = this.cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url }
    val eventHints = this.primalRelayHints.flatMapAsEventHintsPO()
    val wordsCountMap = this.primalLongFormWords.flatMapAsWordCount()

    val primalUserNames = this.primalUserNames.parseAndMapPrimalUserNames()
    val primalPremiumInfo = this.primalPremiumInfo.parseAndMapPrimalPremiumInfo()
    val primalLegendProfiles = this.primalLegendProfiles.parseAndMapPrimalLegendProfiles()

    val blossomServers = this.blossomServers.mapAsMapPubkeyToListOfBlossomServers()

    val profiles = this.metadata.mapAsProfileDataPO(
        cdnResources = cdnResources,
        primalUserNames = primalUserNames,
        primalPremiumInfo = primalPremiumInfo,
        primalLegendProfiles = primalLegendProfiles,
        blossomServers = blossomServers,
    )
    val allArticles = this.articles.mapNotNullAsArticleDataPO(
        wordsCountMap = wordsCountMap,
        cdnResources = cdnResources,
    )
    val referencedHighlights = this.referencedEvents.mapReferencedEventsAsHighlightDataPO()
    val referencedPostsWithoutReplyTo = referencedEvents.mapNotNullAsPostDataPO()
    val referencedPostsWithReplyTo = referencedEvents.mapNotNullAsPostDataPO(
        referencedPosts = referencedPostsWithoutReplyTo,
        referencedArticles = allArticles,
        referencedHighlights = referencedHighlights,
    )

    val allNotes = this.notes.mapAsPostDataPO(
        referencedPosts = referencedPostsWithReplyTo,
        referencedArticles = allArticles,
        referencedHighlights = referencedHighlights,
    )

    val eventZaps = this.zaps.mapAsEventZapDO(profilesMap = profiles.associateBy { it.ownerId })
    val eventStats = this.primalEventStats.mapNotNullAsEventStatsPO()
    val eventUserStats = this.primalEventUserStats.mapNotNullAsEventUserStatsPO(userId = userId)

    database.withTransaction {
        database.profiles().insertOrUpdateAll(data = profiles)
        database.posts().upsertAll(data = allNotes + referencedPostsWithReplyTo)
        database.articles().upsertAll(list = allArticles)
        database.eventStats().upsertAll(data = eventStats)
        database.eventUserStats().upsertAll(data = eventUserStats)
        database.eventZaps().upsertAll(data = eventZaps)
        database.highlights().upsertAll(data = referencedHighlights)

        val eventHintsDao = database.eventHints()
        val hintsMap = eventHints.associateBy { it.eventId }
        eventRelayHintsUpserter(dao = eventHintsDao, eventIds = eventHints.map { it.eventId }) {
            copy(relays = hintsMap[this.eventId]?.relays ?: emptyList())
        }
    }
}

suspend fun ArticleResponse.persistArticleCommentsToDatabase(
    articleId: String,
    articleAuthorId: String,
    database: PrimalDatabase,
) {
    val referencedNotes = this.referencedEvents.mapNotNullAsPostDataPO()
    val comments = this.notes.mapAsPostDataPO(referencedPosts = referencedNotes, emptyList(), emptyList())

    database.withTransaction {
        database.threadConversations().connectArticleWithComment(
            data = comments.map {
                ArticleCommentCrossRef(
                    articleId = articleId,
                    articleAuthorId = articleAuthorId,
                    commentNoteId = it.postId,
                )
            },
        )
    }
}
