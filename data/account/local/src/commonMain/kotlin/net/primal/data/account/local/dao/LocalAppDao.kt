package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalAppDao {
    @Upsert
    suspend fun upsertAll(data: List<LocalAppData>)

    @Transaction
    @Query("SELECT * FROM LocalAppData WHERE identifier = :identifier")
    suspend fun findApp(identifier: String): LocalApp?

    @Transaction
    @Query("SELECT * FROM LocalAppData")
    fun observeAll(): Flow<List<LocalApp>>
}
