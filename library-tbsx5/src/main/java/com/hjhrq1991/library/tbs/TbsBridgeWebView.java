package com.hjhrq1991.library.tbs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebStorage;
import com.tencent.smtt.sdk.WebView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SetJavaScriptEnabled")
public class TbsBridgeWebView extends WebView implements WebViewJavascriptBridge {

    private final String TAG = "BridgeWebView";

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

    public TbsBridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TbsBridgeWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TbsBridgeWebView(Context context) {
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

    public void handlerReturnData(String url) {
        String functionName = BridgeUtil.getFunctionFromReturnUrl(url);
        CallBackFunction f = responseCallbacks.get(functionName);
        String data = BridgeUtil.getDataFromReturnUrl(url);
        if (f != null) {
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
        String javascriptCommand = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA.replace(BridgeConfig.defaultJs, BridgeConfig.customJs), messageJson);
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            this.loadUrl(javascriptCommand);
        }
    }

    public void flushMessageQueue() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            loadUrl(BridgeUtil.JS_FETCH_QUEUE_FROM_JAVA.replace(BridgeConfig.defaultJs, BridgeConfig.customJs), new CallBackFunction() {

                @Override
                public void onCallBack(String data) {
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
                        } else {
                            CallBackFunction responseFunction = null;
                            // if had callbackId
                            final String callbackId = m.getCallbackId();
                            if (!TextUtils.isEmpty(callbackId)) {
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
                            } else {
                                handler = defaultHandler;
                            }
                            if (handler != null) {
                                handler.handler(m.getData(), responseFunction);
                            }
                        }
                    }
                }
            });
        }
    }

    public void loadUrl(String jsUrl, CallBackFunction returnCallback) {
        this.loadUrl(jsUrl);
        responseCallbacks.put(BridgeUtil.parseFunctionName(jsUrl, BridgeConfig.customJs), returnCallback);
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
            public void onExceededDatabaseQuota(String s, String s1, long l, long l1, long l2, WebStorage.QuotaUpdater quotaUpdater) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onExceededDatabaseQuota(s, s1, l, l1, l2, quotaUpdater);
                } else {
                    super.onExceededDatabaseQuota(s, s1, l, l1, l2, quotaUpdater);
                }
            }

            @Override
            public Bitmap getDefaultVideoPoster() {
                return onWebChromeClientListener != null ? onWebChromeClientListener.getDefaultVideoPoster() :
                        super.getDefaultVideoPoster();
            }

            @Override
            public void getVisitedHistory(ValueCallback<String[]> valueCallback) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.getVisitedHistory(valueCallback);
                } else {
                    super.getVisitedHistory(valueCallback);
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onConsoleMessage(consoleMessage) :
                        super.onConsoleMessage(consoleMessage);
            }

            @Override
            public boolean onCreateWindow(WebView webView, boolean b, boolean b1, android.os.Message message) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onCreateWindow(webView, b, b1, message) :
                        super.onCreateWindow(webView, b, b1, message);
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
            public void onGeolocationPermissionsShowPrompt(String s, GeolocationPermissionsCallback geolocationPermissionsCallback) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onGeolocationPermissionsShowPrompt(s, geolocationPermissionsCallback);
                } else {
                    super.onGeolocationPermissionsShowPrompt(s, geolocationPermissionsCallback);
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
            public boolean onJsAlert(WebView webView, String s, String s1, JsResult jsResult) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onJsAlert(webView, s, s1, jsResult) :
                        super.onJsAlert(webView, s, s1, jsResult);
            }

            @Override
            public boolean onJsConfirm(WebView webView, String s, String s1, JsResult jsResult) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onJsConfirm(webView, s, s1, jsResult) :
                        super.onJsConfirm(webView, s, s1, jsResult);
            }

            @Override
            public boolean onJsPrompt(WebView webView, String s, String s1, String s2, JsPromptResult jsPromptResult) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onJsPrompt(webView, s, s1, s2, jsPromptResult) :
                        super.onJsPrompt(webView, s, s1, s2, jsPromptResult);
            }

            @Override
            public boolean onJsBeforeUnload(WebView webView, String s, String s1, JsResult jsResult) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onJsBeforeUnload(webView, s, s1, jsResult) :
                        super.onJsBeforeUnload(webView, s, s1, jsResult);
            }

            @Override
            public boolean onJsTimeout() {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onJsTimeout() :
                        super.onJsTimeout();
            }

            @Override
            public void onProgressChanged(WebView webView, int i) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onProgressChanged(webView, i);
                } else {
                    super.onProgressChanged(webView, i);
                }
            }

            @Override
            public void onReachedMaxAppCacheSize(long l, long l1, WebStorage.QuotaUpdater quotaUpdater) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onReachedMaxAppCacheSize(l, l1, quotaUpdater);
                } else {
                    super.onReachedMaxAppCacheSize(l, l1, quotaUpdater);
                }
            }

            @Override
            public void onReceivedIcon(WebView webView, Bitmap bitmap) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onReceivedIcon(webView, bitmap);
                } else {
                    super.onReceivedIcon(webView, bitmap);
                }
            }

            @Override
            public void onReceivedTouchIconUrl(WebView webView, String s, boolean b) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onReceivedTouchIconUrl(webView, s, b);
                } else {
                    super.onReceivedTouchIconUrl(webView, s, b);
                }
            }

            @Override
            public void onReceivedTitle(WebView webView, String s) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onReceivedTitle(webView, s);
                } else {
                    super.onReceivedTitle(webView, s);
                }
            }

            @Override
            public void onRequestFocus(WebView webView) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onRequestFocus(webView);
                } else {
                    super.onRequestFocus(webView);
                }
            }

            @Override
            public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onShowCustomView(view, customViewCallback);
                } else {
                    super.onShowCustomView(view, customViewCallback);
                }
            }

            @Override
            public void onShowCustomView(View view, int i, IX5WebChromeClient.CustomViewCallback customViewCallback) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onShowCustomView(view, i, customViewCallback);
                } else {
                    super.onShowCustomView(view, i, customViewCallback);
                }
            }

            @Override
            public void onCloseWindow(WebView webView) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.onCloseWindow(webView);
                } else {
                    super.onCloseWindow(webView);
                }
            }

            @Override
            public View getVideoLoadingProgressView() {
                return onWebChromeClientListener != null ? onWebChromeClientListener.getVideoLoadingProgressView() :
                        super.getVideoLoadingProgressView();
            }

            @Override
            public void openFileChooser(ValueCallback<Uri> valueCallback, String s, String s1) {
                if (onWebChromeClientListener != null) {
                    onWebChromeClientListener.openFileChooser(valueCallback, s, s1);
                } else {
                    super.openFileChooser(valueCallback, s, s1);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
                return onWebChromeClientListener != null ? onWebChromeClientListener.onShowFileChooser(webView, valueCallback, fileChooserParams) :
                        super.onShowFileChooser(webView, valueCallback, fileChooserParams);
            }
        };
        return wvcc;
    }

    /**
     * @param customJs 自定义桥名，可为空，为空时使用默认桥名
     *                 自定义桥名回调，如用自定义桥名，请copy一份WebViewJavascriptBridge.js替换文件名
     *                 及脚本内所有包含"WebViewJavascriptBridge"的内容为你的自定义桥名
     * @author hjhrq1991 created at 6/20/16 17:32.
     */
    public void setCustom(String customJs) {
        BridgeConfig.customJs = !TextUtils.isEmpty(customJs) ? customJs : BridgeConfig.defaultJs;
    }
}
