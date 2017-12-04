package com.hjhrq1991.library;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

/**
 * @author hjhrq1991 created at 5/10/16 15:12.
 *         超链接回调
 */
public interface BridgeWebViewClientListener {

    /**
     * @param view webview
     * @param url  url
     * @return boolean
     * 非js桥的超链接回调回去自行处理
     * @author hjhrq1991 created at 5/10/16 15:12.
     */
    boolean shouldOverrideUrlLoading(WebView view, String url);

    void onPageStarted(WebView view, String url, Bitmap favicon);

    void onPageFinished(WebView view, String url);

    void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

    void onLoadResource(WebView view, String url);

    WebResourceResponse shouldInterceptRequest(WebView view, String url);

    WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request);

    void onTooManyRedirects(WebView view, android.os.Message cancelMsg, android.os.Message continueMsg);

    void onFormResubmission(WebView view, android.os.Message dontResend, android.os.Message resend);

    void doUpdateVisitedHistory(WebView view, String url, boolean isReload);

    void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);

    void onReceivedClientCertRequest(WebView view, ClientCertRequest request);

    void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm);

    boolean shouldOverrideKeyEvent(WebView view, KeyEvent event);

    void onUnhandledKeyEvent(WebView view, KeyEvent event);

    void onUnhandledInputEvent(WebView view, InputEvent event);

    void onScaleChanged(WebView view, float oldScale, float newScale);

    void onReceivedLoginRequest(WebView view, String realm, String account, String args);
}
