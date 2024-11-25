package net.primal.android.auth.onboarding.account.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.auth.compose.OnboardingBottomBar
import net.primal.android.auth.compose.onboardingTextHintTypography
import net.primal.android.auth.onboarding.account.OnboardingContract
import net.primal.android.auth.onboarding.account.OnboardingStep
import net.primal.android.auth.onboarding.account.api.Suggestion
import net.primal.android.auth.onboarding.account.api.SuggestionMember
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun OnboardingProfileFollowsScreen(
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
    onBack: () -> Unit,
) {
    fun backSequence() {
        if (state.customizeSuggestions) {
            eventPublisher(OnboardingContract.UiEvent.SetFollowsCustomizing(customizing = false))
        } else {
            onBack()
        }
    }
    BackHandler { backSequence() }

    var shouldCustomize by remember { mutableStateOf(state.customizeSuggestions) }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            PrimalTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                title = stringResource(id = R.string.onboarding_title_your_follows),
                textColor = Color.White,
                showDivider = false,
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconTintColor = Color.White,
                onNavigationIconClick = { backSequence() },
            )
        },
        content = { paddingValues ->
            AnimatedContent(
                targetState = state.customizeSuggestions,
                label = "OnboardingProfileFollowsContent",
            ) { editMode ->
                when (editMode) {
                    false -> {
                        val followsCount by remember(state.suggestions) {
                            mutableIntStateOf(state.suggestions.sumOf { it.members.size })
                        }
                        ProfileAccountFollowsNoticeContent(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            followsCount = followsCount,
                            customizing = shouldCustomize,
                            onCustomizationPreferenceChanged = { shouldCustomize = it },
                        )
                    }

                    true -> {
                        ProfileAccountFollowsCustomizationContent(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            state = state,
                        )
                    }
                }
            }
        },
        bottomBar = {
            OnboardingBottomBar(
                buttonText = stringResource(id = R.string.onboarding_button_next),
                buttonEnabled = state.suggestions.isNotEmpty(),
                onButtonClick = {
                    if (state.customizeSuggestions) {
                        eventPublisher(OnboardingContract.UiEvent.RequestNextStep)
                    } else {
                        if (shouldCustomize) {
                            eventPublisher(OnboardingContract.UiEvent.SetFollowsCustomizing(customizing = true))
                        } else {
                            eventPublisher(OnboardingContract.UiEvent.RequestNextStep)
                        }
                    }
                },
                footer = { OnboardingStepsIndicator(currentPage = OnboardingStep.Interests.index) },
            )
        },
    )
}

@Composable
private fun ProfileAccountFollowsNoticeContent(
    modifier: Modifier,
    followsCount: Int,
    customizing: Boolean,
    onCustomizationPreferenceChanged: (Boolean) -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 56.dp, vertical = 32.dp),
            text = stringResource(id = R.string.onboarding_profile_follows_hint, followsCount),
            textAlign = TextAlign.Center,
            style = onboardingTextHintTypography(),
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            YourFollowsHintListItem(
                checked = !customizing,
                textHeadline = stringResource(R.string.onboarding_profile_follows_keep_follows_headline_text),
                textSupporting = stringResource(R.string.onboarding_profile_follows_keep_follows_support_text),
                onClick = { onCustomizationPreferenceChanged(false) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            YourFollowsHintListItem(
                checked = customizing,
                textHeadline = stringResource(R.string.onboarding_profile_follows_customize_follows_headline_text),
                textSupporting = stringResource(R.string.onboarding_profile_follows_customize_follows_support_text),
                onClick = { onCustomizationPreferenceChanged(true) },
            )
        }
    }
}

@Composable
private fun YourFollowsHintListItem(
    checked: Boolean,
    textHeadline: String,
    textSupporting: String,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .clip(shape = AppTheme.shapes.large)
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1.copy(alpha = 0.4f),
        ),
        leadingContent = {
            FollowsSwitch(
                checked = checked,
            )
        },
        headlineContent = {
            Text(
                text = textHeadline,
                style = AppTheme.typography.bodyMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        supportingContent = {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = textSupporting,
                style = AppTheme.typography.bodyMedium,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
            )
        },
    )
}

private val FollowsSwitchBorder = Color(0xFFD9D9D9)

@Composable
private fun FollowsSwitch(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = FollowsSwitchBorder,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(color = Color.White)
                    .border(
                        width = 6.dp,
                        color = AppTheme.extraColorScheme.surfaceVariantAlt1.copy(alpha = 0.9f),
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
fun ProfileAccountFollowsCustomizationContent(modifier: Modifier, state: OnboardingContract.UiState) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewOnboardingProfilePreviewScreen() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        ColumnWithBackground(
            backgroundPainter = painterResource(id = R.drawable.onboarding_spot4),
        ) {
            OnboardingProfileFollowsScreen(
                state = OnboardingContract.UiState(
                    suggestions = listOf(
                        Suggestion(
                            group = "Bitcoin",
                            members = listOf(
                                SuggestionMember(
                                    name = "Princ Filip",
                                    userId = "npub198q8ksyxpurd7lq6mf409nrtf32pka48yp2z6lhxghpqe9zjllfq5wtwcp",
                                ),
                                SuggestionMember(
                                    name = "ODELL",
                                    userId = "npub1qny3tkh0acurzla8x3zy4nhrjz5zd8l9sy9jys09umwng00manysew95gx",
                                ),
                            ),
                        ),
                        Suggestion(
                            group = "Memes",
                            members = listOf(
                                SuggestionMember(
                                    name = "corndalorian",
                                    userId = "npub1lrnvvs6z78s9yjqxxr38uyqkmn34lsaxznnqgd877j4z2qej3j5s09qnw5",
                                ),
                            ),
                        ),
                    ),
                ),
                eventPublisher = {},
                onBack = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewOnboardingProfileSuccessScreen() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        ColumnWithBackground(
            backgroundPainter = painterResource(id = R.drawable.onboarding_spot4),
        ) {
            OnboardingProfileFollowsScreen(
                state = OnboardingContract.UiState(
                    suggestions = emptyList(),
                    customizeSuggestions = true,
                ),
                eventPublisher = {},
                onBack = {},
            )
        }
    }
}
