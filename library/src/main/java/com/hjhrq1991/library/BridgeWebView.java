package com.hjhrq1991.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge {

    private static final int MAX_BATCH_SIZE = 20;
    private static final long BATCH_DELAY_MS = 50;

    private BridgeWebViewClient bridgeWebViewClient;

    private HandlerThread handlerThread;
    private Handler backgroundHandler;
    private final Semaphore messageSemaphore = new Semaphore(BridgeConfig.MAX_IN_FLIGHT_MESSAGES);
    private boolean handlerPosted = false;

    Map<String, CallBackFunction> responseCallbacks = new HashMap<>();
    Map<String, BridgeHandler> messageHandlers = new HashMap<>();
    BridgeHandler defaultHandler = new DefaultHandler();

    private final PriorityBlockingQueue<Message> messageQueue = new PriorityBlockingQueue<>(50,
            (m1, m2) -> Boolean.compare(m2.isHighPriority(), m1.isHighPriority()));

    private List<Message> startupMessage = new ArrayList<>();
    private final AtomicLong uniqueId = new AtomicLong(0);
    private final Gson gson = new Gson();

    /**
     * 自动清除
     */
    private boolean autoCleanUp = true;
    /**
     * 是否预加载模式，预加载时不会初始化JS桥
     */
    private boolean isPreloadMode = false;

    public void setAutoCleanUp(boolean autoCleanUp) {
        this.autoCleanUp = autoCleanUp;
    }

    private final Runnable batchDispatcher = new Runnable() {
        @Override
        public void run() {
            dispatchBatch();
            handlerPosted = false;
        }
    };

    public List<Message> getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(List<Message> startupMessage) {
        this.startupMessage = startupMessage;
    }

    public BridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BridgeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BridgeWebView(Context context) {
        super(context);
        init();
    }

    /**
     * @param handler default handler,handle messages send by js without assigned handler name,
     *                if js message has handler name, it will be handled by named handlers registered by native
     */
    public void setDefaultHandler(BridgeHandler handler) {
        this.defaultHandler = handler;
    }

    private void init() {
        this.setVerticalScrollBarEnabled(false);
        this.setHorizontalScrollBarEnabled(false);
        this.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // Initialize background handler thread
        handlerThread = new HandlerThread("BridgeWebViewHandler");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());

        this.setWebViewClient(generateBridgeWebViewClient());
    }

    public void setWebViewClient() {
        this.setWebViewClient(generateBridgeWebViewClient());
    }

    protected BridgeWebViewClient generateBridgeWebViewClient() {
        return bridgeWebViewClient = new BridgeWebViewClient(this);
    }

    /**
     * 注册JS桥，可用于预加载等需要重新初始化JS桥的情况
     */
    public void initJSBridge() {
        for (int i = 0; i < BridgeConfig.customBridge.size(); i++) {
            String bridgeName = BridgeConfig.customBridge.get(i);
            BridgeUtil.webViewLoadLocalJs(this, BridgeConfig.toLoadJs, BridgeConfig.defaultBridge, bridgeName);
        }

        if (this.getStartupMessage() != null) {
            for (Message m : this.getStartupMessage()) {
                this.dispatchMessage(m);
            }
            this.setStartupMessage(null);
        }
    }

    /**
     * 获取是否初始化JS桥，可用于预加载等需要重新初始化JS桥的情况
     */
    public boolean hasInitJSBridge() {
        return bridgeWebViewClient.hasInitJSBridge();
    }

    public boolean isPreloadMode() {
        return isPreloadMode;
    }

    public void setPreloadMode(boolean preloadMode) {
        isPreloadMode = preloadMode;
    }

    // url：yy://return/_fetchQueue/[{"handlerName":"jsClick1","data":"{\"title\":\"hello title\"}"}]
    public void handlerReturnData(String url) {
        logQueueStatus();
        if (url.endsWith("[]")) return; //当以[]结尾时表示handlerName也没有，则该情况丢弃
        String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
        CallBackFunction f = responseCallbacks.get(functionName);
        String data = BridgeUtil.getDataFromReturnUrl(url);
        if (f != null) {
            //TODO 这里需处理remove导致无法继续发送其他桥的消息
            f.onCallBack(data);
            responseCallbacks.remove(functionName);
            messageSemaphore.release(); // Release permit when response received
        }
    }

    @Override
    public void send(String data) {
        send(data, null);
    }

    @Override
    public void send(String data, CallBackFunction responseCallback) {
        doSend(null, data, responseCallback, false);
    }

    public void sendHighPriority(String data, CallBackFunction responseCallback) {
        doSend(null, data, responseCallback, true);
    }

