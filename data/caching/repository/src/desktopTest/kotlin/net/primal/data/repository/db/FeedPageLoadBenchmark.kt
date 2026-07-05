package net.primal.data.repository.db

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import net.primal.data.local.db.CachingDatabase
import net.primal.data.local.queries.ChronologicalFeedWithRepostsQueryBuilder
import net.primal.data.local.queries.ExploreFeedQueryBuilder
import net.primal.data.local.queries.FeedQueryBuilder
import net.primal.domain.feeds.supportsNoteReposts
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Measures a single note feed page load through Room.
 *
 * The benchmark calls `FeedPostDao.newestFeedPosts` (`@Transaction @RawQuery` → `List<FeedPost>`) — the
 * read a feed page makes — so the timing includes the @Relation fan-out (11 relations per row) and the
 * JSON/TypeConverter decoding, where most of the cost is.
 *
 * It covers both feed query paths, chosen by the predicate the app uses ([supportsNoteReposts],
 * mirroring `FeedRepositoryImpl.feedQueryBuilder`):
 *   - A. [ChronologicalFeedWithRepostsQueryBuilder] — latest/home, profile, authored, replies (PostData ⋃ RepostData).
 *   - B. [ExploreFeedQueryBuilder] — trending, popular, search, bookmarks (single SELECT, INNER JOIN EventStats).
 *
 * The benchmark reads the snapshot as-is and reports the median load time per path; it applies no
 * schema or query changes of its own. To evaluate a change, run the benchmark, make the change, then
 * run it again against the same snapshot and compare. Feeds are selected deterministically (the most
 * populated of each path), so both runs measure the same data.
 *
 * Opt-in: pass `-PprimalDbSnapshot=<path-to-primal_database.db>`. Without it the test no-ops, so it is
 * safe to leave in CI. The snapshot schema must match the current code; Room opens with destructive
 * migration disabled and fails on a mismatch.
 */
class FeedPageLoadBenchmark {

    private data class Feed(val owner: String, val spec: String, val rows: Int)

    @Test
    fun benchmarkFeedPageLoad() {
        val snapshot = System.getProperty(SNAPSHOT_PROPERTY)?.let(::File)
        if (snapshot == null || !snapshot.exists()) {
            println("[FeedPageLoadBenchmark] skipped — set -PprimalDbSnapshot=<path> to run.")
            return
        }

        val feeds = discoverFeeds(snapshot)
        val targets = listOfNotNull(
            feeds.firstOrNull { it.spec.supportsNoteReposts() }?.let { "A chronological" to it },
            feeds.firstOrNull { !it.spec.supportsNoteReposts() }?.let { "B explore/bookmarks" to it },
        )
        if (targets.isEmpty()) {
            println("[FeedPageLoadBenchmark] no feed rows in snapshot.")
            return
        }

        val db = openDatabase(snapshot)
        try {
            targets.forEach { (label, feed) ->
                val first = runBlocking { db.feedPosts().newestFeedPosts(pageQuery(feed)) }
                val stats = bench { runBlocking { db.feedPosts().newestFeedPosts(pageQuery(feed)) } }
                println("\n[$label] feedSpec=${feed.spec}")
                println(
                    "  loaded ${first.size} of ${feed.rows} rows, ${first.count { it.author != null }} authors, " +
                        "${first.sumOf { it.eventZaps.size }} zaps",
                )
                println("  ${stats.format()}")
            }
        } finally {
            db.close()
        }
    }

    /** Selects the builder using the same rule as FeedRepositoryImpl.feedQueryBuilder. */
    private fun pageQuery(feed: Feed) =
        feedQueryBuilder(feed.spec, feed.owner).newestFeedPostsQuery(limit = INITIAL_LOAD_SIZE)

    private fun feedQueryBuilder(feedSpec: String, owner: String): FeedQueryBuilder =
        if (feedSpec.supportsNoteReposts()) {
            ChronologicalFeedWithRepostsQueryBuilder(feedSpec, owner, allowMutedThreads = false)
        } else {
            ExploreFeedQueryBuilder(feedSpec, owner, allowMutedThreads = false)
        }

    private data class Stats(val minMs: Double, val medianMs: Double, val p90Ms: Double) {
        fun format() = "median %.3f ms (min %.3f, p90 %.3f) over $MEASURE runs".format(medianMs, minMs, p90Ms)
    }

    private fun bench(block: () -> Unit): Stats {
        repeat(WARMUP) { block() }
        val samples = LongArray(MEASURE)
        for (i in 0 until MEASURE) {
            val start = System.nanoTime()
            block()
            samples[i] = System.nanoTime() - start
        }
        samples.sort()
        fun ms(ns: Long) = ns / 1_000_000.0
        return Stats(ms(samples.first()), ms(samples[MEASURE / 2]), ms(samples[(MEASURE * 9) / 10]))
    }

    /** Copies the snapshot (and its -wal/-shm siblings) to a temp file and opens it through Room. */
    private fun openDatabase(snapshot: File): CachingDatabase {
        val dbName = "$DB_NAME_PREFIX.db"
        val tmp = File(System.getProperty("java.io.tmpdir"), dbName)
        snapshot.copyTo(tmp, overwrite = true)
        listOf("-wal", "-shm").forEach { ext ->
            val sib = File(snapshot.parentFile, snapshot.name + ext)
            if (sib.exists()) sib.copyTo(File(tmp.parentFile, dbName + ext), overwrite = true)
        }
        return LocalDatabaseFactory.createDatabase<CachingDatabase>(databaseName = dbName)
    }

    /** Most populated (owner, feedSpec) pairs, highest first. */
    private fun discoverFeeds(snapshot: File): List<Feed> {
        val connection = BundledSQLiteDriver().open(snapshot.absolutePath)
        val s = connection.prepare(
            "SELECT ownerId, feedSpec, COUNT(*) c FROM FeedPostDataCrossRef " +
                "GROUP BY ownerId, feedSpec ORDER BY c DESC",
        )
        return try {
            buildList { while (s.step()) add(Feed(s.getText(0), s.getText(1), s.getLong(2).toInt())) }
        } finally {
            s.close()
            connection.close()
        }
    }

    private companion object {
        const val SNAPSHOT_PROPERTY = "primal.db.snapshot"
        const val DB_NAME_PREFIX = "primal_feedpageload_bench"
        const val INITIAL_LOAD_SIZE = 75
        const val WARMUP = 20
        const val MEASURE = 100
    }
}
