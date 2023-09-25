package net.primal.android.auth.create.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.auth.create.CreateContract
import net.primal.android.theme.AppTheme

@Composable
fun stepTitle(step: CreateContract.UiState.CreateAccountStep): String {
    return when (step) {
        CreateContract.UiState.CreateAccountStep.NEW_ACCOUNT -> stringResource(id = R.string.create_title_new_account)
        CreateContract.UiState.CreateAccountStep.PROFILE_PREVIEW -> stringResource(id = R.string.create_title_profile_preview)
        CreateContract.UiState.CreateAccountStep.ACCOUNT_CREATED -> stringResource(id = R.string.create_title_nostr_account_created)
        CreateContract.UiState.CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS -> stringResource(id = R.string.create_title_people_to_follow)
    }
}

@Composable
fun stepActionText(step: CreateContract.UiState.CreateAccountStep): String {
    return when (step) {
        CreateContract.UiState.CreateAccountStep.NEW_ACCOUNT -> stringResource(id = R.string.create_action_next)
        CreateContract.UiState.CreateAccountStep.PROFILE_PREVIEW -> stringResource(id = R.string.create_action_create_nostr_account)
        CreateContract.UiState.CreateAccountStep.ACCOUNT_CREATED -> stringResource(id = R.string.create_action_find_people_to_follow)
        CreateContract.UiState.CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS -> stringResource(id = R.string.create_action_finish)
    }
}

@Composable
fun stepColor(step: CreateContract.UiState.CreateAccountStep, position: Int): Color {
    return if (position <= step.step) AppTheme.extraColorScheme.onSurfaceVariantAlt1 else AppTheme.colorScheme.outline
}
