package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileMetadataDao {

    @Upsert
    fun upsertAll(profiles: List<ProfileMetadata>)

    @Transaction
    @Query("SELECT * FROM ProfileMetadata WHERE ownerId = :profileId")
    fun observeProfile(profileId: String): Flow<Profile>

}