//    private void doSend(String handlerName, String data, CallBackFunction responseCallback, boolean highPriority) {
//        Message m = new Message();
//        if (!TextUtils.isEmpty(data)) {
//            m.setData(data);
//        }
//        if (responseCallback != null) {
//            String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId.accumulateAndGet() + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
//            responseCallbacks.put(callbackStr, responseCallback);
//            m.setCallbackId(callbackStr);
//        }
//        if (!TextUtils.isEmpty(handlerName)) {
//            m.setHandlerName(handlerName);
//        }
//        queueMessage(m);
//    }

    private void doSend(String handlerName, String data, CallBackFunction responseCallback, boolean highPriority) {
        logQueueStatus();
        try {
            if (!messageSemaphore.tryAcquire()) {
                if (BridgeConfig.isDebug) Log.w(BridgeConfig.TAG, "Message queue full, dropping message");
                return;
            }

            Message m = new Message();
            m.setHighPriority(highPriority);

            if (!TextUtils.isEmpty(data)) {
                m.setData(data);
            }

            if (responseCallback != null) {
                String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT,
                        uniqueId.incrementAndGet() + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));

                // --- 关键修改：双重保险（回调释放 + 超时释放）---
                AtomicBoolean callbackFired = new AtomicBoolean(false);

                // 包装回调
                CallBackFunction wrappedCallback = responseData -> {
                    if (callbackFired.compareAndSet(false, true)) {
                        try {
                            responseCallback.onCallBack(responseData);
                        } finally {
                            messageSemaphore.release();
                            if (BridgeConfig.isDebug) Log.d(BridgeConfig.TAG, "Callback executed: " + callbackStr);
                        }
                    }
                };

                // 设置超时释放（即使前端未回调）
                backgroundHandler.postDelayed(() -> {
                    if (callbackFired.compareAndSet(false, true)) {
                        messageSemaphore.release();
                        responseCallbacks.remove(callbackStr); // 清理残留回调
                        if (BridgeConfig.isDebug) Log.w(BridgeConfig.TAG, "Force released due to timeout: " + callbackStr);
                    }
                }, BridgeConfig.SEMAPHORE_CALLBACK_TIMEOUT_MS); // 超时自动释放

                responseCallbacks.put(callbackStr, wrappedCallback);
                m.setCallbackId(callbackStr);
            } else {
                // 没有回调的消息，需要单独处理释放
                m.setReleaseSemaphore(true);

                // 无回调的消息：设置超时自动释放信号量（例如 5 秒）
                backgroundHandler.postDelayed(() -> {
                    if (BridgeConfig.isDebug) Log.w(BridgeConfig.TAG, "No response received, releasing semaphore for message: " + m.getData());
                    messageSemaphore.release();
                }, BridgeConfig.SEMAPHORE_NO_CALLBACK_TIMEOUT_MS); // 超时自动释放
            }

            if (!TextUtils.isEmpty(handlerName)) {
                m.setHandlerName(handlerName);
            }

            queueMessage(m);
        } catch (Exception e) {
            // 发生异常时释放许可
            if (BridgeConfig.isDebug) Log.e(BridgeConfig.TAG, "Error sending message", e);
            messageSemaphore.release();
        }
    }

    // 在类中添加
    private void logQueueStatus() {
        if (BridgeConfig.isDebug) {
            Log.d(BridgeConfig.TAG, String.format("Queue status: Available permits=%d, Queue size=%d, Callbacks=%d",
                    messageSemaphore.availablePermits(),
                    messageQueue.size(),
                    responseCallbacks.size()));
        }
    }

