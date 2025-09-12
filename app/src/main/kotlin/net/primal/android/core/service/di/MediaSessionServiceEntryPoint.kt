package net.primal.android.core.service.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.core.service.PlayerManager

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MediaSessionServiceEntryPoint {
    fun playerManager(): PlayerManager
}
