package net.primal.data.repository.db

import java.io.File
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.primal.core.utils.asMapByKey
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.remote.api.feed.model.FeedResponse
import net.primal.data.remote.mapper.flatMapNotNullAsCdnResource
import net.primal.data.remote.mapper.flatMapNotNullAsLinkPreviewResource
import net.primal.data.remote.mapper.flatMapNotNullAsVideoThumbnailsMap
import net.primal.data.remote.mapper.mapAsMapPubkeyToListOfBlossomServers
import net.primal.data.repository.feed.processors.persistToDatabaseAsTransaction
import net.primal.data.repository.mappers.remote.applyPollStats
import net.primal.data.repository.mappers.remote.flatMapAsEventHintsPO
import net.primal.data.repository.mappers.remote.flatMapPostsAsEventUriPO
import net.primal.data.repository.mappers.remote.flatMapPostsAsReferencedNostrUriDO
import net.primal.data.repository.mappers.remote.mapAsEventZapDO
import net.primal.data.repository.mappers.remote.mapAsPollResponseVotes
import net.primal.data.repository.mappers.remote.mapAsPostDataPO
import net.primal.data.repository.mappers.remote.mapAsProfileDataPO
import net.primal.data.repository.mappers.remote.mapAsZapPollVotes
import net.primal.data.repository.mappers.remote.mapNotNullAsArticleDataPO
import net.primal.data.repository.mappers.remote.mapNotNullAsEventStatsPO
import net.primal.data.repository.mappers.remote.mapNotNullAsEventUserStatsPO
import net.primal.data.repository.mappers.remote.mapNotNullAsPollDataPO
import net.primal.data.repository.mappers.remote.mapNotNullAsPostDataPO
import net.primal.data.repository.mappers.remote.mapNotNullAsRepostDataPO
import net.primal.data.repository.mappers.remote.mapNotNullAsStreamDataPO
import net.primal.data.repository.mappers.remote.mapReferencedEventsAsArticleDataPO
import net.primal.data.repository.mappers.remote.mapReferencedEventsAsHighlightDataPO
import net.primal.data.repository.mappers.remote.mapReferencedNostrUriAsEventUriNostrPO
import net.primal.data.repository.mappers.remote.parseAndMapPrimalLegendProfiles
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPollStats
import net.primal.data.repository.mappers.remote.parseAndMapPrimalPremiumInfo
import net.primal.data.repository.mappers.remote.parseAndMapPrimalUserNames
import net.primal.domain.common.PrimalEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Repeatable benchmark for the note-feed PERSIST MAPPING cost — the API-events -> DB-entities transform
 * plus the Room write — which on-device profiling identified as the binding constraint for feed
 * first-paint (~340-770 ms/page; the read/paint path is cheap).
 *
 * Runs against a fat fixture of REAL captured events (`feed_persist_fixture.json`, reconstructed verbatim
 * from device socket frames) and a FRESH Room [PrimalDatabase] rebuilt every iteration (cold first-load,
 * no upsert short-circuiting or DB-growth skew).
 *
 * Two views, both opt-in via `-PpersistBench`:
 *   - [benchmarkPersistMapping]  : the whole production path [FeedResponse.persistToDatabaseAsTransaction]
 *                                  (mapping + Room write) — the top-line A/B number.
 *   - [benchmarkMappingBreakdown]: a per-group breakdown of ONLY the mapping phase (no DB write), mirroring
 *                                  `FeedResponseProcessor.persistToDatabase` (lines 48-130) so each group maps
 *                                  1:1 to something an optimizer would touch. Reports median ms + µs/item per
 *                                  group, so you can see exactly which transform moved.
 *
 * NOTE: desktop-JVM wall-clock — absolute ms are much faster than the emulator (no ART, JIT-warmed, native
 * CPU). Use for RELATIVE A/B (run, change, re-run, compare), NOT to verify the on-device 0.5s budget.
 *
 *   ./gradlew :data:caching:repository:desktopTest --tests "*FeedPersistMappingBenchmark*" -PpersistBench
 *
 * KEEP IN SYNC: the mapping mirror below is a copy of `FeedResponseProcessor.persistToDatabase`'s mapping
 * phase. It calls the real mapper functions (so per-group timings are real); only the orchestration is
 * duplicated. If that function's structure changes, update the mirror.
 */
class FeedPersistMappingBenchmark {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // ---------------------------------------------------------------------------------------------
    // View 1 — whole production persist (mapping + Room write)
    // ---------------------------------------------------------------------------------------------

