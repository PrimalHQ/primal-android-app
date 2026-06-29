package net.primal.data.repository.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.test.Test
import net.primal.data.local.queries.ChronologicalFeedWithRepostsQueryBuilder

/**
 * Measures the note feed read path at the SQL level, isolated from Room.
 *
 * It runs the feed query, its COUNT(*) wrapper, and the eventZaps relation query directly on the app's
 * bundled SQLite engine against a real snapshot, timing each and printing its EXPLAIN QUERY PLAN. This
 * complements [FeedPageLoadBenchmark], which measures the same feed through Room (including the relation
 * fan-out and decoding); use this one to see how much of the cost is the SQL itself and what plan the
 * engine chooses.
 *
 * The benchmark reads the snapshot as-is and applies no schema or query changes of its own. To evaluate
 * a change, run it, make the change, then run it again against the same snapshot and compare both the
 * timings and the query plans. The feed is selected deterministically (the most populated one), so both
 * runs measure the same data.
 *
 * Opt-in: pass `-PprimalDbSnapshot=<path-to-primal_database.db>`. Without it the test no-ops, so it is
 * safe to leave in CI. Provide the matching -wal/-shm siblings for a consistent read. Timings are JVM
 * wall-clock medians — relative measurements to compare across runs, not absolute device numbers.
 */
class FeedQueryBenchmark {

    @Test
    fun benchmarkFeedReadPath() {
        val snapshot = System.getProperty(SNAPSHOT_PROPERTY)?.let(::File)
        if (snapshot == null || !snapshot.exists()) {
            println("[FeedQueryBenchmark] skipped — set -P$P_NAME=<path> to run.")
            return
        }

        val connection = openCopy(snapshot)
        try {
            val feed = connection.mostPopulousFeed()
            if (feed == null) {
                println("[FeedQueryBenchmark] no feed rows in snapshot.")
                return
            }
            val (ownerId, feedSpec) = feed
            val pageIds = connection.feedPostIds(ownerId, feedSpec, limit = PAGE_SIZE)
            println("snapshot=${snapshot.name} owner=${ownerId.take(8)}… feedSpec=$feedSpec feedRows=${pageIds.size}")

            val feedSql = ChronologicalFeedWithRepostsQueryBuilder(feedSpec, ownerId, allowMutedThreads = false)
                .feedQuery().sql
            val countSql = "SELECT COUNT(*) FROM ( $feedSql )"
            val zapSql = "SELECT * FROM EventZap WHERE eventId IN (${placeholders(pageIds.size)})"

            println("\n--- EXPLAIN QUERY PLAN ---")
            printPlan("feed", connection.explain(feedSql))
            printPlan("eventZaps", connection.explain(zapSql))

            println("\n--- Timings: median over $MEASURE runs (after $WARMUP warm-up) ---")
            report("feed query (all rows)", bench { connection.runFeed(feedSql, ownerId, feedSpec) })
            report("feed COUNT(*) wrapper", bench { connection.runFeed(countSql, ownerId, feedSpec) })
            report("eventZaps @Relation", bench { connection.runZap(zapSql, pageIds) })
        } finally {
            connection.close()
        }
    }

    // ---- measurement ----

    private data class Stats(val minMs: Double, val medianMs: Double, val p90Ms: Double)

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

    private fun report(label: String, stats: Stats) {
        println(
            "%-24s median %7.3f ms (min %.3f, p90 %.3f)"
                .format(label, stats.medianMs, stats.minMs, stats.p90Ms),
        )
    }

    // ---- query runners (consume every row so real work is timed) ----

    private fun SQLiteConnection.runFeed(
        sql: String,
        owner: String,
        feedSpec: String,
    ): Int {
        val s = prepare(sql)
        return try {
            s.bindFeedParams(owner, feedSpec)
            var rows = 0
            while (s.step()) rows++
            rows
        } finally {
            s.close()
        }
    }

    private fun SQLiteConnection.runZap(sql: String, ids: List<String>): Int {
        val s = prepare(sql)
        return try {
            ids.forEachIndexed { i, id -> s.bindText(i + 1, id) }
            var rows = 0
            while (s.step()) rows++
            rows
        } finally {
            s.close()
        }
    }

    /** Feed query bind order matches ChronologicalFeedWithRepostsQueryBuilder.feedQuery(). */
    private fun SQLiteStatement.bindFeedParams(owner: String, feedSpec: String) {
        bindText(1, owner)
        bindText(2, owner)
        bindText(3, owner)
        bindText(4, feedSpec)
        bindText(5, owner)
        bindLong(6, 0)
        bindText(7, owner)
        bindText(8, owner)
        bindText(9, owner)
        bindText(10, feedSpec)
        bindText(11, owner)
        bindLong(12, 0)
    }

    // ---- setup helpers ----

    private fun openCopy(snapshot: File): SQLiteConnection {
        val dir = File(System.getProperty("java.io.tmpdir"), "primal-db-bench").apply { mkdirs() }
        val target = File(dir, "$DB_NAME.db")
        snapshot.copyTo(target, overwrite = true)
        listOf("-wal", "-shm").forEach { ext ->
            val sib = File(snapshot.parentFile, snapshot.name + ext)
            if (sib.exists()) sib.copyTo(File(dir, target.name + ext), overwrite = true)
        }
        return BundledSQLiteDriver().open(target.absolutePath)
    }

    private fun SQLiteConnection.mostPopulousFeed(): Pair<String, String>? {
        val s = prepare(
            "SELECT ownerId, feedSpec FROM FeedPostDataCrossRef " +
                "GROUP BY ownerId, feedSpec ORDER BY COUNT(*) DESC LIMIT 1",
        )
        return try {
            if (s.step()) s.getText(0) to s.getText(1) else null
        } finally {
            s.close()
        }
    }

    private fun SQLiteConnection.feedPostIds(
        owner: String,
        feedSpec: String,
        limit: Int,
    ): List<String> {
        val s = prepare(
            "SELECT eventId FROM FeedPostDataCrossRef WHERE ownerId = ? AND feedSpec = ? ORDER BY position LIMIT ?",
        )
        return try {
            s.bindText(1, owner)
            s.bindText(2, feedSpec)
            s.bindLong(3, limit.toLong())
            buildList { while (s.step()) add(s.getText(0)) }
        } finally {
            s.close()
        }
    }

    private fun SQLiteConnection.explain(sql: String): List<String> {
        val s = prepare("EXPLAIN QUERY PLAN $sql")
        return try {
            buildList { while (s.step()) add(s.getText(EXPLAIN_DETAIL_COLUMN)) }
        } finally {
            s.close()
        }
    }

    private fun printPlan(label: String, plan: List<String>) {
        val scan = plan.filter { it.startsWith("SCAN") }
        val sort = plan.filter { it.contains("TEMP B-TREE") }
        val flags = buildList {
            if (scan.isNotEmpty()) add("SCAN: ${scan.joinToString()}")
            if (sort.isNotEmpty()) add("TEMP-SORT")
        }.ifEmpty { listOf("index-backed, no sort") }
        println("  $label -> ${flags.joinToString(" ; ")}")
    }

    private fun placeholders(n: Int) = List(n) { "?" }.joinToString(", ")

    private companion object {
        const val P_NAME = "primalDbSnapshot"
        const val SNAPSHOT_PROPERTY = "primal.db.snapshot"
        const val DB_NAME = "primal_feedquery_bench"
        const val EXPLAIN_DETAIL_COLUMN = 3
        const val PAGE_SIZE = 100
        const val WARMUP = 50
        const val MEASURE = 300
    }
}
