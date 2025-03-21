package net.primal.android.settings.zaps

import net.primal.domain.ContentZapConfigItem
import net.primal.domain.ContentZapDefault

interface ZapSettingsContract {

    data class UiState(
        val editPresetIndex: Int? = null,
        val saving: Boolean = false,
        val zapDefault: ContentZapDefault? = null,
        val zapConfig: List<ContentZapConfigItem> = emptyList(),
    )

    sealed class UiEvent {
        data object EditZapDefault : UiEvent()
        data class EditZapPreset(val preset: ContentZapConfigItem) : UiEvent()
        data object CloseEditor : UiEvent()
        data class UpdateZapPreset(
            val index: Int,
            val zapPreset: ContentZapConfigItem,
        ) : UiEvent()

        data class UpdateZapDefault(val newZapDefault: ContentZapDefault) : UiEvent()
    }
}

const val PRESETS_COUNT = 6
