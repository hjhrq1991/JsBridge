package com.hjhrq1991.library

import android.graphics.Bitmap
import android.net.http.SslError
import android.text.TextUtils
import android.util.Log
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
import android.webkit.WebViewClient
import org.json.JSONArray
import org.json.JSONException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * @author hjhrq1991 created at 8/22/16 14 41.
 */
class BridgeWebViewClient(private val webView: BridgeWebView) : WebViewClient() {
    /**
     * 是否重定向，避免web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
     */
    private var isRedirected = false
    /**
     * onPageStarted连续调用次数,避免渲染立马跳转可能连续调用onPageStarted多次并且调用shouldOverrideUrlLoading后不调用onPageStarted引起的js桥未初始化问题
     */
    private var onPageStartedCount = 0

    /**
     * 是否注册JS桥
     */
    private var hasInitJSBridge = false

    private var bridgeWebViewClientListener: BridgeWebViewClientListener? = null

    fun hasInitJSBridge(): Boolean {
        return hasInitJSBridge
    }

    fun setInitJSBridge(hasInitJSBridge: Boolean) {
        this.hasInitJSBridge = hasInitJSBridge
    }

    fun setBridgeWebViewClientListener(bridgeWebViewClientListener: BridgeWebViewClientListener?) {
        this.bridgeWebViewClientListener = bridgeWebViewClientListener
    }

    fun removeListener() {
        bridgeWebViewClientListener = null
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        if (onPageStartedCount < 2) {
            isRedirected = true
        }
        onPageStartedCount = 0

        var decodedUrl = url
        try {
            decodedUrl = URLDecoder.decode(url, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        // =============== 新增批量消息处理 ===============
        if (decodedUrl?.startsWith(BridgeUtil.YY_BATCH_DATA) == true) { // 批量传输数据
            handleBatchMessage(decodedUrl.substring(BridgeUtil.YY_BATCH_DATA.length))
            return true
        } else if (decodedUrl?.startsWith(BridgeUtil.YY_BATCH_IDS) == true) { // 批量传输id去获取数据
            webView.handleBatchMessageIds(decodedUrl.substring(BridgeUtil.YY_BATCH_IDS.length))
            return true
        }
        // =============== 批量消息处理结束 ===============

        if (decodedUrl?.startsWith(BridgeUtil.YY_RETURN_DATA) == true) { // 如果是返回数据
            webView.handlerReturnData(decodedUrl)
            return true
        } else if (decodedUrl?.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA) == true) { // 单条处理消息
            webView.flushMessageQueue()
            return true
        } else {
            return bridgeWebViewClientListener?.shouldOverrideUrlLoading(view, decodedUrl)
                ?: super.shouldOverrideUrlLoading(view, decodedUrl)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString()
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        if (onPageStartedCount < 2) {
            isRedirected = true
        }
        onPageStartedCount = 0

        var decodedUrl = url
        try {
            decodedUrl = URLDecoder.decode(url, "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        // =============== 新增批量消息处理 ===============
        if (decodedUrl?.startsWith(BridgeUtil.YY_BATCH_DATA) == true) { // 批量传输数据
            handleBatchMessage(decodedUrl.substring(BridgeUtil.YY_BATCH_DATA.length))
            return true
        } else if (decodedUrl?.startsWith(BridgeUtil.YY_BATCH_IDS) == true) { // 批量传输id去获取数据
            webView.handleBatchMessageIds(decodedUrl.substring(BridgeUtil.YY_BATCH_IDS.length))
            return true
        }
        // =============== 批量消息处理结束 ===============

        if (decodedUrl?.startsWith(BridgeUtil.YY_RETURN_DATA) == true) { // 如果是返回数据
            webView.handlerReturnData(decodedUrl)
            return true
        } else if (decodedUrl?.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA) == true) { // 单条处理消息
            webView.flushMessageQueue()
            return true
        } else {
            return bridgeWebViewClientListener?.shouldOverrideUrlLoading(view, request)
                ?: super.shouldOverrideUrlLoading(view, request)
        }
    }

    // =============== 新增批量消息处理方法 ===============
    private fun handleBatchMessage(encodedBatchData: String) {
        try {
            val batchData = safeUrlDecode(encodedBatchData)
            val batch = JSONArray(batchData)

            for (i in 0 until batch.length()) {
                val message = batch.getString(i)
                if (message.startsWith(BridgeUtil.YY_RETURN_DATA_PREFIX)) {
                    // 处理批量返回数据
                    webView.handlerReturnData(message)
                } else if (message.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) {
                    // 处理批量队列请求
                    webView.flushMessageQueue()
                } else {
                    if (message.startsWith("[") && message.endsWith("]")) {
                        webView.multiFlushMessageQueue(message)
                    } else {
                        webView.multiFlushMessageQueue("[$message]")
                    }
                }
            }
        } catch (e: JSONException) {
            Log.e("BridgeWebView", "Batch message parse error", e)
        } catch (e: Exception) {
            Log.e("BridgeWebView", "Batch message error", e)
        }
    }

    private fun safeUrlDecode(url: String): String {
        return try {
            // 先替换所有非编码用途的 %（后跟非十六进制字符的情况）
            val sanitized = url.replace("%(?![0-9a-fA-F]{2})".toRegex(), "%25")
            URLDecoder.decode(sanitized, "UTF-8")
        } catch (e: Exception) {
            url // 极端情况下返回原始字符串
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        isRedirected = false
        onPageStartedCount++

        bridgeWebViewClientListener?.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        bridgeWebViewClientListener?.onPageFinishedFirst(view, url)
        var canLoadJS = true
        //modify：hjhrq1991，检查是否需要注入js
        if (BridgeConfig.filterDomain != null && BridgeConfig.filterDomain!!.isNotEmpty()) {
            for (filter in BridgeConfig.filterDomain!!) {
                if (!TextUtils.isEmpty(url) && url!!.contains(filter)) {
                    canLoadJS = false
                }
            }
        }
        if (canLoadJS && !webView.isPreloadMode) {
            //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
            if (BridgeConfig.toLoadJs != null && !url!!.contains("about:blank") && !isRedirected) {
                for (bridgeName in BridgeConfig.customBridge) {
                    BridgeUtil.webViewLoadLocalJs(webView, BridgeConfig.toLoadJs, BridgeConfig.defaultBridge, bridgeName)
                }

                hasInitJSBridge = true

                webView.startupMessage?.let { startupMessages ->
                    for (m in startupMessages) {
                        webView.dispatchMessage(m)
                    }
                    webView.startupMessage = null
                }
            }
        }

        bridgeWebViewClientListener?.onPageFinished(view, url)
    }

    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
        bridgeWebViewClientListener?.onReceivedError(view, errorCode, description, failingUrl)
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        bridgeWebViewClientListener?.onLoadResource(view, url)
    }

    override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
        return bridgeWebViewClientListener?.shouldInterceptRequest(view, url)
            ?: super.shouldInterceptRequest(view, url)
    }

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        return bridgeWebViewClientListener?.shouldInterceptRequest(view, request)
            ?: super.shouldInterceptRequest(view, request)
    }

    override fun onTooManyRedirects(view: WebView?, cancelMsg: android.os.Message?, continueMsg: android.os.Message?) {
        val interrupt = bridgeWebViewClientListener?.onTooManyRedirects(view, cancelMsg, continueMsg) ?: false
        if (!interrupt) {
            super.onTooManyRedirects(view, cancelMsg, continueMsg)
        }
    }

    override fun onFormResubmission(view: WebView?, dontResend: android.os.Message?, resend: android.os.Message?) {
        val interrupt = bridgeWebViewClientListener?.onFormResubmission(view, dontResend, resend) ?: false
        if (!interrupt) {
            super.onFormResubmission(view, dontResend, resend)
        }
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        bridgeWebViewClientListener?.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        val interrupt = bridgeWebViewClientListener?.onReceivedSslError(view, handler, error) ?: false
        if (!interrupt) {
            super.onReceivedSslError(view, handler, error)
        }
    }

    override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
        val interrupt = bridgeWebViewClientListener?.onReceivedClientCertRequest(view, request) ?: false
        if (!interrupt) {
            super.onReceivedClientCertRequest(view, request)
        }
    }