//    private void queueMessage(Message m) {
//        if (startupMessage != null) {
//            startupMessage.add(m);
//        } else {
//            dispatchMessage(m);
//        }
//    }

    private void queueMessage(Message m) {
        messageQueue.add(m);

        if (messageQueue.size() >= MAX_BATCH_SIZE) {
            backgroundHandler.removeCallbacks(batchDispatcher);
            backgroundHandler.post(this::dispatchBatch);
        } else if (!handlerPosted) {
            backgroundHandler.postDelayed(batchDispatcher, BATCH_DELAY_MS);
        }
    }

    private void dispatchBatch() {
        List<Message> batch = new ArrayList<>(MAX_BATCH_SIZE);
        messageQueue.drainTo(batch, MAX_BATCH_SIZE);

        if (!batch.isEmpty()) {
            String batchJson = gson.toJson(batch);
            batchJson = batchJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2")
                    .replaceAll("(?<=[^\\\\])(\")", "\\\\\"");

            dispatchMessageBatch(batchJson);
        }
    }

    private void dispatchMessageBatch(String messageJson) {
        for (String bridgeName : BridgeConfig.customBridge) {
            String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA.replace(BridgeConfig.defaultBridge, bridgeName), messageJson);

            if (BridgeConfig.isDebug) Log.i(BridgeConfig.TAG, bridgeName + " dispatchBatch: " + javascriptCommand);

            if (Looper.myLooper() == Looper.getMainLooper()) {
                evaluateJavascript(javascriptCommand, null);
            } else {
                post(() -> {
                    evaluateJavascript(javascriptCommand, null);
                });
            }
        }
    }


    // 批量获取消息id
    public void handleBatchMessageIds(String messageIdsStr) {
        String[] messageIds = messageIdsStr.split(",");
        for (String messageId : messageIds) {
            getMessageById(messageId);
        }
    }

    private void getMessageById(String messageId) {
        for (String bridgeName : BridgeConfig.customBridge) {
            // 为每个消息ID注入JS获取数据
            String jsCommand = String.format(BridgeUtil.JS_FETCH_MESSAGE_BY_ID.replace(BridgeConfig.defaultBridge, bridgeName), messageId);

            if (BridgeConfig.isDebug) Log.i(BridgeConfig.TAG, bridgeName + " getMessageById: " + jsCommand);

            if (Looper.myLooper() == Looper.getMainLooper()) {
                evaluateJavascript(jsCommand, data -> {
                    if (data != null && !data.equals("null")) {
                        // 处理获取到的消息数据
                        if (data.startsWith("[") && data.endsWith("]")) {
                            multiFlushMessageQueue(data);
                        } else {
                            multiFlushMessageQueue("[" + data + "]");
                        }
                    }
                });
            } else {
                post(() -> {
                    evaluateJavascript(jsCommand, data -> {
                        if (data != null && !data.equals("null")) {
                            // 处理获取到的消息数据
                            if (data.startsWith("[") && data.endsWith("]")) {
                                multiFlushMessageQueue(data);
                            } else {
                                multiFlushMessageQueue("[" + data + "]");
                            }
                        }
                    });
                });
            }
        }
    }

    public void evaluateJavascript(String script, ValueCallback<String> resultCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            super.evaluateJavascript(script, resultCallback);
        } else {
            // 对于低版本Android，使用loadUrl方式
            super.loadUrl(script);
        }
    }

    public void dispatchMessage(Message m) {
        String messageJson = m.toJson();
        //escape special characters for json string
        messageJson = messageJson.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        messageJson = messageJson.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");

        for (int i = 0; i < BridgeConfig.customBridge.size(); i++) {
            String bridgeName = BridgeConfig.customBridge.get(i);
            String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA.replace(BridgeConfig.defaultBridge, bridgeName), messageJson);
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                if (BridgeConfig.isDebug) Log.i(BridgeConfig.TAG, bridgeName + "   console   dispatchMessage：" + javascriptCommand);

                evaluateJavascript(javascriptCommand, null);
            }
        }
    }

//    public void flushMessageQueue() {
//        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
//            for (int i = 0; i < BridgeConfig.customBridge.size(); i++) {
//                String bridgeName = BridgeConfig.customBridge.get(i);
//                flushMessageQueue(bridgeName);
//            }
//        }
//    }

    public void flushMessageQueue() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            for (String bridgeName : BridgeConfig.customBridge) {
                flushMessageQueue(bridgeName);
            }
        } else {
            post(this::flushMessageQueue);
        }
    }

    /**
     * 多数据合并处理
     * @param jsonData
     */
    public void multiFlushMessageQueue(String jsonData) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
