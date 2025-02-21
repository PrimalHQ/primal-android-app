package net.primal.android.user.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.primal.android.user.domain.RelayKind

@Dao
interface RelayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(relays: List<Relay>)

    @Query("DELETE FROM Relay WHERE userId = :userId")
    fun deleteAll(userId: String)

    @Query("DELETE FROM Relay WHERE userId = :userId AND kind = :kind")
    fun deleteAll(userId: String, kind: RelayKind)

    @Query("SELECT * FROM Relay WHERE userId = :userId ORDER BY url ASC")
    fun observeRelays(userId: String): Flow<List<Relay>>

    @Query("SELECT * FROM Relay WHERE userId = :userId AND kind = :kind")
    fun findRelays(userId: String, kind: RelayKind): List<Relay>
}
