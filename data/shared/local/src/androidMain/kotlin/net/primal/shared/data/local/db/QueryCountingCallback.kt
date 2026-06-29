package net.primal.shared.data.local.db

import androidx.room.RoomDatabase
import io.github.aakira.napier.Napier
import java.util.concurrent.atomic.AtomicLong
import net.primal.shared.data.local.db.QueryCountingCallback.reset

/**
 * Debug-only [RoomDatabase.QueryCallback] that counts the "hot" caching-DB queries, making read-path
 * churn that `EXPLAIN` can't reveal observable: how often the feed PagingSource union (+ its hidden
 * `COUNT(*)`) re-runs per scroll and per like/zap, and how often the `@Relation` fan-out
 * (EventZap / StreamData) fires per page. Attached only in debuggable builds (see
 * [AndroidLocalDatabaseFactory]); never in release.
 *
 * Reading method:
 *   adb logcat | grep DbQuery
 * Each line prints a label + the running count, e.g. `DbQuery FEED_UNION #7`. Call [reset] to
 * re-baseline before measuring a specific interaction (scroll a page, tap like) and read the deltas.
 */
object QueryCountingCallback : RoomDatabase.QueryCallback {

    private val counters = mutableMapOf<String, AtomicLong>()

    override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
        val label = classify(sqlQuery) ?: return
        val count = counters.getOrPut(label) { AtomicLong(0) }.incrementAndGet()
        Napier.i(tag = "DbQuery") { "$label #$count" }
    }

    fun reset() = counters.clear()

    private fun classify(sql: String): String? {
        // Room emits identifiers wrapped in backticks; strip them for matching.
        val s = sql.replace("`", "")
        val hasUnion = s.contains("UNION ALL") && s.contains("FeedPostDataCrossRef")
        return when {
            s.contains("COUNT(*)") && hasUnion -> "FEED_COUNT"
            hasUnion -> "FEED_UNION"
            s.contains("FROM EventZap") && s.contains("eventId") && s.contains(" IN ") -> "REL_EventZap"
            s.contains("FROM StreamData") && s.contains("mainHostId") -> "REL_StreamData"
            else -> null
        }
    }
}
