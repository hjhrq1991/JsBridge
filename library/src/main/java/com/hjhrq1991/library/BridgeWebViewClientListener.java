package com.hjhrq1991.library;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

/**
 * @author hjhrq1991 created at 5/10/16 15:12.
 *         超链接回调
 */
public interface BridgeWebViewClientListener {

    /**
     * 非js桥的超链接回调回去自行处理，api21以下会调用
     *
     * @author hjhrq1991 created at 5/10/16 15:12.
     */
    boolean shouldOverrideUrlLoading(WebView view, String url);

    /**
     * 非js桥的超链接回调回去自行处理，api21及以上会调用
     */
    boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request);

    void onPageStarted(WebView view, String url, Bitmap favicon);

    /**
     * 页面完成回调，可用于注入js。在注入JS桥前调用
     * @param view
     * @param url
     */
    void onPageFinishedFirst(WebView view, String url);

    /**
     * 页面完成回调，在注入JS桥后调用
     * @param view
     * @param url
     */
    void onPageFinished(WebView view, String url);

    void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

    void onLoadResource(WebView view, String url);

    WebResourceResponse shouldInterceptRequest(WebView view, String url);

    WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request);

    boolean onTooManyRedirects(WebView view, android.os.Message cancelMsg, android.os.Message continueMsg);

    boolean onFormResubmission(WebView view, android.os.Message dontResend, android.os.Message resend);

    void doUpdateVisitedHistory(WebView view, String url, boolean isReload);

    boolean onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);

    boolean onReceivedClientCertRequest(WebView view, ClientCertRequest request);

    boolean onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm);

    boolean shouldOverrideKeyEvent(WebView view, KeyEvent event);

    boolean onUnhandledKeyEvent(WebView view, KeyEvent event);

    void onScaleChanged(WebView view, float oldScale, float newScale);

    void onReceivedLoginRequest(WebView view, String realm, String account, String args);

    void onPageCommitVisible(WebView view, String url);

    boolean onReceivedError(WebView view, WebResourceRequest request, WebResourceError error);

    void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse);
}
