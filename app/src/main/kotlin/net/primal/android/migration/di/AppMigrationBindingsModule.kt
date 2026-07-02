package net.primal.android.migration.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import net.primal.android.migration.AppMigration

/** Declares the [AppMigration] multibinding so an empty set injects while no migrations are registered. */
@Module
@InstallIn(SingletonComponent::class)
interface AppMigrationBindingsModule {

    @Multibinds
    fun appMigrations(): Set<AppMigration>
}
