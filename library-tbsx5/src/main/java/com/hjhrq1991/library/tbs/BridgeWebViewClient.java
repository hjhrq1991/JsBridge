package com.hjhrq1991.library.tbs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;

import com.tencent.smtt.export.external.interfaces.ClientCertRequest;
import com.tencent.smtt.export.external.interfaces.HttpAuthHandler;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author hjhrq1991 created at 8/22/16 14 41.
 */
public class BridgeWebViewClient extends WebViewClient {
    private TbsBridgeWebView webView;
    /**
     * 是否重定向，避免web为渲染即跳转导致系统未调用onPageStarted就调用onPageFinished方法引起的js桥初始化失败
     */
    private boolean isRedirected;
    /**
     * onPageStarted连续调用次数,避免渲染立马跳转可能连续调用onPageStarted多次并且调用shouldOverrideUrlLoading后不调用onPageStarted引起的js桥未初始化问题
     */
    private int onPageStartedCount = 0;
    private BridgeWebViewClientListener bridgeWebViewClientListener;

    public BridgeWebViewClient(TbsBridgeWebView webView) {
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
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
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
            BridgeUtil.webViewLoadLocalJs(view, BridgeConfig.toLoadJs, BridgeConfig.defaultJs, BridgeConfig.customJs);
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
    public void onLoadResource(WebView webView, String s) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onLoadResource(webView, s);
        }
    }

    @Override
    public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, String s) {
        if (bridgeWebViewClientListener != null) {
            return bridgeWebViewClientListener.shouldInterceptRequest(webView, s);
        } else {
            return super.shouldInterceptRequest(webView, s);
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
        if (bridgeWebViewClientListener != null) {
            return bridgeWebViewClientListener.shouldInterceptRequest(webView, webResourceRequest);
        } else {
            return super.shouldInterceptRequest(webView, webResourceRequest);
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest, Bundle bundle) {
        if (bridgeWebViewClientListener != null) {
            return bridgeWebViewClientListener.shouldInterceptRequest(webView, webResourceRequest, bundle);
        } else {
            return super.shouldInterceptRequest(webView, webResourceRequest, bundle);
        }
    }

    @Override
    public void doUpdateVisitedHistory(WebView webView, String s, boolean b) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.doUpdateVisitedHistory(webView, s, b);
        }
    }

    @Override
    public void onFormResubmission(WebView webView, android.os.Message message, android.os.Message message1) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onFormResubmission(webView, message, message1);
        }
        if (!interrupt) {
            super.onFormResubmission(webView, message, message1);
        }
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler httpAuthHandler, String s, String s1) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onReceivedHttpAuthRequest(webView, httpAuthHandler, s, s1);
        }
        if (!interrupt) {
            super.onReceivedHttpAuthRequest(webView, httpAuthHandler, s, s1);
        }
    }

    @Override
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onReceivedSslError(webView, sslErrorHandler, sslError);
        }
        if (!interrupt) {
            super.onReceivedSslError(webView, sslErrorHandler, sslError);
        }
    }

    @Override
    public void onReceivedClientCertRequest(WebView webView, ClientCertRequest clientCertRequest) {
        boolean interrupt = false;
        if (bridgeWebViewClientListener != null) {
            interrupt = bridgeWebViewClientListener.onReceivedClientCertRequest(webView, clientCertRequest);
        }
        if (!interrupt) {
            super.onReceivedClientCertRequest(webView, clientCertRequest);
        }
    }

    @Override
    public void onScaleChanged(WebView webView, float v, float v1) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onScaleChanged(webView, v, v1);
        }
    }

    @Override
    public void onUnhandledKeyEvent(WebView webView, KeyEvent keyEvent) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onUnhandledKeyEvent(webView, keyEvent);
        }
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView webView, KeyEvent keyEvent) {
        if (bridgeWebViewClientListener != null) {
            return bridgeWebViewClientListener.shouldOverrideKeyEvent(webView, keyEvent);
        } else {
            return super.shouldOverrideKeyEvent(webView, keyEvent);
        }
    }

    @Override
    public void onTooManyRedirects(WebView webView, android.os.Message message, android.os.Message message1) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onTooManyRedirects(webView, message, message1);
        }
    }

    @Override
    public void onReceivedLoginRequest(WebView webView, String s, String s1, String s2) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onReceivedLoginRequest(webView, s, s1, s2);
        }
    }

    @Override
    public void onDetectedBlankScreen(String s, int i) {
        if (bridgeWebViewClientListener != null) {
            bridgeWebViewClientListener.onDetectedBlankScreen(s, i);
        }
    }
}