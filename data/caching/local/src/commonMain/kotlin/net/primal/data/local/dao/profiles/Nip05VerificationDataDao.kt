package net.primal.data.local.dao.profiles

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface Nip05VerificationDataDao {

    @Upsert
    suspend fun upsertAll(data: List<Nip05VerificationData>)

    @Upsert
    suspend fun upsert(data: Nip05VerificationData)

    @Query("SELECT * FROM Nip05VerificationData WHERE ownerId = :ownerId")
    suspend fun find(ownerId: String): Nip05VerificationData?

    @Query("SELECT * FROM Nip05VerificationData WHERE ownerId IN (:ownerIds)")
    suspend fun findAll(ownerIds: List<String>): List<Nip05VerificationData>

    @Query("DELETE FROM Nip05VerificationData WHERE ownerId = :ownerId")
    suspend fun delete(ownerId: String)
}