    override fun onReceivedHttpAuthRequest(view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?) {
        val interrupt = bridgeWebViewClientListener?.onReceivedHttpAuthRequest(view, handler, host, realm) ?: false
        if (!interrupt) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm)
        }
    }

    override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
        return bridgeWebViewClientListener?.shouldOverrideKeyEvent(view, event)
            ?: super.shouldOverrideKeyEvent(view, event)
    }

    override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
        val interrupt = bridgeWebViewClientListener?.onUnhandledKeyEvent(view, event) ?: false
        if (!interrupt) {
            super.onUnhandledKeyEvent(view, event)
        }
    }

    override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
        bridgeWebViewClientListener?.onScaleChanged(view, oldScale, newScale)
    }

    override fun onReceivedLoginRequest(view: WebView?, realm: String?, account: String?, args: String?) {
        bridgeWebViewClientListener?.onReceivedLoginRequest(view, realm, account, args)
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        bridgeWebViewClientListener?.onPageCommitVisible(view, url)
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        val interrupt = bridgeWebViewClientListener?.onReceivedError(view, request, error) ?: false
        if (!interrupt) {
            super.onReceivedError(view, request, error)
        }
    }

    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        bridgeWebViewClientListener?.onReceivedHttpError(view, request, errorResponse)
    }

    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
        return bridgeWebViewClientListener?.onRenderProcessGone(view, detail)
            ?: super.onRenderProcessGone(view, detail)
    }

    override fun onSafeBrowsingHit(view: WebView?, request: WebResourceRequest?, threatType: Int, callback: SafeBrowsingResponse?) {
        val interrupt = bridgeWebViewClientListener?.onSafeBrowsingHit(view, request, threatType, callback) ?: false
        if (!interrupt) {
            super.onSafeBrowsingHit(view, request, threatType, callback)
        }
    }
}
