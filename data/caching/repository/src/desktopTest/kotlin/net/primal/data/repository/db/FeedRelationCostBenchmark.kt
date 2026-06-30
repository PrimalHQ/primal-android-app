package net.primal.data.repository.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteException
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.test.Test
import kotlinx.serialization.json.Json
import net.primal.core.utils.runCatching
import net.primal.domain.feeds.supportsNoteReposts

/**
 * Per-relation cost breakdown for the note feed aggregate.
 *
 * For a real page of post/author IDs taken from a snapshot, this runs EACH @Relation's child query
 * exactly as Room would (`SELECT * FROM Child WHERE joinCol IN (<page keys>)`), and times two things
 * per relation:
 *   - readAll : run the query + step every row + materialize every column value (SQL + I/O + row read)
 *   - readDecode : the same, plus parse every JSON-looking cell (the TypeConverter cost)
 * The (readDecode − readAll) delta attributes JSON-parse cost; the row count shows fan-out.
 *
 * This answers "of the 11 relations, which is the most expensive?" against real data — complementing
 * FeedPageLoadBenchmark (whole-page Room timing) and FeedQueryBenchmark (main SQL only).
 *
 * Note: 3 relations (author / repostAuthor / replyToAuthor) all hit ProfileData; they are measured as
 * one ProfileData query over the union of {authorIds ∪ replyToAuthorIds} (repostAuthor adds a few more
 * rows not extracted here). The other 8 relations are measured exactly.
 *
 * Opt-in: pass `-PprimalDbSnapshot=<path-to-primal_database.db>` (same wiring as the other benchmarks).
 * Desktop-JVM wall-clock medians — relative numbers, not absolute device timings.
 */
class FeedRelationCostBenchmark {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private data class Relation(val label: String, val table: String, val joinCol: String, val idKind: IdKind)
    private enum class IdKind { POST, AUTHOR, PROFILE }

    private val relations = listOf(
        Relation("uris (EventUri)", "EventUri", "eventId", IdKind.POST),
        Relation("nostrUris (EventUriNostr)", "EventUriNostr", "eventId", IdKind.POST),
        Relation("author/repost/reply (ProfileData)", "ProfileData", "ownerId", IdKind.PROFILE),
        Relation("eventStats (EventStats)", "EventStats", "eventId", IdKind.POST),
        Relation("eventRelayHints (EventRelayHints)", "EventRelayHints", "eventId", IdKind.POST),
        Relation("eventZaps (EventZap)", "EventZap", "eventId", IdKind.POST),
        Relation("bookmark (PublicBookmark)", "PublicBookmark", "tagValue", IdKind.POST),
        Relation("streams (StreamData)", "StreamData", "mainHostId", IdKind.AUTHOR),
        Relation("pollData (PollData)", "PollData", "postId", IdKind.POST),
    )

    @Test
    fun benchmarkRelationCosts() {
        val snapshot = System.getProperty(SNAPSHOT_PROPERTY)?.let(::File)
        if (snapshot == null || !snapshot.exists()) {
            println("[FeedRelationCostBenchmark] skipped — set -PprimalDbSnapshot=<path> to run.")
            return
        }

        val connection = openCopy(snapshot)
        try {
            val feeds = connection.discoverFeeds()
            val targets = listOfNotNull(
                feeds.firstOrNull { it.second.supportsNoteReposts() }?.let { "A. chronological (latest-like)" to it },
                feeds.firstOrNull { !it.second.supportsNoteReposts() }?.let { "B. explore (trending-like)" to it },
            )
            targets.forEach { (label, feed) -> connection.reportFeed(label, feed.first, feed.second) }
        } finally {
            connection.close()
        }
    }

