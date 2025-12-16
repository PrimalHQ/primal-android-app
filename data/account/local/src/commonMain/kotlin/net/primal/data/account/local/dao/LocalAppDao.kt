package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface LocalAppDao {
    @Upsert
    suspend fun upsertAll(data: List<LocalAppData>)
}
