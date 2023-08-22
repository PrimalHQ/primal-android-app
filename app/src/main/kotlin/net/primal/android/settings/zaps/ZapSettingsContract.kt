package net.primal.android.settings.zaps

interface ZapSettingsContract {

    data class UiState(
        val defaultZapAmount: ULong? = null,
        val zapOptions: List<ULong?> = List(PRESETS_COUNT) { null },
    )

    sealed class UiEvent {
        data class ZapOptionsChanged(val newOptions: List<ULong?>) : UiEvent()
        data class ZapDefaultAmountChanged(val newAmount: ULong?) : UiEvent()
    }
}

const val PRESETS_COUNT = 6