    private fun SQLiteConnection.reportFeed(
        label: String,
        owner: String,
        feedSpec: String,
    ) {
        val postIds = feedPostIds(owner, feedSpec, PAGE_SIZE)
        val authorIds =
            idColumn("SELECT DISTINCT authorId FROM PostData WHERE postId IN (${ph(postIds.size)})", postIds)
        val replyIds = idColumn(
            "SELECT DISTINCT replyToAuthorId FROM PostData WHERE postId IN (${ph(postIds.size)}) " +
                "AND replyToAuthorId IS NOT NULL AND replyToAuthorId != ''",
            postIds,
        )
        val profileIds = (authorIds + replyIds).distinct()

        println("\n==================================================================================")
        println("[$label] feedSpec=$feedSpec")
        println("  page: ${postIds.size} posts, ${authorIds.size} distinct authors, ${replyIds.size} reply-authors")
        println(
            "  %-38s %6s  %10s  %12s  %10s".format(
                "relation (child table)",
                "rows",
                "readAll ms",
                "readDecode ms",
                "decode ms",
            ),
        )

        var totalReadAll = 0.0
        var totalDecode = 0.0
        val ranked = relations.mapNotNull { rel ->
            val ids = when (rel.idKind) {
                IdKind.POST -> postIds
                IdKind.AUTHOR -> authorIds
                IdKind.PROFILE -> profileIds
            }
            if (ids.isEmpty()) {
                println("  %-38s %6s  %10s".format(rel.label, "0", "(no keys)"))
                return@mapNotNull null
            }
            val sql = "SELECT * FROM ${rel.table} WHERE ${rel.joinCol} IN (${ph(ids.size)})"
            try {
                val rows = countRows(sql, ids)
                val readAll = bench { read(sql, ids, decode = false) }
                val readDecode = bench { read(sql, ids, decode = true) }
                val decodeMs = (readDecode - readAll).coerceAtLeast(0.0)
                totalReadAll += readAll
                totalDecode += decodeMs
                println("  %-38s %6d  %10.3f  %12.3f  %10.3f".format(rel.label, rows, readAll, readDecode, decodeMs))
                Triple(rel.label, rows, readAll + decodeMs)
            } catch (e: SQLiteException) {
                println("  %-38s  ERROR: ${e.message}".format(rel.label))
                null
            }
        }

        println("  ----------------------------------------------------------------------------------")
        println(
            "  %-38s %6s  %10.3f  (+ decode %.3f ms)".format("TOTAL (9 child queries)", "", totalReadAll, totalDecode),
        )
        println("  most expensive (readAll+decode):")
        ranked.sortedByDescending { it.third }.take(4).forEach {
            println("     %-36s %.3f ms  (${it.second} rows)".format(it.first, it.third))
        }
    }

    // ---- measurement ----

    private fun bench(block: () -> Unit): Double {
        repeat(WARMUP) { block() }
        val samples = DoubleArray(MEASURE)
        for (i in 0 until MEASURE) {
            val start = System.nanoTime()
            block()
            samples[i] = (System.nanoTime() - start) / 1_000_000.0
        }
        samples.sort()
        return samples[MEASURE / 2]
    }

    private fun SQLiteConnection.read(
        sql: String,
        ids: List<String>,
        decode: Boolean,
    ) {
        val s = prepare(sql)
        try {
            ids.forEachIndexed { i, id -> s.bindText(i + 1, id) }
            val cols = s.getColumnCount()
            while (s.step()) readRow(s, cols, decode)
        } finally {
            s.close()
        }
    }

    private fun readRow(
        s: SQLiteStatement,
        cols: Int,
        decode: Boolean,
    ) {
        for (i in 0 until cols) {
            if (s.isNull(i)) continue
            val t = s.getText(i)
            if (decode && t.looksLikeJson()) {
                runCatching { json.parseToJsonElement(t) }
            }
        }
    }

    private fun String.looksLikeJson() = isNotEmpty() && (this[0] == '{' || this[0] == '[')

    private fun SQLiteConnection.countRows(sql: String, ids: List<String>): Int {
        val s = prepare(sql)
        return try {
            ids.forEachIndexed { i, id -> s.bindText(i + 1, id) }
            var n = 0
            while (s.step()) n++
            n
        } finally {
            s.close()
        }
    }

    // ---- setup helpers ----

    private fun openCopy(snapshot: File): SQLiteConnection {
        val dir = File(System.getProperty("java.io.tmpdir"), "primal-relcost-bench").apply { mkdirs() }
        val target = File(dir, "$DB_NAME.db")
        snapshot.copyTo(target, overwrite = true)
        listOf("-wal", "-shm").forEach { ext ->
            val sib = File(snapshot.parentFile, snapshot.name + ext)
            if (sib.exists()) sib.copyTo(File(dir, target.name + ext), overwrite = true)
        }
        return BundledSQLiteDriver().open(target.absolutePath)
    }

    private fun SQLiteConnection.discoverFeeds(): List<Pair<String, String>> {
        val s = prepare(
            "SELECT ownerId, feedSpec FROM FeedPostDataCrossRef GROUP BY ownerId, feedSpec ORDER BY COUNT(*) DESC",
        )
        return try {
            buildList { while (s.step()) add(s.getText(0) to s.getText(1)) }
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

    private fun SQLiteConnection.idColumn(sql: String, binds: List<String>): List<String> {
        val s = prepare(sql)
        return try {
            binds.forEachIndexed { i, id -> s.bindText(i + 1, id) }
            buildList {
                while (s.step()) {
                    if (!s.isNull(0)) add(s.getText(0))
                }
            }
        } finally {
            s.close()
        }
    }

    private fun ph(n: Int) = List(n) { "?" }.joinToString(", ")

    private companion object {
        const val SNAPSHOT_PROPERTY = "primal.db.snapshot"
        const val DB_NAME = "primal_relcost_bench"
        const val PAGE_SIZE = 75
        const val WARMUP = 30
        const val MEASURE = 200
    }
}
