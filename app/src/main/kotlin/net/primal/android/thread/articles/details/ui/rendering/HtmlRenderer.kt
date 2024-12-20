package net.primal.android.thread.articles.details.ui.rendering

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
import net.primal.android.thread.articles.details.ui.handleArticleLinkClick
import net.primal.android.user.domain.ContentAppearance

@Composable
fun HtmlRenderer(
    modifier: Modifier,
    html: String,
    onUrlClick: ((url: String) -> Unit)? = null,
    onProfileClick: ((profileId: String) -> Unit)? = null,
    onNoteClick: ((noteId: String) -> Unit)? = null,
    onArticleClick: ((naddr: String) -> Unit)? = null,
) {
    Surface {
        val displaySettings = LocalContentDisplaySettings.current
        val activeTheme = LocalPrimalTheme.current
        val themableHtml by remember(html) {
            mutableStateOf(
                html.replace(
                    oldValue = "{{ THEME }}",
                    newValue = "${activeTheme.themeName} ${displaySettings.contentAppearance.toArticleTextStyle()}",
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

private fun ContentAppearance.toArticleTextStyle(): String {
    return when (this) {
        ContentAppearance.Small -> "small"
        ContentAppearance.Default -> "regular"
        ContentAppearance.Large -> "large"
        ContentAppearance.ExtraLarge -> "huge"
    }
}
