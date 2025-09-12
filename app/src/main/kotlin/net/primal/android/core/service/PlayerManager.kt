package net.primal.android.core.service

import android.content.Context
import androidx.media3.common.Player

interface PlayerManager {

    fun createPlayer(context: Context): Player

    fun cleanup()
}
