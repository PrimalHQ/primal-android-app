/*
 * Contract for the follow list import screen.
 *
 * Ported from Amethyst PR #1785, adapted for Primal's Contract pattern.
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.auth.onboarding.account.followimport

interface ImportFollowListContract {

    data class UiState(
        val phase: Phase = Phase.Idle,
        val identifier: String = "",
        val sourcePubkeyHex: String? = null,
        val follows: List<FollowEntry> = emptyList(),
        val selected: Set<String> = emptySet(),
        val namecoinSource: String? = null,
        val errorMessage: String? = null,
        val appliedCount: Int = 0,
    ) {
        val selectedCount get() = selected.size
        val totalCount get() = follows.size
    }

    enum class Phase {
        Idle,
        Resolving,
        Fetching,
        Preview,
        Applying,
        Done,
        Error,
    }

    sealed class UiEvent {
        data class IdentifierChanged(val identifier: String) : UiEvent()
        data object StartImport : UiEvent()
        data class ToggleSelection(val pubkeyHex: String) : UiEvent()
        data class SetSelectAll(val selectAll: Boolean) : UiEvent()
        data object ApplyFollows : UiEvent()
        data object Reset : UiEvent()
    }
}
