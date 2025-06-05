package net.primal.android.explore.home.people

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import net.primal.android.R
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.handleRefreshLoadState
import net.primal.android.core.compose.isEmpty
import net.primal.android.explore.home.people.ui.FollowPackListItem

@Composable
fun ExplorePeople(
    modifier: Modifier = Modifier,
    onProfileClick: (String) -> Unit,
    onFollowPackClick: (profileId: String, identifier: String) -> Unit,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    val viewModel: ExplorePeopleViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsState()

    ExplorePeople(
        modifier = modifier,
        paddingValues = paddingValues,
        state = uiState,
        onProfileClick = onProfileClick,
        onFollowPackClick = onFollowPackClick,
    )
}

@Composable
fun ExplorePeople(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    state: ExplorePeopleContract.UiState,
    onFollowPackClick: (profileId: String, identifier: String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val pagingItems = state.followPacks.collectAsLazyPagingItems()
    val listState = pagingItems.rememberLazyListStatePagingWorkaround()
    val noContentText = stringResource(id = R.string.explore_people_no_follow_packs)

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        contentPadding = paddingValues,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        if (pagingItems.isEmpty()) {
            handleRefreshLoadState(
                pagingItems = pagingItems,
                noContentText = noContentText,
                noContentVerticalArrangement = Arrangement.Center,
                noContentPaddingValues = PaddingValues(0.dp),
            )
        }

        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey(key = { it.identifier + it.authorId }),
            contentType = pagingItems.itemContentType(),
        ) { index ->
            val item = pagingItems[index]

            if (item != null) {
                FollowPackListItem(
                    followPack = item,
                    onProfileClick = onProfileClick,
                    onClick = onFollowPackClick,
                )
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }
    }
}
