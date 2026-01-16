package net.primal.android.core.compose.signer

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import net.primal.android.R
import net.primal.android.core.compose.AppIconThumbnail
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.HighSecurity
import net.primal.android.core.compose.icons.primaliconpack.LowSecurity
import net.primal.android.core.compose.icons.primaliconpack.MediumSecurity
import net.primal.android.core.compose.nostrconnect.PermissionsListItem
import net.primal.android.core.compose.signer.SignerConnectBottomSheet.DAILY_BUDGET_PICKER_OPTIONS
import net.primal.android.core.ext.selectableItem
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.android.navigation.primalSlideInHorizontallyFromEnd
import net.primal.android.navigation.primalSlideOutHorizontallyToEnd
import net.primal.android.theme.AppTheme
import net.primal.core.utils.toDouble
import net.primal.domain.account.model.TrustLevel

private enum class SignerConnectTab {
    Login,
    Permissions,
}

@Composable
fun SignerConnectBottomSheet(
    appName: String?,
    appDescription: String?,
    accounts: List<UserAccountUi>,
    connecting: Boolean,
    onConnectClick: (UserAccountUi, TrustLevel, Long?) -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    appImageUrl: String? = null,
    appIcon: Drawable? = null,
    hasNwcRequest: Boolean = false,
    budgetToUsdMap: Map<Long, BigDecimal?> = emptyMap(),
) {
    var selectedTab by remember { mutableStateOf(SignerConnectTab.Login) }
    var trustLevel by remember { mutableStateOf(TrustLevel.Medium) }
    var selectedAccount by remember(accounts) { mutableStateOf(accounts.firstOrNull()) }
    var dailyBudget by remember { mutableStateOf<Long?>(0L) }
    var showDailyBudgetPicker by remember { mutableStateOf(false) }
    var tempSelectedBudget by remember { mutableStateOf<Long?>(null) }

    val isBudgetPickerVisible by remember(selectedTab, showDailyBudgetPicker) {
        derivedStateOf { selectedTab == SignerConnectTab.Permissions && showDailyBudgetPicker }
    }

    BackHandler {
        if (isBudgetPickerVisible) showDailyBudgetPicker = false else onCancelClick()
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.extraColorScheme.surfaceVariantAlt2),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeaderSection(
                appIconUrl = appImageUrl,
                appIcon = appIcon,
                appName = appName,
                appDescription = appDescription,
            )

            SignerConnectTabNavigation(
                selectedTab = selectedTab,
                onTabChange = { selectedTab = it },
                permissionsTabEnabled = accounts.isNotEmpty(),
            )

            SignerConnectPages(
                modifier = Modifier
                    .height(400.dp)
                    .weight(1f, fill = false),
                selectedTab = selectedTab,
                accounts = accounts,
                selectedAccount = selectedAccount,
                trustLevel = trustLevel,
                onAccountSelect = { pubkey -> selectedAccount = accounts.find { it.pubkey == pubkey } },
                onTrustLevelSelect = { trustLevel = it },
                hasNwcRequest = hasNwcRequest,
                dailyBudget = dailyBudget,
                showDailyBudgetPicker = showDailyBudgetPicker,
                selectedDailyBudget = tempSelectedBudget,
                budgetToUsdMap = budgetToUsdMap,
                onDailyBudgetClick = {
                    tempSelectedBudget = dailyBudget
                    showDailyBudgetPicker = true
                },
                onDailyBudgetChange = { tempSelectedBudget = it },
            )

            SignerConnectFooter(
                isBudgetPickerVisible = isBudgetPickerVisible,
                connecting = connecting,
                primaryButtonEnabled = selectedAccount != null,
                onApplyBudget = {
                    dailyBudget = tempSelectedBudget
                    showDailyBudgetPicker = false
                },
                onConnect = {
                    selectedAccount?.let {
                        onConnectClick(it, trustLevel, if (hasNwcRequest) dailyBudget else null)
                    }
                },
                onCancelBudget = { showDailyBudgetPicker = false },
                onCancel = onCancelClick,
            )
        }
    }
}

@Composable
private fun SignerConnectFooter(
    isBudgetPickerVisible: Boolean,
    connecting: Boolean,
    primaryButtonEnabled: Boolean,
    onApplyBudget: () -> Unit,
    onConnect: () -> Unit,
    onCancelBudget: () -> Unit,
    onCancel: () -> Unit,
) {
    ActionButtons(
        primaryButtonEnabled = primaryButtonEnabled,
        primaryButtonLoading = connecting,
        primaryButtonText = if (isBudgetPickerVisible) {
            stringResource(id = R.string.signer_connect_apply_budget_button)
        } else {
            stringResource(id = R.string.signer_connect_connect_button)
        },
        onPrimaryClick = {
            if (isBudgetPickerVisible) onApplyBudget() else onConnect()
        },
        secondaryButtonText = stringResource(id = R.string.signer_connect_cancel_button),
        onSecondaryClick = {
            if (isBudgetPickerVisible) onCancelBudget() else onCancel()
        },
    )
}

