package net.primal.android.multiaccount.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Check
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.multiaccount.model.UserAccountUi
import net.primal.android.theme.AppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherBottomSheet(
    modifier: Modifier = Modifier,
    activeAccount: UserAccountUi,
    accounts: List<UserAccountUi>,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onAccountClick: (String) -> Unit,
    onCreateNewAccountClick: () -> Unit,
    onAddExistingAccountClick: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val uiScope = rememberCoroutineScope()
    ModalBottomSheet(
        modifier = modifier.statusBarsPadding(),
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        ) {
            BottomSheetTopAppBar(onEditClick = onEditClick)
            AccountList(
                activeAccount = activeAccount, accounts = accounts,
                onAccountClick = { userId ->
                    uiScope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismissRequest()
                        }
                        onAccountClick(userId)
                    }

                },
            )
            Column {
                PlainTextButton(
                    text = stringResource(id = R.string.account_switcher_create_new_account),
                    onClick = onCreateNewAccountClick,
                )
                PlainTextButton(
                    text = stringResource(id = R.string.account_switcher_add_existing_account),
                    onClick = onAddExistingAccountClick,
                )
            }
        }
    }
}

@Composable
private fun PlainTextButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = text,
            color = AppTheme.colorScheme.secondary,
            style = AppTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AccountList(
    modifier: Modifier = Modifier,
    activeAccount: UserAccountUi,
    accounts: List<UserAccountUi>,
    onAccountClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
    ) {
        item(key = activeAccount.pubkey) {
            AccountListItem(account = activeAccount, isActive = true, onAccountClick = onAccountClick)
        }
        items(
            items = accounts,
            key = { it.pubkey },
        ) { account ->
            AccountListItem(account = account, onAccountClick = onAccountClick)
        }
    }
}

@Composable
private fun AccountListItem(
    modifier: Modifier = Modifier,
    account: UserAccountUi,
    isActive: Boolean = false,
    onAccountClick: (String) -> Unit,
) {
    val backgroundColor = if (isActive) {
        AppTheme.extraColorScheme.surfaceVariantAlt1
    } else {
        AppTheme.extraColorScheme.surfaceVariantAlt2
    }

    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = backgroundColor,
        ),
        modifier = modifier
            .clip(AppTheme.shapes.medium)
            .clickable { onAccountClick(account.pubkey) },
        leadingContent = {
            UniversalAvatarThumbnail(
                avatarSize = 40.dp,
                avatarCdnImage = account.avatarCdnImage,
                legendaryCustomization = account.legendaryCustomization,
            )
        },
        headlineContent = {
            NostrUserText(
                displayName = account.displayName,
                internetIdentifier = account.internetIdentifier,
                legendaryCustomization = account.legendaryCustomization,
            )
        },
        supportingContent = account.internetIdentifier?.let {
            {
                Text(
                    text = account.internetIdentifier.formatNip05Identifier(),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )
            }
        },
        trailingContent = {
            if (isActive) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = PrimalIcons.Check,
                    contentDescription = null,
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        },
    )

}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BottomSheetTopAppBar(onEditClick: () -> Unit) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        ),
        title = {
            Text(
                text = stringResource(id = R.string.account_switcher_sheet_title),
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
            )
        },
        navigationIcon = {
            TextButton(
                onClick = onEditClick,
            ) {
                Text(
                    text = stringResource(id = R.string.account_switcher_edit),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodyLarge,
                )
            }
        },
    )
}
