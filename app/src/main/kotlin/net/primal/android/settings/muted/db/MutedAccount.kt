package net.primal.android.settings.muted.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MutedAccount(
    @PrimaryKey
    val pubkey: String
)