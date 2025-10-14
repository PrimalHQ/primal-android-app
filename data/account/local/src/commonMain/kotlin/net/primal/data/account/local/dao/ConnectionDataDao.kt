package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface ConnectionDataDao {
    @Upsert
    fun upsertAll(data: List<ConnectionData>)
}
