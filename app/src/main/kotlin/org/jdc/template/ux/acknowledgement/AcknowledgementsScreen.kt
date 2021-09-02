package org.jdc.template.ux.acknowledgement

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun AcknowledgementScreen() {
    val viewModel: AcknowledgementViewModel = viewModel()
    val acknowledgementHtmlFlow = viewModel.acknowledgementHtmlFlow

    AcknowledgementWebview(acknowledgementHtmlFlow)
}

@Composable
private fun AcknowledgementWebview(acknowledgementHtmlFlow: StateFlow<String?>) {
    val acknowledgementHtml: String? by acknowledgementHtmlFlow.collectAsState()
    val acknowledgementWebViewClient = remember { getWebviewClient() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )

                webViewClient = acknowledgementWebViewClient
            }
        },
        update = { webview ->
            acknowledgementHtml?.let {
                webview.loadData(it, "text/html", "utf-8")
            }
        }
    )
}

private fun getWebviewClient(): WebViewClient {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        NewAcknowledgmentsWebViewClient()
    } else {
        AcknowledgmentsWebViewClient()
    }
}

private class AcknowledgmentsWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        view.context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        )

        return true
    }
}

@RequiresApi(Build.VERSION_CODES.N)
private class NewAcknowledgmentsWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return super.shouldOverrideUrlLoading(view, request)

        view.context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        )
        return true
    }
}