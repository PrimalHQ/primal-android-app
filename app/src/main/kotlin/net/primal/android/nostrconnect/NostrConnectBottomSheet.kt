@file:OptIn(ExperimentalMaterial3Api::class)

package net.primal.android.nostrconnect

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.HighSecurity
import net.primal.android.core.compose.icons.primaliconpack.LowSecurity
import net.primal.android.core.compose.icons.primaliconpack.MediumSecurity
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.ext.selectableItem
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.android.navigation.primalSlideInHorizontallyFromEnd
import net.primal.android.navigation.primalSlideOutHorizontallyToEnd
import net.primal.android.nostrconnect.NostrConnectContract.Companion.DAILY_BUDGET_PICKER_OPTIONS
import net.primal.android.theme.AppTheme
import net.primal.core.utils.toDouble
import net.primal.domain.links.CdnImage

private val DISABLED_ICON_TINT = Color(0xFF808080)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NostrConnectBottomSheet(
    viewModel: NostrConnectViewModel,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { viewModel.setEvent(NostrConnectContract.UiEvent.DismissError) },
    )

    LaunchedEffect(viewModel, onDismissRequest) {
        viewModel.effects.collect {
            when (it) {
                is NostrConnectContract.SideEffect.ConnectionSuccess -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.nostr_connect_toast_connected),
                        Toast.LENGTH_SHORT,
                    ).show()
                    onDismissRequest()
                }
            }
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
        dragHandle = { NostrConnectBottomSheetDragHandle() },
    ) {
        NostrConnectSheetContent(
            state = state,
            onDismissRequest = onDismissRequest,
            eventPublisher = { viewModel.setEvent(it) },
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun NostrConnectSheetContent(
    state: NostrConnectContract.UiState,
    onDismissRequest: () -> Unit,
    eventPublisher: (NostrConnectContract.UiEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val isBudgetPickerVisible by remember(state.selectedTab, state.showDailyBudgetPicker) {
        derivedStateOf {
            state.selectedTab == NostrConnectContract.Tab.PERMISSIONS && state.showDailyBudgetPicker
        }
    }

    BackHandler {
        if (isBudgetPickerVisible) {
            eventPublisher(NostrConnectContract.UiEvent.CancelDailyBudget)
        } else {
            onDismissRequest()
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.extraColorScheme.surfaceVariantAlt2),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeaderSection(
                imageUrl = state.appImageUrl,
                appName = state.appName,
                appWebUrl = state.appWebUrl,
            )

            NostrConnectTabNavigation(
                selectedTab = state.selectedTab,
                onTabChange = { eventPublisher(NostrConnectContract.UiEvent.ChangeTab(it)) },
                permissionsTabEnabled = state.accounts.isNotEmpty(),
            )

            NostrConnectPages(
                state = state,
                eventPublisher = eventPublisher,
            )

            ActionButtons(
                primaryButtonEnabled = state.selectedAccount != null,
                primaryButtonLoading = state.connecting,
                primaryButtonText = if (isBudgetPickerVisible) {
                    stringResource(id = R.string.nostr_connect_apply_button)
                } else {
                    stringResource(id = R.string.nostr_connect_connect_button)
                },
                onPrimaryClick = {
                    if (isBudgetPickerVisible) {
                        eventPublisher(NostrConnectContract.UiEvent.ApplyDailyBudget)
                    } else {
                        eventPublisher(NostrConnectContract.UiEvent.ClickConnect)
                    }
                },
                secondaryButtonText = stringResource(id = R.string.nostr_connect_cancel_button),
                onSecondaryClick = {
                    if (isBudgetPickerVisible) {
                        eventPublisher(NostrConnectContract.UiEvent.CancelDailyBudget)
                    } else {
                        onDismissRequest()
                    }
                },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NostrConnectBottomSheetDragHandle() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.extraColorScheme.surfaceVariantAlt2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BottomSheetDefaults.DragHandle()
    }
}

@Composable
private fun HeaderSection(
    imageUrl: String?,
    appName: String?,
    appWebUrl: String?,
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        imageUrl?.let {
            UniversalAvatarThumbnail(
                avatarCdnImage = CdnImage(sourceUrl = imageUrl),
                avatarSize = 48.dp,
            )
        }

        Text(
            text = appName ?: stringResource(id = R.string.nostr_connect_unknown_app),
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                lineHeight = 24.sp,
            ),
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.onPrimary,
        )

        appWebUrl?.let { url ->
            Text(
                text = url,
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                ),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        }
    }
}

@Composable
private fun NostrConnectTabNavigation(
    selectedTab: NostrConnectContract.Tab,
    onTabChange: (NostrConnectContract.Tab) -> Unit,
    permissionsTabEnabled: Boolean,
) {
    val selectedTabIndex = selectedTab.ordinal
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        contentColor = AppTheme.colorScheme.onSurface,
        indicator = {
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(selectedTabIndex)
                    .height(6.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 2.dp)
                    .clip(CircleShape)
                    .background(color = AppTheme.colorScheme.primary),
            )
        },
        divider = {
            PrimalDivider()
        },
    ) {
        NostrConnectAppTab(
            text = stringResource(id = R.string.nostr_connect_login_tab).uppercase(),
            selected = selectedTab == NostrConnectContract.Tab.LOGIN,
            onClick = { onTabChange(NostrConnectContract.Tab.LOGIN) },
        )

        NostrConnectAppTab(
            text = stringResource(id = R.string.nostr_connect_permissions_tab).uppercase(),
            selected = selectedTab == NostrConnectContract.Tab.PERMISSIONS,
            onClick = { onTabChange(NostrConnectContract.Tab.PERMISSIONS) },
            enabled = permissionsTabEnabled,
        )
    }
}

