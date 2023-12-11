package net.primal.android.wallet.db

import androidx.room.Embedded
import androidx.room.Relation
import net.primal.android.profile.db.ProfileData

data class WalletTransaction(

    @Embedded
    val data: WalletTransactionData,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "userId",
    )
    val userProfileData: ProfileData? = null,

    @Relation(
        entityColumn = "ownerId",
        parentColumn = "otherUserId",
    )
    val otherProfileData: ProfileData? = null,
)