@Composable
private fun HeaderSection(
    appName: String?,
    appIconUrl: String?,
    appIcon: Drawable?,
    appDescription: String?,
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (appIcon != null) {
            Image(
                modifier = Modifier
                    .size(48.dp)
                    .clip(AppTheme.shapes.small),
                bitmap = appIcon.toBitmap().asImageBitmap(),
                contentDescription = appName,
                contentScale = ContentScale.Crop,
            )
        } else {
            AppIconThumbnail(
                appIconUrl = appIconUrl,
                avatarSize = 48.dp,
                appName = appName,
            )
        }

        Text(
            text = appName ?: stringResource(id = R.string.signer_connect_unknown_app),
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 18.sp,
                lineHeight = 24.sp,
            ),
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.onPrimary,
        )

        appDescription?.let { description ->
            Text(
                text = description,
                style = AppTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                ),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignerConnectTabNavigation(
    selectedTab: SignerConnectTab,
    onTabChange: (SignerConnectTab) -> Unit,
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
        SignerConnectAppTab(
            text = stringResource(id = R.string.signer_connect_login_tab).uppercase(),
            selected = selectedTab == SignerConnectTab.Login,
            onClick = { onTabChange(SignerConnectTab.Login) },
        )

        SignerConnectAppTab(
            text = stringResource(id = R.string.signer_connect_permissions_tab).uppercase(),
            selected = selectedTab == SignerConnectTab.Permissions,
            onClick = { onTabChange(SignerConnectTab.Permissions) },
            enabled = permissionsTabEnabled,
        )
    }
}

@Composable
private fun SignerConnectAppTab(
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
private fun SignerConnectPages(
    modifier: Modifier = Modifier,
    selectedTab: SignerConnectTab,
    accounts: List<UserAccountUi>,
    selectedAccount: UserAccountUi?,
    trustLevel: TrustLevel,
    onAccountSelect: (String) -> Unit,
    onTrustLevelSelect: (TrustLevel) -> Unit,
    hasNwcRequest: Boolean,
    dailyBudget: Long?,
    showDailyBudgetPicker: Boolean,
    selectedDailyBudget: Long?,
    budgetToUsdMap: Map<Long, BigDecimal?>,
    onDailyBudgetClick: () -> Unit,
    onDailyBudgetChange: (Long?) -> Unit,
) {
    AnimatedContent(
        modifier = modifier.background(color = AppTheme.extraColorScheme.surfaceVariantAlt3),
        targetState = selectedTab,
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
            SignerConnectTab.Login -> LoginContent(
                accounts = accounts,
                selectedAccountPubkey = selectedAccount?.pubkey,
                onAccountClick = onAccountSelect,
            )

            SignerConnectTab.Permissions -> PermissionsContent(
                trustLevel = trustLevel,
                onTrustLevelClick = onTrustLevelSelect,
                hasNwcRequest = hasNwcRequest,
                dailyBudget = dailyBudget,
                onDailyBudgetClick = onDailyBudgetClick,
                showDailyBudgetPicker = showDailyBudgetPicker,
                selectedDailyBudget = selectedDailyBudget,
                onDailyBudgetChange = onDailyBudgetChange,
                budgetToUsdMap = budgetToUsdMap,
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
                text = stringResource(id = R.string.signer_connect_no_nsec_accounts_warning),
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
    trustLevel: TrustLevel,
    onTrustLevelClick: (TrustLevel) -> Unit,
    hasNwcRequest: Boolean,
    dailyBudget: Long?,
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
                hasNwcRequest = hasNwcRequest,
                dailyBudget = dailyBudget,
                onDailyBudgetClick = onDailyBudgetClick,
            )
        }
    }
}

@Composable
private fun PermissionsList(
    trustLevel: TrustLevel,
    onTrustLevelClick: (TrustLevel) -> Unit,
    hasNwcRequest: Boolean,
    dailyBudget: Long?,
    onDailyBudgetClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PermissionsListItem(
            icon = PrimalIcons.HighSecurity,
            title = stringResource(id = R.string.signer_connect_full_trust_title),
            subtitle = stringResource(id = R.string.signer_connect_full_trust_subtitle),
            isSelected = trustLevel == TrustLevel.Full,
            onClick = { onTrustLevelClick(TrustLevel.Full) },
        )
        PermissionsListItem(
            icon = PrimalIcons.MediumSecurity,
            title = stringResource(id = R.string.signer_connect_medium_trust_title),
            subtitle = stringResource(id = R.string.signer_connect_medium_trust_subtitle),
            isSelected = trustLevel == TrustLevel.Medium,
            onClick = { onTrustLevelClick(TrustLevel.Medium) },
        )
        PermissionsListItem(
            icon = PrimalIcons.LowSecurity,
            title = stringResource(id = R.string.signer_connect_low_trust_title),
            subtitle = stringResource(id = R.string.signer_connect_low_trust_subtitle),
            isSelected = trustLevel == TrustLevel.Low,
            onClick = { onTrustLevelClick(TrustLevel.Low) },
        )

        if (hasNwcRequest) {
            PrimalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DailyBudgetListItem(
                dailyBudget = dailyBudget,
                onClick = onDailyBudgetClick,
            )
        }
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
                    if (dailyBudget != null) {
                        Text(
                            text = NumberFormat.getNumberInstance().format(dailyBudget),
                            color = AppTheme.colorScheme.onPrimary,
                            style = style,
                        )
                        Text(
                            text = stringResource(id = R.string.nostr_connect_sats_unit),
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            style = style,
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.nostr_connect_no_limit),
                            color = AppTheme.colorScheme.onPrimary,
                            style = style,
                        )
                    }
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
            containerColor = AppTheme.colorScheme.outline,
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

object SignerConnectBottomSheet {
    val DAILY_BUDGET_OPTIONS = listOf(0L, 1000L, 5000L, 10_000L, 20_000L, 50_000L, 100_000L)
    val DAILY_BUDGET_PICKER_OPTIONS = DAILY_BUDGET_OPTIONS + listOf(null)
}
