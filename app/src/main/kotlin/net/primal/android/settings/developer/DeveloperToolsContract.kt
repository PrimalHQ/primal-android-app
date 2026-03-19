package net.primal.android.settings.developer

import java.io.File
import net.primal.domain.wallet.WalletType

interface DeveloperToolsContract {

    data class DevWalletInfo(
        val walletId: String,
        val type: WalletType,
        val isActive: Boolean,
        val lightningAddress: String? = null,
        val balanceInSats: Long? = null,
    )

    data class UiState(
        val isLoggingEnabled: Boolean = false,
        val isWalletPickerEnabled: Boolean = false,
        val logFileCount: Int = 0,
        val totalLogSizeBytes: Long = 0L,
        val isExporting: Boolean = false,
        val wallets: List<DevWalletInfo> = emptyList(),
    )

    sealed class UiEvent {
        data class ToggleLogging(val enabled: Boolean) : UiEvent()
        data class ToggleWalletPicker(val enabled: Boolean) : UiEvent()
        data object ExportLogs : UiEvent()
        data object ClearLogs : UiEvent()
        data class CopySeedWords(val walletId: String) : UiEvent()
    }

    sealed class SideEffect {
        data class ShareLogs(val file: File) : SideEffect()
        data object NoLogsToExport : SideEffect()
        data object ExportFailed : SideEffect()
        data class SeedWordsCopied(val seedWords: String) : SideEffect()
        data object SeedWordsCopyFailed : SideEffect()
    }
}
