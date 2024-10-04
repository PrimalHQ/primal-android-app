package net.primal.android.explore.home.people

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.button.FollowUnfollowButton
import net.primal.android.core.utils.shortened
import net.primal.android.explore.api.model.ExplorePeopleData
import net.primal.android.theme.AppTheme


@Composable
fun ExplorePeople(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    onProfileClick: (String) -> Unit,
) {
    val viewModel: ExplorePeopleViewModel = hiltViewModel()
    val uiState = viewModel.state.collectAsState()

    ExplorePeople(
        modifier = modifier,
        paddingValues = paddingValues,
        state = uiState.value,
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
        PrimalLoadingSpinner()

    } else if (state.error != null) {
        ListNoContent(
            modifier = Modifier.fillMaxSize(),
            noContentText = stringResource(id = R.string.feed_error_loading),
            refreshButtonVisible = true,
            onRefresh = { eventPublisher(ExplorePeopleContract.UiEvent.RefreshPeople) },
        )
    } else if (state.people.isEmpty()) {
        ListNoContent(
            modifier = Modifier.fillMaxSize(),
            noContentText = stringResource(id = R.string.feed_no_content),
            refreshButtonVisible = true,
            onRefresh = { eventPublisher(ExplorePeopleContract.UiEvent.RefreshPeople) },
        )
    } else {
        LazyColumn(
            modifier = modifier
                .padding(paddingValues)
                .padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = state.people,
            ) { item ->
                ExplorePersonListItem(
                    person = item,
                    isFollowed = state.userFollowing.contains(item.profile.pubkey),
                    onItemClick = { onProfileClick(item.profile.pubkey) },
                    onFollowClick = { eventPublisher(ExplorePeopleContract.UiEvent.FollowUser(item.profile.pubkey)) },
                    onUnfollowClick = { eventPublisher(ExplorePeopleContract.UiEvent.UnfollowUser(item.profile.pubkey)) },
                )
            }
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
        ListItem(
            colors = ListItemDefaults.colors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
            ),
            leadingContent = {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                ) {
                    AvatarThumbnail(
                        avatarSize = 72.dp,
                        avatarCdnImage = person.profile.avatarCdnImage,
                    )
                }
            },
            headlineContent = {
                ProfileDetailsColumn(person)
            },
        )
        Row(
            modifier = Modifier
                .padding(start = 12.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FollowUnfollowButton(
                unfollowTextStyle = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                followTextStyle = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.size(width = 78.dp, height = 36.dp),
                isFollowed = isFollowed,
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
private fun FollowersIndicator(
    followersCount: Int,
    increaseCount: Int,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = AppTheme.typography.bodyMedium.fontSize,
                ),
            ) {
                append(followersCount.shortened())
            }
            withStyle(
                style = SpanStyle(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    fontSize = AppTheme.typography.bodyMedium.fontSize,
                ),
            ) {
                append(" " + stringResource(id = R.string.drawer_followers_suffix))
            }
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    baselineShift = BaselineShift.Superscript,
                    fontSize = AppTheme.typography.bodyMedium.fontSize,
                ),
            ) {
                append(" +${increaseCount.shortened()}")
            }
        },
    )
}

@Composable
private fun ProfileDetailsColumn(person: ExplorePeopleData) {
    Column {
        NostrUserText(
            displayName = person.profile.userDisplayName,
            internetIdentifier = person.profile.internetIdentifier,
        )
        person.profile.internetIdentifier?.let {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = person.profile.internetIdentifier,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        person.profile.about?.let {
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = person.profile.about,
                style = AppTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
