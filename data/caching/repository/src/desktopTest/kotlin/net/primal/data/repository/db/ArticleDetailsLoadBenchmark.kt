package net.primal.data.repository.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.primal.data.local.dao.reads.Article
import net.primal.data.local.db.CachingDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Measures opening an article-details screen through Room.
 *
 * The benchmark calls `ArticleDao.observeArticle(articleId, authorId)` — the read the details screen
 * observes — and times resolving the first emission. The returned [Article] carries its full @Relation
 * graph (author, event stats, zaps, bookmark, highlights), so the timing includes the relation fan-out
 * and the JSON/TypeConverter decoding. The comments shown on the same screen load through a separate
 * thread-conversation read and are measured by [NoteThreadLoadBenchmark]; this benchmark covers the
 * article document itself.
 *
 * The benchmark reads the snapshot as-is and reports the median load time; it applies no schema or
 * query changes of its own. To evaluate a change, run the benchmark, make the change, then run it
 * again against the same snapshot and compare. The article is selected deterministically (the one with
 * the most highlights, falling back to the longest by content), so both runs measure the same row.
 *
 * Opt-in: pass `-PprimalDbSnapshot=<path-to-primal_database.db>`. Without it the test no-ops, so it is
 * safe to leave in CI. The snapshot schema must match the current code; Room opens with destructive
 * migration disabled and fails on a mismatch.
 */
class ArticleDetailsLoadBenchmark {

    private data class Target(val articleId: String, val authorId: String)

    @Test
    fun benchmarkArticleDetailsLoad() {
        val snapshot = System.getProperty(SNAPSHOT_PROPERTY)?.let(::File)
        if (snapshot == null || !snapshot.exists()) {
            println("[ArticleDetailsLoadBenchmark] skipped — set -PprimalDbSnapshot=<path> to run.")
            return
        }

        val target = discoverArticle(snapshot)
        if (target == null) {
            println("[ArticleDetailsLoadBenchmark] no article rows in snapshot.")
            return
        }

        val db = openDatabase(snapshot)
        try {
            val first = runBlocking { loadArticle(db, target) }
            val stats = bench { runBlocking { loadArticle(db, target) } }
            println("\n[article details] articleId=${target.articleId.take(16)}… author=${target.authorId.take(12)}…")
            println(
                "  found=${first != null}, author=${first?.author != null}, " +
                    "${first?.eventZaps?.size ?: 0} zaps, ${first?.highlights?.size ?: 0} highlights",
            )
            println("  ${stats.format()}")
        } finally {
            db.close()
        }
    }

    private suspend fun loadArticle(db: CachingDatabase, target: Target): Article? =
        db.articles().observeArticle(articleId = target.articleId, authorId = target.authorId).first()

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

    /** The most-highlighted article (heaviest fan-out); falls back to the longest by content. */
    private fun discoverArticle(snapshot: File): Target? {
        val connection = BundledSQLiteDriver().open(snapshot.absolutePath)
        return try {
            connection.queryTarget(
                "SELECT a.articleId, a.authorId FROM ArticleData a " +
                    "JOIN (SELECT referencedEventATag, COUNT(*) c FROM HighlightData " +
                    "WHERE referencedEventATag IS NOT NULL GROUP BY referencedEventATag ORDER BY c DESC LIMIT 1) h " +
                    "ON a.aTag = h.referencedEventATag LIMIT 1",
            ) ?: connection.queryTarget(
                "SELECT articleId, authorId FROM ArticleData ORDER BY LENGTH(content) DESC LIMIT 1",
            )
        } finally {
            connection.close()
        }
    }

    private fun SQLiteConnection.queryTarget(sql: String): Target? {
        val s = prepare(sql)
        return try {
            if (s.step()) Target(articleId = s.getText(0), authorId = s.getText(1)) else null
        } finally {
            s.close()
        }
    }

    private companion object {
        const val SNAPSHOT_PROPERTY = "primal.db.snapshot"
        const val DB_NAME_PREFIX = "primal_articledetails_bench"
        const val WARMUP = 20
        const val MEASURE = 100
    }
}
