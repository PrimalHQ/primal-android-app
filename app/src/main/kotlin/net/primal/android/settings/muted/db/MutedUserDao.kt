package net.primal.android.settings.muted.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MutedUserDao {
    @Upsert
    fun upsertAll(data: Set<MutedUserData>)

    @Query("SELECT * FROM MutedUserData INNER JOIN ProfileData ON MutedUserData.userId = ProfileData.ownerId")
    fun observeMutedUsers(): Flow<List<MutedUser>>

    @Query("SELECT EXISTS(SELECT * FROM MutedUserData WHERE userId = :pubkey)")
    fun observeIsUserMuted(pubkey: String): Flow<Boolean>

    @Query("DELETE FROM MutedUserData")
    fun deleteAll()
}
