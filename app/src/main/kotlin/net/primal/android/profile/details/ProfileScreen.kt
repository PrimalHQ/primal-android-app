package net.primal.android.profile.details

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.feed.feed.FeedPostList
import net.primal.android.nostr.ext.asEllipsizedNpub
import net.primal.android.theme.AppTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ProfileScreen(
        uiState = uiState.value,
        onClose = onClose,
        onPostClick = onPostClick,
        onProfileClick = onProfileClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileContract.UiState,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                navigationIcon = {
                    AppBarIcon(
                        icon = PrimalIcons.ArrowBack,
                        onClick = onClose,
                    )
                },
                title = {
                    Text(
                        text = uiState.profileDetails?.displayName
                            ?: uiState.profileId.asEllipsizedNpub()
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppTheme.colorScheme.surface,
                    scrolledContainerColor = AppTheme.colorScheme.surface,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
        content = { paddingValues ->
            FeedPostList(
                posts = uiState.posts,
                paddingValues = paddingValues,
                feedListState = listState,
                onPostClick = onPostClick,
                onProfileClick = { postId ->
                    if (postId != uiState.profileId) {
                        onProfileClick(postId)
                    }
                },
            )
        },
    )
}
