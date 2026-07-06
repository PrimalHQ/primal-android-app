package net.primal.data.account.local.dao.apps.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import net.primal.data.account.local.dao.apps.TrustLevel

@Dao
interface LocalAppDao {
    @Upsert
    suspend fun upsertAll(data: List<LocalAppData>)

    @Transaction
    @Query("SELECT * FROM LocalAppData WHERE identifier = :identifier")
    suspend fun findApp(identifier: String): LocalApp?

    @Transaction
    @Query("SELECT * FROM LocalAppData WHERE identifier = :identifier")
    fun observeApp(identifier: String): Flow<LocalApp?>

    @Transaction
    @Query("SELECT * FROM LocalAppData WHERE identifier = :identifier")
    suspend fun getApp(identifier: String): LocalApp?

    @Transaction
    @Query("SELECT * FROM LocalAppData")
    fun observeAll(): Flow<List<LocalApp>>

    @Query("UPDATE LocalAppData SET trustLevel = :trustLevel WHERE identifier = :identifier")
    suspend fun updateTrustLevel(identifier: String, trustLevel: TrustLevel)

    @Query("DELETE FROM LocalAppData WHERE identifier = :identifier")
    suspend fun deleteApp(identifier: String)
}
