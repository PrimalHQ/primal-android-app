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
import net.primal.android.R
import net.primal.android.core.compose.HeightAdjustableLoadingGridPlaceholder
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowUpLeftBlue
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.theme.AppTheme

private val SECTION_TOP_SPACING = 24.dp
private val SECTION_HORIZONTAL_PADDING = 16.dp
private val SECTION_INNER_SPACING = 16.dp
private const val POPULAR_USERS_COLUMNS = 4
private const val POPULAR_USERS_LOADING_ROWS = 3
private val POPULAR_USERS_LOADING_CELL_HEIGHT = 44.dp

@Composable
fun ExploreLanding(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    onProfileClick: (profileId: String) -> Unit,
    onRecentSearchEditClick: (query: String) -> Unit,
    onRecentSearchExecuteClick: (query: String) -> Unit,
) {
    val viewModel: ExploreLandingViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsState()

    ExploreLanding(
        modifier = modifier,
        paddingValues = paddingValues,
        state = uiState,
        onProfileClick = onProfileClick,
        onRecentSearchEditClick = onRecentSearchEditClick,
        onRecentSearchExecuteClick = onRecentSearchExecuteClick,
    )
}

@Composable
private fun ExploreLanding(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    state: ExploreLandingContract.UiState,
    onProfileClick: (profileId: String) -> Unit,
    onRecentSearchEditClick: (query: String) -> Unit,
    onRecentSearchExecuteClick: (query: String) -> Unit,
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
                isLoading = state.recentSearchesLoading,
                onArrowClick = onRecentSearchEditClick,
                onRowClick = onRecentSearchExecuteClick,
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
        if (users.isEmpty()) {
            HeightAdjustableLoadingGridPlaceholder(
                rows = POPULAR_USERS_LOADING_ROWS,
                columns = POPULAR_USERS_COLUMNS,
                cellHeight = POPULAR_USERS_LOADING_CELL_HEIGHT,
                cellShape = CircleShape,
            )
        } else {
            PopularUsersGrid(users = users, onUserClick = onUserClick)
        }
    }
}

@Composable
private fun PopularUsersGrid(users: List<UserProfileItemUi>, onUserClick: (profileId: String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SECTION_INNER_SPACING),
    ) {
        for (rowUsers in users.chunked(POPULAR_USERS_COLUMNS)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowUsers.forEach { user ->
                    UserCell(
                        modifier = Modifier.weight(1f),
                        user = user,
                        onClick = { onUserClick(user.profileId) },
                    )
                }
                repeat(POPULAR_USERS_COLUMNS - rowUsers.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
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
private fun RecentSearchesSection(
    queries: List<String>,
    isLoading: Boolean,
    onArrowClick: (query: String) -> Unit,
    onRowClick: (query: String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = SECTION_HORIZONTAL_PADDING)) {
        Text(
            text = stringResource(id = R.string.explore_landing_recent_searches_title),
            style = AppTheme.typography.titleMedium.copy(lineHeight = 16.sp),
            color = AppTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        when {
            isLoading -> Unit
            queries.isEmpty() -> Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 12.dp),
                text = stringResource(id = R.string.explore_landing_recent_searches_empty),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                textAlign = TextAlign.Center,
            )
            else -> queries.forEach { query ->
                RecentSearchRow(
                    query = query,
                    onArrowClick = { onArrowClick(query) },
                    onRowClick = { onRowClick(query) },
                )
                HorizontalDivider(color = AppTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun RecentSearchRow(
    query: String,
    onArrowClick: () -> Unit,
    onRowClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clickable(onClick = onRowClick)
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
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(36.dp)
                .clickable(onClick = onArrowClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(12.dp),
                imageVector = PrimalIcons.ArrowUpLeftBlue,
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
    }
}
