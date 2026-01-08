package com.hjhrq1991.library

import android.graphics.Bitmap
import android.net.http.SslError
import android.view.KeyEvent
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.SafeBrowsingResponse
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView

/**
 * @author hjhrq1991 created at 2017/12/4 17 42 .
 *         description:
 */
open class SimpleBridgeWebViewClientListener : BridgeWebViewClientListener {
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return false
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
    }

    override fun onPageFinishedFirst(view: WebView?, url: String?) {
    }

    override fun onPageFinished(view: WebView?, url: String?) {
    }

    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
    }

    override fun onLoadResource(view: WebView?, url: String?) {
    }

    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        return null
    }

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        return null
    }

    override fun onTooManyRedirects(view: WebView?, cancelMsg: android.os.Message?, continueMsg: android.os.Message?): Boolean {
        return false
    }

    override fun onFormResubmission(view: WebView?, dontResend: android.os.Message?, resend: android.os.Message?): Boolean {
        return false
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?): Boolean {
        return false
    }

    override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?): Boolean {
        return false
    }

    override fun onReceivedHttpAuthRequest(view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?): Boolean {
        return false
    }

    override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
        return false
    }

    override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
        return false
    }

    override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
    }

    override fun onReceivedLoginRequest(view: WebView?, realm: String?, account: String?, args: String?) {
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?): Boolean {
        return false
    }

    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
    }

    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        return false
    }

    override fun onSafeBrowsingHit(view: WebView?, request: WebResourceRequest?, threatType: Int, callback: SafeBrowsingResponse?): Boolean {
        return false
    }
}
