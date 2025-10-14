package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface PermissionDataDao {
    @Upsert
    fun upsertAll(data: List<PermissionData>)
}
