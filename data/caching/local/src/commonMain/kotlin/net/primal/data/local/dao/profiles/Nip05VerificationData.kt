package net.primal.data.local.dao.profiles

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.primal.domain.profile.Nip05VerificationStatus

@Entity
data class Nip05VerificationData(
    @PrimaryKey
    val ownerId: String,
    val status: Nip05VerificationStatus,
    val lastCheckedAt: Long,
    val verifiedAddress: String,
)