    @Test
    fun benchmarkPersistMapping() {
        if (skip()) return
        val response = loadFeedResponse()
        val total = response.totalEventCount()
        println("\n[FeedPersistMappingBenchmark] fixture: $total events")
        println(
            "  notes=${response.notes.size} profiles=${response.metadata.size} zaps=${response.zaps.size} " +
                "stats=${response.primalEventStats.size} cdn=${response.cdnResources.size} " +
                "refPosts=${response.referencedEvents.size} blossom=${response.blossomServers.size}",
        )

        repeat(WARMUP) { runFreshPersist(response) }
        val samples = DoubleArray(MEASURE) { runFreshPersist(response) }
        samples.sort()

        println("\n  WHOLE persist (mapping + Room write), fresh DB each run:")
        println(
            "    median %.1f ms  (min %.1f, p90 %.1f) over %d runs"
                .format(samples[MEASURE / 2], samples.first(), samples[(MEASURE * 9) / 10], MEASURE),
        )
        println("    normalized: %.1f ms / 1000 events\n".format(samples[MEASURE / 2] / total * 1000))
        printDesktopCaveat()
    }

    // ---------------------------------------------------------------------------------------------
    // View 2 — per-group breakdown of the MAPPING phase only (no DB write)
    // ---------------------------------------------------------------------------------------------

    @Test
    fun benchmarkMappingBreakdown() {
        if (skip()) return
        val response = loadFeedResponse()

        val name = "primal_persist_breakdown.db"
        deleteDbFiles(name)
        val database = LocalDatabaseFactory.createDatabase<PrimalDatabase>(databaseName = name)
        val steps = mutableListOf<Step>()
        try {
            runMappingMirror(response, database, steps)
        } finally {
            database.close()
            deleteDbFiles(name)
        }

        val mappingSum = steps.sumOf { it.medianMs }
        // Comparable whole-persist total (mapping + write) for the derived write share.
        val whole = DoubleArray(
            WARMUP + MEASURE,
        ) { runFreshPersist(response) }.also { it.sort() }[(WARMUP + MEASURE) / 2]

        println("\n[FeedPersistMappingBenchmark] MAPPING breakdown (${response.totalEventCount()} events, no DB write)")
        println("  %-26s %7s %11s %13s".format("group (transform)", "items", "median ms", "µs / item"))
        println("  " + "-".repeat(60))
        steps.sortedByDescending { it.medianMs }.forEach { s ->
            val usPerItem = if (s.items > 0) s.medianMs * 1000.0 / s.items else 0.0
            println("  %-26s %7d %11.3f %13.1f".format(s.label, s.items, s.medianMs, usPerItem))
        }
        println("  " + "-".repeat(60))
        println("  %-26s %7s %11.3f".format("MAPPING TOTAL (sum)", "", mappingSum))
        println(
            "\n  whole persist (map+write) median: %.1f ms  ->  write+txn ≈ %.1f ms (derived)"
                .format(whole, (whole - mappingSum).coerceAtLeast(0.0)),
        )
        println()
        printDesktopCaveat()
    }

