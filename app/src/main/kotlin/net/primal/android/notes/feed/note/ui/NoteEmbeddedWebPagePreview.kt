package net.primal.android.notes.feed.note.ui

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.core.ext.openUriSafely

enum class EmbeddedWebPageState {
    Idle,
    Initializing,
    Ready,
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NoteEmbeddedWebPagePreview(
    modifier: Modifier,
    url: String,
    state: EmbeddedWebPageState,
    onPageLoaded: () -> Unit,
    pageLoadedReadyDelayMillis: Long = 0,
    domStorageEnabled: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val visibleAlpha by animateFloatAsState(
        targetValue = if (state == EmbeddedWebPageState.Ready) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 1_000),
    )

    AndroidView(
        modifier = modifier.alpha(alpha = if (state == EmbeddedWebPageState.Ready) visibleAlpha else 0.0f),
        factory = { context ->
            WebView(context).apply {
                this.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.apply {
                    javaScriptEnabled = true
                    this.domStorageEnabled = domStorageEnabled
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = false
                    allowContentAccess = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        super.onShowCustomView(view, callback)
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        uriHandler.openUriSafely(request.url.toString())
                        return true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        scope.launch {
                            delay(pageLoadedReadyDelayMillis)
                            onPageLoaded()
                        }
                    }
                }

                loadUrl(url)
            }
        },
        onReset = { webView -> webView.release() },
        onRelease = { webView -> webView.release() },
    )
}

private fun WebView.release() {
    this.stopLoading()
    this.clearHistory()
    this.clearCache(true)
    this.loadUrl("about:blank")
    this.destroy()
}
