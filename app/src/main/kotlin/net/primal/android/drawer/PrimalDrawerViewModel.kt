package net.primal.android.drawer

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import javax.inject.Inject

class PrimalDrawerViewModel @Inject constructor(

) : ViewModel() {

    private val _state = MutableStateFlow(
        PrimalDrawerContract.UiState()
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: PrimalDrawerContract.UiState.() -> PrimalDrawerContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

}