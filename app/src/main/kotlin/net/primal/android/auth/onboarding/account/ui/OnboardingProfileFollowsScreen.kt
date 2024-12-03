package net.primal.android.auth.onboarding.account.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.auth.compose.OnboardingBottomBar
import net.primal.android.auth.compose.onboardingTextHintTypography
import net.primal.android.auth.onboarding.account.OnboardingContract
import net.primal.android.auth.onboarding.account.OnboardingStep
import net.primal.android.auth.onboarding.account.ui.model.FollowGroup
import net.primal.android.auth.onboarding.account.ui.model.FollowGroupMember
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun OnboardingProfileFollowsScreen(
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
    onBack: () -> Unit,
) {
    fun backSequence() {
        if (state.customizeFollows) {
            eventPublisher(OnboardingContract.UiEvent.SetFollowsCustomizing(customizing = false))
        } else {
            onBack()
            eventPublisher(OnboardingContract.UiEvent.KeepRecommendedFollows)
        }
    }
    BackHandler { backSequence() }

    var shouldCustomize by remember { mutableStateOf(state.customizeFollows) }
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
                targetState = state.customizeFollows,
                label = "OnboardingProfileFollowsContent",
            ) { editMode ->
                when (editMode) {
                    false -> {
                        val followsCount by remember(state.selectedSuggestions) {
                            mutableIntStateOf(
                                state.selectedSuggestions.flatMap { it.members }.size,
                            )
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
                                .wrapContentHeight()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp, bottom = 8.dp)
                                .clip(AppTheme.shapes.large),
                            selectedSuggestions = state.selectedSuggestions,
                            onGroupClick = { groupName ->
                                eventPublisher(
                                    OnboardingContract.UiEvent.ToggleGroupFollowEvent(groupName = groupName),
                                )
                            },
                            onMemberClick = { groupName, userId ->
                                eventPublisher(
                                    OnboardingContract.UiEvent.ToggleFollowEvent(
                                        groupName = groupName,
                                        userId = userId,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        },
        bottomBar = {
            OnboardingBottomBar(
                buttonText = stringResource(id = R.string.onboarding_button_next),
                buttonEnabled = state.selectedSuggestions.isNotEmpty(),
                onButtonClick = {
                    if (state.customizeFollows) {
                        eventPublisher(OnboardingContract.UiEvent.RequestNextStep)
                    } else {
                        if (shouldCustomize) {
                            eventPublisher(OnboardingContract.UiEvent.SetFollowsCustomizing(customizing = true))
                        } else {
                            eventPublisher(OnboardingContract.UiEvent.KeepRecommendedFollows)
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
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            )
        },
    )
}

private val FollowsSwitchBorder = Color(0xFFD9D9D9)

private val FollowsCustomizationForegroundColor = Color(0xFF111111)
private val FollowsCustomizationForegroundAltColor = Color(0xFF666666)

private val UnfollowMemberBackgroundColor = Color(0xFFE5E5E5)

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileAccountFollowsCustomizationContent(
    modifier: Modifier,
    selectedSuggestions: List<FollowGroup>,
    onGroupClick: (groupName: String) -> Unit,
    onMemberClick: (groupName: String, userId: String) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        val groups = selectedSuggestions.associateBy { it.name }
        groups.forEach { group ->
            stickyHeader {
                FollowGroupListItem(
                    group = group.value,
                    onClick = { onGroupClick(group.value.name) },
                )
            }

            items(items = group.value.members) { member ->
                FollowGroupMemberListItem(
                    member = member,
                    onClick = { onMemberClick(group.value.name, member.userId) },
                )
            }
        }
    }
}

@Composable
private fun FollowGroupListItem(group: FollowGroup, onClick: () -> Unit) {
    val isGroupFollowed = group.members.all { it.followed }
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.7f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        headlineContent = {
            Text(
                text = group.name,
                maxLines = 2,
                style = AppTheme.typography.bodyMedium,
                color = FollowsCustomizationForegroundColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        trailingContent = {
            PrimalFilledButton(
                modifier = Modifier
                    .wrapContentWidth()
                    .size(width = 120.dp, height = 36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                containerColor = if (isGroupFollowed) {
                    Color.White
                } else {
                    Color.White
                },
                contentColor = if (isGroupFollowed) {
                    FollowsCustomizationForegroundColor
                } else {
                    FollowsCustomizationForegroundColor
                },
                textStyle = AppTheme.typography.titleMedium.copy(
                    lineHeight = 18.sp,
                ),
                onClick = onClick,
            ) {
                val text = if (isGroupFollowed) {
                    stringResource(id = R.string.onboarding_profile_unfollow_all_button)
                } else {
                    stringResource(id = R.string.onboarding_profile_follow_all_button)
                }
                Text(text = text.lowercase())
            }
        },
    )
}

@Composable
private fun FollowGroupMemberListItem(member: FollowGroupMember, onClick: () -> Unit) {
    val authorInternetIdentifier = member.metadata?.nip05
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = Color.White,
        ),
        leadingContent = {
            UniversalAvatarThumbnail(
                avatarCdnImage = member.metadata?.picture?.let { CdnImage(sourceUrl = it) },
            )
        },
        headlineContent = {
            NostrUserText(
                displayName = member.name,
                displayNameColor = FollowsCustomizationForegroundColor,
                fontSize = 14.sp,
                internetIdentifier = member.metadata?.nip05,
            )
        },
        supportingContent = {
            if (!authorInternetIdentifier.isNullOrEmpty()) {
                Text(
                    text = authorInternetIdentifier.formatNip05Identifier(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = AppTheme.typography.bodyMedium,
                    color = FollowsCustomizationForegroundAltColor,
                    fontSize = 14.sp,
                )
            }
        },
        trailingContent = {
            PrimalFilledButton(
                modifier = Modifier
                    .wrapContentWidth()
                    .size(width = 96.dp, height = 36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                containerColor = if (member.followed) {
                    UnfollowMemberBackgroundColor
                } else {
                    UnfollowMemberBackgroundColor
                },
                contentColor = if (member.followed) {
                    FollowsCustomizationForegroundColor
                } else {
                    FollowsCustomizationForegroundColor
                },
                textStyle = AppTheme.typography.titleMedium.copy(
                    lineHeight = 14.sp,
                ),
                onClick = onClick,
            ) {
                val text = if (member.followed) {
                    stringResource(id = R.string.onboarding_profile_unfollow_button)
                } else {
                    stringResource(id = R.string.onboarding_profile_follow_button)
                }
                Text(text = text.lowercase())
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PreviewOnboardingFollowsNoticeScreen() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        ColumnWithBackground(
            backgroundPainter = painterResource(id = R.drawable.onboarding_spot4),
        ) {
            OnboardingProfileFollowsScreen(
                state = OnboardingContract.UiState(
                    selectedSuggestions = listOf(
                        FollowGroup(
                            name = "Bitcoin",
                            members = listOf(
                                FollowGroupMember(
                                    name = "Princ Filip",
                                    userId = "npub198q8ksyxpurd7lq6mf409nrtf32pka48yp2z6lhxghpqe9zjllfq5wtwcp",
                                ),
                                FollowGroupMember(
                                    name = "ODELL",
                                    userId = "npub1qny3tkh0acurzla8x3zy4nhrjz5zd8l9sy9jys09umwng00manysew95gx",
                                ),
                            ),
                        ),
                        FollowGroup(
                            name = "Memes",
                            members = listOf(
                                FollowGroupMember(
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
private fun PreviewOnboardingFollowsCustomizationScreen() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        ColumnWithBackground(
            backgroundPainter = painterResource(id = R.drawable.onboarding_spot4),
        ) {
            OnboardingProfileFollowsScreen(
                state = OnboardingContract.UiState(
                    customizeFollows = true,
                    selectedSuggestions = listOf(
                        FollowGroup(
                            name = "Bitcoin",
                            members = listOf(
                                FollowGroupMember(
                                    name = "Princ Filip",
                                    userId = "npub198q8ksyxpurd7lq6mf409nrtf32pka48yp2z6lhxghpqe9zjllfq5wtwcp",
                                    metadata = ContentMetadata(
                                        name = "Princ Filip",
                                        nip05 = "princfilip@primal.net",
                                    ),
                                    followed = true,
                                ),
                                FollowGroupMember(
                                    name = "ODELL",
                                    userId = "npub1qny3tkh0acurzla8x3zy4nhrjz5zd8l9sy9jys09umwng00manysew95gx",
                                    metadata = ContentMetadata(
                                        name = "ODELL",
                                        nip05 = "odell@primal.net",
                                    ),
                                    followed = false,
                                ),
                            ),
                        ),
                        FollowGroup(
                            name = "Memes",
                            members = listOf(
                                FollowGroupMember(
                                    name = "corndalorian",
                                    userId = "npub1lrnvvs6z78s9yjqxxr38uyqkmn34lsaxznnqgd877j4z2qej3j5s09qnw5",
                                    metadata = ContentMetadata(
                                        name = "corndalorian",
                                        nip05 = "corndalorian@primal.net",
                                    ),
                                    followed = true,
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
