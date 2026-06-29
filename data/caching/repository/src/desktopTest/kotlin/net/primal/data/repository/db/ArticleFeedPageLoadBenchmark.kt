package net.primal.data.repository.db

import androidx.paging.PagingSource
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import net.primal.data.local.dao.reads.Article
import net.primal.data.local.db.PrimalDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Measures a single article (reads) feed page load through Room.
 *
 * The benchmark calls `ArticleDao.feed` and drives one [PagingSource] refresh of [INITIAL_LOAD_SIZE]
 * rows — the call the article feed makes on first load. Each returned [Article] carries its full
 * @Relation graph (author, event stats, zaps, bookmark, highlights), so the timing includes the
 * relation fan-out and the JSON/TypeConverter decoding, where most of the cost is.
 *
 * The benchmark reads the snapshot as-is and reports the median load time; it applies no schema or
 * query changes of its own. To evaluate a change, run the benchmark, make the change, then run it
 * again against the same snapshot and compare. The feed is selected deterministically (the most
 * populated one in the snapshot), so both runs measure the same data.
 *
 * Opt-in: pass `-PprimalDbSnapshot=<path-to-primal_database.db>`. Without it the test no-ops, so it is
 * safe to leave in CI. The snapshot schema must match the current code; Room opens with destructive
 * migration disabled and fails on a mismatch.
 */
class ArticleFeedPageLoadBenchmark {

    private data class Feed(val owner: String, val spec: String, val rows: Int)

    @Test
    fun benchmarkArticleFeedPageLoad() {
        val snapshot = System.getProperty(SNAPSHOT_PROPERTY)?.let(::File)
        if (snapshot == null || !snapshot.exists()) {
            println("[ArticleFeedPageLoadBenchmark] skipped — set -PprimalDbSnapshot=<path> to run.")
            return
        }

        val feed = discoverFeed(snapshot)
        if (feed == null) {
            println("[ArticleFeedPageLoadBenchmark] no article feed rows in snapshot.")
            return
        }

        val db = openDatabase(snapshot)
        try {
            val first = runBlocking { loadPage(db, feed) }
            val stats = bench { runBlocking { loadPage(db, feed) } }
            println("\n[article feed] feedSpec=${feed.spec}")
            println(
                "  loaded ${first.size} of ${feed.rows} rows, ${first.count { it.author != null }} authors, " +
                    "${first.sumOf { it.eventZaps.size }} zaps, ${first.sumOf { it.highlights.size }} highlights",
            )
            println("  ${stats.format()}")
        } finally {
            db.close()
        }
    }

    /** Drives one Refresh page like Paging does: a fresh PagingSource, loadSize = initial load. */
    private suspend fun loadPage(db: PrimalDatabase, feed: Feed): List<Article> {
        val source = db.articles().feed(spec = feed.spec, userId = feed.owner)
        val params = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = INITIAL_LOAD_SIZE,
            placeholdersEnabled = true,
        )
        return (source.load(params) as? PagingSource.LoadResult.Page)?.data ?: emptyList()
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
    private fun openDatabase(snapshot: File): PrimalDatabase {
        val dbName = "$DB_NAME_PREFIX.db"
        val tmp = File(System.getProperty("java.io.tmpdir"), dbName)
        snapshot.copyTo(tmp, overwrite = true)
        listOf("-wal", "-shm").forEach { ext ->
            val sib = File(snapshot.parentFile, snapshot.name + ext)
            if (sib.exists()) sib.copyTo(File(tmp.parentFile, dbName + ext), overwrite = true)
        }
        return LocalDatabaseFactory.createDatabase<PrimalDatabase>(databaseName = dbName)
    }

    /** The most populated (owner, spec) article feed in the snapshot. The column is `spec`. */
    private fun discoverFeed(snapshot: File): Feed? {
        val connection = BundledSQLiteDriver().open(snapshot.absolutePath)
        val s = connection.prepare(
            "SELECT ownerId, spec, COUNT(*) c FROM ArticleFeedCrossRef " +
                "GROUP BY ownerId, spec ORDER BY c DESC LIMIT 1",
        )
        return try {
            if (s.step()) Feed(s.getText(0), s.getText(1), s.getLong(2).toInt()) else null
        } finally {
            s.close()
            connection.close()
        }
    }

    private companion object {
        const val SNAPSHOT_PROPERTY = "primal.db.snapshot"
        const val DB_NAME_PREFIX = "primal_articlefeed_bench"
        const val INITIAL_LOAD_SIZE = 125
        const val WARMUP = 20
        const val MEASURE = 100
    }
}
