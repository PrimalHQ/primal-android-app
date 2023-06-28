package net.primal.android.settings.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.primal.android.BuildConfig
import net.primal.android.settings.home.SettingsHomeContract.UiState
import javax.inject.Inject

@HiltViewModel
class SettingsHomeViewModel @Inject constructor(

) : ViewModel() {

    private val _state = MutableStateFlow(UiState(version = BuildConfig.VERSION_NAME))
    val state = _state.asStateFlow()

}
