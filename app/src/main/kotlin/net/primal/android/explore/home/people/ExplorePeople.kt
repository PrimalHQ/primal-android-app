package net.primal.android.explore.home.people

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.HeightAdjustableLoadingListPlaceholder
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.button.FollowUnfollowButton
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.errors.UiError
import net.primal.android.core.utils.shortened
import net.primal.android.explore.api.model.ExplorePeopleData
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun ExplorePeople(
    modifier: Modifier = Modifier,
    onProfileClick: (String) -> Unit,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    onUiError: ((UiError) -> Unit)? = null,
) {
    val viewModel: ExplorePeopleViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(viewModel, uiState.error, onUiError) {
        uiState.error?.let { onUiError?.invoke(it) }
        viewModel.setEvent(ExplorePeopleContract.UiEvent.DismissError)
    }

    ExplorePeople(
        modifier = modifier,
        paddingValues = paddingValues,
        state = uiState,
        eventPublisher = viewModel::setEvent,
        onProfileClick = onProfileClick,
    )
}

@Composable
fun ExplorePeople(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    state: ExplorePeopleContract.UiState,
    eventPublisher: (ExplorePeopleContract.UiEvent) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    if (state.loading && state.people.isEmpty()) {
        HeightAdjustableLoadingListPlaceholder(
            modifier = Modifier.fillMaxSize(),
            contentPaddingValues = paddingValues,
            clipShape = AppTheme.shapes.small,
            height = 132.dp,
        )
    } else if (state.people.isEmpty()) {
        ListNoContent(
            modifier = Modifier.fillMaxSize(),
            noContentText = stringResource(id = R.string.explore_trending_people_no_content),
            refreshButtonVisible = true,
            onRefresh = { eventPublisher(ExplorePeopleContract.UiEvent.RefreshPeople) },
        )
    } else {
        LazyColumn(
            modifier = modifier.padding(horizontal = 12.dp),
            contentPadding = paddingValues,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            items(
                items = state.people,
                key = { it.profile.pubkey },
            ) { item ->
                ExplorePersonListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    person = item,
                    isFollowed = state.userFollowing.contains(item.profile.pubkey),
                    onItemClick = { onProfileClick(item.profile.pubkey) },
                    onFollowClick = {
                        eventPublisher(
                            ExplorePeopleContract.UiEvent.FollowUser(item.profile.pubkey),
                        )
                    },
                    onUnfollowClick = {
                        eventPublisher(
                            ExplorePeopleContract.UiEvent.UnfollowUser(item.profile.pubkey),
                        )
                    },
                )
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
    }
}

@Composable
private fun ExplorePersonListItem(
    modifier: Modifier = Modifier,
    person: ExplorePeopleData,
    isFollowed: Boolean,
    onItemClick: () -> Unit,
    onFollowClick: () -> Unit,
    onUnfollowClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.small)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
            .clickable { onItemClick() }
            .padding(vertical = 12.dp),
    ) {
        Row {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                AvatarThumbnail(
                    modifier = Modifier.padding(bottom = 24.dp),
                    avatarSize = 64.dp,
                    avatarCdnImage = person.profile.avatarCdnImage,
                    onClick = onItemClick,
                )
            }

            Column(
                modifier = Modifier.padding(end = 16.dp),
            ) {
                NostrUserText(
                    displayName = person.profile.userDisplayName,
                    internetIdentifier = person.profile.internetIdentifier,
                )
                person.profile.internetIdentifier?.let {
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = person.profile.internetIdentifier,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                        style = AppTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                person.profile.about?.let {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = person.profile.about,
                        style = AppTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FollowUnfollowButton(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(width = 64.dp, height = 36.dp),
                isFollowed = isFollowed,
                textStyle = AppTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                onClick = {
                    if (isFollowed) {
                        onUnfollowClick()
                    } else {
                        onFollowClick()
                    }
                },
            )

            FollowersIndicator(
                followersCount = person.userFollowersCount,
                increaseCount = person.followersIncrease,
            )
        }
    }
}

@Composable
private fun FollowersIndicator(followersCount: Int, increaseCount: Int) {
    Text(
        modifier = Modifier.padding(bottom = 4.dp),
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = AppTheme.typography.bodySmall.fontSize,
                ),
            ) {
                append(followersCount.shortened())
            }
            withStyle(
                style = SpanStyle(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    fontSize = AppTheme.typography.bodySmall.fontSize,
                ),
            ) {
                append(" " + stringResource(id = R.string.drawer_followers_suffix))
            }
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    baselineShift = BaselineShift.Superscript,
                    fontSize = AppTheme.typography.bodySmall.fontSize,
                ),
            ) {
                append(" +${increaseCount.shortened()}")
            }
        },
    )
}

@Preview
@Composable
fun PreviewExplorePersonListItem() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        Surface {
            Column {
                ExplorePersonListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    person = ExplorePeopleData(
                        profile = ProfileDetailsUi(
                            pubkey = "",
                            authorDisplayName = "miljan",
                            userDisplayName = "miljan",
                            lightningAddress = "miljan@primal.net",
                            internetIdentifier = "miljan@primal.net",
                            about = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
                                "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ",
                        ),
                        userScore = 1.0f,
                        userFollowersCount = 212,
                        followersIncrease = 23,
                        verifiedFollowersCount = 215,
                    ),
                    isFollowed = false,
                    onItemClick = {},
                    onFollowClick = {},
                    onUnfollowClick = {},
                )
            }
        }
    }
}
