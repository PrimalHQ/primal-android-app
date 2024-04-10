package net.primal.android.settings.appearance.di

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.assisted.AssistedFactory
import dagger.hilt.android.EntryPointAccessors
import net.primal.android.core.di.ViewModelFactoryProvider
import net.primal.android.settings.appearance.AppearanceSettingsViewModel
import net.primal.android.theme.domain.PrimalTheme

@AssistedFactory
interface AppearanceSettingsViewModelFactory {
    fun create(primalTheme: PrimalTheme): AppearanceSettingsViewModel
}

@Composable
fun appearanceSettingsViewModel(primalTheme: PrimalTheme): AppearanceSettingsViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java,
    ).appearanceSettingsViewModelFactory()

    return viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(primalTheme = primalTheme) as T
            }
        },
    )
}