    /**
     * Faithful mirror of `FeedResponseProcessor.persistToDatabase` mapping phase (lines 48-130), with each
     * logical group wrapped in [timed]. Calls the REAL mapper functions; only orchestration is duplicated.
     */
    @Suppress("LongMethod") // intentional: faithful mirror of the production mapping phase, one timed group per step.
    private fun runMappingMirror(
        response: FeedResponse,
        database: PrimalDatabase,
        steps: MutableList<Step>,
    ) {
        fun <T> timed(
            label: String,
            items: Int,
            block: () -> T,
        ): T {
            repeat(STEP_WARMUP) { block() }
            val s = DoubleArray(STEP_MEASURE)
            for (i in 0 until STEP_MEASURE) {
                val t = System.nanoTime()
                block()
                s[i] = (System.nanoTime() - t) / 1_000_000.0
            }
            s.sort()
            steps += Step(label, items, s[STEP_MEASURE / 2])
            return block()
        }

        with(response) {
            val cdn = timed("cdn_media_hints", cdnResources.size) {
                Triple(
                    cdnResources.flatMapNotNullAsCdnResource().asMapByKey { it.url },
                    cdnResources.flatMapNotNullAsVideoThumbnailsMap(),
                    primalLinkPreviews.flatMapNotNullAsLinkPreviewResource().asMapByKey { it.url },
                )
            }
            val cdnResources = cdn.first
            val videoThumbnails = cdn.second
            val linkPreviews = cdn.third
            timed("event_relay_hints", primalRelayHints.size) { primalRelayHints.flatMapAsEventHintsPO() }

            val arts = timed("articles_highlights", articles.size + referencedEvents.size) {
                val mappedArticles = articles.mapNotNullAsArticleDataPO(cdnResources = cdnResources)
                val referencedArticles = referencedEvents.mapReferencedEventsAsArticleDataPO(
                    cdnResources = cdnResources,
                )
                val referencedHighlights = referencedEvents.mapReferencedEventsAsHighlightDataPO()
                referencedHighlights to (mappedArticles + referencedArticles)
            }
            val referencedHighlights = arts.first
            val allArticles = arts.second

            val refPosts = timed("referenced_posts", referencedEvents.size) {
                val withoutReplyTo = referencedEvents.mapNotNullAsPostDataPO()
                val withReplyTo = referencedEvents.mapNotNullAsPostDataPO(
                    referencedPosts = withoutReplyTo,
                    referencedArticles = allArticles,
                    referencedHighlights = referencedHighlights,
                )
                withReplyTo
            }
            val feedPosts = timed("feed_posts_map", notes.size + polls.size) {
                (notes + polls).mapAsPostDataPO(
                    referencedPosts = refPosts,
                    referencedArticles = allArticles,
                    referencedHighlights = referencedHighlights,
                )
            }

            val meta = timed("primal_meta_parse", 3) {
                Triple(
                    primalUserNames.parseAndMapPrimalUserNames(),
                    primalPremiumInfo.parseAndMapPrimalPremiumInfo(),
                    primalLegendProfiles.parseAndMapPrimalLegendProfiles(),
                )
            }
            val existingLegend = timed("legend_profiles_db_read", metadata.size) {
                runBlocking {
                    database.profiles().findLegendProfileData(profileIds = metadata.map { it.pubKey })
                        .mapNotNull { it.value?.legendProfile?.let { v -> it.key to v } }
                        .toMap()
                }
            }

            val prof = timed("profiles_map", metadata.size) {
                val blossomServers = blossomServers.mapAsMapPubkeyToListOfBlossomServers()
                val profiles = metadata.mapAsProfileDataPO(
                    cdnResourcesMap = cdnResources,
                    primalUserNames = meta.first,
                    primalPremiumInfo = meta.second,
                    primalLegendProfiles = meta.third + existingLegend,
                    blossomServers = blossomServers,
                )
                profiles
            }
            val profileIdToProfileDataMap = prof.asMapByKey { it.ownerId }
            val eventIdMap = profileIdToProfileDataMap.mapValues { it.value.eventId }

            val allPosts = timed("all_posts_author_meta", refPosts.size + feedPosts.size) {
                (refPosts + feedPosts).map { it.copy(authorMetadataId = eventIdMap[it.authorId]) }
            }

            timed("note_attachments", allPosts.size) {
                allPosts.flatMapPostsAsEventUriPO(
                    cdnResources = cdnResources,
                    linkPreviews = linkPreviews,
                    videoThumbnails = videoThumbnails,
                )
            }

            val refEvents = timed("ref_events_decode", referencedEvents.size) {
                referencedEvents.mapNotNull { it.content.decodeFromJsonStringOrNull<NostrEvent>() }
            }
            val streamData = timed("stream_data", liveActivity.size + refEvents.size) {
                liveActivity.mapNotNullAsStreamDataPO() + refEvents.mapNotNullAsStreamDataPO()
            }

            val pollData = timed("poll_data", polls.size + refEvents.size) {
                val pollStatsMap = primalPollStats.parseAndMapPrimalPollStats()
                val allPollData = (polls + refEvents).mapNotNullAsPollDataPO()
                allPollData.filter { it.postId in pollStatsMap }.applyPollStats(pollStatsMap) +
                    allPollData.filter { it.postId !in pollStatsMap }
            }
            timed("poll_votes", pollResponses.size + zaps.size) {
                pollResponses.mapAsPollResponseVotes() + zaps.mapAsZapPollVotes()
            }

            timed("event_zaps", zaps.size) { zaps.mapAsEventZapDO(profilesMap = prof.associateBy { it.ownerId }) }
            timed("reposts", reposts.size) { reposts.mapNotNullAsRepostDataPO() }
            timed("event_stats", primalEventStats.size) { primalEventStats.mapNotNullAsEventStatsPO() }
            val userPostStats = timed("event_user_stats", primalEventUserStats.size) {
                primalEventUserStats.mapNotNullAsEventUserStatsPO(userId = USER_ID)
            }
            val userVotedOptionMap = userPostStats
                .filter { it.votedForOption != null }
                .associate { it.eventId to it.votedForOption }

            timed("note_nostr_uris", allPosts.size) {
                allPosts.flatMapPostsAsReferencedNostrUriDO(
                    eventIdToNostrEvent = refEvents.associateBy { it.id },
                    postIdToPostDataMap = allPosts.associateBy { it.postId },
                    articleIdToArticle = allArticles.associateBy { it.articleId },
                    streamIdToStreamData = streamData.associateBy { it.dTag },
                    profileIdToProfileDataMap = profileIdToProfileDataMap,
                    cdnResources = cdnResources,
                    videoThumbnails = videoThumbnails,
                    linkPreviews = linkPreviews,
                    postIdToPollDataMap = pollData.associateBy { it.postId },
                    postIdToUserVotedOption = userVotedOptionMap,
                ).mapReferencedNostrUriAsEventUriNostrPO()
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // shared helpers
    // ---------------------------------------------------------------------------------------------

    private data class Step(val label: String, val items: Int, val medianMs: Double)

    private fun skip(): Boolean {
        if (System.getProperty(BENCH_PROPERTY) == null) {
            println("[FeedPersistMappingBenchmark] skipped — pass -PpersistBench to run.")
            return true
        }
        return false
    }

    private fun printDesktopCaveat() {
        println(
            "  NOTE: desktop-JVM wall-clock — absolute ms are much faster than the emulator (no ART, JIT, " +
                "native CPU). Use for RELATIVE A/B of mapping changes, NOT to verify the on-device 0.5s budget.\n",
        )
    }

    /** Builds a fresh DB, times ONLY the persist call, tears the DB down. Returns elapsed ms. */
    private fun runFreshPersist(response: FeedResponse): Double {
        val name = "primal_persist_bench_${counter++}.db"
        deleteDbFiles(name)
        val db = LocalDatabaseFactory.createDatabase<PrimalDatabase>(databaseName = name)
        return try {
            val start = System.nanoTime()
            runBlocking { response.persistToDatabaseAsTransaction(userId = USER_ID, database = db) }
            (System.nanoTime() - start) / 1_000_000.0
        } finally {
            db.close()
            deleteDbFiles(name)
        }
    }

    private fun loadFeedResponse(): FeedResponse {
        val text = javaClass.getResourceAsStream(FIXTURE)?.bufferedReader()?.use { it.readText() }
            ?: error("fixture $FIXTURE not found on desktopTest classpath")
        val root = json.parseToJsonElement(text).jsonObject

        fun nostr(field: String): List<NostrEvent> =
            root[field]?.let { json.decodeFromJsonElement<List<NostrEvent>>(it) } ?: emptyList()

        fun primal(field: String): List<PrimalEvent> =
            root[field]?.let { json.decodeFromJsonElement<List<PrimalEvent>>(it) } ?: emptyList()

        fun primalOne(field: String): PrimalEvent? =
            root[field]?.takeUnless { it is JsonNull }?.let { json.decodeFromJsonElement<PrimalEvent>(it) }

        return FeedResponse(
            paging = null,
            metadata = nostr("metadata"),
            notes = nostr("notes"),
            articles = nostr("articles"),
            reposts = nostr("reposts"),
            zaps = nostr("zaps"),
            referencedEvents = primal("referencedEvents"),
            primalEventStats = primal("primalEventStats"),
            primalEventUserStats = primal("primalEventUserStats"),
            cdnResources = primal("cdnResources"),
            primalLinkPreviews = primal("primalLinkPreviews"),
            primalRelayHints = primal("primalRelayHints"),
            blossomServers = nostr("blossomServers"),
            primalUserNames = primalOne("primalUserNames"),
            primalLegendProfiles = primalOne("primalLegendProfiles"),
            primalPremiumInfo = primalOne("primalPremiumInfo"),
            genericReposts = nostr("genericReposts"),
            pictureNotes = nostr("pictureNotes"),
            polls = nostr("polls"),
            pollResponses = nostr("pollResponses"),
            primalPollStats = primal("primalPollStats"),
            liveActivity = nostr("liveActivity"),
        )
    }

    private fun FeedResponse.totalEventCount(): Int =
        metadata.size + notes.size + articles.size + reposts.size + zaps.size + referencedEvents.size +
            primalEventStats.size + primalEventUserStats.size + cdnResources.size + primalLinkPreviews.size +
            primalRelayHints.size + blossomServers.size + genericReposts.size + pictureNotes.size +
            polls.size + pollResponses.size + primalPollStats.size + liveActivity.size +
            listOfNotNull(primalUserNames, primalLegendProfiles, primalPremiumInfo).size

    private fun deleteDbFiles(name: String) {
        val dir = File(System.getProperty("java.io.tmpdir"))
        listOf("", "-wal", "-shm").forEach { File(dir, name + it).delete() }
    }

    private var counter = 0

    private companion object {
        const val BENCH_PROPERTY = "primal.persist.bench"
        const val FIXTURE = "/feed_persist_fixture.json"
        const val USER_ID = "88cc134b1a65f54ef48acc1df3665075cb1e0f37f39e894e5a9c1a7688e4f0c8"
        const val WARMUP = 5
        const val MEASURE = 20
        const val STEP_WARMUP = 5
        const val STEP_MEASURE = 40
    }
}
