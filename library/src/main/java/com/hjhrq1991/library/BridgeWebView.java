package com.hjhrq1991.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class BridgeWebView extends WebView implements WebViewJavascriptBridge {

    private final String TAG = "BridgeWebView";

    private boolean isDebug = false;
    private BridgeWebViewClient bridgeWebViewClient;

    Map<String, CallBackFunction> responseCallbacks = new HashMap<String, CallBackFunction>();
    Map<String, BridgeHandler> messageHandlers = new HashMap<String, BridgeHandler>();
    BridgeHandler defaultHandler = new DefaultHandler();

    private List<Message> startupMessage = new ArrayList<Message>();

    public List<Message> getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(List<Message> startupMessage) {
        this.startupMessage = startupMessage;
    }

    private long uniqueId = 0;

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
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
        this.setWebViewClient(generateBridgeWebViewClient());
    }

    protected BridgeWebViewClient generateBridgeWebViewClient() {
        return bridgeWebViewClient = new BridgeWebViewClient(this);
    }

    // url：yy://return/_fetchQueue/[{"handlerName":"jsClick1","data":"{\"title\":\"hello title\"}"}]
    public void handlerReturnData(String url) {
        if (url.endsWith("[]")) return; //当以[]结尾时表示handlerName也没有，则该情况丢弃
        String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
        CallBackFunction f = responseCallbacks.get(functionName);
        String data = BridgeUtil.getDataFromReturnUrl(url);
        if (f != null) {
            //TODO 这里需处理remove导致无法继续发送其他桥的消息
            f.onCallBack(data);
            responseCallbacks.remove(functionName);
            return;
        }
    }

    @Override
    public void send(String data) {
        send(data, null);
    }

    @Override
    public void send(String data, CallBackFunction responseCallback) {
        doSend(null, data, responseCallback);
    }

    private void doSend(String handlerName, String data, CallBackFunction responseCallback) {
        Message m = new Message();
        if (!TextUtils.isEmpty(data)) {
            m.setData(data);
        }
        if (responseCallback != null) {
            String callbackStr = String.format(BridgeUtil.CALLBACK_ID_FORMAT, ++uniqueId + (BridgeUtil.UNDERLINE_STR + SystemClock.currentThreadTimeMillis()));
            responseCallbacks.put(callbackStr, responseCallback);
            m.setCallbackId(callbackStr);
        }
        if (!TextUtils.isEmpty(handlerName)) {
            m.setHandlerName(handlerName);
        }
        queueMessage(m);
    }

    private void queueMessage(Message m) {
        if (startupMessage != null) {
            startupMessage.add(m);
        } else {
            dispatchMessage(m);
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
                if (isDebug) Log.i(TAG, bridgeName + "   console   dispatchMessage：" + javascriptCommand);
                this.loadUrl(javascriptCommand);
            }
        }
    }

    public void flushMessageQueue() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            for (int i = 0; i < BridgeConfig.customBridge.size(); i++) {
                String bridgeName = BridgeConfig.customBridge.get(i);
                flushMessageQueue(bridgeName);
            }
        }
    }

    private void flushMessageQueue(String bridgeName) {
        if (isDebug) Log.i(TAG, bridgeName + "   console  执行 flushMessageQueue");
        loadUrl(BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA.replace(BridgeConfig.defaultBridge, bridgeName), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue.onCallBack：" + data);
                // deserializeMessage
                List<Message> list = null;
                try {
                    list = Message.toArrayList(data);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (list == null || list.size() == 0) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    Message m = list.get(i);
                    String responseId = m.getResponseId();
                    // 是否是response
                    if (!TextUtils.isEmpty(responseId)) {
                        CallBackFunction function = responseCallbacks.get(responseId);
                        String responseData = m.getResponseData();
                        function.onCallBack(responseData);
                        responseCallbacks.remove(responseId);

                        if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：responseId不为空");
                    } else {
                        if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：responseId为空");
                        CallBackFunction responseFunction = null;
                        // if had callbackId
                        final String callbackId = m.getCallbackId();
                        if (!TextUtils.isEmpty(callbackId)) {
                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：onCallBackId不为空");
                            responseFunction = new CallBackFunction() {
                                @Override
                                public void onCallBack(String data) {

                                    Message responseMsg = new Message();
                                    responseMsg.setResponseId(callbackId);
                                    responseMsg.setResponseData(data);
                                    queueMessage(responseMsg);
                                }
                            };
                        } else {
                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：onCallBackId为空");
                            responseFunction = new CallBackFunction() {
                                @Override
                                public void onCallBack(String data) {
                                    // do nothing
                                }
                            };
                        }
                        BridgeHandler handler;
                        if (!TextUtils.isEmpty(m.getHandlerName())) {
                            handler = messageHandlers.get(m.getHandlerName());
                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：handlerName："+m.getHandlerName());
                        } else {
                            handler = defaultHandler;
                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：handlerName：为空");
                        }

                        if (handler != null) {
                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：handler不为空，回调数据");
                            handler.handler(m.getData(), responseFunction);
                        } else {
                            if (isDebug) Log.i(TAG, bridgeName + "   console   flushMessageQueue：handler为空");
                        }
                    }
                }
            }
        });
    }

    public void loadUrl(String jsUrl, CallBackFunction returnCallback) {
        this.loadUrl(jsUrl);
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
        doSend(handlerName, data, callBack);
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

            @Override
            public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
                } else {
                    super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
                }
            }

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
