package net.primal.android.db.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.primal.android.db.PrimalDatabaseBuilder

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePrimalDatabase(builder: PrimalDatabaseBuilder) = builder.build()
}