@Composable
private fun NostrConnectAppTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Tab(
        modifier = modifier.padding(horizontal = 16.dp),
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        text = {
            Text(
                text = text,
                style = AppTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                ),
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (enabled) {
                    AppTheme.colorScheme.onPrimary
                } else {
                    AppTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
        },
    )
}

@Composable
private fun NostrConnectPages(
    state: NostrConnectContract.UiState,
    eventPublisher: (NostrConnectContract.UiEvent) -> Unit,
) {
    AnimatedContent(
        modifier = Modifier
            .background(color = AppTheme.extraColorScheme.surfaceVariantAlt3)
            .height(400.dp),
        targetState = state.selectedTab,
        transitionSpec = {
            val slideIn = if (targetState.ordinal > initialState.ordinal) {
                primalSlideInHorizontallyFromEnd
            } else {
                slideInHorizontally { -it }
            }
            val slideOut = if (targetState.ordinal > initialState.ordinal) {
                primalSlideOutHorizontallyToEnd
            } else {
                slideOutHorizontally { it }
            }
            slideIn.togetherWith(slideOut)
        },
        label = "TabAnimation",
    ) { tab ->
        when (tab) {
            NostrConnectContract.Tab.LOGIN -> LoginContent(
                accounts = state.accounts,
                selectedAccountPubkey = state.selectedAccount?.pubkey,
                onAccountClick = { eventPublisher(NostrConnectContract.UiEvent.SelectAccount(it)) },
            )
            NostrConnectContract.Tab.PERMISSIONS -> PermissionsContent(
                trustLevel = state.trustLevel,
                dailyBudget = state.dailyBudget,
                onTrustLevelClick = { eventPublisher(NostrConnectContract.UiEvent.SelectTrustLevel(it)) },
                onDailyBudgetClick = { eventPublisher(NostrConnectContract.UiEvent.ClickDailyBudget) },
                showDailyBudgetPicker = state.showDailyBudgetPicker,
                selectedDailyBudget = state.selectedDailyBudget,
                onDailyBudgetChange = { eventPublisher(NostrConnectContract.UiEvent.ChangeDailyBudget(it)) },
                budgetToUsdMap = state.budgetToUsdMap,
            )
        }
    }
}

@Composable
private fun LoginContent(
    accounts: List<UserAccountUi>,
    selectedAccountPubkey: String?,
    onAccountClick: (String) -> Unit,
) {
    if (accounts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(id = R.string.nostr_connect_no_nsec_accounts_warning),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(accounts, key = { it.pubkey }) { account ->
                AccountListItem(
                    account = account,
                    isSelected = account.pubkey == selectedAccountPubkey,
                    onClick = { onAccountClick(account.pubkey) },
                )
            }
        }
    }
}

