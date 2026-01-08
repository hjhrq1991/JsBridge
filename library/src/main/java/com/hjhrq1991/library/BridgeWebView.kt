package com.hjhrq1991.library

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.SystemClock
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import com.google.gson.Gson
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

@SuppressLint("SetJavaScriptEnabled")
class BridgeWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : WebView(context, attrs, defStyle), WebViewJavascriptBridge {

    companion object {
        private const val MAX_BATCH_SIZE = 20
        private const val BATCH_DELAY_MS = 50L
    }

    private var bridgeWebViewClient: BridgeWebViewClient? = null

    private var handlerThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private val messageSemaphore = Semaphore(BridgeConfig.MAX_IN_FLIGHT_MESSAGES)
    private var handlerPosted = false

    private val responseCallbacks = HashMap<String, CallBackFunction>()
    private val messageHandlers = HashMap<String, BridgeHandler>()
    private var defaultHandler: BridgeHandler = DefaultHandler()

    private val messageQueue = PriorityBlockingQueue<Message>(50, Comparator { m1, m2 ->
        // 高优先级排在前面：如果 m2 优先级高返回正数，m1 优先级高返回负数
        (if (m2.highPriority) 1 else 0).compareTo(if (m1.highPriority) 1 else 0)
    })

    var startupMessage: MutableList<Message>? = ArrayList()
    private val uniqueId = AtomicLong(0)
    private val gson = Gson()

    /**
     * 自动清除
     */
    var autoCleanUp = true
    /**
     * 是否预加载模式，预加载时不会初始化JS桥
     */
    var isPreloadMode = false

    private val batchDispatcher = Runnable {
        dispatchBatch()
        handlerPosted = false
    }

    init {
        init()
    }

    /**
     * @param handler default handler,handle messages send by js without assigned handler name,
     *                if js message has handler name, it will be handled by named handlers registered by native
     */
    fun setDefaultHandler(handler: BridgeHandler?) {
        this.defaultHandler = handler ?: DefaultHandler()
    }

    private fun init() {
        this.isVerticalScrollBarEnabled = false
        this.isHorizontalScrollBarEnabled = false
        this.settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // Initialize background handler thread
        handlerThread = HandlerThread("BridgeWebViewHandler")
        handlerThread?.start()
        backgroundHandler = Handler(handlerThread?.looper ?: Looper.getMainLooper())

        this.webViewClient = generateBridgeWebViewClient()
    }

    fun setWebViewClient() {
        this.webViewClient = generateBridgeWebViewClient()
    }

    protected fun generateBridgeWebViewClient(): BridgeWebViewClient {
        bridgeWebViewClient = BridgeWebViewClient(this)
        return bridgeWebViewClient!!
    }

    /**
     * 注册JS桥，可用于预加载等需要重新初始化JS桥的情况
     */
    fun initJSBridge() {
        for (bridgeName in BridgeConfig.customBridge) {
            BridgeUtil.webViewLoadLocalJs(this, BridgeConfig.toLoadJs, BridgeConfig.defaultBridge, bridgeName)
        }
        bridgeWebViewClient?.setInitJSBridge(true)
        startupMessage?.let { messages ->
            for (m in messages) {
                this.dispatchMessage(m)
            }
            this.startupMessage = null
        }
    }

    /**
     * 获取是否初始化JS桥，可用于预加载等需要重新初始化JS桥的情况
     */
    fun hasInitJSBridge(): Boolean {
        return bridgeWebViewClient?.hasInitJSBridge() ?: false
    }

    // url：yy://return/_fetchQueue/[{"handlerName":"jsClick1","data":"{\"title\":\"hello title\"}"}]
    fun handlerReturnData(url: String?) {
        logQueueStatus()
        if (url?.endsWith("[]") == true) return //当以[]结尾时表示handlerName也没有，则该情况丢弃
        val functionName = BridgeUtil.getFunctionFromReturnUrl(url)
        val f = responseCallbacks[functionName]
        val data = BridgeUtil.getDataFromReturnUrl(url)
        if (f != null) {
            //TODO 这里需处理remove导致无法继续发送其他桥的消息
            f.onCallBack(data)
            responseCallbacks.remove(functionName)
            messageSemaphore.release() // Release permit when response received
        }
    }

    override fun send(data: String?) {
        send(data, null)
    }

    override fun send(data: String?, responseCallback: CallBackFunction?) {
        doSend(null, data, responseCallback, false)
    }

