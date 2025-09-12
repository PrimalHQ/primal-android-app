package net.primal.android.core.player.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.core.player.GooglePlayerManager
import net.primal.android.core.service.PlayerManager

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerModule {

    @Binds
    abstract fun bindPlayerManager(googlePlayerManager: GooglePlayerManager): PlayerManager
}
