package net.primal.wallet.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal val MIGRATION_6_7: Migration = object : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) {
        // The migration runs inside a Room-managed transaction; failure rolls back to v6.

        // 1. Resolve Primal walletIds into a temp table for reuse.
        connection.execSQL("CREATE TEMP TABLE _primal_wallet_ids (walletId TEXT NOT NULL PRIMARY KEY)")
        connection.execSQL(
            "INSERT INTO _primal_wallet_ids (walletId) SELECT walletId FROM WalletInfo WHERE type = 'PRIMAL'",
        )

        // 2. Cascade-clean child rows that reference Primal wallets.
        connection.execSQL(
            "DELETE FROM ActiveWalletData WHERE walletId IN (SELECT walletId FROM _primal_wallet_ids)",
        )
        connection.execSQL(
            "DELETE FROM WalletUserLink WHERE walletId IN (SELECT walletId FROM _primal_wallet_ids)",
        )
        connection.execSQL(
            "DELETE FROM WalletSettings WHERE walletId IN (SELECT walletId FROM _primal_wallet_ids)",
        )
        connection.execSQL(
            "DELETE FROM WalletTransactionData " +
                "WHERE walletType = 'PRIMAL' OR walletId IN (SELECT walletId FROM _primal_wallet_ids)",
        )
        connection.execSQL(
            "DELETE FROM WalletTransactionRemoteKey WHERE walletId IN (SELECT walletId FROM _primal_wallet_ids)",
        )
        connection.execSQL(
            "DELETE FROM ReceiveRequestData WHERE walletId IN (SELECT walletId FROM _primal_wallet_ids)",
        )
        // ZapEnrichmentTracker has no walletId column — cleanup is transitive via WalletTransactionData.

        // 3. Delete parent WalletInfo rows.
        connection.execSQL("DELETE FROM WalletInfo WHERE type = 'PRIMAL'")

        // 4. Second pass: clear dangling ActiveWalletData rows whose WalletInfo was missing pre-migration.
        connection.execSQL(
            "DELETE FROM ActiveWalletData WHERE walletId NOT IN (SELECT walletId FROM WalletInfo)",
        )

        // 5. Drop now-empty Primal table + the temp table.
        connection.execSQL("DROP TABLE PrimalWalletData")
        connection.execSQL("DROP TABLE _primal_wallet_ids")

        // 6. Recreate SparkWalletData without the primalTxsMigrated* columns.
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS SparkWalletData_new (
                walletId TEXT NOT NULL,
                seedWords TEXT NOT NULL,
                backedUp INTEGER NOT NULL,
                PRIMARY KEY(walletId)
            )
            """,
        )
        connection.execSQL(
            "INSERT INTO SparkWalletData_new (walletId, seedWords, backedUp) " +
                "SELECT walletId, seedWords, backedUp FROM SparkWalletData",
        )
        connection.execSQL("DROP TABLE SparkWalletData")
        connection.execSQL("ALTER TABLE SparkWalletData_new RENAME TO SparkWalletData")
    }
}