@Composable
private fun AccountListItem(
    account: UserAccountUi,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.selectableItem(
            selected = isSelected,
            onClick = onClick,
        ),
        colors = ListItemDefaults.colors(containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1),
        leadingContent = {
            UniversalAvatarThumbnail(
                avatarSize = 40.dp,
                avatarCdnImage = account.avatarCdnImage,
                avatarBlossoms = account.avatarBlossoms,
                legendaryCustomization = account.legendaryCustomization,
            )
        },
        headlineContent = {
            NostrUserText(
                displayName = account.displayName,
                internetIdentifier = account.internetIdentifier,
                legendaryCustomization = account.legendaryCustomization,
                displayNameColor = AppTheme.colorScheme.onSurface,
            )
        },
        supportingContent = account.internetIdentifier?.let {
            {
                Text(
                    text = it.formatNip05Identifier(),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )
            }
        },
    )
}

@Composable
private fun PermissionsContent(
    trustLevel: NostrConnectContract.TrustLevel,
    dailyBudget: Long?,
    onTrustLevelClick: (NostrConnectContract.TrustLevel) -> Unit,
    onDailyBudgetClick: () -> Unit,
    showDailyBudgetPicker: Boolean,
    selectedDailyBudget: Long?,
    onDailyBudgetChange: (Long?) -> Unit,
    budgetToUsdMap: Map<Long, BigDecimal?>,
) {
    AnimatedContent(
        targetState = showDailyBudgetPicker,
        label = "PermissionsPickerAnimation",
    ) { showPicker ->
        if (showPicker) {
            DailyBudgetPicker(
                selectedBudget = selectedDailyBudget,
                onBudgetChange = onDailyBudgetChange,
                budgetToUsdMap = budgetToUsdMap,
            )
        } else {
            PermissionsList(
                trustLevel = trustLevel,
                onTrustLevelClick = onTrustLevelClick,
                onDailyBudgetClick = onDailyBudgetClick,
                dailyBudget = dailyBudget,
            )
        }
    }
}

@Composable
private fun PermissionsList(
    trustLevel: NostrConnectContract.TrustLevel,
    onTrustLevelClick: (NostrConnectContract.TrustLevel) -> Unit,
    onDailyBudgetClick: () -> Unit,
    dailyBudget: Long?,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PermissionsListItem(
            icon = PrimalIcons.HighSecurity,
            title = stringResource(id = R.string.nostr_connect_full_trust_title),
            subtitle = stringResource(id = R.string.nostr_connect_full_trust_subtitle),
            isSelected = trustLevel == NostrConnectContract.TrustLevel.FULL,
            onClick = { onTrustLevelClick(NostrConnectContract.TrustLevel.FULL) },
        )
        PermissionsListItem(
            icon = PrimalIcons.MediumSecurity,
            title = stringResource(id = R.string.nostr_connect_medium_trust_title),
            subtitle = stringResource(id = R.string.nostr_connect_medium_trust_subtitle),
            isSelected = trustLevel == NostrConnectContract.TrustLevel.MEDIUM,
            onClick = { onTrustLevelClick(NostrConnectContract.TrustLevel.MEDIUM) },
        )
        PermissionsListItem(
            icon = PrimalIcons.LowSecurity,
            title = stringResource(id = R.string.nostr_connect_low_trust_title),
            subtitle = stringResource(id = R.string.nostr_connect_low_trust_subtitle),
            isSelected = trustLevel == NostrConnectContract.TrustLevel.LOW,
            onClick = { onTrustLevelClick(NostrConnectContract.TrustLevel.LOW) },
        )

        PrimalDivider(modifier = Modifier.padding(vertical = 8.dp))

        DailyBudgetListItem(
            dailyBudget = dailyBudget,
            onClick = onDailyBudgetClick,
        )
    }
}

