package net.primal.data.repository.db

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.primal.data.local.db.PrimalDatabase
import net.primal.shared.data.local.db.LocalDatabaseFactory

/**
 * Measures the profile-details read through Room.
 *
 * The profile screen observes two independent single-row selects, `ProfileDataDao.observeProfileData`
 * and `ProfileStatsDao.observeProfileStats`; the benchmark times each. Unlike the feed and thread
 * reads this path has no @Relation fan-out — both are flat primary-key lookups — so the only non-trivial
 * cost is decoding the TypeConverter/JSON columns on `ProfileData` (avatar/banner images, the about
 * URI/hashtag lists, premium info). It serves as a baseline reference for the profile screen and a
 * guard against the read growing more expensive.
 *
 * The benchmark reads the snapshot as-is and reports the median load time; it applies no schema or
 * query changes of its own. To evaluate a change, run the benchmark, make the change, then run it
 * again against the same snapshot and compare. The profile is selected deterministically (the
 * most-followed one present in both tables), so both runs measure the same rows.
 *
 * Opt-in: pass `-PprimalDbSnapshot=<path-to-primal_database.db>`. Without it the test no-ops, so it is
 * safe to leave in CI. The snapshot schema must match the current code; Room opens with destructive
 * migration disabled and fails on a mismatch.
 */
class ProfileLoadBenchmark {

    @Test
    fun benchmarkProfileLoad() {
        val snapshot = System.getProperty(SNAPSHOT_PROPERTY)?.let(::File)
        if (snapshot == null || !snapshot.exists()) {
            println("[ProfileLoadBenchmark] skipped — set -PprimalDbSnapshot=<path> to run.")
            return
        }

        val profileId = discoverProfileId(snapshot)
        if (profileId == null) {
            println("[ProfileLoadBenchmark] no profile rows in snapshot.")
            return
        }

        val db = openDatabase(snapshot)
        try {
            val data = runBlocking { db.profiles().observeProfileData(profileId).first() }
            val stats = runBlocking { db.profileStats().observeProfileStats(profileId).first() }
            val dataBench = bench { runBlocking { db.profiles().observeProfileData(profileId).first() } }
            val statsBench = bench { runBlocking { db.profileStats().observeProfileStats(profileId).first() } }
            println("\n[profile] profileId=${profileId.take(12)}… data=${data != null} stats=${stats != null}")
            println("  observeProfileData  ${dataBench.format()}")
            println("  observeProfileStats ${statsBench.format()}")
        } finally {
            db.close()
        }
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

    /** The most-followed profile that exists in both ProfileData and ProfileStats. */
    private fun discoverProfileId(snapshot: File): String? {
        val connection = BundledSQLiteDriver().open(snapshot.absolutePath)
        val s = connection.prepare(
            "SELECT profileId FROM ProfileStats WHERE profileId IN (SELECT ownerId FROM ProfileData) " +
                "ORDER BY followers DESC LIMIT 1",
        )
        return try {
            if (s.step()) s.getText(0) else null
        } finally {
            s.close()
            connection.close()
        }
    }

    private companion object {
        const val SNAPSHOT_PROPERTY = "primal.db.snapshot"
        const val DB_NAME_PREFIX = "primal_profile_bench"
        const val WARMUP = 20
        const val MEASURE = 100
    }
}
