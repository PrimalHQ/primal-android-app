package net.primal.data.repository.db

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.primal.data.local.dao.notes.FeedPost
import net.primal.data.local.db.PrimalDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Measures opening a note thread (the conversation screen) through Room.
 *
 * The benchmark calls `ThreadConversationDao.observeNoteConversation(postId, userId)` — the read the
 * thread screen observes — and times resolving the first emission. The query joins `PostData` to itself
 * through `NoteConversationCrossRef` and returns `List<FeedPost>`, the same relation-heavy row the feed
 * uses (author, event stats, zaps, streams, uris, bookmark, poll, …), so the timing includes the
 * relation fan-out and the JSON/TypeConverter decoding. The larger the thread, the more fan-out.
 *
 * The benchmark reads the snapshot as-is and reports the median load time; it applies no schema or
 * query changes of its own. To evaluate a change, run the benchmark, make the change, then run it
 * again against the same snapshot and compare. The thread is selected deterministically (the
 * conversation with the most replies), so both runs measure the same data.
 *
 * Opt-in: pass `-PprimalDbSnapshot=<path-to-primal_database.db>`. Without it the test no-ops, so it is
 * safe to leave in CI. The snapshot schema must match the current code; Room opens with destructive
 * migration disabled and fails on a mismatch.
 */
class NoteThreadLoadBenchmark {

    private data class Thread(val rootId: String, val userId: String, val replies: Int)

    @Test
    fun benchmarkNoteThreadLoad() {
        val snapshot = System.getProperty(SNAPSHOT_PROPERTY)?.let(::File)
        if (snapshot == null || !snapshot.exists()) {
            println("[NoteThreadLoadBenchmark] skipped — set -PprimalDbSnapshot=<path> to run.")
            return
        }

        val thread = discoverThread(snapshot)
        if (thread == null) {
            println("[NoteThreadLoadBenchmark] no conversation rows in snapshot.")
            return
        }

        val db = openDatabase(snapshot)
        try {
            val first = runBlocking { loadThread(db, thread) }
            val stats = bench { runBlocking { loadThread(db, thread) } }
            println("\n[note thread] rootId=${thread.rootId.take(12)}… crossRefReplies=${thread.replies}")
            println(
                "  loaded ${first.size} notes, ${first.count { it.author != null }} authors, " +
                    "${first.sumOf { it.eventZaps.size }} zaps, ${first.sumOf { it.streams.size }} streams",
            )
            println("  ${stats.format()}")
        } finally {
            db.close()
        }
    }

    private suspend fun loadThread(db: PrimalDatabase, thread: Thread): List<FeedPost> =
        db.threadConversations().observeNoteConversation(postId = thread.rootId, userId = thread.userId).first()

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

    /**
     * The conversation with the most replies, read as the snapshot's busiest owner. `userId` only
     * feeds the EventUserStats join and the mute filters, so the busiest owner mirrors the active
     * account the snapshot belongs to.
     */
    private fun discoverThread(snapshot: File): Thread? {
        val connection = BundledSQLiteDriver().open(snapshot.absolutePath)
        return try {
            val root = connection.prepare(
                "SELECT noteId, COUNT(replyNoteId) c FROM NoteConversationCrossRef " +
                    "GROUP BY noteId ORDER BY c DESC LIMIT 1",
            )
            val (rootId, replies) = try {
                if (root.step()) root.getText(0) to root.getLong(1).toInt() else return null
            } finally {
                root.close()
            }
            val owner = connection.prepare(
                "SELECT ownerId FROM FeedPostDataCrossRef GROUP BY ownerId ORDER BY COUNT(*) DESC LIMIT 1",
            )
            val userId = try {
                if (owner.step()) owner.getText(0) else rootId
            } finally {
                owner.close()
            }
            Thread(rootId = rootId, userId = userId, replies = replies)
        } finally {
            connection.close()
        }
    }

    private companion object {
        const val SNAPSHOT_PROPERTY = "primal.db.snapshot"
        const val DB_NAME_PREFIX = "primal_thread_bench"
        const val WARMUP = 20
        const val MEASURE = 100
    }
}
