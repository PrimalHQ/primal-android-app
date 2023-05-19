package net.primal.android.profile.db

import androidx.room.Dao
import androidx.room.Upsert

@Dao
interface ProfileMetadataDao {

    @Upsert
    fun upsertAll(events: List<ProfileMetadata>)

}