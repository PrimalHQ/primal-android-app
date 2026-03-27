package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserWalletPreferencesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreferences(prefs: UserWalletPreferences)

    @Query("SELECT nwcAutoStart FROM UserWalletPreferences WHERE userId = :userId")
    suspend fun isNwcAutoStartEnabled(userId: String): Boolean?

    @Query("SELECT nwcAutoStart FROM UserWalletPreferences WHERE userId = :userId")
    fun observeNwcAutoStart(userId: String): Flow<Boolean?>

    @Query("UPDATE UserWalletPreferences SET nwcAutoStart = :autoStart WHERE userId = :userId")
    suspend fun updateNwcAutoStart(userId: String, autoStart: Boolean)
}
