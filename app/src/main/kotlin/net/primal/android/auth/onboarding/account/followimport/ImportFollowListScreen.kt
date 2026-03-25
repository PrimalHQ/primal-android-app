/*
 * Compose UI for follow list import during signup.
 *
 * Ported from Amethyst PR #1785 by mstrofnone, adapted for Primal's design system.
 * Original: https://github.com/vitorpamplona/amethyst/pull/1785
 *
 * SPDX-License-Identifier: MIT
 */
package net.primal.android.auth.onboarding.account.followimport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.auth.compose.OnboardingBottomBar
import net.primal.android.auth.onboarding.account.OnboardingStep
import net.primal.android.auth.onboarding.account.followimport.ImportFollowListContract.Phase
import net.primal.android.auth.onboarding.account.followimport.ImportFollowListContract.UiEvent
import net.primal.android.auth.onboarding.account.followimport.ImportFollowListContract.UiState
import net.primal.android.auth.onboarding.account.ui.OnboardingStepsIndicator
import net.primal.android.core.compose.PrimalDarkTextColor
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

private val NamecoinBlue = Color(0xFF4A90D9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportFollowListScreen(
    viewModel: ImportFollowListViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onApplyFollows: (List<FollowEntry>) -> Unit,
) {
    val uiState by viewModel.state.collectAsState()

    PrimalScaffold(
        containerColor = Color.Transparent,
        topBar = {
            PrimalTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                title = "Import Follow List",
                textColor = PrimalDarkTextColor,
                showDivider = false,
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconTintColor = PrimalDarkTextColor,
                onNavigationIconClick = onBack,
            )
        },
        content = { paddingValues ->
            ImportFollowListContent(
                paddingValues = paddingValues,
                state = uiState,
                eventPublisher = { viewModel.setEvent(it) },
            )
        },
        bottomBar = {
            ImportFollowListBottomBar(
                state = uiState,
                onSkip = onNext,
                onApply = {
                    val selected = viewModel.getSelectedAndApply()
                    if (selected.isNotEmpty()) {
                        onApplyFollows(selected)
                        viewModel.markDone(selected.size)
                    }
                },
                onSearchAnother = { viewModel.setEvent(UiEvent.Reset) },
                onContinue = onNext,
            )
        },
    )
}

@Composable
private fun ImportFollowListContent(
    paddingValues: PaddingValues,
    state: UiState,
    eventPublisher: (UiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        // Header description
        Text(
            text = "Start with a great feed by following the same people as someone you trust.",
            style = AppTheme.typography.bodyMedium,
            color = PrimalDarkTextColor.copy(alpha = 0.7f),
        )

        Spacer(Modifier.height(16.dp))

        // Input section
        InputSection(
            identifier = state.identifier,
            enabled = state.phase == Phase.Idle || state.phase == Phase.Error,
            onIdentifierChanged = { eventPublisher(UiEvent.IdentifierChanged(it)) },
            onLookup = { eventPublisher(UiEvent.StartImport) },
        )

        Spacer(Modifier.height(16.dp))

        // Main content area
        Box(modifier = Modifier.weight(1f)) {
            when (state.phase) {
                Phase.Idle -> IdleHint()
                Phase.Resolving -> LoadingIndicator("Resolving ${state.identifier}…")
                Phase.Fetching -> LoadingIndicator("Fetching follow list…")
                Phase.Preview -> PreviewList(
                    state = state,
                    onToggle = { eventPublisher(UiEvent.ToggleSelection(it)) },
                    onSelectAll = { eventPublisher(UiEvent.SetSelectAll(it)) },
                )
                Phase.Applying -> LoadingIndicator("Following ${state.selectedCount} accounts…")
                Phase.Done -> DoneMessage(state.appliedCount)
                Phase.Error -> ErrorMessage(
                    message = state.errorMessage ?: "An error occurred.",
                    onRetry = { eventPublisher(UiEvent.Reset) },
                )
            }
        }
    }
}

@Composable
private fun InputSection(
    identifier: String,
    enabled: Boolean,
    onIdentifierChanged: (String) -> Unit,
    onLookup: () -> Unit,
) {
    val kb = LocalSoftwareKeyboardController.current

    Column {
        OutlinedTextField(
            value = identifier,
            onValueChange = onIdentifierChanged,
            label = { Text("Profile to import from") },
            placeholder = { Text("npub1…, alice@example.com, or example.bit") },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                Text(
                    "Supports npub, NIP-05, hex, and Namecoin (.bit / d/ / id/)",
                    style = AppTheme.typography.bodySmall,
                    color = PrimalDarkTextColor.copy(alpha = 0.5f),
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                kb?.hide()
                onLookup()
            }),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                kb?.hide()
                onLookup()
            },
            enabled = enabled && identifier.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Look Up Follow List")
        }
    }
}

