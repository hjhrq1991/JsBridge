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
 * @author hjhrq1991 created at 5/10/16 15:12.
 * 超链接回调
 */
interface BridgeWebViewClientListener {
    /**
     * 非js桥的超链接回调回去自行处理，api21以下会调用
     *
     * @author hjhrq1991 created at 5/10/16 15:12.
     */
    fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean

    /**
     * 非js桥的超链接回调回去自行处理，api21及以上会调用
     */
    fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean

    fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?)

    /**
     * 页面完成回调，可用于注入js。在注入JS桥前调用
     *
     * @param view
     * @param url
     */
    fun onPageFinishedFirst(view: WebView?, url: String?)

    /**
     * 页面完成回调，在注入JS桥后调用
     *
     * @param view
     * @param url
     */
    fun onPageFinished(view: WebView?, url: String?)

    fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?)

    fun onLoadResource(view: WebView?, url: String?)

    fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse?

    fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse?

    fun onTooManyRedirects(view: WebView?, cancelMsg: android.os.Message?, continueMsg: android.os.Message?): Boolean

    fun onFormResubmission(view: WebView?, dontResend: android.os.Message?, resend: android.os.Message?): Boolean

    fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean)

    fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?): Boolean

    fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?): Boolean

    fun onReceivedHttpAuthRequest(view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?): Boolean

    fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean

    fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?): Boolean

    fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float)

    fun onReceivedLoginRequest(view: WebView?, realm: String?, account: String?, args: String?)

    fun onPageCommitVisible(view: WebView?, url: String?)

    fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?): Boolean

    fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?)

    fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean

    fun onSafeBrowsingHit(view: WebView?, request: WebResourceRequest?, threatType: Int, callback: SafeBrowsingResponse?): Boolean
}
