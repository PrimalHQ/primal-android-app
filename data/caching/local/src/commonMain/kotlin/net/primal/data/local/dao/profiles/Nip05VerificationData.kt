package net.primal.data.local.dao.profiles

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import net.primal.domain.profile.Nip05VerificationStatus

@Entity
data class Nip05VerificationData(
    @PrimaryKey
    val ownerId: String,
    val status: Nip05VerificationStatus,
    val lastCheckedAt: Long,
    val verifiedAddress: String,
)
