package net.primal.android.drawer.multiaccount.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Check
import net.primal.android.core.compose.icons.primaliconpack.RemoveAccount
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.core.utils.hideAndRun
import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherBottomSheet(
    modifier: Modifier = Modifier,
    activeAccount: UserAccountUi,
    accounts: List<UserAccountUi>,
    onDismissRequest: () -> Unit,
    onAccountClick: (String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onLogoutClick: (String) -> Unit = {},
    onCreateNewAccountClick: () -> Unit = {},
    onAddExistingAccountClick: () -> Unit = {},
    showEditButton: Boolean = true,
    showAddAccountButtons: Boolean = true,
) {
    var isEditMode by remember { mutableStateOf(false) }

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
            BottomSheetTopAppBar(
                isEditMode = isEditMode,
                showEditButton = showEditButton,
                onToggleEditMode = { isEditMode = !isEditMode },
            )

            AnimatedContent(
                targetState = isEditMode,
                transitionSpec = { transitionSpecBetweenStages() },
            ) { editMode ->
                AccountList(
                    isEditMode = editMode,
                    onLogoutClick = onLogoutClick,
                    activeAccount = activeAccount,
                    accounts = accounts,
                    onAccountClick = { userId ->
                        sheetState.hideAndRun(coroutineScope = uiScope, onDismissRequest = onDismissRequest) {
                            onAccountClick(userId)
                        }
                    },
                )
            }
            if (showAddAccountButtons) {
                Column {
                    PlainTextButton(
                        text = stringResource(id = R.string.account_switcher_create_new_account),
                        onClick = {
                            sheetState.hideAndRun(coroutineScope = uiScope, onDismissRequest = onDismissRequest) {
                                onCreateNewAccountClick()
                            }
                        },
                    )
                    PlainTextButton(
                        text = stringResource(id = R.string.account_switcher_add_existing_account),
                        onClick = {
                            sheetState.hideAndRun(coroutineScope = uiScope, onDismissRequest = onDismissRequest) {
                                onAddExistingAccountClick()
                            }
                        },
                    )
                }
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
    isEditMode: Boolean,
    onLogoutClick: (String) -> Unit,
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
            AccountListItem(
                isEditMode = isEditMode,
                account = activeAccount,
                isActive = true,
                onAccountClick = onAccountClick,
                onLogoutClick = onLogoutClick,
            )
        }
        items(
            items = accounts.filter { it.pubkey != activeAccount.pubkey },
            key = { it.pubkey },
        ) { account ->
            AccountListItem(
                isEditMode = isEditMode,
                account = account,
                onAccountClick = onAccountClick,
                onLogoutClick = onLogoutClick,
            )
        }
    }
}

@Composable
private fun AccountListItem(
    modifier: Modifier = Modifier,
    account: UserAccountUi,
    isEditMode: Boolean,
    onLogoutClick: (String) -> Unit,
    onAccountClick: (String) -> Unit,
    isActive: Boolean = false,
) {
    val backgroundColor = if (isActive && !isEditMode) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                if (isEditMode) {
                    IconButton(onClick = { onLogoutClick(account.pubkey) }) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = PrimalIcons.RemoveAccount,
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    }
                }

                UniversalAvatarThumbnail(
                    avatarSize = 40.dp,
                    avatarCdnImage = account.avatarCdnImage,
                    avatarBlossoms = account.avatarBlossoms,
                    legendaryCustomization = account.legendaryCustomization,
                )
            }
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
            if (isActive && !isEditMode) {
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
private fun BottomSheetTopAppBar(
    isEditMode: Boolean,
    showEditButton: Boolean,
    onToggleEditMode: () -> Unit,
) {
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
            if (showEditButton) {
                TextButton(
                    onClick = onToggleEditMode,
                ) {
                    Text(
                        text = if (isEditMode) {
                            stringResource(id = R.string.account_switcher_done)
                        } else {
                            stringResource(id = R.string.account_switcher_edit)
                        },
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        style = AppTheme.typography.bodyLarge,
                    )
                }
            }
        },
    )
}

private fun AnimatedContentTransitionScope<Boolean>.transitionSpecBetweenStages() =
    when (initialState) {
        false -> {
            slideInHorizontally(initialOffsetX = { it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
        }

        true -> {
            slideInHorizontally(initialOffsetX = { -it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        }
    }
