package net.primal.android.thread.articles.details.ui.rendering

import androidx.compose.runtime.Composable
import com.halilibo.richtext.markdown.AstBlockNodeComposer
import com.halilibo.richtext.markdown.node.AstBlockNodeType
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.markdown.node.AstParagraph
import com.halilibo.richtext.ui.RichTextScope
import net.primal.android.highlights.model.HighlightUi
import net.primal.android.thread.articles.details.ui.richtext.MarkdownRichText


fun customBlockNodeComposer(@Suppress("UnusedParameter") highlights: List<HighlightUi>) =
    object : AstBlockNodeComposer {
        override fun predicate(astBlockNodeType: AstBlockNodeType): Boolean =
            when (astBlockNodeType) {
                AstParagraph -> true
                else -> false
            }

        @Composable
        override fun RichTextScope.Compose(astNode: AstNode, visitChildren: @Composable (AstNode) -> Unit) {
            require(astNode.type == AstParagraph)
            MarkdownRichText(astNode = astNode)
        }
    }
