package net.primal.android.core.compose.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Quote
import net.primal.android.core.compose.icons.primaliconpack.Repost

@ExperimentalMaterial3Api
@Composable
fun RepostOrQuoteBottomSheet(
    onDismiss: () -> Unit,
    onRepostClick: () -> Unit,
    onPostQuoteClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        ListItem(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable {
                    onDismiss()
                    onRepostClick()
                },
            leadingContent = {
                Icon(imageVector = PrimalIcons.Repost, contentDescription = null)
            },
            headlineContent = {
                Text(text = stringResource(id = R.string.post_repost_button_confirmation))
            }
        )
        ListItem(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable {
                    onDismiss()
                    onPostQuoteClick()
                },
            leadingContent = {
                Icon(imageVector = PrimalIcons.Quote, contentDescription = null)
            },
            headlineContent = {
                Text(text = stringResource(id = R.string.post_quote_button_confirmation))
            }
        )
    }
}
