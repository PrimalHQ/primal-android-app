package net.primal.android.feed.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.primal.android.feed.ui.FeedPostUi

@Composable
fun FeedPostListItem(
    data: FeedPostUi,
) {
    Text(
        text = data.content,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {

        }
    )
}