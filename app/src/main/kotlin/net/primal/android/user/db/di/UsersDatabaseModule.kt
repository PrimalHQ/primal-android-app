package net.primal.android.user.db.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.user.db.UsersDatabaseBuilder

@Module
@InstallIn(SingletonComponent::class)
object UsersDatabaseModule {

    @Provides
    @Singleton
    fun provideUsersDatabase(builder: UsersDatabaseBuilder) = builder.build()
}
