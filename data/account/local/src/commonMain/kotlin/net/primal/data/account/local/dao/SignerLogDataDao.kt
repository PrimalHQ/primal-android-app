package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface SignerLogDataDao {
    @Upsert
    suspend fun upsert(data: SignerLogData)
}
