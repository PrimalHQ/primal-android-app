package net.primal.android.auth.create.ui.steps

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.auth.create.CreateAccountContract
import net.primal.android.auth.create.ui.CreateAccountContent
import net.primal.android.auth.create.ui.RecommendedFollow
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FollowRecommendedAccountsStep(
    state: CreateAccountContract.UiState,
    eventPublisher: (CreateAccountContract.UiEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
    ) {
        if (state.loading && state.recommendedFollows.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    PrimalLoadingSpinner()
                }
            }
        } else {
            val follows = state.recommendedFollows.groupBy { it.groupName }
            follows.forEach { group ->
                val isGroupFollowed = isGroupFollowed(
                    data = state.recommendedFollows,
                    groupName = group.key,
                )
                stickyHeader {
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        headlineContent = {
                            Text(
                                text = group.key,
                                maxLines = 2,
                                style = AppTheme.typography.bodyMedium,
                            )
                        },
                        trailingContent = {
                            PrimalFilledButton(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .height(36.dp),
                                containerColor = if (isGroupFollowed) {
                                    AppTheme.extraColorScheme.surfaceVariantAlt2
                                } else {
                                    AppTheme.colorScheme.onSurface
                                },
                                contentColor = if (isGroupFollowed) {
                                    AppTheme.colorScheme.onSurface
                                } else {
                                    AppTheme.colorScheme.surface
                                },
                                textStyle = AppTheme.typography.titleMedium.copy(
                                    lineHeight = 18.sp,
                                ),
                                onClick = {
                                    eventPublisher(
                                        CreateAccountContract.UiEvent.ToggleGroupFollowEvent(
                                            groupName = group.key,
                                        ),
                                    )
                                },
                            ) {
                                val text = if (isGroupFollowed) {
                                    stringResource(id = R.string.create_recommended_unfollow_all)
                                } else {
                                    stringResource(id = R.string.create_recommended_follow_all)
                                }
                                Text(text = text.lowercase())
                            }
                        },
                    )
                }

                items(group.value) { suggestion ->
                    val authorInternetIdentifier = suggestion.content.nip05
                    val isSuggestionFollowed = isSuggestionFollowed(
                        data = state.recommendedFollows,
                        suggestion = suggestion,
                    )
                    ListItem(
                        leadingContent = {
                            AvatarThumbnail(
                                modifier = Modifier.padding(start = 8.dp),
                                avatarCdnImage = suggestion.content.picture?.let { CdnImage(sourceUrl = it) },
                            )
                        },
                        headlineContent = {
                            NostrUserText(
                                displayName = suggestion.content.usernameUiFriendly(
                                    pubkey = suggestion.pubkey,
                                ),
                                fontSize = 14.sp,
                                internetIdentifier = suggestion.content.nip05,
                            )
                        },
                        supportingContent = {
                            if (!authorInternetIdentifier.isNullOrEmpty()) {
                                Text(
                                    text = suggestion.content.nip05.formatNip05Identifier(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = AppTheme.typography.bodyMedium,
                                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                                    fontSize = 14.sp,
                                )
                            }
                        },
                        trailingContent = {
                            PrimalFilledButton(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .height(36.dp)
                                    .padding(end = 8.dp),
                                containerColor = if (isSuggestionFollowed) {
                                    AppTheme.extraColorScheme.surfaceVariantAlt1
                                } else {
                                    AppTheme.colorScheme.onSurface
                                },
                                contentColor = if (isSuggestionFollowed) {
                                    AppTheme.colorScheme.onSurface
                                } else {
                                    AppTheme.colorScheme.surface
                                },
                                textStyle = AppTheme.typography.titleMedium.copy(
                                    lineHeight = 14.sp,
                                ),
                                onClick = {
                                    eventPublisher(
                                        CreateAccountContract.UiEvent.ToggleFollowEvent(
                                            groupName = group.key,
                                            pubkey = suggestion.pubkey,
                                        ),
                                    )
                                },
                            ) {
                                val text = if (isSuggestionFollowed) {
                                    stringResource(id = R.string.create_recommended_unfollow)
                                } else {
                                    stringResource(id = R.string.create_recommended_follow)
                                }
                                Text(text = text.lowercase())
                            }
                        },
                    )
                }
            }
        }
    }
}

private fun isSuggestionFollowed(data: List<RecommendedFollow>, suggestion: RecommendedFollow) =
    data
        .first { it.pubkey == suggestion.pubkey && it.groupName == suggestion.groupName }
        .isCurrentUserFollowing

private fun isGroupFollowed(data: List<RecommendedFollow>, groupName: String) =
    data
        .filter { it.groupName == groupName }
        .all { it.isCurrentUserFollowing }

@Preview
@Composable
fun PreviewFollowRecommendedAccountsStep() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        val state = CreateAccountContract.UiState(
            currentStep = CreateAccountContract.UiState.CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS,
            recommendedFollows = listOf(
                RecommendedFollow(
                    pubkey = "88c124151safasf",
                    isCurrentUserFollowing = false,
                    groupName = "Nostr",
                    content = ContentMetadata(
                        name = "jack",
                        displayName = "Jack",
                        nip05 = "jack@cash.app",
                    ),
                ),
                RecommendedFollow(
                    pubkey = "88c124151safasf",
                    isCurrentUserFollowing = false,
                    groupName = "Primal",
                    content = ContentMetadata(
                        name = "miljan",
                        displayName = "miljan",
                        nip05 = "miljan@primal.net",
                    ),
                ),
                RecommendedFollow(
                    pubkey = "88c124151safasf",
                    isCurrentUserFollowing = false,
                    groupName = "Primal",
                    content = ContentMetadata(
                        name = "qauser",
                        displayName = "qauser",
                        nip05 = "qa@primal.net",
                    ),
                ),
                RecommendedFollow(
                    pubkey = "88c124151safasf",
                    isCurrentUserFollowing = true,
                    groupName = "Bitcoin",
                    content = ContentMetadata(
                        name = "Gigi",
                        displayName = "Gigi",
                        nip05 = "_@dergigi.com",
                    ),
                ),
                RecommendedFollow(
                    pubkey = "88c124151safasf",
                    isCurrentUserFollowing = true,
                    groupName = "Bitcoin",
                    content = ContentMetadata(
                        name = "satoshi",
                        displayName = "satoshi",
                        nip05 = "satoshi@nakamoto.net",
                    ),
                ),
            ),
        )
        CreateAccountContent(
            state = state,
            eventPublisher = {},
            paddingValues = PaddingValues(),
        )
    }
}