@Composable
private fun DailyBudgetListItem(dailyBudget: Long?, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier
            .clip(AppTheme.shapes.medium)
            .clickable(onClick = onClick)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt1),
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        ),
        headlineContent = {
            Text(
                text = stringResource(id = R.string.nostr_connect_daily_budget_title),
                color = AppTheme.colorScheme.onPrimary,
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                ),
            )
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val style = AppTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                    )
                    Text(
                        text = NumberFormat.getNumberInstance().format(dailyBudget ?: 0),
                        color = AppTheme.colorScheme.onPrimary,
                        style = style,
                    )
                    Text(
                        text = stringResource(id = R.string.nostr_connect_sats_unit),
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        style = style,
                    )
                }
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        },
    )
}

@Composable
private fun DailyBudgetPicker(
    selectedBudget: Long?,
    onBudgetChange: (Long?) -> Unit,
    budgetToUsdMap: Map<Long, BigDecimal?>,
) {
    val numberFormat = remember {
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("USD")
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp),
            text = stringResource(id = R.string.nostr_connect_daily_spending_budget_title),
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 20.sp,
            ),
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colorScheme.onPrimary,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(DAILY_BUDGET_PICKER_OPTIONS) { budget ->
                val subtext = if (budget != null) {
                    budgetToUsdMap[budget]?.let { usdAmount ->
                        stringResource(
                            id = R.string.nostr_connect_usd_amount_approx,
                            numberFormat.format(usdAmount.toDouble()),
                        )
                    }
                } else {
                    null
                }

                val primaryColor = AppTheme.colorScheme.onPrimary
                val secondaryColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2
                val annotatedText = buildAnnotatedString {
                    if (budget != null) {
                        withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) {
                            append(NumberFormat.getNumberInstance().format(budget))
                        }
                        append(" ")
                        withStyle(style = SpanStyle(color = secondaryColor, fontWeight = FontWeight.Normal)) {
                            append(stringResource(id = R.string.nostr_connect_sats_unit))
                        }
                    } else {
                        withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) {
                            append(stringResource(id = R.string.nostr_connect_no_limit).uppercase())
                        }
                    }
                }

                BudgetOption(
                    text = annotatedText,
                    subtext = subtext,
                    isSelected = selectedBudget == budget,
                    onClick = { onBudgetChange(budget) },
                )
            }
        }
    }
}

@Composable
private fun BudgetOption(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    subtext: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        AppTheme.extraColorScheme.surfaceVariantAlt2
    } else {
        AppTheme.extraColorScheme.surfaceVariantAlt1
    }

    Surface(
        modifier = modifier
            .height(52.dp)
            .selectableItem(selected = isSelected, onClick = onClick),
        color = backgroundColor,
        shape = AppTheme.shapes.medium,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = text,
                    style = AppTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
                )
                if (subtext != null) {
                    Text(
                        text = subtext,
                        style = AppTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                        ),
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionsListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.selectableItem(
            selected = isSelected,
            onClick = onClick,
        ),
        colors = ListItemDefaults.colors(containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1),
        leadingContent = {
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) AppTheme.colorScheme.onPrimary else DISABLED_ICON_TINT,
            )
        },
        headlineContent = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.onSurface,
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                ),
            )
        },
        supportingContent = {
            Text(
                modifier = Modifier.padding(top = 3.dp),
                text = subtitle,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                ),
            )
        },
    )
}

@Composable
private fun ActionButtons(
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String,
    onSecondaryClick: () -> Unit,
    primaryButtonLoading: Boolean = false,
    primaryButtonEnabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt3,
            )
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PrimalFilledButton(
            modifier = Modifier.weight(1f),
            height = 50.dp,
            containerColor = Color.Transparent,
            contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            border = BorderStroke(width = 1.dp, color = AppTheme.colorScheme.outline),
            textStyle = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
            onClick = onSecondaryClick,
        ) {
            Text(text = secondaryButtonText)
        }

        PrimalLoadingButton(
            modifier = Modifier.weight(1f),
            height = 50.dp,
            loading = primaryButtonLoading,
            enabled = primaryButtonEnabled,
            text = primaryButtonText,
            onClick = onPrimaryClick,
        )
    }
}
