package net.primal.android.thread.articles.ui.rendering

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.LocalPrimalTheme
import net.primal.android.thread.articles.ui.handleArticleLinkClick
import net.primal.android.user.domain.NoteAppearance

@Composable
fun HtmlRenderer(
    modifier: Modifier,
    html: String,
    onProfileClick: (profileId: String) -> Unit,
    onNoteClick: (noteId: String) -> Unit,
    onArticleClick: (naddr: String) -> Unit,
    onUrlClick: (url: String) -> Unit,
) {
    Surface {
        val displaySettings = LocalContentDisplaySettings.current
        val activeTheme = LocalPrimalTheme.current
        val themableHtml by remember(html) {
            mutableStateOf(
                html.replace(
                    oldValue = "{{ THEME }}",
                    newValue = "${activeTheme.themeName} ${displaySettings.noteAppearance.toArticleTextStyle()}",
                ),
            )
        }

        val context = LocalContext.current
        var contentWebView by remember { mutableStateOf<WebView?>(null) }

        LaunchedEffect(themableHtml) {
            contentWebView = WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        checkNotNull(request)
                        if (request.url.host == "file:///android_assets") {
                            return super.shouldOverrideUrlLoading(view, request)
                        }

                        request.url.toString().handleArticleLinkClick(
                            onProfileClick = onProfileClick,
                            onNoteClick = onNoteClick,
                            onArticleClick = onArticleClick,
                            onUrlClick = onUrlClick,
                        )

                        return true
                    }
                }
            }
        }

        contentWebView?.let { webView ->
            AndroidView(
                modifier = modifier,
                factory = { _ -> webView },
            )

            LaunchedEffect(webView) {
                webView.loadDataWithBaseURL(
                    "file:///android_assets/",
                    themableHtml,
                    "text/html",
                    "utf-8",
                    null,
                )
            }
        }
    }
}

private fun NoteAppearance.toArticleTextStyle(): String {
    return when (this) {
        NoteAppearance.Small -> "small"
        NoteAppearance.Default -> "regular"
        NoteAppearance.Large -> "large"
        NoteAppearance.ExtraLarge -> "huge"
    }
}
