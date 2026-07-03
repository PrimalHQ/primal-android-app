package net.primal.shared.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import io.github.aakira.napier.Napier
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching

/**
 * Room callback that reclaims disk space for every database built through [buildLocalDatabase].
 *
 * A destructive migration drops tables inside the same file without shrinking it, so this
 * callback flags [onDestructiveMigration] and runs `VACUUM` from the next [onOpen] — after the
 * migration transaction has committed, since `VACUUM` cannot run inside it — followed by a
 * truncating WAL checkpoint (`VACUUM` writes through the WAL).
 *
 * Independently, every connection gets `PRAGMA journal_size_limit` so checkpoints keep the
 * `-wal` file at or below [JOURNAL_SIZE_LIMIT_BYTES], and the first [onOpen] runs a one-time
 * truncating checkpoint to shrink an already-bloated `-wal`.
 *
 * [onOpen] fires once per pooled connection, so each one-time step is claimed with an atomic
 * compare-and-set — concurrent opens can't duplicate it. A failed `VACUUM` restores its flag and
 * retries on the next connection (it is idempotent). The VACUUM and one-time WAL-truncate steps
 * are independent, so a permanently failing `VACUUM` never starves the WAL truncate.
 * Instantiate one instance per database — the flags are per-database state.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class DatabaseSpaceReclaimCallback : RoomDatabase.Callback() {

    private val vacuumPending = AtomicBoolean(false)
    private val walTruncatePending = AtomicBoolean(true)

    override fun onDestructiveMigration(connection: SQLiteConnection) {
        vacuumPending.store(true)
    }

    override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA journal_size_limit = $JOURNAL_SIZE_LIMIT_BYTES")

        reclaimAfterDestructiveMigration(connection)
        truncateBloatedWalOnce(connection)
    }

    private fun reclaimAfterDestructiveMigration(connection: SQLiteConnection) {
        if (!vacuumPending.compareAndSet(expectedValue = true, newValue = false)) return

        runCatching {
            connection.execSQL("VACUUM")
            connection.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
        }.onSuccess {
            // VACUUM already truncated the WAL, so the one-time checkpoint below is redundant.
            walTruncatePending.store(false)
        }.onFailure { error ->
            vacuumPending.store(true)
            Napier.w(tag = TAG, throwable = error) {
                "VACUUM after destructive migration failed; retrying on next connection open."
            }
        }
    }

    private fun truncateBloatedWalOnce(connection: SQLiteConnection) {
        if (!walTruncatePending.compareAndSet(expectedValue = true, newValue = false)) return

        runCatching {
            connection.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
        }.onFailure { error ->
            Napier.w(tag = TAG, throwable = error) { "One-time WAL truncate checkpoint failed." }
        }
    }

    private companion object {
        private const val TAG = "DatabaseSpaceReclaim"

        /**
         * Matches SQLite's default WAL auto-checkpoint threshold (1000 pages x 4 KiB pages);
         * checkpoints truncate the -wal file back to at most this many bytes.
         */
        private const val JOURNAL_SIZE_LIMIT_BYTES = 4L * 1024 * 1024
    }
}
