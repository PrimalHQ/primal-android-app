package net.primal.android.settings.muted.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Muted(
    @PrimaryKey
    val pubkey: String
)