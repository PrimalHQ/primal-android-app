package net.primal.android.notes.feed.note.ui

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
    var fullScreenView by remember { mutableStateOf<View?>(null) }
    var fullScreenCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    val activity = LocalActivity.current
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    val localView = LocalView.current
    val windowInsetsController = if (activity != null) {
        remember { WindowCompat.getInsetsController(activity.window, localView) }
    } else {
        null
    }

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
                                fullScreenView = view
                                fullScreenCallback = callback
                                fullScreenDialogOpen = true
                                webView.forceVideoPlaying()
                                windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
                            }
                        }

                        override fun onHideCustomView() {
                            fullScreenView = null
                            fullScreenCallback = null
                            fullScreenDialogOpen = false
                            webView.forceVideoPausing()
                            windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
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

    val fullScreenWebPlayerView = fullScreenView
    if (fullScreenDialogOpen && fullScreenWebPlayerView != null) {
        BasicAlertDialog(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            onDismissRequest = {
                fullScreenCallback?.onCustomViewHidden()
                fullScreenDialogOpen = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    webView.forceVideoPlaying()
                    fullScreenWebPlayerView
                },
            )
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
