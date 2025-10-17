package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface AppPermissionDataDao {
    @Upsert
    fun upsertAll(data: List<AppPermissionData>)
}
