package com.hjhrq1991.library;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author hjhrq1991 created at 8/22/16 14 41.
 */
public class BridgeWebViewClient extends WebViewClient {
    private BridgeWebView webView;
    /**
     * 是否重定向，避免web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
     */
    private boolean isRedirected;
    /**
     * onPageStarted连续调用次数,避免渲染立马跳转可能连续调用onPageStarted多次并且调用shouldOverrideUrlLoading后不调用onPageStarted引起的js桥未初始化问题
     */
    private int onPageStartedCount = 0;

    private BridgeWebViewClientListener bridgeWebViewClientListener;

    public BridgeWebViewClient(BridgeWebView webView) {
        this.webView = webView;
    }

    public void setBridgeWebViewClientListener(BridgeWebViewClientListener bridgeWebViewClientListener) {
        this.bridgeWebViewClientListener = bridgeWebViewClientListener;
    }

    public void removeListener() {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener = null;
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        if (onPageStartedCount < 2) {
            isRedirected = true;
        }
        onPageStartedCount = 0;

        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            webView.flushMessageQueue();
            return true;
        } else {
            if (bridgeWebViewClientListener != null) {
                return bridgeWebViewClientListener.shouldOverrideUrlLoading(view, url);
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        if (onPageStartedCount < 2) {
            isRedirected = true;
        }
        onPageStartedCount = 0;

        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            Log.i("BridgeWebViewClient",  "console   url.startsWith(BridgeUtil.YY_RETURN_DATA");
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            Log.i("BridgeWebViewClient",  "console   url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA");
            webView.flushMessageQueue();
            return true;
        } else {
            if (bridgeWebViewClientListener != null) {
                return bridgeWebViewClientListener.shouldOverrideUrlLoading(view, request);
            } else {
                return super.shouldOverrideUrlLoading(view, request);
            }
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        isRedirected = false;
        onPageStartedCount++;

        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onPageStarted(view, url, favicon);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        //modify：hjhrq1991，web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
        if (BridgeConfig.toLoadJs != null && !url.contains("about:blank") && !isRedirected) {
            for (int i = 0; i < BridgeConfig.customBridge.size(); i++) {
                String bridgeName = BridgeConfig.customBridge.get(i);
                BridgeUtil.webViewLoadLocalJs(view, BridgeConfig.toLoadJs, BridgeConfig.defaultBridge, bridgeName);
            }
        }

        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }

        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onPageFinished(view, url);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onLoadResource(view, url);
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (bridgeWebViewClientListener != null) {
            WebResourceResponse response = bridgeWebViewClientListener.shouldInterceptRequest(view, url);
            if (response != null) {
                return response;
            } else return super.shouldInterceptRequest(view, url);
        } else {
            return super.shouldInterceptRequest(view, url);
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (bridgeWebViewClientListener != null) {
            WebResourceResponse response = bridgeWebViewClientListener.shouldInterceptRequest(view, request);
            if (response != null) {
                return response;
            } else return super.shouldInterceptRequest(view, request);
        } else {
            return super.shouldInterceptRequest(view, request);
        }
    }

    @Override
    public void onTooManyRedirects(WebView view, android.os.Message cancelMsg, android.os.Message continueMsg) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onTooManyRedirects(view, cancelMsg, continueMsg);
        }
        if (!interrupt) {
            super.onTooManyRedirects(view, cancelMsg, continueMsg);
        }
    }

    @Override
    public void onFormResubmission(WebView view, android.os.Message dontResend, android.os.Message resend) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onFormResubmission(view, dontResend, resend);
        }
        if (!interrupt) {
            super.onFormResubmission(view, dontResend, resend);
        }
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.doUpdateVisitedHistory(view, url, isReload);
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onReceivedSslError(view, handler, error);
        }
        if (!interrupt) {
            super.onReceivedSslError(view, handler, error);
        }
    }

    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onReceivedClientCertRequest(view, request);
        }
        if (!interrupt) {
            super.onReceivedClientCertRequest(view, request);
        }
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onReceivedHttpAuthRequest(view, handler, host, realm);
        }
        if (!interrupt) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        if (bridgeWebViewClientListener != null) {
            return bridgeWebViewClientListener.shouldOverrideKeyEvent(view, event);
        } else {
            return super.shouldOverrideKeyEvent(view, event);
        }
    }

    @Override
    public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onUnhandledKeyEvent(view, event);
        }
        if (!interrupt) {
            super.onUnhandledKeyEvent(view, event);
        }
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onScaleChanged(view, oldScale, newScale);
        }
    }

    @Override
    public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onReceivedLoginRequest(view, realm, account, args);
        }
    }

    @Override
    public void onPageCommitVisible(WebView view, String url) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onPageCommitVisible(view, url);
        }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onReceivedError(view, request, error);
        }
        if (!interrupt) {
            super.onReceivedError(view, request, error);
        }
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onReceivedHttpError(view, request, errorResponse);
        }
    }
}