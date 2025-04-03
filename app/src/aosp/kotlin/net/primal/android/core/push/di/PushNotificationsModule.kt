package net.primal.android.core.push.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.core.push.AospPushNotificationsTokenUpdater
import net.primal.android.core.push.PushNotificationsTokenUpdater

@Module
@InstallIn(SingletonComponent::class)
abstract class PushNotificationsModule {
    @Binds
    abstract fun provideTokenUpdater(updater: AospPushNotificationsTokenUpdater): PushNotificationsTokenUpdater
}
