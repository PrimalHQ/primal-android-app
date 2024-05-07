package net.primal.android.editor.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.explore.search.ui.UserProfileListItem
import net.primal.android.theme.AppTheme

@Composable
fun NoteTagUserLazyColumn(
    modifier: Modifier,
    content: TextFieldValue,
    taggedUsers: List<NoteTaggedUser>,
    users: List<UserProfileItemUi>,
    onUserClick: (content: TextFieldValue, taggedUsers: List<NoteTaggedUser>) -> Unit,
    userTaggingQuery: String,
    userTagHighlightColor: Color,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        items(items = users) {
            UserProfileListItem(
                data = it,
                colors = ListItemDefaults.colors(containerColor = AppTheme.colorScheme.surface),
                onClick = { item ->
                    val taggedUser = NoteTaggedUser(userId = item.profileId, userHandle = item.displayName)

                    val cursorPosition = content.selection.start
                    val textUntilCursor = content.text.substring(
                        startIndex = 0,
                        endIndex = cursorPosition,
                    )
                    val lastIndexOfUserTaggingSign = textUntilCursor.lastIndexOf("@")
                    val query = userTaggingQuery.ifEmpty { "" }
                    val endOfQueryIndex = lastIndexOfUserTaggingSign + 1 + query.length

                    val newTaggedUsers = taggedUsers.toMutableList().apply { add(taggedUser) }
                    val newCursorPosition = lastIndexOfUserTaggingSign + 1 + taggedUser.displayUsername.length
                    val newText = content.text.substring(0, lastIndexOfUserTaggingSign) +
                        taggedUser.displayUsername +
                        content.text.substring(startIndex = endOfQueryIndex)

                    val newContent = content.copy(
                        annotatedString = newText.asAnnotatedStringWithTaggedUsers(
                            taggedUsers = newTaggedUsers,
                            highlightColor = userTagHighlightColor,
                        ),
                        selection = TextRange(start = newCursorPosition, end = newCursorPosition),
                    )

                    onUserClick(newContent, newTaggedUsers)
                },
            )
        }
    }
}