@Composable
private fun IdleHint() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = null,
                tint = PrimalDarkTextColor.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Enter the profile of a friend or community leader. " +
                    "You can use their npub, NIP-05 address, or a Namecoin name " +
                    "like alice@example.bit or id/alice for blockchain-verified identities.",
                style = AppTheme.typography.bodySmall,
                color = PrimalDarkTextColor.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun LoadingIndicator(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(Modifier.size(40.dp), strokeWidth = 3.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                message,
                style = AppTheme.typography.bodyMedium,
                color = PrimalDarkTextColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun PreviewList(
    state: UiState,
    onToggle: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        // Namecoin badge
        AnimatedVisibility(
            visible = state.namecoinSource != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            NamecoinResolvedBadge(state.namecoinSource ?: "")
        }

        // Summary
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${state.totalCount} accounts found",
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PrimalDarkTextColor,
            )
            Text(
                "${state.selectedCount} selected",
                style = AppTheme.typography.bodySmall,
                color = NamecoinBlue,
            )
        }

        // Select all
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onSelectAll(state.selectedCount < state.totalCount) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = state.selectedCount == state.totalCount,
                onCheckedChange = { onSelectAll(it) },
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Select All",
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = PrimalDarkTextColor,
            )
        }

        HorizontalDivider()

        LazyColumn(
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items = state.follows, key = { it.pubkeyHex }) { entry ->
                FollowEntryRow(
                    entry = entry,
                    isSelected = entry.pubkeyHex in state.selected,
                    onToggle = { onToggle(entry.pubkeyHex) },
                )
            }
        }
    }
}

@Composable
private fun NamecoinResolvedBadge(namecoinSource: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(
                color = NamecoinBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("\u26D3", fontSize = 16.sp) // ⛓ chain link
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                "Resolved via Namecoin",
                style = AppTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = NamecoinBlue,
            )
            Text(
                formatNamecoinDisplay(namecoinSource),
                style = AppTheme.typography.bodySmall,
                color = PrimalDarkTextColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun FollowEntryRow(
    entry: FollowEntry,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (isSelected) NamecoinBlue else PrimalDarkTextColor.copy(alpha = 0.3f),
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(NamecoinBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                entry.pubkeyHex.take(2).uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NamecoinBlue,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                entry.petname ?: shortPubkey(entry.pubkeyHex),
                style = AppTheme.typography.bodyMedium,
                fontWeight = if (entry.petname != null) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = PrimalDarkTextColor,
            )
            if (entry.petname != null) {
                Text(
                    shortPubkey(entry.pubkeyHex),
                    style = AppTheme.typography.bodySmall,
                    color = PrimalDarkTextColor.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (entry.relayHint != null) {
                Text(
                    entry.relayHint,
                    style = AppTheme.typography.bodySmall,
                    color = PrimalDarkTextColor.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DoneMessage(count: Int) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = NamecoinBlue,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Now following $count accounts",
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = PrimalDarkTextColor,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your feed is ready.",
                style = AppTheme.typography.bodyMedium,
                color = PrimalDarkTextColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                message,
                style = AppTheme.typography.bodyMedium,
                color = Color(0xFFCC4444),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onRetry) { Text("Try Again") }
        }
    }
}

@Composable
private fun ImportFollowListBottomBar(
    state: UiState,
    onSkip: () -> Unit,
    onApply: () -> Unit,
    onSearchAnother: () -> Unit,
    onContinue: () -> Unit,
) {
    when (state.phase) {
        Phase.Preview -> {
            OnboardingBottomBar(
                buttonText = "Follow ${state.selectedCount} accounts",
                buttonEnabled = state.selectedCount > 0,
                onButtonClick = onApply,
                footer = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = onSkip) {
                            Text("Skip", color = PrimalDarkTextColor.copy(alpha = 0.7f))
                        }
                        TextButton(onClick = onSearchAnother) {
                            Text("Search Another", color = PrimalDarkTextColor.copy(alpha = 0.7f))
                        }
                    }
                    OnboardingStepsIndicator(currentPage = OnboardingStep.ImportFollows.index)
                },
            )
        }

        Phase.Done -> {
            OnboardingBottomBar(
                buttonText = "Continue",
                onButtonClick = onContinue,
                footer = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(onClick = onSearchAnother) {
                            Text("Import More", color = PrimalDarkTextColor.copy(alpha = 0.7f))
                        }
                    }
                    OnboardingStepsIndicator(currentPage = OnboardingStep.ImportFollows.index)
                },
            )
        }

        Phase.Idle, Phase.Error -> {
            OnboardingBottomBar(
                buttonText = "Skip",
                onButtonClick = onSkip,
                footer = {
                    OnboardingStepsIndicator(currentPage = OnboardingStep.ImportFollows.index)
                },
            )
        }

        else -> {
            // Resolving, Fetching, Applying — no bottom bar actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OnboardingStepsIndicator(currentPage = OnboardingStep.ImportFollows.index)
            }
        }
    }
}

// Helpers

private fun shortPubkey(hex: String): String =
    if (hex.length < 12) hex else "npub:${hex.take(8)}…${hex.takeLast(4)}"

private fun formatNamecoinDisplay(source: String): String {
    val s = source.trim()
    return when {
        s.startsWith("d/", ignoreCase = true) -> "${s.removePrefix("d/")}.bit"
        s.startsWith("_@") -> s.removePrefix("_@")
        else -> s
    }
}


