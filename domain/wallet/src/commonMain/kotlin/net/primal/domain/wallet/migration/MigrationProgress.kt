package net.primal.domain.wallet.migration

sealed class MigrationProgress {
    data object NotStarted : MigrationProgress()

    data class InProgress(
        val step: MigrationStep,
    ) : MigrationProgress()

    data object Completed : MigrationProgress()

    data class Failed(
        val step: MigrationStep,
        val error: Throwable,
    ) : MigrationProgress()
}

enum class MigrationStep {
    CREATING_WALLET,
    REGISTERING_WALLET,
    CHECKING_BALANCE,
    CREATING_INVOICE,
    TRANSFERRING_FUNDS,
    AWAITING_CONFIRMATION,
    FINALIZING_WALLET,
    IMPORTING_HISTORY,
}
