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

    void onLoadResource(WebView webView, String s);

    void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse);

    WebResourceResponse shouldInterceptRequest(WebView webView, String s);

    WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest);

    WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest, Bundle bundle);

    void doUpdateVisitedHistory(WebView webView, String s, boolean b);

    boolean onFormResubmission(WebView webView, android.os.Message message, android.os.Message message1);

    boolean onReceivedHttpAuthRequest(WebView webView, HttpAuthHandler httpAuthHandler, String s, String s1);

    boolean onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError);

    boolean onReceivedClientCertRequest(WebView webView, ClientCertRequest clientCertRequest);

    void onScaleChanged(WebView webView, float v, float v1);

    void onUnhandledKeyEvent(WebView webView, KeyEvent keyEvent);

    boolean shouldOverrideKeyEvent(WebView webView, KeyEvent keyEvent);

    void onTooManyRedirects(WebView webView, android.os.Message message, android.os.Message message1);

    void onReceivedLoginRequest(WebView webView, String s, String s1, String s2);

    void onDetectedBlankScreen(String s, int i);
}
