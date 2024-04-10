package net.primal.android.core.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.primal.android.settings.appearance.di.AppearanceSettingsViewModelFactory

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactoryProvider {
    fun appearanceSettingsViewModelFactory(): AppearanceSettingsViewModelFactory
}