    fun sendHighPriority(data: String?, responseCallback: CallBackFunction?) {
        doSend(null, data, responseCallback, true)
    }

    private fun doSend(handlerName: String?, data: String?, responseCallback: CallBackFunction?, highPriority: Boolean) {
        logQueueStatus()
        try {
            if (!messageSemaphore.tryAcquire()) {
                if (BridgeConfig.isDebug) Log.w(BridgeConfig.TAG, "Message queue full, dropping message")
                return
            }

            val m = Message()
            m.highPriority = highPriority

            if (!TextUtils.isEmpty(data)) {
                m.data = data
            }

            if (responseCallback != null) {
                val callbackStr = String.format(
                    BridgeUtil.CALLBACK_ID_FORMAT,
                    uniqueId.incrementAndGet().toString() + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis())
                )

                // --- 关键修改：双重保险（回调释放 + 超时释放）---
                val callbackFired = AtomicBoolean(false)

                // 包装回调
                val wrappedCallback = CallBackFunction { responseData ->
                    if (callbackFired.compareAndSet(false, true)) {
                        try {
                            responseCallback.onCallBack(responseData)
                        } finally {
                            messageSemaphore.release()
                            if (BridgeConfig.isDebug) Log.d(BridgeConfig.TAG, "Callback executed: $callbackStr")
                        }
                    }
                }

                // 设置超时释放（即使前端未回调）
                backgroundHandler?.postDelayed({
                    if (callbackFired.compareAndSet(false, true)) {
                        messageSemaphore.release()
                        responseCallbacks.remove(callbackStr) // 清理残留回调
                        if (BridgeConfig.isDebug) Log.w(BridgeConfig.TAG, "Force released due to timeout: $callbackStr")
                    }
                }, BridgeConfig.SEMAPHORE_CALLBACK_TIMEOUT_MS) // 超时自动释放

                responseCallbacks[callbackStr] = wrappedCallback
                m.callbackId = callbackStr
            } else {
                // 没有回调的消息，需要单独处理释放
                m.releaseSemaphore = true

                // 无回调的消息：设置超时自动释放信号量（例如 5 秒）
                backgroundHandler?.postDelayed({
                    if (BridgeConfig.isDebug) Log.w(BridgeConfig.TAG, "No response received, releasing semaphore for message: ${m.data}")
                    messageSemaphore.release()
                }, BridgeConfig.SEMAPHORE_NO_CALLBACK_TIMEOUT_MS) // 超时自动释放
            }

            if (!TextUtils.isEmpty(handlerName)) {
                m.handlerName = handlerName
            }

            queueMessage(m)
        } catch (e: Exception) {
            // 发生异常时释放许可
            if (BridgeConfig.isDebug) Log.e(BridgeConfig.TAG, "Error sending message", e)
            messageSemaphore.release()
        }
    }

    // 在类中添加
    private fun logQueueStatus() {
        if (BridgeConfig.isDebug) {
            Log.d(
                BridgeConfig.TAG,
                String.format(
                    "Queue status: Available permits=%d, Queue size=%d, Callbacks=%d",
                    messageSemaphore.availablePermits(),
                    messageQueue.size,
                    responseCallbacks.size
                )
            )
        }
    }

    private fun queueMessage(m: Message) {
        messageQueue.add(m)

        if (messageQueue.size >= MAX_BATCH_SIZE) {
            backgroundHandler?.removeCallbacks(batchDispatcher)
            backgroundHandler?.post { dispatchBatch() }
        } else if (!handlerPosted) {
            backgroundHandler?.postDelayed(batchDispatcher, BATCH_DELAY_MS)
        }
    }

    private fun dispatchBatch() {
        val batch = ArrayList<Message>(MAX_BATCH_SIZE)
        messageQueue.drainTo(batch, MAX_BATCH_SIZE)

        if (batch.isNotEmpty()) {
            var batchJson = gson.toJson(batch)
            batchJson = batchJson.replace("(\\\\)([^utrn])".toRegex(), "\\\\\\\\$1$2")
                .replace("(?<=[^\\\\])(\")".toRegex(), "\\\\\"")

            dispatchMessageBatch(batchJson)
        }
    }

    private fun dispatchMessageBatch(messageJson: String) {
        for (bridgeName in BridgeConfig.customBridge) {
            val javascriptCommand = String.format(
                BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA.replace(BridgeConfig.defaultBridge, bridgeName),
                messageJson
            )

            if (BridgeConfig.isDebug) Log.i(BridgeConfig.TAG, "$bridgeName dispatchBatch: $javascriptCommand")

            if (Looper.myLooper() == Looper.getMainLooper()) {
                evaluateJavascript(javascriptCommand, null)
            } else {
                post {
                    evaluateJavascript(javascriptCommand, null)
                }
            }
        }
    }

    // 批量获取消息id
    fun handleBatchMessageIds(messageIdsStr: String?) {
        val messageIds = messageIdsStr?.split(",") ?: return
        for (messageId in messageIds) {
            getMessageById(messageId)
        }
    }

    private fun getMessageById(messageId: String?) {
        for (bridgeName in BridgeConfig.customBridge) {
            // 为每个消息ID注入JS获取数据
            val jsCommand = String.format(
                BridgeUtil.JS_FETCH_MESSAGE_BY_ID.replace(BridgeConfig.defaultBridge, bridgeName),
                messageId
            )

            if (BridgeConfig.isDebug) Log.i(BridgeConfig.TAG, "$bridgeName getMessageById: $jsCommand")

            if (Looper.myLooper() == Looper.getMainLooper()) {
                evaluateJavascript(jsCommand) { data ->
                    if (data != null && data != "null") {
                        // 处理获取到的消息数据
                        if (data.startsWith("[") && data.endsWith("]")) {
                            multiFlushMessageQueue(data)
                        } else {
                            multiFlushMessageQueue("[$data]")
                        }
                    }
                }
            } else {
                post {
                    evaluateJavascript(jsCommand) { data ->
                        if (data != null && data != "null") {
                            // 处理获取到的消息数据
                            if (data.startsWith("[") && data.endsWith("]")) {
                                multiFlushMessageQueue(data)
                            } else {
                                multiFlushMessageQueue("[$data]")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun evaluateJavascript(script: String, resultCallback: ValueCallback<String?>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            super.evaluateJavascript(script, resultCallback)
        } else {
            // 对于低版本Android，使用loadUrl方式
            super.loadUrl(script)
        }
    }

    fun dispatchMessage(m: Message) {
        var messageJson = m.toJson()
        //escape special characters for json string
        messageJson = messageJson?.replace("(\\\\)([^utrn])".toRegex(), "\\\\\\\\$1$2")
        messageJson = messageJson?.replace("(?<=[^\\\\])(\")".toRegex(), "\\\\\"")

        for (bridgeName in BridgeConfig.customBridge) {
            val javascriptCommand = String.format(
                BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA.replace(BridgeConfig.defaultBridge, bridgeName),
                messageJson
            )
            if (Thread.currentThread() == Looper.getMainLooper().thread) {
                if (BridgeConfig.isDebug) Log.i(BridgeConfig.TAG, "$bridgeName   console   dispatchMessage：$javascriptCommand")

                evaluateJavascript(javascriptCommand, null)
            }
        }
    }

    fun flushMessageQueue() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            for (bridgeName in BridgeConfig.customBridge) {
                flushMessageQueue(bridgeName)
            }
        } else {
            post { flushMessageQueue() }
        }
    }

    /**
     * 多数据合并处理
     * @param jsonData
     */
    fun multiFlushMessageQueue(jsonData: String?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (BridgeConfig.customBridge.isNotEmpty()) {
                handleFlushResponse(BridgeConfig.customBridge[0], jsonData)
            } else {
                handleFlushResponse(BridgeConfig.defaultBridge, jsonData)
            }
        } else {
            post { multiFlushMessageQueue(jsonData) }
        }
    }

    private fun flushMessageQueue(bridgeName: String) {
        if (BridgeConfig.isDebug) Log.i(BridgeConfig.TAG, "$bridgeName flushMessageQueue")

        val jsCommand = BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA.replace(BridgeConfig.defaultBridge, bridgeName)

        loadUrl(jsCommand) { data -> handleFlushResponse(bridgeName, data) }
    }

    private fun handleFlushResponse(bridgeName: String, jsonData: String?) {
        if (BridgeConfig.isDebug) Log.i(BridgeConfig.TAG, "$bridgeName flushResponse: $jsonData")

        try {
            val list = Message.toArrayList(jsonData)
            if (list.isEmpty()) return

            for (m in list) {
                if (!TextUtils.isEmpty(m.responseId)) {
                    // Handle response
                    val function = responseCallbacks[m.responseId]
                    if (function != null) {
                        function.onCallBack(m.responseData)
                        responseCallbacks.remove(m.responseId)
                    } else {
                        // 如果回调不存在，可能是超时释放了，但仍需确保信号量被释放
                        messageSemaphore.release()
                        if (BridgeConfig.isDebug) Log.w(BridgeConfig.TAG, "Orphan response received, releasing semaphore")
                    }
                } else {
                    // Handle request with distinct parameter name
                    val responseFunction = if (!TextUtils.isEmpty(m.callbackId))
                        createResponseCallback(m.callbackId)
                    else
                        CallBackFunction { }  // Changed parameter name to responseData

                    val handler = if (!TextUtils.isEmpty(m.handlerName))
                        messageHandlers[m.handlerName]
                    else
                        defaultHandler

                    handler?.handler(m.data, responseFunction)
                }

                // 对于没有回调的消息，处理完成后释放许可
                if (m.releaseSemaphore) {
                    messageSemaphore.release()
                }
            }
        } catch (e: Exception) {
            if (BridgeConfig.isDebug) Log.e(BridgeConfig.TAG, "Error processing flush response", e)
        }
    }

    private fun createResponseCallback(callbackId: String?): CallBackFunction {
        return CallBackFunction { responseData ->
            val responseMsg = Message()
            responseMsg.responseId = callbackId
            responseMsg.responseData = responseData
            queueMessage(responseMsg)
        }
    }

    fun loadUrl(jsUrl: String, returnCallback: CallBackFunction?) {
        evaluateJavascript(jsUrl, null)

        for (bridgeName in BridgeConfig.customBridge) {
            returnCallback?.let {
                responseCallbacks[BridgeUtil.parseFunctionName(jsUrl, bridgeName)] = it
            }
        }
    }

    /**
     * register handler,so that javascript can call it
     *
     * @param handlerName handlerName
     * @param handler     Handler
     */
    fun registerHandler(handlerName: String?, handler: BridgeHandler?) {
        if (handler != null) {
            messageHandlers[handlerName ?: ""] = handler
        }
    }

    /**
     * call javascript registered handler
     *
     * @param handlerName handlerName
     * @param data        data
     * @param callBack    callBack
     */
    fun callHandler(handlerName: String?, data: String?, callBack: CallBackFunction?) {
        doSend(handlerName, data, callBack, false)
    }

    fun setBridgeWebViewClientListener(bridgeWebViewClientListener: BridgeWebViewClientListener?) {
        bridgeWebViewClient?.setBridgeWebViewClientListener(bridgeWebViewClientListener)
    }

    /**
     * 销毁时调用，移除listener
     */
    fun removeListener() {
        bridgeWebViewClient?.removeListener()
        onWebChromeClientListener = null
    }

    private var onWebChromeClientListener: OnWebChromeClientListener? = null

    fun setWebChromeClientListener(onWebChromeClientListener: OnWebChromeClientListener?) {
        this.onWebChromeClientListener = onWebChromeClientListener
        webChromeClient = newWebChromeClient()
    }

    fun setWebChromeClientListener(webChromeClientListener: WebChromeClientListener?) {
        this.onWebChromeClientListener = webChromeClientListener
        webChromeClient = newWebChromeClient()
    }

    private fun newWebChromeClient(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                onWebChromeClientListener?.onProgressChanged(view, newProgress)
                    ?: super.onProgressChanged(view, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                onWebChromeClientListener?.onReceivedTitle(view, title)
                    ?: super.onReceivedTitle(view, title)
            }

            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
                onWebChromeClientListener?.onReceivedIcon(view, icon)
                    ?: super.onReceivedIcon(view, icon)
            }

            override fun onReceivedTouchIconUrl(view: WebView?, url: String?, precomposed: Boolean) {
                onWebChromeClientListener?.onReceivedTouchIconUrl(view, url, precomposed)
                    ?: super.onReceivedTouchIconUrl(view, url, precomposed)
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                onWebChromeClientListener?.onShowCustomView(view, callback)
                    ?: super.onShowCustomView(view, callback)
            }

            override fun onShowCustomView(view: View?, requestedOrientation: Int, callback: CustomViewCallback?) {
                onWebChromeClientListener?.onShowCustomView(view, requestedOrientation, callback)
                    ?: super.onShowCustomView(view, requestedOrientation, callback)
            }

            override fun onHideCustomView() {
                onWebChromeClientListener?.onHideCustomView()
                    ?: super.onHideCustomView()
            }

            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean {
                return onWebChromeClientListener?.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                    ?: super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
            }

            override fun onRequestFocus(view: WebView?) {
                onWebChromeClientListener?.onRequestFocus(view)
                    ?: super.onRequestFocus(view)
            }

            override fun onCloseWindow(window: WebView?) {
                onWebChromeClientListener?.onCloseWindow(window)
                    ?: super.onCloseWindow(window)
            }

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return onWebChromeClientListener?.onJsAlert(view, url, message, result)
                    ?: super.onJsAlert(view, url, message, result)
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return onWebChromeClientListener?.onJsConfirm(view, url, message, result)
                    ?: super.onJsConfirm(view, url, message, result)
            }

            override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                return onWebChromeClientListener?.onJsPrompt(view, url, message, defaultValue, result)
                    ?: super.onJsPrompt(view, url, message, defaultValue, result)
            }

            override fun onJsBeforeUnload(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                return onWebChromeClientListener?.onJsBeforeUnload(view, url, message, result)
                    ?: super.onJsBeforeUnload(view, url, message, result)
            }

            override fun onExceededDatabaseQuota(url: String?, databaseIdentifier: String?, quota: Long, estimatedDatabaseSize: Long, totalQuota: Long, quotaUpdater: WebStorage.QuotaUpdater?) {
                onWebChromeClientListener?.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater)
                    ?: super.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater)
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                onWebChromeClientListener?.onGeolocationPermissionsShowPrompt(origin, callback)
                    ?: super.onGeolocationPermissionsShowPrompt(origin, callback)
            }

            override fun onGeolocationPermissionsHidePrompt() {
                onWebChromeClientListener?.onGeolocationPermissionsHidePrompt()
                    ?: super.onGeolocationPermissionsHidePrompt()
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                onWebChromeClientListener?.onPermissionRequest(request)
                    ?: super.onPermissionRequest(request)
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                onWebChromeClientListener?.onPermissionRequestCanceled(request)
                    ?: super.onPermissionRequestCanceled(request)
            }

            override fun onJsTimeout(): Boolean {
                return onWebChromeClientListener?.onJsTimeout()
                    ?: super.onJsTimeout()
            }

            override fun onConsoleMessage(message: String?, lineNumber: Int, sourceID: String?) {
                onWebChromeClientListener?.onConsoleMessage(message, lineNumber, sourceID)
                    ?: super.onConsoleMessage(message, lineNumber, sourceID)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                return onWebChromeClientListener?.onConsoleMessage(consoleMessage)
                    ?: super.onConsoleMessage(consoleMessage)
            }

            override fun getDefaultVideoPoster(): Bitmap? {
                return onWebChromeClientListener?.getDefaultVideoPoster()
                    ?: super.getDefaultVideoPoster()
            }

            override fun getVideoLoadingProgressView(): View? {
                return onWebChromeClientListener?.getVideoLoadingProgressView()
                    ?: super.getVideoLoadingProgressView()
            }

            override fun getVisitedHistory(callback: ValueCallback<Array<String>>?) {
                onWebChromeClientListener?.getVisitedHistory(callback)
                    ?: super.getVisitedHistory(callback)
            }

            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                return onWebChromeClientListener?.onShowFileChooser(webView, filePathCallback, fileChooserParams)
                    ?: super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (autoCleanUp) {
            cleanup()
        }
    }

    fun cleanup() {
        handlerThread?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                it.quitSafely()
            } else {
                it.quit()
            }
        }
        messageQueue.clear()
        responseCallbacks.clear()
        messageHandlers.clear()
        startupMessage?.clear()

        bridgeWebViewClient?.removeListener()
    }

    /**
     * @param bridgeName 自定义桥名，可为空，为空时使用默认桥名，默认桥名为 [BridgeConfig.defaultBridge] WebViewJavascriptBridge
     *                   自定义桥名时，会动态替换 WebViewJavascriptBridge.js 文件内 WebViewJavascriptBridge 字段
     * @author hjhrq1991 created at 6/20/16 17:32.
     */
    fun setBridge(vararg bridgeName: String) {
        BridgeConfig.customBridge = if (bridgeName.isNotEmpty()) {
            bridgeName.toList()
        } else {
            Collections.singletonList(BridgeConfig.defaultBridge)
        }
    }
}
