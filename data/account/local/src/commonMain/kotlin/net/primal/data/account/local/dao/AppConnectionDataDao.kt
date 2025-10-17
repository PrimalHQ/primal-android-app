package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface AppConnectionDataDao {
    @Upsert
    fun upsertAll(data: List<AppConnectionData>)
}
