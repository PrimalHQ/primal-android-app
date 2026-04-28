package net.primal.android.main.explore.landing

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.foundation.fade
import io.github.fornewid.placeholder.material3.placeholder
import net.primal.android.R
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowUpLeftBlue
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.theme.AppTheme

private val SECTION_TOP_SPACING = 24.dp
private val SECTION_HORIZONTAL_PADDING = 16.dp
private val SECTION_INNER_SPACING = 16.dp
private const val POPULAR_USERS_COLUMNS = 4
private const val PLACEHOLDER_USERS_COUNT = 12
private const val PLACEHOLDER_TEXT_WIDTH_FRACTION = 0.7f

@Composable
fun ExploreLanding(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    onProfileClick: (profileId: String) -> Unit,
    onRecentSearchClick: (query: String) -> Unit,
) {
    val viewModel: ExploreLandingViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsState()

    ExploreLanding(
        modifier = modifier,
        paddingValues = paddingValues,
        state = uiState,
        onProfileClick = onProfileClick,
        onRecentSearchClick = onRecentSearchClick,
    )
}

@Composable
private fun ExploreLanding(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    state: ExploreLandingContract.UiState,
    onProfileClick: (profileId: String) -> Unit,
    onRecentSearchClick: (query: String) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = paddingValues,
    ) {
        item { Spacer(Modifier.height(SECTION_TOP_SPACING)) }
        item {
            RecentUsersSection(
                users = state.recommendedUsers,
                onUserClick = onProfileClick,
            )
        }
        item { Spacer(Modifier.height(SECTION_TOP_SPACING)) }
        item {
            RecentSearchesSection(
                queries = state.recentSearches,
                onQueryClick = onRecentSearchClick,
            )
        }
    }
}

@Composable
private fun RecentUsersSection(users: List<UserProfileItemUi>, onUserClick: (profileId: String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = SECTION_HORIZONTAL_PADDING),
        verticalArrangement = Arrangement.spacedBy(SECTION_INNER_SPACING),
    ) {
        Text(
            text = stringResource(id = R.string.explore_landing_recent_users_title),
            style = AppTheme.typography.titleMedium.copy(lineHeight = 16.sp),
            color = AppTheme.colorScheme.onSurface,
        )
        PopularUsersGrid(users = users, onUserClick = onUserClick)
    }
}

@Composable
private fun PopularUsersGrid(users: List<UserProfileItemUi>, onUserClick: (profileId: String) -> Unit) {
    val isLoading = users.isEmpty()
    val cellCount = if (isLoading) PLACEHOLDER_USERS_COUNT else users.size

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SECTION_INNER_SPACING),
    ) {
        for (rowIndices in (0 until cellCount).chunked(POPULAR_USERS_COLUMNS)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowIndices.forEach { index ->
                    if (isLoading) {
                        PlaceholderUserCell(modifier = Modifier.weight(1f))
                    } else {
                        UserCell(
                            modifier = Modifier.weight(1f),
                            user = users[index],
                            onClick = { onUserClick(users[index].profileId) },
                        )
                    }
                }
                repeat(POPULAR_USERS_COLUMNS - rowIndices.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PlaceholderUserCell(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Spacer(
            modifier = Modifier
                .size(44.dp)
                .placeholder(
                    visible = true,
                    color = AppTheme.colorScheme.surface,
                    shape = CircleShape,
                    highlight = PlaceholderHighlight.fade(
                        highlightColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    ),
                ),
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth(PLACEHOLDER_TEXT_WIDTH_FRACTION)
                .height(16.dp)
                .placeholder(
                    visible = true,
                    color = AppTheme.colorScheme.surface,
                    shape = AppTheme.shapes.extraSmall,
                    highlight = PlaceholderHighlight.fade(
                        highlightColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    ),
                ),
        )
    }
}

@Composable
private fun UserCell(
    modifier: Modifier = Modifier,
    user: UserProfileItemUi,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        UniversalAvatarThumbnail(
            avatarCdnImage = user.avatarCdnImage,
            avatarSize = 44.dp,
            avatarBlossoms = user.avatarBlossoms,
            legendaryCustomization = user.legendaryCustomization,
            isLive = user.isLive,
            onClick = onClick,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            text = user.displayName,
            style = AppTheme.typography.labelSmall,
            color = AppTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RecentSearchesSection(queries: List<String>, onQueryClick: (query: String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = SECTION_HORIZONTAL_PADDING)) {
        Text(
            text = stringResource(id = R.string.explore_landing_recent_searches_title),
            style = AppTheme.typography.titleMedium.copy(lineHeight = 16.sp),
            color = AppTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (queries.isEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                text = stringResource(id = R.string.explore_landing_recent_searches_empty),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                textAlign = TextAlign.Center,
            )
        } else {
            queries.forEach { query ->
                RecentSearchRow(query = query, onClick = { onQueryClick(query) })
                HorizontalDivider(color = AppTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun RecentSearchRow(query: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(end = 32.dp),
            text = query,
            style = AppTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                lineHeight = 16.sp,
            ),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(12.dp),
            imageVector = PrimalIcons.ArrowUpLeftBlue,
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}
