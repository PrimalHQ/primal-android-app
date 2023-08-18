package net.primal.android.settings.zaps

interface ZapSettingsContract {

    data class UiState(
        val defaultZapAmount: Long? = null,
        val zapOptions: List<Long?> = List(PRESETS_COUNT) { null },
    )

    sealed class UiEvent {
        data class ZapOptionsChanged(val newOptions: List<Long?>) : UiEvent()
        data class ZapDefaultAmountChanged(val newAmount: Long?) : UiEvent()
    }
}

const val PRESETS_COUNT = 6
