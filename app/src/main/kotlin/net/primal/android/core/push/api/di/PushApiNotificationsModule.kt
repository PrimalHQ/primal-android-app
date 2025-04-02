package net.primal.android.core.push.api.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.primal.android.core.push.api.PrimalPushMessagesApi
import net.primal.android.core.push.api.PrimalPushMessagesApiImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PushApiNotificationsModule {

    @Binds
    abstract fun provideApi(impl: PrimalPushMessagesApiImpl): PrimalPushMessagesApi
}