//            for (String bridgeName : BridgeConfig.customBridge) {
////                if (isDebug) Log.i(TAG, bridgeName + " multiFlushMessageQueue");
////                String jsCommand = BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA.replace(BridgeConfig.defaultBridge, bridgeName);
////                loadUrl(jsCommand, data -> handleFlushResponse(bridgeName, jsonData));
//                handleFlushResponse(bridgeName, jsonData);
//            }
            if (BridgeConfig.customBridge.size() > 0) {
                handleFlushResponse(BridgeConfig.customBridge.get(0), jsonData);
            } else {
                handleFlushResponse(BridgeConfig.defaultBridge, jsonData);
            }
        } else {
            post(() -> multiFlushMessageQueue(jsonData));
        }
    }

//    private void flushMessageQueue(String bridgeName) {
//        if (isDebug) Log.i(TAG, bridgeName + "   console  执行 flushMessageQueue");
//        loadUrl(BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA.replace(BridgeConfig.defaultBridge, bridgeName), new CallBackFunction() {
//            @Override
//            public void onCallBack(String data) {
//                if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue.onCallBack：" + data);
//                // deserializeMessage
//                List<Message> list = null;
//                try {
//                    list = Message.toArrayList(data);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return;
//                }
//                if (list == null || list.size() == 0) {
//                    return;
//                }
//                for (int i = 0; i < list.size(); i++) {
//                    Message m = list.get(i);
//                    String responseId = m.getResponseId();
//                    // 是否是response
//                    if (!TextUtils.isEmpty(responseId)) {
//                        CallBackFunction function = responseCallbacks.get(responseId);
//                        String responseData = m.getResponseData();
//                        if (function != null) function.onCallBack(responseData);
//                        responseCallbacks.remove(responseId);
//
//                        if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：responseId不为空");
//                    } else {
//                        if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：responseId为空");
//                        CallBackFunction responseFunction = null;
//                        // if had callbackId
//                        final String callbackId = m.getCallbackId();
//                        if (!TextUtils.isEmpty(callbackId)) {
//                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：onCallBackId不为空");
//                            responseFunction = new CallBackFunction() {
//                                @Override
//                                public void onCallBack(String data) {
//
//                                    Message responseMsg = new Message();
//                                    responseMsg.setResponseId(callbackId);
//                                    responseMsg.setResponseData(data);
//                                    queueMessage(responseMsg);
//                                }
//                            };
//                        } else {
//                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：onCallBackId为空");
//                            responseFunction = new CallBackFunction() {
//                                @Override
//                                public void onCallBack(String data) {
//                                    // do nothing
//                                }
//                            };
//                        }
//                        BridgeHandler handler;
//                        if (!TextUtils.isEmpty(m.getHandlerName())) {
//                            handler = messageHandlers.get(m.getHandlerName());
//                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：handlerName："+m.getHandlerName());
//                        } else {
//                            handler = defaultHandler;
//                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：handlerName：为空");
//                        }
//
//                        if (handler != null) {
//                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：handler不为空，回调数据");
//                            handler.handler(m.getData(), responseFunction);
//                        } else {
//                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：handler为空");
//                        }
//                    }
//                }
//            }
//        });
//    }

    private void flushMessageQueue(String bridgeName) {
        if (BridgeConfig.isDebug) Log.i(BridgeConfig.TAG, bridgeName + " flushMessageQueue");

        String jsCommand = BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA.replace(BridgeConfig.defaultBridge, bridgeName);

        loadUrl(jsCommand, data -> handleFlushResponse(bridgeName, data));
    }

    private void handleFlushResponse(String bridgeName, String jsonData) {
        if (BridgeConfig.isDebug) Log.i(BridgeConfig.TAG, bridgeName + " flushResponse: " + jsonData);

        try {
            List<Message> list = Message.toArrayList(jsonData);
            if (list == null || list.isEmpty()) return;

            for (Message m : list) {
                if (!TextUtils.isEmpty(m.getResponseId())) {
                    // Handle response
                    CallBackFunction function = responseCallbacks.get(m.getResponseId());
                    if (function != null) {
                        function.onCallBack(m.getResponseData());
                        responseCallbacks.remove(m.getResponseId());
                    } else {
                        // 如果回调不存在，可能是超时释放了，但仍需确保信号量被释放
                        messageSemaphore.release();
                        if (BridgeConfig.isDebug) Log.w(BridgeConfig.TAG, "Orphan response received, releasing semaphore");
                    }
                } else {
                    // Handle request with distinct parameter name
                    CallBackFunction responseFunction = !TextUtils.isEmpty(m.getCallbackId())
                            ? createResponseCallback(m.getCallbackId())
                            : responseData -> {};  // Changed parameter name to responseData

                    BridgeHandler handler = !TextUtils.isEmpty(m.getHandlerName())
                            ? messageHandlers.get(m.getHandlerName())
                            : defaultHandler;

                    if (handler != null) {
                        handler.handler(m.getData(), responseFunction);
                    }
                }

                // 对于没有回调的消息，处理完成后释放许可
                if (m.isReleaseSemaphore()) {
                    messageSemaphore.release();
                }
            }
        } catch (Exception e) {
            if (BridgeConfig.isDebug) Log.e(BridgeConfig.TAG, "Error processing flush response", e);
        }
    }

    private CallBackFunction createResponseCallback(String callbackId) {
        return responseData -> {
            Message responseMsg = new Message();
            responseMsg.setResponseId(callbackId);
            responseMsg.setResponseData(responseData);
            queueMessage(responseMsg);
        };
    }

    public void loadUrl(String jsUrl, CallBackFunction returnCallback) {
        evaluateJavascript(jsUrl, null);

        for (int i = 0; i < BridgeConfig.customBridge.size(); i++) {
            String bridgeName = BridgeConfig.customBridge.get(i);
            responseCallbacks.put(BridgeUtil.parseFunctionName(jsUrl, bridgeName), returnCallback);
        }
    }

    /**
     * register handler,so that javascript can call it
     *
     * @param handlerName handlerName
     * @param handler     Handler
     */
    public void registerHandler(String handlerName, BridgeHandler handler) {
        if (handler != null) {
            messageHandlers.put(handlerName, handler);
        }
    }

    /**
     * call javascript registered handler
     *
     * @param handlerName handlerName
     * @param data        data
     * @param callBack    callBack
     */
    public void callHandler(String handlerName, String data, CallBackFunction callBack) {
        doSend(handlerName, data, callBack, false);
    }

    public void setBridgeWebViewClientListener(BridgeWebViewClientListener bridgeWebViewClientListener) {
        bridgeWebViewClient.setBridgeWebViewClientListener(bridgeWebViewClientListener);
    }

    /**
     * 销毁时调用，移除listener
     */
    /**
     * 销毁时调用，移除listener
     */
    public void removeListener() {
        if (bridgeWebViewClient != null) {
            bridgeWebViewClient.removeListener();
        }
        if (onWebChromeClientListener != null) {
            onWebChromeClientListener = null;
        }
    }

    private OnWebChromeClientListener onWebChromeClientListener;

    public void setWebChromeClientListener(OnWebChromeClientListener onWebChromeClientListener) {
        this.onWebChromeClientListener = onWebChromeClientListener;
        setWebChromeClient(newWebChromeClient());
    }

    public void setWebChromeClientListener(WebChromeClientListener webChromeClientListener) {
        this.onWebChromeClientListener = webChromeClientListener;
        setWebChromeClient(newWebChromeClient());
    }

    private WebChromeClient newWebChromeClient() {
        WebChromeClient wvcc = new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onProgressChanged(view, newProgress);
                } else {
                    super.onProgressChanged(view, newProgress);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onReceivedTitle(view, title);
                } else {
                    super.onReceivedTitle(view, title);
                }
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onReceivedIcon(view, icon);
                } else {
                    super.onReceivedIcon(view, icon);
                }
            }

            @Override
            public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onReceivedTouchIconUrl(view, url, precomposed);
                } else {
                    super.onReceivedTouchIconUrl(view, url, precomposed);
                }
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onShowCustomView(view, callback);
                } else {
                    super.onShowCustomView(view, callback);
                }
            }

            @Override
            public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onShowCustomView(view, requestedOrientation, callback);
                } else {
                    super.onShowCustomView(view, requestedOrientation, callback);
                }
            }

            @Override
            public void onHideCustomView() {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onHideCustomView();
                } else {
                    super.onHideCustomView();
                }
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onCreateWindow(view, isDialog, isUserGesture, resultMsg) :
                        super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            }

            @Override
            public void onRequestFocus(WebView view) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onRequestFocus(view);
                } else {
                    super.onRequestFocus(view);
                }
            }

            @Override
            public void onCloseWindow(WebView window) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onCloseWindow(window);
                } else {
                    super.onCloseWindow(window);
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onJsAlert(view, url, message, result) :
                        super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onJsConfirm(view, url, message, result) :
                        super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onJsPrompt(view, url, message, defaultValue, result) :
                        super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onJsBeforeUnload(view, url, message, result) :
                        super.onJsBeforeUnload(view, url, message, result);
            }

            @Override
            public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
                } else {
                    super.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
                }
            }

