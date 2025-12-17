package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface LocalAppDao {
    @Upsert
    suspend fun upsertAll(data: List<LocalAppData>)

    @Transaction
    @Query("SELECT * FROM LocalAppData WHERE packageName = :packageName")
    suspend fun findApp(packageName: String): LocalApp?
}
