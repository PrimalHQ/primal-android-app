package net.primal.android.auth.onboarding.account.ui

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import net.primal.android.auth.onboarding.account.ui.model.OnboardingFollowPack
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
            FollowPacksContent(
                paddingValues = paddingValues,
                state = state,
                eventPublisher = eventPublisher,
            )
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
private fun FollowPacksContent(
    paddingValues: PaddingValues,
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
) {
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
        ) {
            state.followPacks.forEachIndexed { index, pack ->
                if (index > 0) {
                    item(key = "${pack.name}_spacer") {
                        Spacer(Modifier.height(16.dp))
                    }
                }
                followPackItems(
                    pack = pack,
                    isExpanded = pack.name in state.expandedPackNames,
                    followedUserIds = state.followedUserIds,
                    eventPublisher = eventPublisher,
                )
            }
        }
    }
}

private fun LazyListScope.followPackItems(
    pack: OnboardingFollowPack,
    isExpanded: Boolean,
    followedUserIds: Set<String>,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
) {
    item(key = pack.name) {
        FollowPackCard(
            pack = pack,
            isExpanded = isExpanded,
            followedUserIds = followedUserIds,
            onToggleExpanded = {
                eventPublisher(OnboardingContract.UiEvent.TogglePackExpanded(pack.name))
            },
            onFollowAll = {
                eventPublisher(OnboardingContract.UiEvent.ToggleFollowAllInPack(pack.name))
            },
        )
    }

    if (isExpanded) {
        item(key = "${pack.name}_divider") {
            HorizontalDivider(
                modifier = Modifier
                    .animateItem(fadeInSpec = spring(), fadeOutSpec = null, placementSpec = spring())
                    .background(Color.White),
                color = SubtleBorderColor,
            )
        }

        items(
            items = pack.members,
            key = { "${pack.name}_${it.userId}" },
        ) { member ->
            FollowPackMemberRow(
                modifier = Modifier
                    .animateItem(fadeInSpec = spring(), fadeOutSpec = null, placementSpec = spring())
                    .background(Color.White),
                member = member,
                isFollowed = member.userId in followedUserIds,
                onFollowClick = {
                    eventPublisher(OnboardingContract.UiEvent.ToggleFollowUser(member.userId))
                },
            )
        }

        item(key = "${pack.name}_bottom") {
            Box(
                modifier = Modifier
                    .animateItem(fadeInSpec = spring(), fadeOutSpec = null, placementSpec = spring())
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(Color.White),
            )
        }
    }
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
