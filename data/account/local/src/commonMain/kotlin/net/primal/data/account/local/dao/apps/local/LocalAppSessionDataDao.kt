package net.primal.data.account.local.dao.apps.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalAppSessionDataDao {
    @Transaction
    @Query("SELECT * FROM AppSessionData WHERE sessionId = :sessionId")
    fun observeSession(sessionId: String): Flow<LocalAppSession?>
}
