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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.core.ext.openUriSafely

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NoteEmbeddedWebPagePreview(
    modifier: Modifier,
    url: String,
    state: EmbeddedWebPageState,
    onPageLoaded: () -> Unit,
    pageLoadedReadyDelayMillis: Long = 0,
    domStorageEnabled: Boolean = false,
    fullScreenSupported: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var fullScreenDialogOpen by remember { mutableStateOf(false) }
    val fullScreenView = remember { mutableStateOf<View?>(null) }
    val fullScreenCallback = remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    val context = LocalContext.current
    val webView = remember { WebView(context) }

    val visibleAlpha by animateFloatAsState(
        targetValue = if (state == EmbeddedWebPageState.Ready) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 1_000),
    )

    AndroidView(
        modifier = modifier.alpha(alpha = if (state == EmbeddedWebPageState.Ready) visibleAlpha else 0.0f),
        factory = {
            webView.apply {
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

                if (fullScreenSupported) {
                    webChromeClient = object : WebChromeClient() {
                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            if (view != null && callback != null) {
                                fullScreenView.value = view
                                fullScreenCallback.value = callback
                                fullScreenDialogOpen = true
                                webView.forceVideoPlaying()
                            }
                        }

                        override fun onHideCustomView() {
                            fullScreenView.value = null
                            fullScreenCallback.value = null
                            fullScreenDialogOpen = false
                            webView.forceVideoPausing()
                        }
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
        onReset = { it.release() },
        onRelease = { it.release() },
    )

    if (fullScreenDialogOpen) {
        val insets = WindowInsets.systemBars
        val navigationBarHeight = insets.getBottom(LocalDensity.current)

        BasicAlertDialog(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(
                    bottom = with(LocalDensity.current) { navigationBarHeight.toDp() },
                ),
            onDismissRequest = {
                fullScreenCallback.value?.onCustomViewHidden()
                fullScreenDialogOpen = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            fullScreenView.value?.let { fullScreenView ->
                AndroidView(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(
                            bottom = with(LocalDensity.current) { navigationBarHeight.toDp() },
                        ),
                    factory = {
                        webView.forceVideoPlaying()
                        fullScreenView
                    },
                )
            }
        }
    }
}

private fun WebView.forceVideoPlaying() {
    this.evaluateJavascript(
        "(function() { var video = document.querySelector('video'); if (video) { video.play(); } })();",
        null,
    )
}

private fun WebView.forceVideoPausing() {
    this.evaluateJavascript(
        "(function() { var video = document.querySelector('video'); if (video) { video.pause(); } })();",
        null,
    )
}

private fun WebView.release() {
    this.stopLoading()
    this.clearHistory()
    this.clearCache(true)
    this.loadUrl("about:blank")
    this.destroy()
}

enum class EmbeddedWebPageState {
    Idle,
    Initializing,
    Ready,
}
