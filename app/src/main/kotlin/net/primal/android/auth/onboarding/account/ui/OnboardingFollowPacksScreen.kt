package net.primal.android.auth.onboarding.account.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.auth.compose.OnboardingBottomBar
import net.primal.android.auth.onboarding.account.OnboardingContract
import net.primal.android.auth.onboarding.account.OnboardingStep
import net.primal.android.core.compose.PrimalDarkTextColor
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

private const val BadgeDisabledAlpha = 0.2f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFollowPacksScreen(
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
    onBack: () -> Unit,
) {
    PrimalScaffold(
        containerColor = Color.Transparent,
        topBar = {
            PrimalTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                title = stringResource(id = R.string.onboarding_follow_packs_title),
                textColor = PrimalDarkTextColor,
                showDivider = false,
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconTintColor = PrimalDarkTextColor,
                onNavigationIconClick = onBack,
                actions = {
                    FollowCountBadge(
                        count = state.followedUserIds.size,
                        enabled = state.followedUserIds.isNotEmpty(),
                    )
                },
            )
        },
        content = { paddingValues ->
            if (state.followPacks.isEmpty() && state.working) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    PrimalLoadingSpinner()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(
                        items = state.followPacks,
                        key = { it.name },
                    ) { pack ->
                        FollowPackCard(
                            pack = pack,
                            isExpanded = pack.name in state.expandedPackNames,
                            followedUserIds = state.followedUserIds,
                            onToggleExpanded = {
                                eventPublisher(OnboardingContract.UiEvent.TogglePackExpanded(pack.name))
                            },
                            onFollowUser = { userId ->
                                eventPublisher(OnboardingContract.UiEvent.ToggleFollowUser(userId))
                            },
                            onFollowAll = {
                                eventPublisher(OnboardingContract.UiEvent.ToggleFollowAllInPack(pack.name))
                            },
                        )
                    }
                }
            }
        },
        bottomBar = {
            OnboardingBottomBar(
                buttonText = stringResource(id = R.string.onboarding_button_next),
                buttonEnabled = state.followedUserIds.isNotEmpty(),
                onButtonClick = { eventPublisher(OnboardingContract.UiEvent.RequestNextStep) },
                footer = { OnboardingStepsIndicator(currentPage = OnboardingStep.FollowPacks.index) },
            )
        },
    )
}

@Composable
private fun FollowCountBadge(count: Int, enabled: Boolean) {
    val badgeAlpha = if (enabled) 1f else BadgeDisabledAlpha
    Box(
        modifier = Modifier
            .padding(end = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PrimalDarkTextColor.copy(alpha = badgeAlpha))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = AppTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
            ),
            color = Color.White,
        )
    }
}