//            @Override
//            public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
//                if (onWebChromeClientListener != null) {
//                    onWebChromeClientListener.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
//                } else {
//                    super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
//                }
//            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onGeolocationPermissionsShowPrompt(origin, callback);
                } else {
                    super.onGeolocationPermissionsShowPrompt(origin, callback);
                }
            }

            @Override
            public void onGeolocationPermissionsHidePrompt() {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onGeolocationPermissionsHidePrompt();
                } else {
                    super.onGeolocationPermissionsHidePrompt();
                }
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onPermissionRequest(request);
                } else {
                    super.onPermissionRequest(request);
                }
            }

            @Override
            public void onPermissionRequestCanceled(PermissionRequest request) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onPermissionRequestCanceled(request);
                } else {
                    super.onPermissionRequestCanceled(request);
                }
            }

            @Override
            public boolean onJsTimeout() {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onJsTimeout() :
                        super.onJsTimeout();
            }

            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onConsoleMessage(message, lineNumber, sourceID);
                } else {
                    super.onConsoleMessage(message, lineNumber, sourceID);
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onConsoleMessage(consoleMessage) :
                        super.onConsoleMessage(consoleMessage);
            }

            @Override
            public Bitmap getDefaultVideoPoster() {
                return onWebChromeClientListener != null ? onWebChromeClientListener.getDefaultVideoPoster() :
                        super.getDefaultVideoPoster();
            }

            @Override
            public View getVideoLoadingProgressView() {
                return onWebChromeClientListener != null ? onWebChromeClientListener.getVideoLoadingProgressView() :
                        super.getVideoLoadingProgressView();
            }

            @Override
            public void getVisitedHistory(ValueCallback<String[]> callback) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.getVisitedHistory(callback);
                } else {
                    super.getVisitedHistory(callback);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onShowFileChooser(webView, filePathCallback, fileChooserParams) :
                        super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
            }
        };
        return wvcc;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (autoCleanUp) {
            cleanup();
        }
    }

    public void cleanup() {
        if (handlerThread != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                handlerThread.quitSafely();
            } else {
                handlerThread.quit();
            }
        }
        if (messageQueue != null) messageQueue.clear();
        if (responseCallbacks != null) responseCallbacks.clear();
        if (messageHandlers != null) messageHandlers.clear();
        if (startupMessage != null) startupMessage.clear();

        if (bridgeWebViewClient != null) {
            bridgeWebViewClient.removeListener();
        }
    }

    /**
     * @param bridgeName 自定义桥名，可为空，为空时使用默认桥名，默认桥名为 [BridgeConfig.defaultBridge] WebViewJavascriptBridge
     *                   自定义桥名时，会动态替换 WebViewJavascriptBridge.js 文件内 WebViewJavascriptBridge 字段
     * @author hjhrq1991 created at 6/20/16 17:32.
     */
    public void setBridge(String... bridgeName) {
        if (bridgeName.length > 0) {
            BridgeConfig.customBridge = Arrays.asList(bridgeName);
        } else {
            BridgeConfig.customBridge = Collections.singletonList(BridgeConfig.defaultBridge);
        }
    }
}