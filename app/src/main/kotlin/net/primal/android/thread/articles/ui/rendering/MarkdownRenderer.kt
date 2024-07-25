package net.primal.android.thread.articles.ui.rendering

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.commonmark.MarkdownParseOptions
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.ui.material3.RichText
import net.primal.android.theme.AppTheme
import net.primal.android.thread.articles.ui.handleArticleLinkClick

@Composable
fun MarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier,
    onProfileClick: ((profileId: String) -> Unit)? = null,
    onNoteClick: ((noteId: String) -> Unit)? = null,
    onArticleClick: ((naddr: String) -> Unit)? = null,
    onUrlClick: ((url: String) -> Unit)? = null,
) {
    val richTextStyle = buildPrimalRichTextStyle(
        highlightColor = AppTheme.colorScheme.secondary,
        codeBlockBackground = AppTheme.extraColorScheme.surfaceVariantAlt1,
        codeBlockContent = AppTheme.colorScheme.onSurface,
        outlineColor = AppTheme.colorScheme.outline,
    )
    val parser = remember(markdown) { CommonmarkAstNodeParser(MarkdownParseOptions.Default) }
    val astNode = remember(parser) { parser.parse(markdown) }

    SelectionContainer {
        PrimalMarkdownStylesProvider {
            RichText(
                modifier = modifier,
                style = richTextStyle,
                linkClickHandler = { url ->
                    url.handleArticleLinkClick(
                        onProfileClick = onProfileClick,
                        onNoteClick = onNoteClick,
                        onArticleClick = onArticleClick,
                        onUrlClick = onUrlClick,
                    )
                },
            ) {
                BasicMarkdown(astNode = astNode)
            }
        }
    }
}
